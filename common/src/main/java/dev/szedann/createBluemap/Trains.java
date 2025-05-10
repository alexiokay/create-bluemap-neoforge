package dev.szedann.createBluemap;

import com.simibubi.create.Create;
import de.bluecolored.bluemap.api.BlueMapAPI;
import de.bluecolored.bluemap.api.BlueMapMap;
import de.bluecolored.bluemap.api.markers.MarkerSet;
import de.bluecolored.bluemap.api.markers.POIMarker;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;

public class Trains {
    public static void update(BlueMapAPI api) {

        Map<ResourceKey<Level>, MarkerSet> POIMarkerSets = new HashMap<>();

        Create.RAILWAYS.trains.forEach((uuid, train) -> {
            ResourceKey<Level> level = train.carriages.get(0).getLeadingPoint().node1.getLocation().dimension;
            if(!POIMarkerSets.containsKey(level)) {
                POIMarkerSets.put(level, MarkerSet.builder()
                        .label(String.format("Trains in %s",level.location().toShortLanguageKey())).build());
            }


            Vec3 pos = train.carriages.get(0).getLeadingPoint().getPosition(train.graph);
            var marker = POIMarker.builder()
                    .label(train.name.getString())
                    .position(pos.x, pos.y, pos.z)
                    .build();

            POIMarkerSets.get(level).put(uuid.toString(), marker);
        });

        POIMarkerSets.forEach((level, markerSet) -> {
            api.getWorld(level).ifPresent(world -> {
                for(BlueMapMap map : world.getMaps()) {
                    map.getMarkerSets().put(String.format("trains-%s",level.location().toShortLanguageKey()), markerSet);
                }
            });
        });

    }
}
