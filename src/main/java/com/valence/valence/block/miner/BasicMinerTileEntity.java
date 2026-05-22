package com.valence.valence.block.miner;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.Container;
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

public class BasicMinerTileEntity extends BlockEntity implements WorldlyContainer, MenuProvider {
    private final List<ItemStack> scannedOres = new ArrayList<>();
    private boolean hasScanned = false;
    private final ItemStack[] slots = new ItemStack[4];

    // Constructor for BlockEntityType.Builder.of (BlockPos, BlockState)
    public BasicMinerTileEntity(BlockPos pos, BlockState state) {
        this((BlockEntityType<BasicMinerTileEntity>) null, pos, state);
    }

    // Full constructor with BlockEntityType
    public BasicMinerTileEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        Arrays.fill(slots, ItemStack.EMPTY);
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

    // Container implementation
    @Override
    public boolean isEmpty() {
        for (ItemStack slot : slots) {
            if (!slot.isEmpty()) return false;
        }
        return true;
    }

    // WorldlyContainer implementation
    @Override
    public int[] getSlotsForFace(net.minecraft.core.Direction p_155524_1_) {
        return new int[]{0, 1, 2, 3};
    }

    @Override
    public boolean canPlaceItemThroughFace(int index, ItemStack stack, net.minecraft.core.Direction direction) {
        return false;
    }

    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, net.minecraft.core.Direction direction) {
        return true;
    }

    @Override
    public int getContainerSize() {
        return 4;
    }

    @Override
    public ItemStack getItem(int index) {
        return index >= 0 && index < 4 ? slots[index] : ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItem(int index, int count) {
        if (index >= 0 && index < 4) {
            ItemStack stack = slots[index];
            slots[index] = ItemStack.EMPTY;
            return stack;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItemNoUpdate(int index) {
        if (index >= 0 && index < 4) {
            ItemStack stack = slots[index];
            slots[index] = ItemStack.EMPTY;
            return stack;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public void setItem(int index, ItemStack stack) {
        if (index >= 0 && index < 4) {
            slots[index] = stack;
        }
    }

    @Override
    public boolean stillValid(net.minecraft.world.entity.player.Player player) {
        return true;
    }

    @Override
    public void clearContent() {
        Arrays.fill(slots, ItemStack.EMPTY);
    }
}