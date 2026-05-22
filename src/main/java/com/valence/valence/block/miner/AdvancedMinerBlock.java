package com.valence.valence.block.miner;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import com.valence.valence.Registration;

public class AdvancedMinerBlock extends BaseEntityBlock {
    public AdvancedMinerBlock(net.minecraft.world.level.block.state.BlockBehaviour.Properties p) {
        super(p);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AdvancedMinerTileEntity(Registration.ADVANCED_MINER_TE.get(), pos, state);
    }

    @Override
    public InteractionResult use(BlockState st, net.minecraft.world.level.Level lvl, BlockPos pos, 
                                  Player pl, InteractionHand hnd, BlockHitResult hit) {
        if (lvl.isClientSide) return InteractionResult.SUCCESS;
        
        BlockEntity te = lvl.getBlockEntity(pos);
        if (!(te instanceof AdvancedMinerTileEntity miner)) return InteractionResult.FAIL;
        
        // Right-click: Open GUI
        pl.openMenu((MenuProvider) miner);
        return InteractionResult.SUCCESS;
    }
}