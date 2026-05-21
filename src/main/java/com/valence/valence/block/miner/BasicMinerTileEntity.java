package com.valence.valence.block.miner;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ContainerOpenersCounter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.nbt.CompoundTag;

public class BasicMinerTileEntity extends BlockEntity implements ContainerOpenersCounter {
    private final List<ItemStack> scannedOres = new ArrayList<ItemStack>();
    private boolean hasScanned = false;
    public final ContainerOpenersCounter openersCounter = new ContainerOpenersCounter();

    // Required constructor for 1.20.1
    public BasicMinerTileEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public Component getDisplayName() {
        return Component.literal("Basic Miner");
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
}