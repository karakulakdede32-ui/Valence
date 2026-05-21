package com.valence.valence;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import com.valence.valence.block.miner.BasicMinerBlock;
import com.valence.valence.block.miner.AdvancedMinerBlock;

public class Registration {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, ValenceMod.MODID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, ValenceMod.MODID);
    
    // Blocks
    public static final Block BASIC_MINER = BLOCKS.register("basic_miner", BasicMinerBlock::new);
    public static final Block ADVANCED_MINER = BLOCKS.register("advanced_miner", AdvancedMinerBlock::new);
    
    // Block Items
    public static final Item BASIC_MINER_ITEM = ITEMS.register("basic_miner", 
        () -> new BlockItem(BASIC_MINER, new Item.Properties().tab(net.minecraft.creativetab.CreativeTab.TAB_MISC)));
    public static final Item ADVANCED_MINER_ITEM = ITEMS.register("advanced_miner",
        () -> new BlockItem(ADVANCED_MINER, new Item.Properties().tab(net.minecraft.creativetab.CreativeTab.TAB_MISC)));
    
    public static void register(IForgeRegistry<?> registry) {
        // This space intentionally left blank
        // DeferredRegisters auto-register when added to the mod event bus
    }
}