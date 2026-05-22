package com.valence.valence.client;

import com.valence.valence.Registration;
import com.valence.valence.ValenceMod;
import com.valence.valence.client.gui.AdvancedMinerScreen;
import com.valence.valence.client.gui.BasicMinerScreen;
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
        });
    }
}
