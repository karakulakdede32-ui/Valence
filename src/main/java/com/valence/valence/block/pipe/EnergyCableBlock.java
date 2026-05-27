package com.valence.valence.block.pipe;

import com.valence.valence.Registration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import org.jetbrains.annotations.Nullable;

public class EnergyCableBlock extends BaseEntityBlock {
    public static final BooleanProperty NORTH = BooleanProperty.create("north");
    public static final BooleanProperty EAST = BooleanProperty.create("east");
    public static final BooleanProperty SOUTH = BooleanProperty.create("south");
    public static final BooleanProperty WEST = BooleanProperty.create("west");
    public static final BooleanProperty UP = BooleanProperty.create("up");
    public static final BooleanProperty DOWN = BooleanProperty.create("down");

    private static final VoxelShape CORE = box(5, 5, 5, 11, 11, 11);
    private static final VoxelShape NORTH_ARM = box(5, 5, 0, 11, 11, 5);
    private static final VoxelShape EAST_ARM = box(11, 5, 5, 16, 11, 11);
    private static final VoxelShape SOUTH_ARM = box(5, 5, 11, 11, 11, 16);
    private static final VoxelShape WEST_ARM = box(0, 5, 5, 5, 11, 11);
    private static final VoxelShape UP_ARM = box(5, 11, 5, 11, 16, 11);
    private static final VoxelShape DOWN_ARM = box(5, 0, 5, 11, 5, 11);

    public EnergyCableBlock(Properties p) { super(p.noOcclusion()); }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(NORTH, EAST, SOUTH, WEST, UP, DOWN);
    }

    @Nullable @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        Level level = ctx.getLevel();
        BlockPos pos = ctx.getClickedPos();
        return defaultBlockState()
            .setValue(NORTH, connectsTo(level, pos, Direction.NORTH))
            .setValue(EAST, connectsTo(level, pos, Direction.EAST))
            .setValue(SOUTH, connectsTo(level, pos, Direction.SOUTH))
            .setValue(WEST, connectsTo(level, pos, Direction.WEST))
            .setValue(UP, connectsTo(level, pos, Direction.UP))
            .setValue(DOWN, connectsTo(level, pos, Direction.DOWN));
    }

    @Override
    public BlockState updateShape(BlockState state, Direction dir, BlockState neighbor, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        return state.setValue(getProperty(dir), connectsTo((Level)level, pos, dir));
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        if (!level.isClientSide) {
            for (Direction dir : Direction.values()) {
                boolean conn = connectsTo(level, pos, dir);
                if (state.getValue(getProperty(dir)) != conn)
                    level.setBlock(pos, state.setValue(getProperty(dir), conn), 3);
            }
        }
    }

    static BooleanProperty getProperty(Direction dir) {
        return switch (dir) {
            case NORTH -> NORTH; case EAST -> EAST; case SOUTH -> SOUTH;
            case WEST -> WEST; case UP -> UP; case DOWN -> DOWN;
        };
    }

    private boolean connectsTo(Level level, BlockPos pos, Direction dir) {
        BlockPos neighbor = pos.relative(dir);
        BlockState neighborState = level.getBlockState(neighbor);
        if (neighborState.getBlock() instanceof EnergyCableBlock) return true;
        BlockEntity be = level.getBlockEntity(neighbor);
        if (be != null) return be.getCapability(ForgeCapabilities.ENERGY, dir.getOpposite()).isPresent();
        return false;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter getter, BlockPos pos, CollisionContext ctx) {
        VoxelShape shape = CORE;
        if (state.getValue(NORTH)) shape = Shapes.joinUnoptimized(shape, NORTH_ARM, BooleanOp.OR);
        if (state.getValue(EAST)) shape = Shapes.joinUnoptimized(shape, EAST_ARM, BooleanOp.OR);
        if (state.getValue(SOUTH)) shape = Shapes.joinUnoptimized(shape, SOUTH_ARM, BooleanOp.OR);
        if (state.getValue(WEST)) shape = Shapes.joinUnoptimized(shape, WEST_ARM, BooleanOp.OR);
        if (state.getValue(UP)) shape = Shapes.joinUnoptimized(shape, UP_ARM, BooleanOp.OR);
        if (state.getValue(DOWN)) shape = Shapes.joinUnoptimized(shape, DOWN_ARM, BooleanOp.OR);
        return shape;
    }

    @Override public RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }
    @Nullable @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new EnergyCableTileEntity(pos, state); }
    @Nullable @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level lvl, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, Registration.ENERGY_CABLE_TE.get(), EnergyCableTileEntity::tick);
    }
}
