package com.valence.valence.block.miner;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.tags.BlockTags;
import net.minecraft.resources.ResourceLocation;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

public class AdvancedMinerTileEntity extends BlockEntity implements WorldlyContainer, MenuProvider {
    private int fuel = 0;
    private boolean hasScanned = false;
    
    // ItemStackHandler for mod compatibility (EnderIO, Create, Mekanism, etc.)
    // Slot 0 = Fuel input, Slots 1-8 = Output slots
    private final ItemStackHandler itemHandler = new ItemStackHandler(9) {
        @Override
        public void onContentsChanged(int slot) {
            setChanged();
            if (level != null && !level.isClientSide()) {
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
            }
            // Auto-detect fuel from slot 0
            if (slot == 0) {
                updateFuelFromSlot();
            }
        }
        
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            if (slot == 0) {
                // Fuel slot - accept any fuel item (coal, etc.)
                // Use getBurnTime with a RecipeType parameter
                return stack.getBurnTime(net.minecraft.world.item.crafting.RecipeType.SMELTING) > 0;
            }
            return false; // Output slots don't accept items
        }
    };
    
    // LazyOptional for capability-based access
    private final LazyOptional<IItemHandler> itemHandlerCapability = LazyOptional.of(() -> itemHandler);

    // Constructor for BlockEntityType.Builder.of (BlockPos, BlockState)
    public AdvancedMinerTileEntity(BlockPos pos, BlockState state) {
        this((BlockEntityType<AdvancedMinerTileEntity>) null, pos, state);
    }

    // Full constructor with BlockEntityType
    public AdvancedMinerTileEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("Items")) {
            itemHandler.deserializeNBT(tag.getCompound("Items"));
        }
        // Sync fuel from itemHandler if items were loaded
        updateFuelFromSlot();
        hasScanned = tag.getBoolean("hasScanned");
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Items", itemHandler.serializeNBT());
        tag.putInt("fuel", fuel);
        tag.putBoolean("hasScanned", hasScanned);
    }
    
    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return itemHandlerCapability.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public Component getDisplayName() {
        return Component.literal("Advanced Miner");
    }

    @Override
    public net.minecraft.world.inventory.AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
        return new AdvancedMinerMenu(id, inv, this);
    }

    public boolean hasFuel() {
        return fuel > 0;
    }

    public void setFuel(int amount) {
        this.fuel = amount;
    }
    
    private void updateFuelFromSlot() {
        if (fuel <= 0) {
            ItemStack fuelStack = itemHandler.getStackInSlot(0);
            if (!fuelStack.isEmpty()) {
                int burnTime = net.minecraftforge.common.ForgeHooks.getBurnTime(fuelStack, net.minecraft.world.item.crafting.RecipeType.SMELTING);
                if (burnTime > 0) {
                    fuel = burnTime;
                    itemHandler.extractItem(0, 1, false);
                }
            }
        }
    }
    
    public ItemStackHandler getItemHandler() {
        return itemHandler;
    }

    public int getFuel() {
        return fuel;
    }

    public void extractAllOreTypes(ServerLevel lvl) {
        if (lvl == null || fuel <= 0) return;
        
        BlockPos p = this.getBlockPos();
        if (p == null) return;
        
        int chunkX = (p.getX() >> 4) << 4;
        int chunkZ = (p.getZ() >> 4) << 4;
        
        Set<net.minecraft.world.level.block.Block> foundOres = new HashSet<>();
        
        for (int x = chunkX; x < chunkX + 16; x++) {
            for (int z = chunkZ; z < chunkZ + 16; z++) {
                for (int y = -64; y < 320; y++) {
                    BlockPos checkPos = new BlockPos(x, y, z);
                    BlockState bs = lvl.getBlockState(checkPos);
                    if (isOre(bs) && !foundOres.contains(bs.getBlock())) {
                        foundOres.add(bs.getBlock());
                        
                        // Find first empty output slot and add the ore
                        for (int slot = 1; slot < 9; slot++) {
                            if (itemHandler.getStackInSlot(slot).isEmpty()) {
                                itemHandler.setStackInSlot(slot, new ItemStack(bs.getBlock(), 1));
                                break;
                            }
                        }
                        
                        if (foundOres.size() >= 8) break;
                    }
                }
                if (foundOres.size() >= 8) break;
            }
            if (foundOres.size() >= 8) break;
        }
        
        fuel--;
        hasScanned = true;
        setChanged();
    }

    private boolean isOre(BlockState state) {
        net.minecraft.world.level.block.Block blk = state.getBlock();
        return blk.builtInRegistryHolder().is(BlockTags.IRON_ORES) ||
               blk.builtInRegistryHolder().is(BlockTags.GOLD_ORES) ||
               blk.builtInRegistryHolder().is(BlockTags.COPPER_ORES) ||
               blk.builtInRegistryHolder().is(BlockTags.REDSTONE_ORES) ||
               blk.builtInRegistryHolder().is(BlockTags.LAPIS_ORES) ||
               blk.builtInRegistryHolder().is(BlockTags.DIAMOND_ORES) ||
               blk.builtInRegistryHolder().is(BlockTags.EMERALD_ORES) ||
               blk.builtInRegistryHolder().is(BlockTags.COAL_ORES);
    }
    
    public boolean hasScanned() {
        return hasScanned;
    }

    public void scanStep(ServerLevel lvl) {
        if (lvl == null) return;
        extractAllOreTypes(lvl);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, AdvancedMinerTileEntity pEntity) {
        if (level.isClientSide()) return;

        if (!pEntity.hasScanned() && pEntity.hasFuel()) {
            pEntity.scanStep((ServerLevel) level);
            setChanged(level, pos, state);
        }
    }

    // ========== Container implementation (for vanilla) ==========
    @Override
    public boolean isEmpty() {
        return java.util.stream.IntStream.range(0, itemHandler.getSlots())
                .noneMatch(i -> !itemHandler.getStackInSlot(i).isEmpty());
    }

    // WorldlyContainer implementation - all sides have access to all slots
    @Override
    public int[] getSlotsForFace(Direction side) {
        int[] result = new int[9];
        for (int i = 0; i < 9; i++) result[i] = i;
        return result;
    }

    @Override
    public boolean canPlaceItemThroughFace(int index, ItemStack stack, Direction direction) {
        return index == 0; // Only allow fuel in slot 0
    }

    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
        return index > 0; // Can only take from output slots
    }

    @Override
    public int getContainerSize() {
        return 9;
    }

    @Override
    public ItemStack getItem(int index) {
        return itemHandler.getStackInSlot(index);
    }

    @Override
    public ItemStack removeItem(int index, int count) {
        return itemHandler.extractItem(index, count, false);
    }

    @Override
    public ItemStack removeItemNoUpdate(int index) {
        ItemStack stack = itemHandler.getStackInSlot(index);
        itemHandler.setStackInSlot(index, ItemStack.EMPTY);
        return stack;
    }

    @Override
    public void setItem(int index, ItemStack stack) {
        itemHandler.setStackInSlot(index, stack);
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public void clearContent() {
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            itemHandler.setStackInSlot(i, ItemStack.EMPTY);
        }
    }
}