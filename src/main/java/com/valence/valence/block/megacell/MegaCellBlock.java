package com.valence.valence.block.megacell;

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

public class MegaCellBlock extends BaseEntityBlock {
    public MegaCellBlock(Properties p) { super(p.noOcclusion()); }
    @Override public RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }
    @Nullable @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new MegaCellTileEntity(pos, state); }
    @Nullable @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level lvl, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, Registration.MEGA_CELL_TE.get(), MegaCellTileEntity::tick);
    }
    @Override
    public InteractionResult use(BlockState state, Level lvl, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (lvl.isClientSide) return InteractionResult.SUCCESS;
        if (lvl.getBlockEntity(pos) instanceof MegaCellTileEntity te && player instanceof ServerPlayer sp)
            NetworkHooks.openScreen(sp, te, pos);
        return InteractionResult.SUCCESS;
    }
}
