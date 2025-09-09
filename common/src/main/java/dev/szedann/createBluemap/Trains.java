package dev.szedann.createBluemap;

import com.flowpowered.math.vector.Vector3d;
import com.simibubi.create.Create;
import com.simibubi.create.content.trains.entity.Carriage;
import com.simibubi.create.content.trains.graph.TrackNode;
import com.simibubi.create.content.trains.schedule.ScheduleRuntime;
import de.bluecolored.bluemap.api.BlueMapAPI;
import de.bluecolored.bluemap.api.BlueMapMap;
import de.bluecolored.bluemap.api.markers.LineMarker;
import de.bluecolored.bluemap.api.markers.MarkerSet;
import de.bluecolored.bluemap.api.markers.POIMarker;
import de.bluecolored.bluemap.api.math.Color;
import de.bluecolored.bluemap.api.math.Line;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class Trains {
    private static final Color manualColor = new Color("#f99");
    private static final Color scheduledColor = new Color("#99f");
    
    // Movement detection cache
    private static final Map<UUID, Vec3> lastTrainPositions = new ConcurrentHashMap<>();
    private static final Map<UUID, Boolean> lastTrainMovingState = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> lastTrainUpdateTime = new ConcurrentHashMap<>();
    
    // Persistent marker cache - avoid recreating markers
    private static final Map<String, POIMarker> persistentMarkers = new ConcurrentHashMap<>(); // level-uuid -> marker
    private static final Map<ResourceKey<Level>, MarkerSet> persistentMarkerSets = new ConcurrentHashMap<>();

    public static void update(BlueMapAPI api) {
        if (CreateBluemap.config.renderTrains.get())
            updatePOIs(api);
        if (CreateBluemap.config.renderCarriages.get())
            updateCarriages(api);
    }

    private static void updatePOIs(BlueMapAPI api) {
        long currentTime = System.currentTimeMillis();
        double threshold = CreateBluemap.config.movementThreshold.get() / 10.0; // Convert to decimal
        Set<String> activeTrainKeys = new HashSet<>(); // Track which trains are still active

        Create.RAILWAYS.trains.forEach((uuid, train) -> {
            TrackNode node = train.carriages.get(0).getLeadingPoint().node1;
            if (node == null)
                return;
            ResourceKey<Level> level = node.getLocation().dimension;
            String markerKey = level.location().toString() + "-" + uuid.toString();
            activeTrainKeys.add(markerKey);
            
            // Ensure marker set exists for this level
            if (!persistentMarkerSets.containsKey(level)) {
                MarkerSet markerSet = MarkerSet.builder()
                        .defaultHidden(!CreateBluemap.config.trainsVisibleByDefault.get())
                        .label(String.format("Trains in %s", level.location().toShortLanguageKey()))
                        .build();
                persistentMarkerSets.put(level, markerSet);
            }

            Vec3 pos = train.carriages.get(0).getLeadingPoint().getPosition(train.graph);
            
            // Check if we should only render moving trains
            boolean isMoving = train.speed > 0.01;
            if (CreateBluemap.config.onlyRenderMovingTrains.get() && !isMoving) {
                // Remove marker if train stopped and we only show moving trains
                POIMarker existingMarker = persistentMarkers.remove(markerKey);
                if (existingMarker != null) {
                    persistentMarkerSets.get(level).remove(uuid.toString());
                }
                return;
            }
            
            // Update position cache
            lastTrainPositions.put(uuid, pos);
            lastTrainMovingState.put(uuid, isMoving);
            lastTrainUpdateTime.put(uuid, currentTime);
            
            // Calculate train info
            boolean scheduled = train.runtime.state == ScheduleRuntime.State.IN_TRANSIT;
            String status = isMoving ? (scheduled ? "Scheduled" : "Manual") : "Stopped";
            String speedInfo = String.format("Speed: %.1f", train.speed);
            String label = String.format("%s (%s)\n%s", train.name.getString(), status, speedInfo);
            
            // Get or create marker
            POIMarker marker = persistentMarkers.get(markerKey);
            if (marker == null) {
                // Create new marker only if it doesn't exist
                POIMarker.Builder markerBuilder = POIMarker.builder()
                        .label(label)
                        .position(pos.x, pos.y + 1, pos.z)
                        .maxDistance(CreateBluemap.config.markerMaxDistance.get())
                        .minDistance(0);
                
                // Add custom icon if available
                String customIcon = CreateBluemap.getCustomTrainIcon();
                if (customIcon != null) {
                    int iconSize = CreateBluemap.config.trainIconSize.get();
                    markerBuilder.icon(customIcon, iconSize, iconSize);
                }
                
                marker = markerBuilder.build();
                persistentMarkers.put(markerKey, marker);
                persistentMarkerSets.get(level).put(uuid.toString(), marker);
            } else {
                // Update existing marker position and label
                marker.setPosition(pos.x, pos.y + 1, pos.z);
                marker.setLabel(label);
                
                // Note: Icon updates require recreating marker (BlueMap API limitation)
                String currentIcon = CreateBluemap.getCustomTrainIcon();
                if (currentIcon != null) {
                    // Only recreate if icon changed or wasn't set before
                    // For now, we assume icon doesn't change during runtime
                }
            }
        });

        // Clean up markers for trains that no longer exist
        persistentMarkers.entrySet().removeIf(entry -> {
            String markerKey = entry.getKey();
            if (!activeTrainKeys.contains(markerKey)) {
                // Extract level and uuid from markerKey
                String[] parts = markerKey.split("-", 2);
                if (parts.length == 2) {
                    try {
                        ResourceKey<Level> level = ResourceKey.create(
                            net.minecraft.core.registries.Registries.DIMENSION, 
                            ResourceLocation.parse(parts[0])
                        );
                        String uuid = parts[1];
                        MarkerSet markerSet = persistentMarkerSets.get(level);
                        if (markerSet != null) {
                            markerSet.remove(uuid);
                        }
                    } catch (Exception e) {
                        CreateBluemap.LOGGER.warn("Failed to clean up marker: {}", e.getMessage());
                    }
                }
                return true; // Remove from persistent cache
            }
            return false; // Keep
        });

        // Update BlueMap with persistent marker sets
        persistentMarkerSets.forEach((level, markerSet) -> {
            api.getWorld(level).ifPresent(world -> {
                for (BlueMapMap map : world.getMaps()) {
                    map.getMarkerSets().put(String.format("trains-%s", level.location().toShortLanguageKey()),
                            markerSet);
                }
            });
        });
    }

    private static void updateCarriages(BlueMapAPI api) {
        Map<ResourceKey<Level>, MarkerSet> lineMarkerMap = new HashMap<>();

        Create.RAILWAYS.trains.forEach((uuid, train) -> {

            int i = 0;
            for (Carriage carriage : train.carriages) {
                TrackNode node = carriage.getLeadingPoint().node1;
                if (node == null)
                    return;
                ResourceKey<Level> level = node.getLocation().dimension;

                if (carriage.getTrailingPoint().node2.getLocation().dimension != level)
                    return;
                if (!lineMarkerMap.containsKey(level)) {
                    lineMarkerMap.put(level, MarkerSet.builder()
                            .defaultHidden(!CreateBluemap.config.carriagesVisibleByDefault.get())
                            .label(String.format("Carriages in %s", level.location().toShortLanguageKey())).build());
                }
                i++;
                Vec3 p1 = carriage.getLeadingPoint().getPosition(train.graph);
                Vec3 p2 = carriage.getTrailingPoint().getPosition(train.graph);
                boolean front = train.currentlyBackwards ? i == train.carriages.size() : i == 1;
                boolean scheduled = train.runtime.state == ScheduleRuntime.State.IN_TRANSIT;
                lineMarkerMap.get(level).put(train.id.toString() + "-" + carriage.id, LineMarker.builder()
                        .label(String.format("%s carriage %s", train.name.getString(), i))
                        .line(Line.builder()
                                .addPoint(new Vector3d(p1.x, p1.y + 1, p1.z))
                                .addPoint(new Vector3d(p2.x, p2.y + 1, p2.z))
                                .build())
                        .lineColor(scheduled ? scheduledColor : manualColor)
                        .lineWidth(front ? 7 : 5)
                        .depthTestEnabled(false)
                        // .maxDistance(400)
                        .build());
            }

        });

        lineMarkerMap.forEach((level, markerSet) -> {
            api.getWorld(level).ifPresent(world -> {
                for (BlueMapMap map : world.getMaps()) {
                    map.getMarkerSets().put(String.format("carriages-%s", level.location().toShortLanguageKey()),
                            markerSet);
                }
            });
        });
    }
}
