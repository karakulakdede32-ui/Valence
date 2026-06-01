package com.valence.valence.block.blastfurnace;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;
import com.valence.valence.Registration;

public class BlastFurnaceBlock extends BaseEntityBlock {
    private static final java.util.Set<net.minecraft.world.level.block.Block> HEAT_BRICKS = java.util.Set.of(
        Blocks.BRICKS, Blocks.NETHER_BRICKS, Blocks.STONE_BRICKS, Blocks.DEEPSLATE_BRICKS,
        Blocks.POLISHED_BLACKSTONE_BRICKS, Blocks.END_STONE_BRICKS
    );

    public BlastFurnaceBlock(Properties p) { super(p.noOcclusion()); }
    @Override public RenderShape getRenderShape(BlockState p) { return RenderShape.MODEL; }
    @Nullable @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new BlastFurnaceTileEntity(pos, state); }
    @Nullable @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level l, BlockState s, BlockEntityType<T> t) {
        return createTickerHelper(t, Registration.BLAST_FURNACE_TE.get(), BlastFurnaceTileEntity::tick);
    }
    @Override public InteractionResult use(BlockState st, Level l, BlockPos pos, Player p, InteractionHand h, BlockHitResult hit) {
        if (l.isClientSide) return InteractionResult.SUCCESS;
        if (l.getBlockEntity(pos) instanceof BlastFurnaceTileEntity te && p instanceof ServerPlayer sp)
            NetworkHooks.openScreen(sp, te, pos);
        return InteractionResult.SUCCESS;
    }

    // Real multiblock: lava below, air above, brick ring around
    public static boolean isFormed(Level level, BlockPos pos) {
        BlockPos below = pos.below();
        if (!level.getBlockState(below).is(Blocks.LAVA) && !level.getBlockState(below).is(Blocks.LAVA_CAULDRON))
            return false;
        if (!level.getBlockState(pos.above()).isAir())
            return false;
        // Check 4 sides for heat-resistant brick blocks
        for (Direction dir : java.util.List.of(Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST)) {
            BlockState side = level.getBlockState(pos.relative(dir));
            if (!HEAT_BRICKS.contains(side.getBlock())) return false;
        }
        return true;
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) { return true; }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof BlastFurnaceTileEntity te) {
            return te.getComparatorOutput();
        }
        return 0;
    }
}
