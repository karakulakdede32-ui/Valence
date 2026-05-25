package com.valence.valence;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import com.valence.valence.recipe.GrinderRecipe;
import com.valence.valence.block.miner.BasicMinerBlock;
import com.valence.valence.block.miner.BasicMinerTileEntity;
import com.valence.valence.block.miner.AdvancedMinerBlock;
import com.valence.valence.block.miner.AdvancedMinerTileEntity;
import com.valence.valence.block.miner.BasicMinerMenu;
import com.valence.valence.block.miner.AdvancedMinerMenu;
import com.valence.valence.block.collector.WaterCollectorBlock;
import com.valence.valence.block.collector.WaterCollectorTileEntity;
import com.valence.valence.block.collector.WaterCollectorMenu;
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
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ValenceMod.MODID);
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES = DeferredRegister.create(Registries.RECIPE_TYPE, ValenceMod.MODID);
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(Registries.RECIPE_SERIALIZER, ValenceMod.MODID);

    // Register blocks
    public static final RegistryObject<Block> BASIC_MINER = BLOCKS.register("basic_miner",
            () -> new BasicMinerBlock(BLOCK_PROPS));
    public static final RegistryObject<Block> ADVANCED_MINER = BLOCKS.register("advanced_miner",
            () -> new AdvancedMinerBlock(BLOCK_PROPS));
    public static final RegistryObject<Block> GRINDER = BLOCKS.register("grinder",
            () -> new com.valence.valence.block.GrinderBlock());
    public static final RegistryObject<Block> WATER_COLLECTOR = BLOCKS.register("water_collector",
            () -> new WaterCollectorBlock(BLOCK_PROPS));

    // BlockEntityType suppliers
    public static final RegistryObject<BlockEntityType<BasicMinerTileEntity>> BASIC_MINER_TE = BLOCK_ENTITIES.register("basic_miner",
            () -> BlockEntityType.Builder.of(BasicMinerTileEntity::new, BASIC_MINER.get()).build(null));
    public static final RegistryObject<BlockEntityType<AdvancedMinerTileEntity>> ADVANCED_MINER_TE = BLOCK_ENTITIES.register("advanced_miner",
            () -> BlockEntityType.Builder.of(AdvancedMinerTileEntity::new, ADVANCED_MINER.get()).build(null));
    public static final RegistryObject<BlockEntityType<com.valence.valence.block.GrinderTileEntity>> GRINDER_TE = BLOCK_ENTITIES.register("grinder",
            () -> BlockEntityType.Builder.of(com.valence.valence.block.GrinderTileEntity::new, GRINDER.get()).build(null));
    public static final RegistryObject<BlockEntityType<WaterCollectorTileEntity>> WATER_COLLECTOR_TE = BLOCK_ENTITIES.register("water_collector",
            () -> BlockEntityType.Builder.of(WaterCollectorTileEntity::new, WATER_COLLECTOR.get()).build(null));

    // MenuType suppliers
    public static final RegistryObject<MenuType<BasicMinerMenu>> BASIC_MINER_MENU = MENUS.register("basic_miner",
            () -> IForgeMenuType.create(BasicMinerMenu::new));
    public static final RegistryObject<MenuType<AdvancedMinerMenu>> ADVANCED_MINER_MENU = MENUS.register("advanced_miner",
            () -> IForgeMenuType.create(AdvancedMinerMenu::new));
    public static final RegistryObject<MenuType<com.valence.valence.block.GrinderMenu>> GRINDER_MENU = MENUS.register("grinder",
            () -> IForgeMenuType.create(com.valence.valence.block.GrinderMenu::new));
    public static final RegistryObject<MenuType<WaterCollectorMenu>> WATER_COLLECTOR_MENU = MENUS.register("water_collector",
            () -> IForgeMenuType.create(WaterCollectorMenu::new));

    public static final RegistryObject<RecipeType<GrinderRecipe>> GRINDING_RECIPE_TYPE = RECIPE_TYPES.register("grinding", () -> GrinderRecipe.Type.INSTANCE);
    public static final RegistryObject<RecipeSerializer<GrinderRecipe>> GRINDING_RECIPE_SERIALIZER = RECIPE_SERIALIZERS.register("grinding", () -> GrinderRecipe.Serializer.INSTANCE);

    // Register block items
    public static final RegistryObject<Item> BASIC_MINER_ITEM = ITEMS.register("basic_miner",
            () -> new BlockItem(BASIC_MINER.get(), new Item.Properties()));
    public static final RegistryObject<Item> STONE_PEBBLE = ITEMS.register("stone_pebble",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> ADVANCED_MINER_ITEM = ITEMS.register("advanced_miner",
            () -> new BlockItem(ADVANCED_MINER.get(), new Item.Properties()));
    public static final RegistryObject<Item> GRINDER_ITEM = ITEMS.register("grinder",
            () -> new BlockItem(GRINDER.get(), new Item.Properties()));
    public static final RegistryObject<Item> WATER_COLLECTOR_ITEM = ITEMS.register("water_collector",
            () -> new BlockItem(WATER_COLLECTOR.get(), new Item.Properties()));

    public static final RegistryObject<Item> IRON_POWDER = ITEMS.register("iron_powder",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> GOLD_POWDER = ITEMS.register("gold_powder",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> REDSTONE_POWDER = ITEMS.register("redstone_powder",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> COPPER_POWDER = ITEMS.register("copper_powder",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> BRONZE_INGOT = ITEMS.register("bronze_ingot",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> LAPIS_POWDER = ITEMS.register("lapis_powder",
            () -> new Item(new Item.Properties()));

    // Creative tab registration
    public static final RegistryObject<CreativeModeTab> VALENCE_TAB = CREATIVE_MODE_TABS.register("valence_tab",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.valence"))
                    .icon(() -> new ItemStack(BASIC_MINER_ITEM.get()))
                    .displayItems((params, output) -> {
                        output.accept(BASIC_MINER_ITEM.get());
                        output.accept(ADVANCED_MINER_ITEM.get());
                        output.accept(GRINDER_ITEM.get());
                        output.accept(WATER_COLLECTOR_ITEM.get());
                        output.accept(IRON_POWDER.get());
                        output.accept(GOLD_POWDER.get());
                        output.accept(REDSTONE_POWDER.get());
                        output.accept(COPPER_POWDER.get());
                        output.accept(BRONZE_INGOT.get());
                        output.accept(LAPIS_POWDER.get());
                    })
                    .build());

    // Helper method for ResourceLocation - Fixed for 1.20.1
    public static ResourceLocation location(String name) {
        return new ResourceLocation(ValenceMod.MODID, name);
    }
}
