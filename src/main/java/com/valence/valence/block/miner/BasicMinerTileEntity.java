package com.valence.valence.block.miner;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

public class BasicMinerTileEntity extends BlockEntity implements WorldlyContainer, MenuProvider {
    private final List<ItemStack> scannedOres = new ArrayList<>();
    private boolean hasScanned = false;
    
    // ItemStackHandler for mod compatibility (EnderIO, Create, Mekanism, etc.)
    private final ItemStackHandler itemHandler = new ItemStackHandler(4) {
        @Override
        public void onContentsChanged(int slot) {
            setChanged();
            if (level != null && !level.isClientSide()) {
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
            }
        }
    };
    
    // LazyOptional for capability-based access
    private final LazyOptional<IItemHandler> itemHandlerCapability = LazyOptional.of(() -> itemHandler);

    // Constructor for BlockEntityType.Builder.of (BlockPos, BlockState)
    public BasicMinerTileEntity(BlockPos pos, BlockState state) {
        this((BlockEntityType<BasicMinerTileEntity>) null, pos, state);
    }

    // Full constructor with BlockEntityType
    public BasicMinerTileEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("Items")) {
            itemHandler.deserializeNBT(tag.getCompound("Items"));
        }
        hasScanned = tag.getBoolean("hasScanned");
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Items", itemHandler.serializeNBT());
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
        return Component.literal("Basic Miner");
    }

    @Override
    public net.minecraft.world.inventory.AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
        return new BasicMinerMenu(id, inv, this);
    }

    public void scanChunk(ServerLevel lvl) {
        if (lvl == null) return;
        BlockPos p = this.getBlockPos();
        if (p == null) return;
        
        scannedOres.clear();
        
        int chunkX = (p.getX() >> 4) << 4;
        int chunkZ = (p.getZ() >> 4) << 4;
        
        // Count ore types
        Map<net.minecraft.world.level.block.Block, Integer> oreCounts = new HashMap<>();
        
        // Scan chunk area
        for (int x = chunkX; x < chunkX + 16; x++) {
            for (int z = chunkZ; z < chunkZ + 16; z++) {
                for (int y = -64; y < 320; y++) {
                    BlockPos checkPos = new BlockPos(x, y, z);
                    BlockState bs = lvl.getBlockState(checkPos);
                    if (isOre(bs)) {
                        oreCounts.merge(bs.getBlock(), 1, Integer::sum);
                    }
                }
            }
        }
        
        // Sort by count (descending)
        List<Map.Entry<net.minecraft.world.level.block.Block, Integer>> sorted = new ArrayList<>(oreCounts.entrySet());
        sorted.sort((a, b) -> Integer.compare(b.getValue(), a.getValue()));
        
        // Take top 4 unique ores
        int count = 0;
        Set<net.minecraft.world.level.block.Block> seen = new HashSet<>();
        for (Map.Entry<net.minecraft.world.level.block.Block, Integer> entry : sorted) {
            net.minecraft.world.level.block.Block blk = entry.getKey();
            if (count >= 4 || seen.contains(blk)) continue;
            seen.add(blk);
            scannedOres.add(new ItemStack(blk, entry.getValue()));
            count++;
        }
        
        hasScanned = true;
    }
    
    private boolean isOre(BlockState state) {
        net.minecraft.world.level.block.Block blk = state.getBlock();
        return blk == Blocks.COAL_ORE || blk == Blocks.IRON_ORE 
            || blk == Blocks.GOLD_ORE || blk == Blocks.COPPER_ORE
            || blk == Blocks.DIAMOND_ORE || blk == Blocks.EMERALD_ORE
            || blk == Blocks.LAPIS_ORE || blk == Blocks.REDSTONE_ORE;
    }

    public List<ItemStack> getScannedOres() {
        return scannedOres;
    }

    public boolean hasScanned() {
        return hasScanned;
    }

    // ========== Container implementation (for vanilla) ==========
    @Override
    public boolean isEmpty() {
        return itemHandler.getSlots() == 0 || java.util.stream.IntStream.range(0, itemHandler.getSlots())
                .noneMatch(i -> !itemHandler.getStackInSlot(i).isEmpty());
    }

    @Override
    public int getContainerSize() {
        return 4;
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

    // ========== WorldlyContainer implementation ==========
    @Override
    public int[] getSlotsForFace(Direction side) {
        return new int[]{0, 1, 2, 3};
    }

    @Override
    public boolean canPlaceItemThroughFace(int index, ItemStack stack, Direction direction) {
        return false; // Miners don't accept items from automation
    }

    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
        return true;
    }
}