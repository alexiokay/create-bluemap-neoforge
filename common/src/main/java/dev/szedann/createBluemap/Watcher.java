package dev.szedann.createBluemap;

import de.bluecolored.bluemap.api.BlueMapAPI;

import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class Watcher {
    static long delay = 10;
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private static ScheduledFuture<?> future;
    public static void start() {
        Runnable task = () -> {
            Optional<BlueMapAPI> apiOptional = BlueMapAPI.getInstance();
            apiOptional.ifPresent(api -> {
                Trains.update(api);
            });
        };
        future = scheduler.scheduleAtFixedRate(task, 0, delay, TimeUnit.SECONDS);
    }
    public static void stop() {
        future.cancel(false);
    }
}
