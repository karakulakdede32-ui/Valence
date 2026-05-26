package com.valence.valence.client;

import com.valence.valence.Registration;
import com.valence.valence.ValenceMod;
import com.valence.valence.client.gui.*;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = ValenceMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetup {
    @SubscribeEvent
    public static void init(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.register(Registration.BASIC_MINER_MENU.get(), BasicMinerScreen::new);
            MenuScreens.register(Registration.ADVANCED_MINER_MENU.get(), AdvancedMinerScreen::new);
            MenuScreens.register(Registration.GRINDER_MENU.get(), GrinderScreen::new);
            MenuScreens.register(Registration.WATER_COLLECTOR_MENU.get(), WaterCollectorScreen::new);
            MenuScreens.register(Registration.STEAM_DYNAMO_MENU.get(), SteamDynamoScreen::new);
            MenuScreens.register(Registration.STEAM_ALLOYER_MENU.get(), SteamAlloyerScreen::new);
            MenuScreens.register(Registration.STEAM_FURNACE_MENU.get(), SteamFurnaceScreen::new);
            MenuScreens.register(Registration.STEAM_TURBINE_MENU.get(), SteamTurbineScreen::new);
            MenuScreens.register(Registration.DF_CELL_MENU.get(), DFCellScreen::new);
            MenuScreens.register(Registration.SEED_DUPLICATOR_MENU.get(), SeedDuplicatorScreen::new);
        });
    }
}
