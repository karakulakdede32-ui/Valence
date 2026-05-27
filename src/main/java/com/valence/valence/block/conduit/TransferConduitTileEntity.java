package com.valence.valence.block.conduit;

import com.valence.valence.Registration;
import com.valence.valence.energy.DFStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TransferConduitTileEntity extends BlockEntity implements MenuProvider {
    public static final int FLUID_CAPACITY = 1000;
    public static final int FLUID_TRANSFER = 50;
    public static final int DF_CAPACITY = 100;
    public static final int DF_TRANSFER = 10;

    private final FluidTank fluidTank = new FluidTank(FLUID_CAPACITY) {
        @Override public boolean isFluidValid(FluidStack stack) { return true; }
        @Override protected void onContentsChanged() { setChanged(); sync(); }
    };

    private final DFStorage dfStorage = new DFStorage(DF_CAPACITY, DF_TRANSFER, DF_TRANSFER) {
        @Override protected void onEnergyChanged() { setChanged(); sync(); }
    };

    private final LazyOptional<IFluidHandler> fluidHandler = LazyOptional.of(() -> fluidTank);
    private final LazyOptional<IEnergyStorage> energyHandler = LazyOptional.of(() -> dfStorage);

    // Linked positions for directed transfer
    private BlockPos linkedSource = null;
    private BlockPos linkedDest = null;

    public TransferConduitTileEntity(BlockPos pos, BlockState state) {
        super(Registration.TRANSFER_CONDUIT_TE.get(), pos, state);
    }

    public void setLinkedSource(BlockPos source) { this.linkedSource = source; setChanged(); sync(); }
    public void setLinkedDest(BlockPos dest) { this.linkedDest = dest; setChanged(); sync(); }
    public BlockPos getLinkedSource() { return linkedSource; }
    public BlockPos getLinkedDest() { return linkedDest; }

    private void sync() {
        if (level != null && !level.isClientSide()) level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
    }

    @Override public Component getDisplayName() { return Component.translatable("container.valence.transfer_conduit"); }

    @Nullable @Override
    public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
        return new TransferConduitMenu(id, inv, this);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        fluidTank.readFromNBT(tag.getCompound("fluid_tank"));
        dfStorage.deserializeNBT(tag.get("df_storage"));
        if (tag.contains("linked_source")) linkedSource = BlockPos.of(tag.getLong("linked_source"));
        else linkedSource = null;
        if (tag.contains("linked_dest")) linkedDest = BlockPos.of(tag.getLong("linked_dest"));
        else linkedDest = null;
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("fluid_tank", fluidTank.writeToNBT(new CompoundTag()));
        tag.put("df_storage", dfStorage.serializeNBT());
        if (linkedSource != null) tag.putLong("linked_source", linkedSource.asLong());
        if (linkedDest != null) tag.putLong("linked_dest", linkedDest.asLong());
    }

    @Override public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        tag.put("fluid_tank", fluidTank.writeToNBT(new CompoundTag()));
        tag.put("df_storage", dfStorage.serializeNBT());
        if (linkedSource != null) tag.putLong("linked_source", linkedSource.asLong());
        if (linkedDest != null) tag.putLong("linked_dest", linkedDest.asLong());
        return tag;
    }

    @Override public void handleUpdateTag(CompoundTag tag) {
        super.handleUpdateTag(tag);
        fluidTank.readFromNBT(tag.getCompound("fluid_tank"));
        dfStorage.deserializeNBT(tag.get("df_storage"));
        if (tag.contains("linked_source")) linkedSource = BlockPos.of(tag.getLong("linked_source"));
        else linkedSource = null;
        if (tag.contains("linked_dest")) linkedDest = BlockPos.of(tag.getLong("linked_dest"));
        else linkedDest = null;
    }

    @Nullable @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        CompoundTag tag = pkt.getTag();
        if (tag != null) {
            fluidTank.readFromNBT(tag.getCompound("fluid_tank"));
            dfStorage.deserializeNBT(tag.get("df_storage"));
            if (tag.contains("linked_source")) linkedSource = BlockPos.of(tag.getLong("linked_source"));
            else linkedSource = null;
            if (tag.contains("linked_dest")) linkedDest = BlockPos.of(tag.getLong("linked_dest"));
            else linkedDest = null;
        }
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.FLUID_HANDLER) return fluidHandler.cast();
        if (cap == ForgeCapabilities.ENERGY) return energyHandler.cast();
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        fluidHandler.invalidate();
        energyHandler.invalidate();
    }

    private void transferFromSource() {
        if (linkedSource == null || level == null) return;
        BlockEntity sourceBE = level.getBlockEntity(linkedSource);
        if (sourceBE == null) { linkedSource = null; return; }

        if (fluidTank.getFluidAmount() < fluidTank.getCapacity()) {
            int space = fluidTank.getCapacity() - fluidTank.getFluidAmount();
            int request = Math.min(FLUID_TRANSFER, space);
            sourceBE.getCapability(ForgeCapabilities.FLUID_HANDLER).ifPresent(handler -> {
                FluidStack drained = handler.drain(request, IFluidHandler.FluidAction.SIMULATE);
                if (!drained.isEmpty()) {
                    int filled = fluidTank.fill(drained, IFluidHandler.FluidAction.SIMULATE);
                    if (filled > 0) {
                        handler.drain(new FluidStack(drained.getFluid(), filled), IFluidHandler.FluidAction.EXECUTE);
                        fluidTank.fill(new FluidStack(drained.getFluid(), filled), IFluidHandler.FluidAction.EXECUTE);
                    }
                }
            });
        }

        if (dfStorage.getDF() < dfStorage.getMaxDF()) {
            int missing = dfStorage.getMaxDF() - dfStorage.getDF();
            int request = Math.min(DF_TRANSFER, missing);
            sourceBE.getCapability(ForgeCapabilities.ENERGY).ifPresent(handler -> {
                int extracted = handler.extractEnergy(request, false);
                if (extracted > 0) dfStorage.receiveEnergy(extracted, false);
            });
        }
    }

    private void transferToDest() {
        if (linkedDest == null || level == null) return;
        BlockEntity destBE = level.getBlockEntity(linkedDest);
        if (destBE == null) { linkedDest = null; return; }

        if (fluidTank.getFluidAmount() > 0) {
            destBE.getCapability(ForgeCapabilities.FLUID_HANDLER).ifPresent(handler -> {
                FluidStack toPush = fluidTank.drain(FLUID_TRANSFER, IFluidHandler.FluidAction.SIMULATE);
                if (!toPush.isEmpty()) {
                    int filled = handler.fill(toPush, IFluidHandler.FluidAction.SIMULATE);
                    if (filled > 0) {
                        handler.fill(new FluidStack(toPush.getFluid(), filled), IFluidHandler.FluidAction.EXECUTE);
                        fluidTank.drain(filled, IFluidHandler.FluidAction.EXECUTE);
                    }
                }
            });
        }

        if (dfStorage.getDF() > 0) {
            destBE.getCapability(ForgeCapabilities.ENERGY).ifPresent(handler -> {
                int pushed = handler.receiveEnergy(dfStorage.getDF(), false);
                if (pushed > 0) dfStorage.consumeDF(pushed, false);
            });
        }
    }

    public static void tick(Level level, BlockPos pos, BlockState state, TransferConduitTileEntity te) {
        if (level.isClientSide()) return;

        // If conduit has linked source/dest, prioritize directed transfer
        if (te.linkedSource != null || te.linkedDest != null) {
            if (te.linkedSource != null) te.transferFromSource();
            if (te.linkedDest != null) te.transferToDest();
            return; // Don't do bus mode when linked
        }

        // Default bus mode: pull from/push to all neighbors
        for (Direction dir : Direction.values()) {
            BlockEntity neighbor = level.getBlockEntity(pos.relative(dir));
            if (neighbor == null) continue;

            Direction opposite = dir.getOpposite();

            // Pull fluids
            if (te.fluidTank.getFluidAmount() < te.fluidTank.getCapacity()) {
                int space = te.fluidTank.getCapacity() - te.fluidTank.getFluidAmount();
                int request = Math.min(FLUID_TRANSFER, space);
                neighbor.getCapability(ForgeCapabilities.FLUID_HANDLER, opposite).ifPresent(handler -> {
                    FluidStack drained = handler.drain(request, IFluidHandler.FluidAction.SIMULATE);
                    if (!drained.isEmpty()) {
                        int filled = te.fluidTank.fill(drained, IFluidHandler.FluidAction.SIMULATE);
                        if (filled > 0) {
                            handler.drain(new FluidStack(drained.getFluid(), filled), IFluidHandler.FluidAction.EXECUTE);
                            te.fluidTank.fill(new FluidStack(drained.getFluid(), filled), IFluidHandler.FluidAction.EXECUTE);
                        }
                    }
                });
            }

            // Pull DF
            if (te.dfStorage.getDF() < te.dfStorage.getMaxDF()) {
                int missing = te.dfStorage.getMaxDF() - te.dfStorage.getDF();
                int request = Math.min(DF_TRANSFER, missing);
                neighbor.getCapability(ForgeCapabilities.ENERGY, opposite).ifPresent(handler -> {
                    int extracted = handler.extractEnergy(request, false);
                    if (extracted > 0) te.dfStorage.receiveEnergy(extracted, false);
                });
            }

            // Push fluids
            if (te.fluidTank.getFluidAmount() > 0) {
                neighbor.getCapability(ForgeCapabilities.FLUID_HANDLER, opposite).ifPresent(handler -> {
                    FluidStack toPush = te.fluidTank.drain(FLUID_TRANSFER, IFluidHandler.FluidAction.SIMULATE);
                    if (!toPush.isEmpty()) {
                        int filled = handler.fill(toPush, IFluidHandler.FluidAction.SIMULATE);
                        if (filled > 0) {
                            handler.fill(new FluidStack(toPush.getFluid(), filled), IFluidHandler.FluidAction.EXECUTE);
                            te.fluidTank.drain(filled, IFluidHandler.FluidAction.EXECUTE);
                        }
                    }
                });
            }

            // Push DF
            if (te.dfStorage.getDF() > 0) {
                neighbor.getCapability(ForgeCapabilities.ENERGY, opposite).ifPresent(handler -> {
                    int pushed = handler.receiveEnergy(te.dfStorage.getDF(), false);
                    if (pushed > 0) te.dfStorage.consumeDF(pushed, false);
                });
            }
        }
    }

    public FluidTank getFluidTank() { return fluidTank; }
    public DFStorage getDFStorage() { return dfStorage; }
}
