package com.valence.valence.block.pipe;

import com.valence.valence.Registration;
import com.valence.valence.energy.DFStorage;
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
import net.minecraftforge.energy.IEnergyStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EnergyCableTileEntity extends BlockEntity {
    public static final int DF_CAPACITY = 500;
    public static final int DF_TRANSFER = 20;

    private final DFStorage dfStorage = new DFStorage(DF_CAPACITY, DF_TRANSFER, DF_TRANSFER) {
        @Override protected void onEnergyChanged() { setChanged(); sync(); }
    };

    private final LazyOptional<IEnergyStorage> energyHandler = LazyOptional.of(() -> dfStorage);

    public EnergyCableTileEntity(BlockPos pos, BlockState state) {
        super(Registration.ENERGY_CABLE_TE.get(), pos, state);
    }

    private void sync() {
        if (level != null && !level.isClientSide()) level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        dfStorage.deserializeNBT(tag.get("df_storage"));
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("df_storage", dfStorage.serializeNBT());
    }

    @Override public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        tag.put("df_storage", dfStorage.serializeNBT());
        return tag;
    }

    @Override public void handleUpdateTag(CompoundTag tag) {
        super.handleUpdateTag(tag);
        dfStorage.deserializeNBT(tag.get("df_storage"));
    }

    @Nullable @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        CompoundTag tag = pkt.getTag();
        if (tag != null) dfStorage.deserializeNBT(tag.get("df_storage"));
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ENERGY) return energyHandler.cast();
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() { super.invalidateCaps(); energyHandler.invalidate(); }

    public static void tick(Level level, BlockPos pos, BlockState state, EnergyCableTileEntity te) {
        if (level.isClientSide()) return;

        // Pull DF from connected neighbors into the cable
        for (Direction dir : Direction.values()) {
            if (!state.getValue(EnergyCableBlock.getProperty(dir))) continue;
            if (te.dfStorage.getDF() >= te.dfStorage.getMaxDF()) break;

            BlockEntity neighbor = level.getBlockEntity(pos.relative(dir));
            if (neighbor == null) continue;

            int missing = te.dfStorage.getMaxDF() - te.dfStorage.getDF();
            int request = Math.min(DF_TRANSFER, missing);

            neighbor.getCapability(ForgeCapabilities.ENERGY, dir.getOpposite()).ifPresent(handler -> {
                int extracted = handler.extractEnergy(request, false);
                if (extracted > 0) te.dfStorage.receiveEnergy(extracted, false);
            });
        }

        // Push DF from cable to connected neighbors
        if (te.dfStorage.getDF() <= 0) return;

        for (Direction dir : Direction.values()) {
            if (!state.getValue(EnergyCableBlock.getProperty(dir))) continue;
            if (te.dfStorage.getDF() <= 0) break;

            BlockEntity neighbor = level.getBlockEntity(pos.relative(dir));
            if (neighbor == null) continue;

            neighbor.getCapability(ForgeCapabilities.ENERGY, dir.getOpposite()).ifPresent(handler -> {
                int pushed = handler.receiveEnergy(te.dfStorage.getDF(), false);
                if (pushed > 0) te.dfStorage.consumeDF(pushed, false);
            });
        }
    }

    public DFStorage getDFStorage() { return dfStorage; }
}
