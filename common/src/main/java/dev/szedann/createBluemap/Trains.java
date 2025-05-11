package dev.szedann.createBluemap;

import com.flowpowered.math.vector.Vector3d;
import com.simibubi.create.Create;
import com.simibubi.create.content.trains.entity.Carriage;
import de.bluecolored.bluemap.api.BlueMapAPI;
import de.bluecolored.bluemap.api.BlueMapMap;
import de.bluecolored.bluemap.api.markers.LineMarker;
import de.bluecolored.bluemap.api.markers.MarkerSet;
import de.bluecolored.bluemap.api.markers.POIMarker;
import de.bluecolored.bluemap.api.math.Color;
import de.bluecolored.bluemap.api.math.Line;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;

public class Trains {
    public static boolean renderPOIs = true;
    public static boolean renderCarriages = true;

    public static void update(BlueMapAPI api) {
        if (renderPOIs)
            updatePOIs(api);
        if (renderCarriages)
            updateCarriages(api);

    }

    private static void updatePOIs(BlueMapAPI api) {
        Map<ResourceKey<Level>, MarkerSet> POIMarkerSets = new HashMap<>();

        Create.RAILWAYS.trains.forEach((uuid, train) -> {
            ResourceKey<Level> level = train.carriages.get(0).getLeadingPoint().node1.getLocation().dimension;
            if (!POIMarkerSets.containsKey(level)) {
                POIMarkerSets.put(level, MarkerSet.builder()
                        .label(String.format("Trains in %s", level.location().toShortLanguageKey())).build());
            }

            Vec3 pos = train.carriages.get(0).getLeadingPoint().getPosition(train.graph);
            var marker = POIMarker.builder()
                    .label(train.name.getString())
                    .position(pos.x, pos.y, pos.z)
                    .build();

            POIMarkerSets.get(level).put(uuid.toString(), marker);
        });

        POIMarkerSets.forEach((level, markerSet) -> {
            CreateBluemap.LOGGER.info("{} trains updated", markerSet.getMarkers().size());
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
            ResourceKey<Level> level = train.carriages.get(0).getLeadingPoint().node1.getLocation().dimension;
            if (!lineMarkerMap.containsKey(level)) {
                lineMarkerMap.put(level, MarkerSet.builder()
                        .label(String.format("Carriages in %s", level.location().toShortLanguageKey())).build());
            }

            int i = 0;
            for (Carriage carriage : train.carriages) {
                Vec3 p1 = carriage.getLeadingPoint().getPosition(train.graph);
                Vec3 p2 = carriage.getTrailingPoint().getPosition(train.graph);
                lineMarkerMap.get(level).put(train.id.toString() + "-" + carriage.id, LineMarker.builder()
                        .label(String.format("%s carriage %s", train.name.getString(), ++i))
                        .line(Line.builder()
                                .addPoint(new Vector3d(p1.x, p1.y + 1, p1.z))
                                .addPoint(new Vector3d(p2.x, p2.y + 1, p2.z))
                                .build())
                        .lineColor(new Color("#99f"))
                        .lineWidth(5)
                        .build());
            }

        });

        lineMarkerMap.forEach((level, markerSet) -> {
            api.getWorld(level).ifPresent(world -> {
                CreateBluemap.LOGGER.info("{} carriages updated", markerSet.getMarkers().size());
                for (BlueMapMap map : world.getMaps()) {
                    map.getMarkerSets().put(String.format("carriages-%s", level.location().toShortLanguageKey()),
                            markerSet);
                }
            });
        });
    }
}
