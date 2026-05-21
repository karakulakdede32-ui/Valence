package com.valence.valence.block.miner;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.BlockBehaviour;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class BasicMinerBlock extends BaseEntityBlock {
    public BasicMinerBlock(BlockBehaviour.Properties p) {
        super(p);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BasicMinerTileEntity(BlockEntityType.BARREL, pos, state);
    }

    @Override
    public InteractionResult use(BlockState st, net.minecraft.world.level.Level lvl, BlockPos pos, 
                                  Player pl, InteractionHand hnd, BlockHitResult hit) {
        if (lvl.isClientSide) return InteractionResult.SUCCESS;
        
        BlockEntity te = lvl.getBlockEntity(pos);
        if (!(te instanceof BasicMinerTileEntity miner)) return InteractionResult.FAIL;
        
        // Sneak + right-click: Open GUI
        if (pl.isShiftKeyDown()) {
            pl.openMenu((MenuProvider) miner);
            return InteractionResult.SUCCESS;
        }
        
        // Single right-click: Quick scan
        miner.scanChunk(lvl);
        
        if (miner.getScannedOres().isEmpty()) {
            pl.displayClientMessage(Component.literal("No ores found"), true);
        } else {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < Math.min(4, miner.getScannedOres().size()); i++) {
                if (i > 0) sb.append(", ");
                sb.append(miner.getScannedOres().get(i).getDisplayName().getString());
            }
            pl.displayClientMessage(Component.literal("Top ores: " + sb), true);
        }
        
        return InteractionResult.SUCCESS;
    }
}