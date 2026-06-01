package com.valence.valence;

import com.valence.valence.config.ValenceConfig;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

@Mod(ValenceMod.MODID)
public class ValenceMod {
    public static final String MODID = "valence";
    private static final Logger LOGGER = LogUtils.getLogger();

    public ValenceMod() {
        com.valence.valence.config.ValenceConfig.register();
        var bus = FMLJavaModLoadingContext.get().getModEventBus();
        
        // Register all deferred registries
        Registration.BLOCKS.register(bus);
        Registration.ITEMS.register(bus);
        Registration.BLOCK_ENTITIES.register(bus);
        Registration.MENUS.register(bus);
        Registration.CREATIVE_MODE_TABS.register(bus);
        Registration.RECIPE_TYPES.register(bus);
        Registration.RECIPE_SERIALIZERS.register(bus);
        
        Registration.FLUIDS.register(bus);
        Registration.FLUID_TYPES.register(bus);
        com.valence.valence.event.ModLootModifiers.LOOT_MODIFIER_SERIALIZERS.register(bus);
        
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
