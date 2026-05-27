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
import net.minecraft.world.level.material.Fluid;
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
import com.valence.valence.block.dynamo.SteamDynamoBlock;
import com.valence.valence.block.dynamo.SteamDynamoTileEntity;
import com.valence.valence.block.dynamo.SteamDynamoMenu;
import com.valence.valence.block.alloyer.SteamAlloyerBlock;
import com.valence.valence.block.alloyer.SteamAlloyerTileEntity;
import com.valence.valence.block.alloyer.SteamAlloyerMenu;
import com.valence.valence.block.furnace.SteamFurnaceBlock;
import com.valence.valence.block.furnace.SteamFurnaceTileEntity;
import com.valence.valence.block.furnace.SteamFurnaceMenu;
import com.valence.valence.block.turbine.SteamTurbineBlock;
import com.valence.valence.block.turbine.SteamTurbineTileEntity;
import com.valence.valence.block.turbine.SteamTurbineMenu;
import com.valence.valence.block.dfcell.DFCellBlock;
import com.valence.valence.block.dfcell.DFCellTileEntity;
import com.valence.valence.block.dfcell.DFCellMenu;
import com.valence.valence.block.seeder.SeedDuplicatorBlock;
import com.valence.valence.block.seeder.SeedDuplicatorTileEntity;
import com.valence.valence.block.seeder.SeedDuplicatorMenu;
import com.valence.valence.fluid.SteamFluid;
import com.valence.valence.block.conduit.TransferConduitBlock;
import com.valence.valence.block.conduit.TransferConduitTileEntity;
import com.valence.valence.block.conduit.TransferConduitMenu;
import com.valence.valence.item.LinkingTool;
import com.valence.valence.item.ChunkExcavator;
import com.valence.valence.block.pipe.FluidPipeBlock;
import com.valence.valence.block.pipe.FluidPipeTileEntity;
import com.valence.valence.block.pipe.EnergyCableBlock;
import com.valence.valence.block.pipe.EnergyCableTileEntity;
import com.valence.valence.block.wireless.WirelessNodeBlock;
import com.valence.valence.block.wireless.WirelessNodeTileEntity;
import com.valence.valence.block.wireless.WirelessNodeMenu;
import net.minecraftforge.common.extensions.IForgeMenuType;

public class Registration {
    private static final BlockBehaviour.Properties BLOCK_PROPS = BlockBehaviour.Properties.copy(Blocks.STONE);

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, ValenceMod.MODID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, ValenceMod.MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, ValenceMod.MODID);
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, ValenceMod.MODID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ValenceMod.MODID);
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES = DeferredRegister.create(Registries.RECIPE_TYPE, ValenceMod.MODID);
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(Registries.RECIPE_SERIALIZER, ValenceMod.MODID);
    public static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(ForgeRegistries.FLUIDS, ValenceMod.MODID);

    // Blocks
    public static final RegistryObject<Block> BASIC_MINER = BLOCKS.register("basic_miner", () -> new BasicMinerBlock(BLOCK_PROPS));
    public static final RegistryObject<Block> ADVANCED_MINER = BLOCKS.register("advanced_miner", () -> new AdvancedMinerBlock(BLOCK_PROPS));
    public static final RegistryObject<Block> GRINDER = BLOCKS.register("grinder", () -> new com.valence.valence.block.GrinderBlock());
    public static final RegistryObject<Block> WATER_COLLECTOR = BLOCKS.register("water_collector", () -> new WaterCollectorBlock(BLOCK_PROPS));
    public static final RegistryObject<Block> STEAM_DYNAMO = BLOCKS.register("steam_dynamo", () -> new SteamDynamoBlock(BLOCK_PROPS));
    public static final RegistryObject<Block> STEAM_ALLOYER = BLOCKS.register("steam_alloyer", () -> new SteamAlloyerBlock(BLOCK_PROPS));
    public static final RegistryObject<Block> STEAM_FURNACE = BLOCKS.register("steam_furnace", () -> new SteamFurnaceBlock(BLOCK_PROPS));
    public static final RegistryObject<Block> STEAM_TURBINE = BLOCKS.register("steam_turbine", () -> new SteamTurbineBlock(BLOCK_PROPS));
    public static final RegistryObject<Block> DF_CELL = BLOCKS.register("df_cell", () -> new DFCellBlock(BLOCK_PROPS));
    public static final RegistryObject<Block> SEED_DUPLICATOR = BLOCKS.register("seed_duplicator", () -> new SeedDuplicatorBlock(BLOCK_PROPS));
    public static final RegistryObject<Block> TRANSFER_CONDUIT = BLOCKS.register("transfer_conduit", () -> new TransferConduitBlock(BLOCK_PROPS));
    public static final RegistryObject<Block> FLUID_PIPE = BLOCKS.register("fluid_pipe", () -> new FluidPipeBlock(BLOCK_PROPS.noOcclusion()));
    public static final RegistryObject<Block> ENERGY_CABLE = BLOCKS.register("energy_cable", () -> new EnergyCableBlock(BLOCK_PROPS.noOcclusion()));
    public static final RegistryObject<Block> WIRELESS_NODE = BLOCKS.register("wireless_node", () -> new WirelessNodeBlock(BLOCK_PROPS));

    // BlockEntityTypes
    public static final RegistryObject<BlockEntityType<BasicMinerTileEntity>> BASIC_MINER_TE = BLOCK_ENTITIES.register("basic_miner",
            () -> BlockEntityType.Builder.of(BasicMinerTileEntity::new, BASIC_MINER.get()).build(null));
    public static final RegistryObject<BlockEntityType<AdvancedMinerTileEntity>> ADVANCED_MINER_TE = BLOCK_ENTITIES.register("advanced_miner",
            () -> BlockEntityType.Builder.of(AdvancedMinerTileEntity::new, ADVANCED_MINER.get()).build(null));
    public static final RegistryObject<BlockEntityType<com.valence.valence.block.GrinderTileEntity>> GRINDER_TE = BLOCK_ENTITIES.register("grinder",
            () -> BlockEntityType.Builder.of(com.valence.valence.block.GrinderTileEntity::new, GRINDER.get()).build(null));
    public static final RegistryObject<BlockEntityType<WaterCollectorTileEntity>> WATER_COLLECTOR_TE = BLOCK_ENTITIES.register("water_collector",
            () -> BlockEntityType.Builder.of(WaterCollectorTileEntity::new, WATER_COLLECTOR.get()).build(null));
    public static final RegistryObject<BlockEntityType<SteamDynamoTileEntity>> STEAM_DYNAMO_TE = BLOCK_ENTITIES.register("steam_dynamo",
            () -> BlockEntityType.Builder.of(SteamDynamoTileEntity::new, STEAM_DYNAMO.get()).build(null));
    public static final RegistryObject<BlockEntityType<SteamAlloyerTileEntity>> STEAM_ALLOYER_TE = BLOCK_ENTITIES.register("steam_alloyer",
            () -> BlockEntityType.Builder.of(SteamAlloyerTileEntity::new, STEAM_ALLOYER.get()).build(null));
    public static final RegistryObject<BlockEntityType<SteamFurnaceTileEntity>> STEAM_FURNACE_TE = BLOCK_ENTITIES.register("steam_furnace",
            () -> BlockEntityType.Builder.of(SteamFurnaceTileEntity::new, STEAM_FURNACE.get()).build(null));
    public static final RegistryObject<BlockEntityType<SteamTurbineTileEntity>> STEAM_TURBINE_TE = BLOCK_ENTITIES.register("steam_turbine",
            () -> BlockEntityType.Builder.of(SteamTurbineTileEntity::new, STEAM_TURBINE.get()).build(null));
    public static final RegistryObject<BlockEntityType<DFCellTileEntity>> DF_CELL_TE = BLOCK_ENTITIES.register("df_cell",
            () -> BlockEntityType.Builder.of(DFCellTileEntity::new, DF_CELL.get()).build(null));
    public static final RegistryObject<BlockEntityType<SeedDuplicatorTileEntity>> SEED_DUPLICATOR_TE = BLOCK_ENTITIES.register("seed_duplicator",
            () -> BlockEntityType.Builder.of(SeedDuplicatorTileEntity::new, SEED_DUPLICATOR.get()).build(null));
    public static final RegistryObject<BlockEntityType<TransferConduitTileEntity>> TRANSFER_CONDUIT_TE = BLOCK_ENTITIES.register("transfer_conduit",
            () -> BlockEntityType.Builder.of(TransferConduitTileEntity::new, TRANSFER_CONDUIT.get()).build(null));
    public static final RegistryObject<BlockEntityType<FluidPipeTileEntity>> FLUID_PIPE_TE = BLOCK_ENTITIES.register("fluid_pipe",
            () -> BlockEntityType.Builder.of(FluidPipeTileEntity::new, FLUID_PIPE.get()).build(null));
    public static final RegistryObject<BlockEntityType<EnergyCableTileEntity>> ENERGY_CABLE_TE = BLOCK_ENTITIES.register("energy_cable",
            () -> BlockEntityType.Builder.of(EnergyCableTileEntity::new, ENERGY_CABLE.get()).build(null));
    public static final RegistryObject<BlockEntityType<WirelessNodeTileEntity>> WIRELESS_NODE_TE = BLOCK_ENTITIES.register("wireless_node",
            () -> BlockEntityType.Builder.of(WirelessNodeTileEntity::new, WIRELESS_NODE.get()).build(null));

    // Menus
    public static final RegistryObject<MenuType<BasicMinerMenu>> BASIC_MINER_MENU = MENUS.register("basic_miner", () -> IForgeMenuType.create(BasicMinerMenu::new));
    public static final RegistryObject<MenuType<AdvancedMinerMenu>> ADVANCED_MINER_MENU = MENUS.register("advanced_miner", () -> IForgeMenuType.create(AdvancedMinerMenu::new));
    public static final RegistryObject<MenuType<com.valence.valence.block.GrinderMenu>> GRINDER_MENU = MENUS.register("grinder", () -> IForgeMenuType.create(com.valence.valence.block.GrinderMenu::new));
    public static final RegistryObject<MenuType<WaterCollectorMenu>> WATER_COLLECTOR_MENU = MENUS.register("water_collector", () -> IForgeMenuType.create(WaterCollectorMenu::new));
    public static final RegistryObject<MenuType<SteamDynamoMenu>> STEAM_DYNAMO_MENU = MENUS.register("steam_dynamo", () -> IForgeMenuType.create(SteamDynamoMenu::new));
    public static final RegistryObject<MenuType<SteamAlloyerMenu>> STEAM_ALLOYER_MENU = MENUS.register("steam_alloyer", () -> IForgeMenuType.create(SteamAlloyerMenu::new));
    public static final RegistryObject<MenuType<SteamFurnaceMenu>> STEAM_FURNACE_MENU = MENUS.register("steam_furnace", () -> IForgeMenuType.create(SteamFurnaceMenu::new));
    public static final RegistryObject<MenuType<SteamTurbineMenu>> STEAM_TURBINE_MENU = MENUS.register("steam_turbine", () -> IForgeMenuType.create(SteamTurbineMenu::new));
    public static final RegistryObject<MenuType<DFCellMenu>> DF_CELL_MENU = MENUS.register("df_cell", () -> IForgeMenuType.create(DFCellMenu::new));
    public static final RegistryObject<MenuType<SeedDuplicatorMenu>> SEED_DUPLICATOR_MENU = MENUS.register("seed_duplicator", () -> IForgeMenuType.create(SeedDuplicatorMenu::new));
    public static final RegistryObject<MenuType<TransferConduitMenu>> TRANSFER_CONDUIT_MENU = MENUS.register("transfer_conduit", () -> IForgeMenuType.create(TransferConduitMenu::new));
    public static final RegistryObject<MenuType<WirelessNodeMenu>> WIRELESS_NODE_MENU = MENUS.register("wireless_node", () -> IForgeMenuType.create(WirelessNodeMenu::new));

    // Fluids
    public static final RegistryObject<Fluid> STEAM = FLUIDS.register("steam", SteamFluid.Source::new);
    public static final RegistryObject<Fluid> STEAM_FLOWING = FLUIDS.register("steam_flowing", SteamFluid.Flowing::new);

    // Recipes
    public static final RegistryObject<RecipeType<GrinderRecipe>> GRINDING_RECIPE_TYPE = RECIPE_TYPES.register("grinding", () -> GrinderRecipe.Type.INSTANCE);
    public static final RegistryObject<RecipeSerializer<GrinderRecipe>> GRINDING_RECIPE_SERIALIZER = RECIPE_SERIALIZERS.register("grinding", () -> GrinderRecipe.Serializer.INSTANCE);

    // Items
    public static final RegistryObject<Item> BASIC_MINER_ITEM = ITEMS.register("basic_miner", () -> new BlockItem(BASIC_MINER.get(), new Item.Properties()));
    public static final RegistryObject<Item> STONE_PEBBLE = ITEMS.register("stone_pebble", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> ADVANCED_MINER_ITEM = ITEMS.register("advanced_miner", () -> new BlockItem(ADVANCED_MINER.get(), new Item.Properties()));
    public static final RegistryObject<Item> GRINDER_ITEM = ITEMS.register("grinder", () -> new BlockItem(GRINDER.get(), new Item.Properties()));
    public static final RegistryObject<Item> WATER_COLLECTOR_ITEM = ITEMS.register("water_collector", () -> new BlockItem(WATER_COLLECTOR.get(), new Item.Properties()));
    public static final RegistryObject<Item> STEAM_DYNAMO_ITEM = ITEMS.register("steam_dynamo", () -> new BlockItem(STEAM_DYNAMO.get(), new Item.Properties()));
    public static final RegistryObject<Item> STEAM_ALLOYER_ITEM = ITEMS.register("steam_alloyer", () -> new BlockItem(STEAM_ALLOYER.get(), new Item.Properties()));
    public static final RegistryObject<Item> STEAM_FURNACE_ITEM = ITEMS.register("steam_furnace", () -> new BlockItem(STEAM_FURNACE.get(), new Item.Properties()));
    public static final RegistryObject<Item> STEAM_TURBINE_ITEM = ITEMS.register("steam_turbine", () -> new BlockItem(STEAM_TURBINE.get(), new Item.Properties()));
    public static final RegistryObject<Item> DF_CELL_ITEM = ITEMS.register("df_cell", () -> new BlockItem(DF_CELL.get(), new Item.Properties()));
    public static final RegistryObject<Item> SEED_DUPLICATOR_ITEM = ITEMS.register("seed_duplicator", () -> new BlockItem(SEED_DUPLICATOR.get(), new Item.Properties()));
    public static final RegistryObject<Item> TRANSFER_CONDUIT_ITEM = ITEMS.register("transfer_conduit", () -> new BlockItem(TRANSFER_CONDUIT.get(), new Item.Properties()));
    public static final RegistryObject<Item> FLUID_PIPE_ITEM = ITEMS.register("fluid_pipe", () -> new BlockItem(FLUID_PIPE.get(), new Item.Properties()));
    public static final RegistryObject<Item> ENERGY_CABLE_ITEM = ITEMS.register("energy_cable", () -> new BlockItem(ENERGY_CABLE.get(), new Item.Properties()));
    public static final RegistryObject<Item> WIRELESS_NODE_ITEM = ITEMS.register("wireless_node", () -> new BlockItem(WIRELESS_NODE.get(), new Item.Properties()));
    public static final RegistryObject<Item> LINKING_TOOL = ITEMS.register("linking_tool", () -> new LinkingTool(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CHUNK_EXCAVATOR = ITEMS.register("chunk_excavator", () -> new ChunkExcavator(new Item.Properties()));
    public static final RegistryObject<Item> IRON_POWDER = ITEMS.register("iron_powder", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> GOLD_POWDER = ITEMS.register("gold_powder", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> REDSTONE_POWDER = ITEMS.register("redstone_powder", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> COPPER_POWDER = ITEMS.register("copper_powder", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> BRONZE_INGOT = ITEMS.register("bronze_ingot", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> LAPIS_POWDER = ITEMS.register("lapis_powder", () -> new Item(new Item.Properties()));

    // Creative tab
    public static final RegistryObject<CreativeModeTab> VALENCE_TAB = CREATIVE_MODE_TABS.register("valence_tab",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.valence"))
                    .icon(() -> new ItemStack(BASIC_MINER_ITEM.get()))
                    .displayItems((params, output) -> {
                        output.accept(BASIC_MINER_ITEM.get());
                        output.accept(ADVANCED_MINER_ITEM.get());
                        output.accept(GRINDER_ITEM.get());
                        output.accept(WATER_COLLECTOR_ITEM.get());
                        output.accept(STEAM_DYNAMO_ITEM.get());
                        output.accept(STEAM_ALLOYER_ITEM.get());
                        output.accept(STEAM_FURNACE_ITEM.get());
                        output.accept(STEAM_TURBINE_ITEM.get());
                        output.accept(DF_CELL_ITEM.get());
                        output.accept(SEED_DUPLICATOR_ITEM.get());
                        output.accept(TRANSFER_CONDUIT_ITEM.get());
                        output.accept(FLUID_PIPE_ITEM.get());
                        output.accept(ENERGY_CABLE_ITEM.get());
                        output.accept(WIRELESS_NODE_ITEM.get());
                        output.accept(LINKING_TOOL.get());
                        output.accept(CHUNK_EXCAVATOR.get());
                        output.accept(IRON_POWDER.get());
                        output.accept(GOLD_POWDER.get());
                        output.accept(REDSTONE_POWDER.get());
                        output.accept(COPPER_POWDER.get());
                        output.accept(BRONZE_INGOT.get());
                        output.accept(LAPIS_POWDER.get());
                    })
                    .build());

    public static ResourceLocation location(String name) {
        return new ResourceLocation(ValenceMod.MODID, name);
    }
}
