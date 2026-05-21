package com.valence.valence;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

@Mod("valence")
public class ValenceMod {
    public static final String MODID = "valence";
    private static final Logger LOGGER = LogUtils.getLogger();

    public ValenceMod() {
        var bus = FMLJavaModLoadingContext.get().getModEventBus();
        
        // Register blocks and items
        Registration.BLOCKS.register(bus);
        Registration.ITEMS.register(bus);
        
        bus.addListener(this::commonSetup);
        bus.addListener(this::clientSetup);
        LOGGER.info("Valence mod loaded");
    }

    private void commonSetup(FMLCommonSetupEvent e) {
        LOGGER.info("Valence common setup");
    }

    private void clientSetup(FMLClientSetupEvent e) {
        LOGGER.info("Valence client setup");
    }
}
