package com.valence.valence;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import com.valence.valence.block.miner.BasicMinerBlock;
import com.valence.valence.block.miner.AdvancedMinerBlock;

public class Registration {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, ValenceMod.MODID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, ValenceMod.MODID);
    
    // Block property - stone-like
    private static final BlockBehaviour.Properties BLOCK_props = BlockBehaviour.Properties.of(Material.STONE);
    
    // Register blocks with properties
    public static final RegistryObject<Block> BASIC_MINER = BLOCKS.register("basic_miner", 
        () -> new BasicMinerBlock(BLOCK_props));
    public static final RegistryObject<Block> ADVANCED_MINER = BLOCKS.register("advanced_miner", 
        () -> new AdvancedMinerBlock(BLOCK_props));
    
    // Register block items
    public static final RegistryObject<Item> BASIC_MINER_ITEM = ITEMS.register("basic_miner", 
        () -> new BlockItem(BASIC_MINER.get(), new Item.Properties()));
    public static final RegistryObject<Item> ADVANCED_MINER_ITEM = ITEMS.register("advanced_miner", 
        () -> new BlockItem(ADVANCED_MINER.get(), new Item.Properties()));
}