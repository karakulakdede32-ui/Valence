package com.valence.valence.block.assembler;

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
import com.valence.valence.Registration;

public class AssemblerBlock extends BaseEntityBlock {
    public AssemblerBlock(Properties p) { super(p.noOcclusion()); }
    @Override public RenderShape getRenderShape(BlockState p) { return RenderShape.MODEL; }
    @Nullable @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new AssemblerTileEntity(pos, state); }
    @Nullable @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level l, BlockState s, BlockEntityType<T> t) {
        return createTickerHelper(t, Registration.ASSEMBLER_TE.get(), AssemblerTileEntity::tick);
    }
    @Override public InteractionResult use(BlockState st, Level l, BlockPos pos, Player p, InteractionHand h, BlockHitResult hit) {
        if (l.isClientSide) return InteractionResult.SUCCESS;
        if (l.getBlockEntity(pos) instanceof AssemblerTileEntity te && p instanceof ServerPlayer sp) NetworkHooks.openScreen(sp, te, pos);
        return InteractionResult.SUCCESS;
    }
}