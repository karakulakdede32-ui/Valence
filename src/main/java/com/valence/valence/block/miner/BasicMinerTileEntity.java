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

public class BasicMinerTileEntity extends BlockEntity {
    private final List<ItemStack> scannedOres = new ArrayList<ItemStack>();
    private boolean hasScanned = false;

    public BasicMinerTileEntity(BlockPos pos, BlockState state) {
        super(pos, state);
    }

    @Override
    public Component getDisplayName() {
        return Component.literal("Basic Miner");
    }

    public void scanChunk(ServerLevel lvl) {
        if (lvl == null) return;
        BlockPos p = this.getBlockPos();
        int cx = p.getX() >> 4;
        int cz = p.getZ() >> 4;
        LevelChunk ch = lvl.getChunk(cx, cz);
        if (ch == null) return;

        Map<String, Integer> counts = new HashMap<String, Integer>();
        counts.put("coal", 0); counts.put("iron", 0); counts.put("gold", 0);
        counts.put("redstone", 0); counts.put("lapis", 0); counts.put("diamond", 0);
        counts.put("emerald", 0); counts.put("copper", 0);

        int minY = lvl.getMinBuildHeight();
        int maxY = lvl.getMaxBuildHeight();
        int bx = ch.getPos().getMinBlockX();
        int bz = ch.getPos().getMinBlockZ();

        for (int x = 0; x < 16; x++) {
            for (int y = minY; y < maxY; y++) {
                for (int z = 0; z < 16; z++) {
                    BlockPos sp = new BlockPos(bx + x, y, bz + z);
                    net.minecraft.world.level.block.Block b = lvl.getBlockState(sp).getBlock();
                    if (b == Blocks.COAL_ORE) counts.merge("coal", 1, Integer::sum);
                    else if (b == Blocks.IRON_ORE) counts.merge("iron", 1, Integer::sum);
                    else if (b == Blocks.GOLD_ORE) counts.merge("gold", 1, Integer::sum);
                    else if (b == Blocks.REDSTONE_ORE) counts.merge("redstone", 1, Integer::sum);
                    else if (b == Blocks.LAPIS_ORE) counts.merge("lapis", 1, Integer::sum);
                    else if (b == Blocks.DIAMOND_ORE) counts.merge("diamond", 1, Integer::sum);
                    else if (b == Blocks.EMERALD_ORE) counts.merge("emerald", 1, Integer::sum);
                    else if (b == Blocks.COPPER_ORE) counts.merge("copper", 1, Integer::sum);
                }
            }
        }

        List<Map.Entry<String, Integer>> sorted = new ArrayList<Map.Entry<String, Integer>>(counts.entrySet());
        sorted.sort((a, b) -> b.getValue().compareTo(a.getValue()));

        scannedOres.clear();
        for (int i = 0; i < 4 && i < sorted.size(); i++) {
            Map.Entry<String, Integer> e = sorted.get(i);
            if (e.getValue() > 0) {
                scannedOres.add(new ItemStack(getOreItem(e.getKey()), e.getValue()));
            }
        }
        hasScanned = true;
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

    public List<ItemStack> getScannedOres() { return scannedOres; }
    public boolean hasScanned() { return hasScanned; }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        hasScanned = nbt.getBoolean("hasScanned");
    }

    @Override
    public void saveAdditional(CompoundTag nbt) {
        super.saveAdditional(nbt);
        nbt.putBoolean("hasScanned", hasScanned);
    }
}
