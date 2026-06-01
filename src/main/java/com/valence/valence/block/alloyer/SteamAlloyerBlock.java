package com.valence.valence.block.alloyer;

import com.valence.valence.Registration;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

public class SteamAlloyerBlock extends BaseEntityBlock {
    public SteamAlloyerBlock(Properties p) { super(p.noOcclusion()); }
    @Override public RenderShape getRenderShape(BlockState pState) { return RenderShape.MODEL; }
    @Nullable @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new SteamAlloyerTileEntity(pos, state); }
    @Nullable @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, Registration.STEAM_ALLOYER_TE.get(), SteamAlloyerTileEntity::tick);
    }
    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) return InteractionResult.SUCCESS;
        if (level.getBlockEntity(pos) instanceof SteamAlloyerTileEntity alloyer && player instanceof ServerPlayer sp) {
            NetworkHooks.openScreen(sp, alloyer, pos);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) { return true; }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof SteamAlloyerTileEntity te) {
            return te.getComparatorOutput();
        }
        return 0;
    }

}
