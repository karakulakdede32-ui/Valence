package com.valence.valence.block.miner;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.Container;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.item.ItemStack;

public class AdvancedMinerTileEntity extends BlockEntity implements WorldlyContainer, MenuProvider {
    private int fuel = 0;
    private final List<ItemStack> extractedOres = new ArrayList<>();
    private final ItemStack[] slots = new ItemStack[9]; // 1 fuel + 8 output slots

    // Constructor for BlockEntityType.Builder.of (BlockPos, BlockState)
    public AdvancedMinerTileEntity(BlockPos pos, BlockState state) {
        this((BlockEntityType<AdvancedMinerTileEntity>) null, pos, state);
    }

    // Full constructor with BlockEntityType
    public AdvancedMinerTileEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        Arrays.fill(slots, ItemStack.EMPTY);
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

    public void extractAllOreTypes(ServerLevel lvl) {
        if (lvl == null || fuel <= 0) return;
        
        BlockPos p = this.getBlockPos();
        if (p == null) return;
        
        int chunkX = (p.getX() >> 4) << 4;
        int chunkZ = (p.getZ() >> 4) << 4;
        
        // Collect one of each ore type
        Set<net.minecraft.world.level.block.Block> foundOres = new HashSet<>();
        
        for (int x = chunkX; x < chunkX + 16; x++) {
            for (int z = chunkZ; z < chunkZ + 16; z++) {
                for (int y = -64; y < 320; y++) {
                    BlockPos checkPos = new BlockPos(x, y, z);
                    BlockState bs = lvl.getBlockState(checkPos);
                    if (isOre(bs) && !foundOres.contains(bs.getBlock())) {
                        foundOres.add(bs.getBlock());
                        extractedOres.add(new ItemStack(bs.getBlock(), 1));
                        
                        if (foundOres.size() >= 8) break;
                    }
                }
                if (foundOres.size() >= 8) break;
            }
            if (foundOres.size() >= 8) break;
        }
        
        fuel--;
    }

    private boolean isOre(BlockState state) {
        net.minecraft.world.level.block.Block blk = state.getBlock();
        return blk == Blocks.COAL_ORE || blk == Blocks.IRON_ORE 
            || blk == Blocks.GOLD_ORE || blk == Blocks.COPPER_ORE
            || blk == Blocks.DIAMOND_ORE || blk == Blocks.EMERALD_ORE
            || blk == Blocks.LAPIS_ORE || blk == Blocks.REDSTONE_ORE;
    }

    public List<ItemStack> getExtractedOres() {
        return extractedOres;
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
        int[] slots = new int[9];
        for (int i = 0; i < 9; i++) slots[i] = i;
        return slots;
    }

    @Override
    public boolean canPlaceItemThroughFace(int index, ItemStack stack, net.minecraft.core.Direction direction) {
        return index == 0; // Only allow fuel in slot 0
    }

    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, net.minecraft.core.Direction direction) {
        return index > 0; // Can only take from output slots
    }

    @Override
    public int getContainerSize() {
        return 9;
    }

    @Override
    public ItemStack getItem(int index) {
        return index >= 0 && index < 9 ? slots[index] : ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItem(int index, int count) {
        if (index >= 0 && index < 9) {
            ItemStack stack = slots[index];
            slots[index] = ItemStack.EMPTY;
            return stack;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItemNoUpdate(int index) {
        if (index >= 0 && index < 9) {
            ItemStack stack = slots[index];
            slots[index] = ItemStack.EMPTY;
            return stack;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public void setItem(int index, ItemStack stack) {
        if (index >= 0 && index < 9) {
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