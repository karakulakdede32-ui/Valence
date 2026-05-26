package com.valence.valence.block.dfcell;

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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DFCellTileEntity extends BlockEntity implements MenuProvider {
    public static final int DF_CAPACITY = 1000;
    public static final int DF_TRANSFER = 20;

    private final DFStorage dfStorage = new DFStorage(DF_CAPACITY, DF_TRANSFER, DF_TRANSFER) {
        @Override protected void onEnergyChanged() { setChanged(); sync(); }
    };

    private final LazyOptional<IEnergyStorage> energyHandler = LazyOptional.of(() -> dfStorage);

    public DFCellTileEntity(BlockPos pos, BlockState state) { super(com.valence.valence.Registration.DF_CELL_TE.get(), pos, state); }

    private void sync() { if (level != null && !level.isClientSide()) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3); }

    @Override public Component getDisplayName() { return Component.translatable("container.valence.df_cell"); }
    @Nullable @Override public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) { return new DFCellMenu(id, inv, this); }

    @Override public void load(CompoundTag tag) { super.load(tag); dfStorage.deserializeNBT(tag.get("df_storage")); }
    @Override public void saveAdditional(CompoundTag tag) { super.saveAdditional(tag); tag.put("df_storage", dfStorage.serializeNBT()); }
    @Override public CompoundTag getUpdateTag() { CompoundTag t = super.getUpdateTag(); t.put("df_storage", dfStorage.serializeNBT()); return t; }
    @Override public void handleUpdateTag(CompoundTag tag) { super.handleUpdateTag(tag); dfStorage.deserializeNBT(tag.get("df_storage")); }
    @Nullable @Override public Packet<ClientGamePacketListener> getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }
    @Override public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        CompoundTag tag = pkt.getTag(); if (tag != null) dfStorage.deserializeNBT(tag.get("df_storage"));
    }

    @Override public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ENERGY) return energyHandler.cast();
        return super.getCapability(cap, side);
    }

    @Override public void invalidateCaps() { super.invalidateCaps(); energyHandler.invalidate(); }

    public static void tick(Level level, BlockPos pos, BlockState state, DFCellTileEntity te) {
        if (level.isClientSide()) return;
        // Push/pull DF to neighbors
        if (te.dfStorage.getDF() > 0) {
            for (Direction dir : Direction.values()) {
                BlockEntity neighbor = level.getBlockEntity(pos.relative(dir));
                if (neighbor == null) continue;
                neighbor.getCapability(ForgeCapabilities.ENERGY, dir.getOpposite()).ifPresent(handler -> {
                    if (handler.canReceive()) {
                        int pushed = handler.receiveEnergy(Math.min(DF_TRANSFER, te.dfStorage.getDF()), false);
                        te.dfStorage.consumeDF(pushed, false);
                    }
                });
            }
        }
    }

    public DFStorage getDFStorage() { return dfStorage; }
}
