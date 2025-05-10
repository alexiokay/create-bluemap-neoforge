package dev.szedann.createBluemap.forge;

import dev.szedann.createBluemap.CreateBluemap;
import dev.architectury.platform.forge.EventBuses;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(CreateBluemap.MOD_ID)
public final class CreateBluemapForge {
    public CreateBluemapForge() {
        // Submit our event bus to let Architectury API register our content on the right time.
        EventBuses.registerModEventBus(CreateBluemap.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());

        // Run our common setup.
        CreateBluemap.init();
    }
}
