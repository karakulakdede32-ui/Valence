package com.valence.valence.block.miner;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class BasicMinerBlock extends BaseEntityBlock {
    public BasicMinerBlock(Properties p) {
        super(p);
    }

    @Nullable @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BasicMinerTileEntity(pos, state);
    }

    @Override
    public InteractionResult use(BlockState st, ServerLevel lvl, BlockPos pos, 
                                   Player pl, InteractionHand hnd, BlockHitResult hit) {
        if (lvl.isClientSide) return InteractionResult.SUCCESS;
        
        BlockEntity te = lvl.getBlockEntity(pos);
        if (!(te instanceof BasicMinerTileEntity miner)) return InteractionResult.FAIL;
        
        // Sneak + double right-click: Open GUI
        if (pl.isShiftDown()) {
            pl.openMenu((MenuProvider) miner);
            return InteractionResult.SUCCESS;
        }
        
        // Single right-click: Quick scan (no GUI)
        miner.scanChunk(lvl);
        
        // Show scan results in chat
        pl.displayClientMessage(Component.literal("§aScanned: §r" + 
            miner.getScannedOres().stream()
                .map(s -> s.getCount() + "x " + s.getItem().getDescriptionId())
                .reduce((a, b) -> a + ", " + b)
                .orElse("No ores found")), true);
        
        return InteractionResult.SUCCESS;
    }

    @Override
    public void onRemove(BlockState st, net.minecraft.world.level.Level lvl, 
                        BlockPos pos, BlockState newSt, boolean moving) {
        if (st.hasBlockEntity() && !st.is(newSt.getBlock())) {
            lvl.removeBlockEntity(pos);
        }
    }
}
