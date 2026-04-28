package com.valence;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafxmod.FXModLanguageProvider;

@Mod(ValenceMod.MOD_ID)
public class ValenceMod {
    public static final String MOD_ID = "valence";
    public static final String MOD_NAME = "Valence";
    public static final String MOD_VERSION = "0.1.0";

    public ValenceMod() {
        // Constructor - initialization code
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        // Common setup
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        // Client setup
    }
}
