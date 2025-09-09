package dev.szedann.createBluemap;

import me.fzzyhmstrs.fzzy_config.api.ConfigApiJava;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.bluecolored.bluemap.api.BlueMapAPI;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public final class CreateBluemap {
    public static final String MOD_ID = "create_bluemap";

    public static void init() {
        // Basic mod initialization - no BlueMap callbacks here
        LOGGER.info("Initializing Create Bluemap mod");
    }
    
    public static void startWatcher() {
        // Wait for BlueMap to be available before starting
        BlueMapAPI.onEnable(api -> {
            LOGGER.info("BlueMap API enabled - setting up assets and starting watcher");
            setupBlueMapAssets();
            Watcher.start();
        });
    }
    
    public static void stopWatcher() {
        LOGGER.info("Stopping Create Bluemap watcher");
        Watcher.stop();
    }

    public static ResourceLocation asResource(String name) {
        return ResourceLocation.parse(MOD_ID + ":" + name);
    }

    public static Config config = ConfigApiJava.registerAndLoadConfig(Config::new);

    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    
    // Cache for detected custom icon
    private static String detectedCustomIcon = null;

    private static void setupBlueMapAssets() {
        try {
            // Try to copy train icons to BlueMap's web assets folder
            Path blueMapWebRoot = Paths.get("bluemap", "web", "assets", "create_bluemap");
            Files.createDirectories(blueMapWebRoot);
            
            // Try different icon formats: custom_train_icon.png, custom_train_icon.webp, train_icon.png
            String[] iconFiles = {"custom_train_icon.png", "custom_train_icon.webp", "train_icon.png"};
            
            for (String iconFile : iconFiles) {
                InputStream iconStream = CreateBluemap.class.getClassLoader()
                    .getResourceAsStream("assets/create_bluemap/" + iconFile);
                    
                if (iconStream != null) {
                    Path iconDestination = blueMapWebRoot.resolve(iconFile);
                    Files.copy(iconStream, iconDestination, StandardCopyOption.REPLACE_EXISTING);
                    LOGGER.info("Successfully copied {} to BlueMap web assets: {}", iconFile, iconDestination);
                    iconStream.close();
                    
                    // Cache the first found icon as the detected custom icon
                    if (detectedCustomIcon == null) {
                        detectedCustomIcon = "assets/create_bluemap/" + iconFile;
                        LOGGER.info("Using custom train icon: {}", detectedCustomIcon);
                    }
                }
            }
        } catch (IOException e) {
            LOGGER.error("Failed to setup BlueMap assets: {}", e.getMessage());
        }
    }
    
    public static String getCustomTrainIcon() {
        return detectedCustomIcon;
    }
}