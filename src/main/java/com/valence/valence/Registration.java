package com.valence.valence;

import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.BlockEntity;
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
import net.minecraft.core.BlockPos;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

public class Registration {
    // Block property - stone-like using correct API for 1.20.1
    private static final BlockBehaviour.Properties BLOCK_PROPS = 
            BlockBehaviour.Properties.copy(Blocks.STONE);

    // Deferred Registries
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, ValenceMod.MODID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, ValenceMod.MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, ValenceMod.MODID);
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, ValenceMod.MODID);

    // Register blocks first
    public static final RegistryObject<Block> BASIC_MINER = BLOCKS.register("basic_miner",
            () -> new BasicMinerBlock(BLOCK_PROPS));
    public static final RegistryObject<Block> ADVANCED_MINER = BLOCKS.register("advanced_miner",
            () -> new AdvancedMinerBlock(BLOCK_PROPS));

    // BlockEntityType suppliers
    public static final RegistryObject<BlockEntityType<BasicMinerTileEntity>> BASIC_MINER_TE = BLOCK_ENTITIES.register("basic_miner",
            () -> BlockEntityType.Builder.of(BasicMinerTileEntity::new, BASIC_MINER.get()).build(null));
    public static final RegistryObject<BlockEntityType<AdvancedMinerTileEntity>> ADVANCED_MINER_TE = BLOCK_ENTITIES.register("advanced_miner",
            () -> BlockEntityType.Builder.of(AdvancedMinerTileEntity::new, ADVANCED_MINER.get()).build(null));

    // MenuType suppliers - fully raw approach
    public static final RegistryObject<MenuType<BasicMinerMenu>> BASIC_MINER_MENU = MENUS.register("basic_miner",
            () -> createMenuTypeRaw(BasicMinerMenu.class));
    public static final RegistryObject<MenuType<AdvancedMinerMenu>> ADVANCED_MINER_MENU = MENUS.register("advanced_miner",
            () -> createMenuTypeRaw(AdvancedMinerMenu.class));

    // Helper to create MenuType using reflection - bypasses Java compile-time issues
    @SuppressWarnings("unchecked")
    private static <T extends net.minecraft.world.inventory.AbstractContainerMenu> MenuType<T> createMenuTypeRaw(Class<T> menuClass) {
        try {
            // Get the MenuSupplier interface
            Class<?> menuSupplierClass = Class.forName("net.minecraft.world.inventory.MenuSupplier");
            
            // Get the EMPTY FeatureFlagSet using reflection
            Class<?> featureFlagSetClass = Class.forName("net.minecraft.world.util.FeatureFlagSet");
            Field emptyField = featureFlagSetClass.getDeclaredField("EMPTY");
            emptyField.setAccessible(true);
            Object emptyFlagSet = emptyField.get(null);
            
            // Get constructor MenuType(MenuSupplier, FeatureFlagSet)
            Constructor<?> constructor = MenuType.class.getDeclaredConstructor(menuSupplierClass, featureFlagSetClass);
            constructor.setAccessible(true);
            
            // Get Level class for cast
            Class<?> levelClass = Class.forName("net.minecraft.world.level.Level");
            Class<?> blockPosClass = Class.forName("net.minecraft.core.BlockPos");
            
            // Create lambda for the menu supplier that matches the expected signature
            // MenuSupplier has method: T createMenu(int id, Inventory inv, ContainerLevelAccess access)
            Object menuSupplier = java.lang.reflect.Proxy.newProxyInstance(
                    menuSupplierClass.getClassLoader(),
                    new Class<?>[] { menuSupplierClass },
                    (proxy, method, args) -> {
                        if (method.getName().equals("createMenu")) {
                            int id = (int) args[0];
                            net.minecraft.world.entity.player.Inventory inv = (net.minecraft.world.entity.player.Inventory) args[1];
                            Object access = args[2];
                            
                            // Get tile entity from ContainerLevelAccess using reflection
                            Class<?> containerLevelAccessClass = Class.forName("net.minecraft.world.inventory.ContainerLevelAccess");
                            java.lang.reflect.Method evaluateMethod = containerLevelAccessClass.getDeclaredMethod("evaluate", 
                                    java.util.function.BiFunction.class, Object.class);
                            
                            // Use raw BiFunction with Object types
                            java.util.function.BiFunction<Object, BlockPos, Object> biFunc = (lvl, pos) -> {
                                BlockEntity be = ((net.minecraft.world.level.Level) lvl).getBlockEntity(pos);
                                if (be instanceof BasicMinerTileEntity te) return te;
                                if (be instanceof AdvancedMinerTileEntity te2) return te2;
                                return null;
                            };
                            
                            Object tileEntity = evaluateMethod.invoke(access, biFunc, null);
                            
                            // Call menu constructor with id, inv, and tileEntity
                            Constructor<?> menuConstructor = menuClass.getDeclaredConstructor(
                                    int.class,
                                    net.minecraft.world.entity.player.Inventory.class,
                                    tileEntity.getClass());
                            return menuConstructor.newInstance(id, inv, tileEntity);
                        }
                        return null;
                    });
            
            return (MenuType<T>) constructor.newInstance(menuSupplier, emptyFlagSet);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create MenuType for " + menuClass.getSimpleName(), e);
        }
    }

    // Register block items
    public static final RegistryObject<Item> BASIC_MINER_ITEM = ITEMS.register("basic_miner",
            () -> new BlockItem(BASIC_MINER.get(), new Item.Properties()));
    public static final RegistryObject<Item> ADVANCED_MINER_ITEM = ITEMS.register("advanced_miner",
            () -> new BlockItem(ADVANCED_MINER.get(), new Item.Properties()));
}