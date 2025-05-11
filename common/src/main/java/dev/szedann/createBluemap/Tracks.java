package dev.szedann.createBluemap;

import com.flowpowered.math.vector.Vector3d;
import com.simibubi.create.Create;
import com.simibubi.create.content.trains.graph.TrackEdge;
import de.bluecolored.bluemap.api.BlueMapAPI;
import de.bluecolored.bluemap.api.BlueMapMap;
import de.bluecolored.bluemap.api.markers.LineMarker;
import de.bluecolored.bluemap.api.markers.MarkerSet;
import de.bluecolored.bluemap.api.math.Color;
import de.bluecolored.bluemap.api.math.Line;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.*;

public class Tracks {
    public static boolean renderTracks = false;

    public static void update(BlueMapAPI api) {
        if (!renderTracks)
            return;
        Map<ResourceKey<Level>, MarkerSet> lineMarkerSets = new HashMap<>();

        Create.RAILWAYS.trackNetworks.forEach((graphUuid, graph) -> {
            var edges = new HashSet<TrackEdge>();
            graph.getNodes().forEach(graphNode -> {
                var node = graph.locateNode(graphNode);
                edges.addAll(graph.getConnectionsFrom(node).values());

                ResourceKey<Level> level = node.getLocation().dimension;
                if (!lineMarkerSets.containsKey(level)) {
                    lineMarkerSets.put(level, MarkerSet.builder()
                            .label(String.format("Tracks in %s", level.location().toShortLanguageKey())).build());
                }
            });
            edges.forEach(edge -> {
                MarkerSet lineMarkerSet = lineMarkerSets.get(edge.node1.getLocation().dimension);
                int segmentCount = edge.isTurn() ? (int) (edge.getLength() / 16) + 2 : 2;
                Line.Builder line = Line.builder();
                for (int i = 0; i < segmentCount; i++) {
                    Vec3 pos = edge.getPosition(graph, (double) i / (segmentCount - 1));
                    line.addPoint(new Vector3d(pos.x, pos.y + 1, pos.z));
                }
                LineMarker marker = LineMarker.builder()
                        .line(line.build())
                        .lineWidth(6)
                        .maxDistance(300)
                        .label("edge")
                        .lineColor(new Color("#777"))
                        .build();
                lineMarkerSet.put(edge.toString(), marker);

            });
        });

        lineMarkerSets.forEach((level, markerSet) -> {
            api.getWorld(level).ifPresent(world -> {
                for (BlueMapMap map : world.getMaps()) {
                    map.getMarkerSets().put(String.format("tracks-%s", level.location().toShortLanguageKey()),
                            markerSet);
                }
            });
        });
    }
}
