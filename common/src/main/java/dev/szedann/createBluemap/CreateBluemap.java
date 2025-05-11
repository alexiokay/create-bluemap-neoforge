package dev.szedann.createBluemap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.bluecolored.bluemap.api.BlueMapAPI;

public final class CreateBluemap {
    public static final String MOD_ID = "create-bluemap";

    public static void init() {
        // Write common init code here.
        BlueMapAPI.onEnable(api -> {
            // Watcher.start();
            Tracks.update(api);
            Trains.update(api);
        });
    }

    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
}