package dev.szedann.createBluemap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class CreateBluemap {
    public static final String MOD_ID = "create-bluemap";

    public static void init() {
        // Write common init code here.

        Watcher.start();
    }

    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
}