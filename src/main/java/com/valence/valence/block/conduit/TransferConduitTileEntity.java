package com.valence.valence.block.conduit;

import com.valence.valence.Registration;
import com.valence.valence.energy.DFStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongTag;
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TransferConduitTileEntity extends BlockEntity implements MenuProvider {
    public static final int FLUID_CAPACITY = 1000;
    public static final int FLUID_TRANSFER = 50;
    public static final int DF_CAPACITY = 100;
    public static final int DF_TRANSFER = 10;
    public static final int MAX_LINKS = 12;

    private final FluidTank fluidTank = new FluidTank(FLUID_CAPACITY) {
        @Override public boolean isFluidValid(FluidStack stack) { return true; }
        @Override protected void onContentsChanged() { setChanged(); sync(); }
    };

    private final DFStorage dfStorage = new DFStorage(DF_CAPACITY, DF_TRANSFER, DF_TRANSFER) {
        @Override protected void onEnergyChanged() { setChanged(); sync(); }
    };

    private final LazyOptional<IFluidHandler> fluidHandler = LazyOptional.of(() -> fluidTank);
    private final LazyOptional<IEnergyStorage> energyHandler = LazyOptional.of(() -> dfStorage);

    // Multiple linked positions for directed transfer
    private final List<BlockPos> linkedSources = new ArrayList<>();
    private final List<BlockPos> linkedDests = new ArrayList<>();

    public TransferConduitTileEntity(BlockPos pos, BlockState state) {
        super(Registration.TRANSFER_CONDUIT_TE.get(), pos, state);
    }

    // ========== Multi-link management ==========

    public boolean addSource(BlockPos source) {
        if (linkedSources.size() >= MAX_LINKS) return false;
        if (linkedSources.contains(source)) return false;
        linkedSources.add(source);
        setChanged(); sync();
        return true;
    }

    public boolean addDest(BlockPos dest) {
        if (linkedDests.size() >= MAX_LINKS) return false;
        if (linkedDests.contains(dest)) return false;
        linkedDests.add(dest);
        setChanged(); sync();
        return true;
    }

    public boolean removeSource(BlockPos source) {
        boolean ok = linkedSources.remove(source);
        if (ok) { setChanged(); sync(); }
        return ok;
    }

    public boolean removeDest(BlockPos dest) {
        boolean ok = linkedDests.remove(dest);
        if (ok) { setChanged(); sync(); }
        return ok;
    }

    public void clearAllLinks() {
        linkedSources.clear();
        linkedDests.clear();
        setChanged(); sync();
    }

    public List<BlockPos> getLinkedSources() { return linkedSources; }
    public List<BlockPos> getLinkedDests() { return linkedDests; }

    private void sync() {
        if (level != null && !level.isClientSide()) level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
    }

    @Override public Component getDisplayName() { return Component.translatable("container.valence.transfer_conduit"); }

    @Nullable @Override
    public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
        return new TransferConduitMenu(id, inv, this);
    }

    // ========== NBT ==========

    private void saveLinks(CompoundTag tag) {
        ListTag srcTag = new ListTag();
        for (BlockPos p : linkedSources) srcTag.add(LongTag.valueOf(p.asLong()));
        tag.put("sources", srcTag);
        ListTag dstTag = new ListTag();
        for (BlockPos p : linkedDests) dstTag.add(LongTag.valueOf(p.asLong()));
        tag.put("dests", dstTag);
    }

    private void loadLinks(CompoundTag tag) {
        linkedSources.clear();
        linkedDests.clear();
        if (tag.contains("sources")) {
            for (var t : tag.getList("sources", 4)) // TAG_LONG
                linkedSources.add(BlockPos.of(((LongTag) t).getAsLong()));
        }
        if (tag.contains("dests")) {
            for (var t : tag.getList("dests", 4))
                linkedDests.add(BlockPos.of(((LongTag) t).getAsLong()));
        }
    }

    @Override public void load(CompoundTag tag) {
        super.load(tag);
        fluidTank.readFromNBT(tag.getCompound("fluid_tank"));
        dfStorage.deserializeNBT(tag.get("df_storage"));
        loadLinks(tag);
    }

    @Override public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("fluid_tank", fluidTank.writeToNBT(new CompoundTag()));
        tag.put("df_storage", dfStorage.serializeNBT());
        saveLinks(tag);
    }

    @Override public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        tag.put("fluid_tank", fluidTank.writeToNBT(new CompoundTag()));
        tag.put("df_storage", dfStorage.serializeNBT());
        saveLinks(tag);
        return tag;
    }

    @Override public void handleUpdateTag(CompoundTag tag) {
        super.handleUpdateTag(tag);
        fluidTank.readFromNBT(tag.getCompound("fluid_tank"));
        dfStorage.deserializeNBT(tag.get("df_storage"));
        loadLinks(tag);
    }

    @Nullable @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        CompoundTag tag = pkt.getTag();
        if (tag != null) {
            fluidTank.readFromNBT(tag.getCompound("fluid_tank"));
            dfStorage.deserializeNBT(tag.get("df_storage"));
            loadLinks(tag);
        }
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.FLUID_HANDLER) return fluidHandler.cast();
        if (cap == ForgeCapabilities.ENERGY) return energyHandler.cast();
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() { super.invalidateCaps(); fluidHandler.invalidate(); energyHandler.invalidate(); }

    // ========== Transfer logic ==========

    private void pullFrom(BlockPos sourcePos) {
        if (level == null) return;
        BlockEntity be = level.getBlockEntity(sourcePos);
        if (be == null) { linkedSources.remove(sourcePos); return; }

        // Pull fluids
        if (fluidTank.getFluidAmount() < fluidTank.getCapacity()) {
            int space = fluidTank.getCapacity() - fluidTank.getFluidAmount();
            int request = Math.min(FLUID_TRANSFER, space);
            be.getCapability(ForgeCapabilities.FLUID_HANDLER).ifPresent(handler -> {
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

        // Pull DF
        if (dfStorage.getDF() < dfStorage.getMaxDF()) {
            int missing = dfStorage.getMaxDF() - dfStorage.getDF();
            be.getCapability(ForgeCapabilities.ENERGY).ifPresent(handler -> {
                int extracted = handler.extractEnergy(Math.min(DF_TRANSFER, missing), false);
                if (extracted > 0) dfStorage.receiveEnergy(extracted, false);
            });
        }
    }

    private void pushTo(BlockPos destPos) {
        if (level == null) return;
        BlockEntity be = level.getBlockEntity(destPos);
        if (be == null) { linkedDests.remove(destPos); return; }

        // Push fluids
        if (fluidTank.getFluidAmount() > 0) {
            be.getCapability(ForgeCapabilities.FLUID_HANDLER).ifPresent(handler -> {
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

        // Push DF
        if (dfStorage.getDF() > 0) {
            be.getCapability(ForgeCapabilities.ENERGY).ifPresent(handler -> {
                int pushed = handler.receiveEnergy(dfStorage.getDF(), false);
                if (pushed > 0) dfStorage.consumeDF(pushed, false);
            });
        }
    }

    public static void tick(Level level, BlockPos pos, BlockState state, TransferConduitTileEntity te) {
        if (level.isClientSide()) return;

        // Linked mode: pull from all sources, push to all dests
        if (!te.linkedSources.isEmpty() || !te.linkedDests.isEmpty()) {
            // Remove duplicates/invalid
            te.linkedSources.removeIf(p -> {
                if (level.getBlockEntity(p) == null) return true;
                return false;
            });
            te.linkedDests.removeIf(p -> {
                if (level.getBlockEntity(p) == null) return true;
                return false;
            });

            for (BlockPos src : te.linkedSources) te.pullFrom(src);
            for (BlockPos dst : te.linkedDests) te.pushTo(dst);
            return;
        }

        // Default bus mode: pull from/push to all neighbors
        for (Direction dir : Direction.values()) {
            BlockEntity neighbor = level.getBlockEntity(pos.relative(dir));
            if (neighbor == null) continue;
            Direction opposite = dir.getOpposite();

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

            if (te.dfStorage.getDF() < te.dfStorage.getMaxDF()) {
                neighbor.getCapability(ForgeCapabilities.ENERGY, opposite).ifPresent(handler -> {
                    int extracted = handler.extractEnergy(Math.min(DF_TRANSFER, te.dfStorage.getMaxDF() - te.dfStorage.getDF()), false);
                    if (extracted > 0) te.dfStorage.receiveEnergy(extracted, false);
                });
            }

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
