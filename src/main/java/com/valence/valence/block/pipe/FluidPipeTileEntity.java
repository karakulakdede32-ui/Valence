package com.valence.valence.block.pipe;

import com.valence.valence.Registration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FluidPipeTileEntity extends BlockEntity {
    public static final int FLUID_CAPACITY = 500;
    public static final int FLUID_TRANSFER = 50;

    private final FluidTank tank = new FluidTank(FLUID_CAPACITY) {
        @Override public boolean isFluidValid(FluidStack stack) { return true; }
        @Override protected void onContentsChanged() { setChanged(); sync(); }
    };

    private final LazyOptional<IFluidHandler> fluidHandler = LazyOptional.of(() -> tank);

    public FluidPipeTileEntity(BlockPos pos, BlockState state) {
        super(Registration.FLUID_PIPE_TE.get(), pos, state);
    }

    private void sync() {
        if (level != null && !level.isClientSide()) level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        tank.readFromNBT(tag.getCompound("tank"));
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("tank", tank.writeToNBT(new CompoundTag()));
    }

    @Override public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        tag.put("tank", tank.writeToNBT(new CompoundTag()));
        return tag;
    }

    @Override public void handleUpdateTag(CompoundTag tag) {
        super.handleUpdateTag(tag);
        tank.readFromNBT(tag.getCompound("tank"));
    }

    @Nullable @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        CompoundTag tag = pkt.getTag();
        if (tag != null) tank.readFromNBT(tag.getCompound("tank"));
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.FLUID_HANDLER) return fluidHandler.cast();
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() { super.invalidateCaps(); fluidHandler.invalidate(); }

    public static void tick(Level level, BlockPos pos, BlockState state, FluidPipeTileEntity te) {
        if (level.isClientSide()) return;

        // Pull from connected neighbors into the pipe
        for (Direction dir : Direction.values()) {
            if (!state.getValue(FluidPipeBlock.getProperty(dir))) continue;
            if (te.tank.getFluidAmount() >= te.tank.getCapacity()) break;

            BlockEntity neighbor = level.getBlockEntity(pos.relative(dir));
            if (neighbor == null) continue;

            int space = te.tank.getCapacity() - te.tank.getFluidAmount();
            int request = Math.min(FLUID_TRANSFER, space);

            neighbor.getCapability(ForgeCapabilities.FLUID_HANDLER, dir.getOpposite()).ifPresent(handler -> {
                FluidStack drained = handler.drain(request, IFluidHandler.FluidAction.SIMULATE);
                if (!drained.isEmpty()) {
                    int filled = te.tank.fill(drained, IFluidHandler.FluidAction.SIMULATE);
                    if (filled > 0) {
                        handler.drain(new FluidStack(drained.getFluid(), filled), IFluidHandler.FluidAction.EXECUTE);
                        te.tank.fill(new FluidStack(drained.getFluid(), filled), IFluidHandler.FluidAction.EXECUTE);
                    }
                }
            });
        }

        // Push from pipe to connected neighbors
        if (te.tank.getFluidAmount() <= 0) return;

        for (Direction dir : Direction.values()) {
            if (!state.getValue(FluidPipeBlock.getProperty(dir))) continue;
            if (te.tank.getFluidAmount() <= 0) break;

            BlockEntity neighbor = level.getBlockEntity(pos.relative(dir));
            if (neighbor == null) continue;

            neighbor.getCapability(ForgeCapabilities.FLUID_HANDLER, dir.getOpposite()).ifPresent(handler -> {
                FluidStack toPush = te.tank.drain(FLUID_TRANSFER, IFluidHandler.FluidAction.SIMULATE);
                if (!toPush.isEmpty()) {
                    int filled = handler.fill(toPush, IFluidHandler.FluidAction.SIMULATE);
                    if (filled > 0) {
                        handler.fill(new FluidStack(toPush.getFluid(), filled), IFluidHandler.FluidAction.EXECUTE);
                        te.tank.drain(filled, IFluidHandler.FluidAction.EXECUTE);
                    }
                }
            });
        }
    }

    public FluidTank getTank() { return tank; }
}
