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
            MenuScreens.register(Registration.TRANSFER_CONDUIT_MENU.get(), TransferConduitScreen::new);
            MenuScreens.register(Registration.WIRELESS_NODE_MENU.get(), WirelessNodeScreen::new);
            MenuScreens.register(Registration.ELECTRIC_FURNACE_MENU.get(), ElectricFurnaceScreen::new);
            MenuScreens.register(Registration.MEGA_CELL_MENU.get(), MegaCellScreen::new);
            MenuScreens.register(Registration.TREE_GROWTH_CHAMBER_MENU.get(), TreeGrowthChamberScreen::new);
            MenuScreens.register(Registration.ASSEMBLER_MENU.get(), AssemblerScreen::new);
            MenuScreens.register(Registration.BLAST_FURNACE_MENU.get(), BlastFurnaceScreen::new);
            MenuScreens.register(Registration.CENTRIFUGE_MENU.get(), CentrifugeScreen::new);
            MenuScreens.register(Registration.ORE_WASHER_MENU.get(), OreWasherScreen::new);
            MenuScreens.register(Registration.QUANTUM_MINER_MENU.get(), QuantumMinerScreen::new);
            MenuScreens.register(Registration.REACTOR_MENU.get(), ChemicalReactorScreen::new);

        });
    }
}
