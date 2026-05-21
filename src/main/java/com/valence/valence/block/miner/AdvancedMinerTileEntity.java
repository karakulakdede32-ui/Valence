package com.valence.valence.block.miner;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ContainerOpenersCounter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;

public class AdvancedMinerTileEntity extends BlockEntity implements ContainerOpenersCounter {
    private int fuel = 0;
    private final List<ItemStack> extractedOres = new ArrayList<>();
    public final ContainerOpenersCounter openersCounter = new ContainerOpenersCounter();

    public AdvancedMinerTileEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public Component getDisplayName() {
        return Component.literal("Advanced Miner");
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
}