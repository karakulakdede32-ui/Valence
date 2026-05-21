package com.valence.valence.block.miner;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public class AdvancedMinerBlock extends BaseEntityBlock {
    public AdvancedMinerBlock(Properties p) { super(p); }

    @Nullable @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AdvancedMinerTileEntity(pos, state);
    }

    @Override
    public InteractionResult use(BlockState st, ServerLevel lvl, BlockPos pos, Player pl, InteractionHand hnd, BlockHitResult hit) {
        if (lvl.isClientSide) return InteractionResult.SUCCESS;
        BlockEntity te = lvl.getBlockEntity(pos);
        if (te instanceof AdvancedMinerTileEntity) {
            pl.openMenu((MenuProvider)te);
            return InteractionResult.sidedSuccess(lvl.isClientSide);
        }
        return InteractionResult.FAIL;
    }
}
