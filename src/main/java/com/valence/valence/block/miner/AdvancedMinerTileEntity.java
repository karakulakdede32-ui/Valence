package com.valence.valence.block.miner;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.nbt.CompoundTag;

public class AdvancedMinerTileEntity extends BlockEntity {
    private final List<ItemStack> outputItems = new ArrayList<ItemStack>();
    private boolean hasProcessed = false;

    public AdvancedMinerTileEntity(BlockPos pos, BlockState state) {
        super(pos, state);
    }

    @Override
    public Component getDisplayName() {
        return Component.literal("Advanced Miner");
    }

    public void processChunk(ServerLevel lvl, ItemStack fuel) {
        if (lvl == null) return;
        if (fuel.getItem() != Items.COAL && fuel.getItem() != Items.CHARCOAL) return;

        BlockPos p = this.getBlockPos();
        int cx = p.getX() >> 4;
        int cz = p.getZ() >> 4;
        LevelChunk ch = lvl.getChunk(cx, cz);
        if (ch == null) return;

        Set<String> found = new HashSet<String>();

        int minY = lvl.getMinBuildHeight();
        int maxY = lvl.getMaxBuildHeight();
        int bx = ch.getPos().getMinBlockX();
        int bz = ch.getPos().getMinBlockZ();

        for (int x = 0; x < 16; x++) {
            for (int y = minY; y < maxY; y++) {
                for (int z = 0; z < 16; z++) {
                    BlockPos sp = new BlockPos(bx + x, y, bz + z);
                    net.minecraft.world.level.block.Block b = lvl.getBlockState(sp).getBlock();
                    if (b == Blocks.COAL_ORE) found.add("coal");
                    else if (b == Blocks.IRON_ORE) found.add("iron");
                    else if (b == Blocks.GOLD_ORE) found.add("gold");
                    else if (b == Blocks.REDSTONE_ORE) found.add("redstone");
                    else if (b == Blocks.LAPIS_ORE) found.add("lapis");
                    else if (b == Blocks.DIAMOND_ORE) found.add("diamond");
                    else if (b == Blocks.EMERALD_ORE) found.add("emerald");
                    else if (b == Blocks.COPPER_ORE) found.add("copper");
                }
            }
        }

        outputItems.clear();
        for (String ore : found) {
            outputItems.add(new ItemStack(getOreItem(ore), 1));
        }
        hasProcessed = true;
        setChanged();
    }

    private net.minecraft.world.item.Item getOreItem(String name) {
        switch (name) {
            case "coal": return Items.COAL;
            case "iron": return Items.RAW_IRON;
            case "gold": return Items.RAW_GOLD;
            case "redstone": return Items.REDSTONE;
            case "lapis": return Items.LAPIS_LAZULI;
            case "diamond": return Items.DIAMOND;
            case "emerald": return Items.EMERALD;
            case "copper": return Items.RAW_COPPER;
            default: return Items.AIR;
        }
    }

    public List<ItemStack> getOutputItems() { return outputItems; }
    public boolean hasProcessed() { return hasProcessed; }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        hasProcessed = nbt.getBoolean("hasProcessed");
    }

    @Override
    public void saveAdditional(CompoundTag nbt) {
        super.saveAdditional(nbt);
        nbt.putBoolean("hasProcessed", hasProcessed);
    }
}
