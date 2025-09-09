package dev.szedann.createBluemap.neoforge;

import dev.szedann.createBluemap.CreateBluemap;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;

@Mod(CreateBluemap.MOD_ID)
public final class CreateBluemapNeoForge {
    public CreateBluemapNeoForge(IEventBus modEventBus) {
        // Initialize the mod
        CreateBluemap.init();
        
        // Register server lifecycle events
        NeoForge.EVENT_BUS.addListener(this::onServerStarted);
        NeoForge.EVENT_BUS.addListener(this::onServerStopped);
    }
    
    private void onServerStarted(ServerStartedEvent event) {
        CreateBluemap.LOGGER.info("Server started - starting Create Bluemap watcher");
        CreateBluemap.startWatcher();
    }
    
    private void onServerStopped(ServerStoppedEvent event) {
        CreateBluemap.LOGGER.info("Server stopped - stopping Create Bluemap watcher");
        CreateBluemap.stopWatcher();
    }
}