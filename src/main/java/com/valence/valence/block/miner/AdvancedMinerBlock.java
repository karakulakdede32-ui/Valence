package com.valence.valence.block.miner;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkHooks;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import com.valence.valence.Registration;

public class AdvancedMinerBlock extends BaseEntityBlock {
    public AdvancedMinerBlock(net.minecraft.world.level.block.state.BlockBehaviour.Properties p) {
        super(p.noOcclusion());
    }

    @Override
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AdvancedMinerTileEntity(Registration.ADVANCED_MINER_TE.get(), pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, Registration.ADVANCED_MINER_TE.get(), AdvancedMinerTileEntity::tick);
    }

    @Override
    public InteractionResult use(BlockState st, net.minecraft.world.level.Level lvl, BlockPos pos, 
                                  Player pl, InteractionHand hnd, BlockHitResult hit) {
        if (lvl.isClientSide) return InteractionResult.SUCCESS;
        
        BlockEntity te = lvl.getBlockEntity(pos);
        if (!(te instanceof AdvancedMinerTileEntity miner)) return InteractionResult.FAIL;
        
        // Right-click: Open GUI with position data
        if (pl instanceof ServerPlayer serverPlayer) {
            NetworkHooks.openScreen(serverPlayer, (MenuProvider) miner, pos);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) { return true; }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof AdvancedMinerTileEntity te) {
            return te.getComparatorOutput();
        }
        return 0;
    }

}