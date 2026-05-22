package com.valence.valence;

import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import com.valence.valence.block.miner.BasicMinerBlock;
import com.valence.valence.block.miner.BasicMinerTileEntity;
import com.valence.valence.block.miner.AdvancedMinerBlock;
import com.valence.valence.block.miner.AdvancedMinerTileEntity;
import com.valence.valence.block.miner.BasicMinerMenu;
import com.valence.valence.block.miner.AdvancedMinerMenu;
import net.minecraftforge.common.extensions.IForgeMenuType;

public class Registration {
    // Block property - stone-like
    private static final BlockBehaviour.Properties BLOCK_PROPS = 
            BlockBehaviour.Properties.copy(Blocks.STONE);

    // Deferred Registries
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, ValenceMod.MODID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, ValenceMod.MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, ValenceMod.MODID);
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, ValenceMod.MODID);

    // Register blocks
    public static final RegistryObject<Block> BASIC_MINER = BLOCKS.register("basic_miner",
            () -> new BasicMinerBlock(BLOCK_PROPS));
    public static final RegistryObject<Block> ADVANCED_MINER = BLOCKS.register("advanced_miner",
            () -> new AdvancedMinerBlock(BLOCK_PROPS));

    // BlockEntityType suppliers
    public static final RegistryObject<BlockEntityType<BasicMinerTileEntity>> BASIC_MINER_TE = BLOCK_ENTITIES.register("basic_miner",
            () -> BlockEntityType.Builder.of(BasicMinerTileEntity::new, BASIC_MINER.get()).build(null));
    public static final RegistryObject<BlockEntityType<AdvancedMinerTileEntity>> ADVANCED_MINER_TE = BLOCK_ENTITIES.register("advanced_miner",
            () -> BlockEntityType.Builder.of(AdvancedMinerTileEntity::new, ADVANCED_MINER.get()).build(null));

    // MenuType suppliers - using IForgeMenuType.create for TileEntity-backed menus
    public static final RegistryObject<MenuType<BasicMinerMenu>> BASIC_MINER_MENU = MENUS.register("basic_miner",
            () -> IForgeMenuType.create(BasicMinerMenu::new));
    public static final RegistryObject<MenuType<AdvancedMinerMenu>> ADVANCED_MINER_MENU = MENUS.register("advanced_miner",
            () -> IForgeMenuType.create(AdvancedMinerMenu::new));

    // Register block items
    public static final RegistryObject<Item> BASIC_MINER_ITEM = ITEMS.register("basic_miner",
            () -> new BlockItem(BASIC_MINER.get(), new Item.Properties()));
    public static final RegistryObject<Item> ADVANCED_MINER_ITEM = ITEMS.register("advanced_miner",
            () -> new BlockItem(ADVANCED_MINER.get(), new Item.Properties()));
}