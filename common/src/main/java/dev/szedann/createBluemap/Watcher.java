package dev.szedann.createBluemap;

import de.bluecolored.bluemap.api.BlueMapAPI;

import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class Watcher {
    private static ScheduledExecutorService scheduler;
    private static ScheduledFuture<?> trainFuture;
    private static ScheduledFuture<?> trackFuture;
    private static long updateCounter = 0;

    public static void start() {
        // Create a new scheduler if needed
        if (scheduler == null || scheduler.isShutdown()) {
            scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "create-bluemap-updater");
                t.setDaemon(true); // Don't prevent JVM shutdown
                return t;
            });
        }
        
        CreateBluemap.LOGGER.info("Starting Create Bluemap updater with smart polling");
        CreateBluemap.LOGGER.info("Train updates: {}s, Track updates: {}s", 
            CreateBluemap.config.trainUpdateInterval.get(), 
            CreateBluemap.config.trackUpdateInterval.get());
        
        // Fast updates for trains (1 second default)
        Runnable trainTask = () -> {
            Optional<BlueMapAPI> apiOptional = BlueMapAPI.getInstance();
            apiOptional.ifPresent(api -> {
                try {
                    updateCounter++;
                    if (CreateBluemap.config.renderTrains.get() || CreateBluemap.config.renderCarriages.get()) {
                        Trains.update(api);
                    }
                } catch (Exception e) {
                    CreateBluemap.LOGGER.error("Error updating trains: {}", e.getMessage());
                }
            });
        };
        
        // Slow updates for tracks (30 seconds default)
        Runnable trackTask = () -> {
            Optional<BlueMapAPI> apiOptional = BlueMapAPI.getInstance();
            apiOptional.ifPresent(api -> {
                try {
                    if (CreateBluemap.config.renderTracks.get()) {
                        Tracks.update(api);
                        CreateBluemap.LOGGER.debug("Updated tracks (update #{})", updateCounter);
                    }
                } catch (Exception e) {
                    CreateBluemap.LOGGER.error("Error updating tracks: {}", e.getMessage());
                }
            });
        };
        
        // Schedule both tasks with different intervals - use legacy interval if trainUpdateInterval is default
        int trainInterval = CreateBluemap.config.trainUpdateInterval.get();
        if (trainInterval == 1 && CreateBluemap.config.interval.get() != 5) {
            // Use legacy interval setting if it was customized
            trainInterval = CreateBluemap.config.interval.get();
            CreateBluemap.LOGGER.info("Using legacy interval setting: {} seconds", trainInterval);
        }
        trainFuture = scheduler.scheduleAtFixedRate(trainTask, 0, trainInterval, TimeUnit.SECONDS);
        trackFuture = scheduler.scheduleAtFixedRate(trackTask, 5, // 5 second delay for tracks
            CreateBluemap.config.trackUpdateInterval.get(), TimeUnit.SECONDS);
    }

    public static void stop() {
        CreateBluemap.LOGGER.info("Stopping Create Bluemap updater");
        if (trainFuture != null) {
            trainFuture.cancel(true); // Allow interruption
        }
        if (trackFuture != null) {
            trackFuture.cancel(true); // Allow interruption
        }
        
        // Properly shutdown the executor service
        scheduler.shutdown();
        try {
            // Wait up to 5 seconds for existing tasks to terminate
            if (!scheduler.awaitTermination(5, java.util.concurrent.TimeUnit.SECONDS)) {
                CreateBluemap.LOGGER.warn("Executor did not terminate gracefully, forcing shutdown");
                scheduler.shutdownNow();
                // Wait a bit more for tasks to respond to being cancelled
                if (!scheduler.awaitTermination(2, java.util.concurrent.TimeUnit.SECONDS)) {
                    CreateBluemap.LOGGER.error("Executor did not terminate even after forced shutdown");
                }
            }
        } catch (InterruptedException e) {
            // Current thread was interrupted during shutdown
            CreateBluemap.LOGGER.warn("Interrupted while shutting down executor");
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
        CreateBluemap.LOGGER.info("Create Bluemap updater stopped successfully");
    }
    
    public static long getUpdateCounter() {
        return updateCounter;
    }
}
