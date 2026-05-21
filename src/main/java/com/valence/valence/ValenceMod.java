package com.valence.valence;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;
import com.valence.valence.block.miner.BasicMinerBlock;
import com.valence.valence.block.miner.BasicMinerTileEntity;
import com.valence.valence.block.miner.BasicMinerMenu;
import com.valence.valence.block.miner.AdvancedMinerBlock;
import com.valence.valence.block.miner.AdvancedMinerTileEntity;
import com.valence.valence.block.miner.AdvancedMinerMenu;

@Mod("valence")
public class ValenceMod {
    public static final String MODID = "valence";
    private static final Logger LOGGER = LogUtils.getLogger();

    public ValenceMod() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
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
