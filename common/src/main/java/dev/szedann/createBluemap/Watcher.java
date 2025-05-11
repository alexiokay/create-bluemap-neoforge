package dev.szedann.createBluemap;

import de.bluecolored.bluemap.api.BlueMapAPI;

import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class Watcher {
    static long refreshInterval = 10;
    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private static ScheduledFuture<?> future;

    public static void start() {
        CreateBluemap.LOGGER.info("Starting Create Bluemap updater");
        Runnable task = () -> {
            Optional<BlueMapAPI> apiOptional = BlueMapAPI.getInstance();
            apiOptional.ifPresent(api -> {
                try {
                    Trains.update(api);
                    Tracks.update(api);
                } catch (Exception e) {
                    CreateBluemap.LOGGER.error(e.getMessage());
                }
            });
        };
        future = scheduler.scheduleAtFixedRate(task, 0, refreshInterval, TimeUnit.SECONDS);

    }

    public static void stop() {
        future.cancel(false);
    }
}
