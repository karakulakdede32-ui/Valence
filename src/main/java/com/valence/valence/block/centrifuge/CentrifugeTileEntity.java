package com.valence.valence.block.centrifuge;

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
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.Optional;

public class CentrifugeTileEntity extends BlockEntity implements MenuProvider {
    public static final int DF_CAPACITY = 2000;
    public static final int DF_PER_TICK = 10;
    public static final int PROGRESS_MAX = 120;

    private final DFStorage dfStorage = new DFStorage(DF_CAPACITY, DF_PER_TICK * 2, 0) {
        @Override protected void onEnergyChanged() { setChanged(); sync(); }
    };
    private final ItemStackHandler itemHandler = new ItemStackHandler(2) {
        @Override public boolean isItemValid(int slot, @NotNull ItemStack stack) { return slot == 0; }
        @Override protected void onContentsChanged(int slot) { setChanged(); }
    };
    private int progress = 0;
    private final LazyOptional<IEnergyStorage> energyHandler = LazyOptional.of(() -> dfStorage);

    public CentrifugeTileEntity(BlockPos pos, BlockState state) { super(Registration.CENTRIFUGE_TE.get(), pos, state); }
    private void sync() { if (level != null && !level.isClientSide()) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3); }

    @Override public Component getDisplayName() { return Component.translatable("container.valence.centrifuge"); }
    @Nullable @Override public AbstractContainerMenu createMenu(int id, Inventory inv, Player p) { return new CentrifugeMenu(id, inv, this); }

    @Override public void load(CompoundTag t) { super.load(t); dfStorage.deserializeNBT(t.get("df")); itemHandler.deserializeNBT(t.getCompound("items")); progress = t.getInt("progress"); }
    @Override public void saveAdditional(CompoundTag t) { super.saveAdditional(t); t.put("df", dfStorage.serializeNBT()); t.put("items", itemHandler.serializeNBT()); t.putInt("progress", progress); }
    @Override public CompoundTag getUpdateTag() { CompoundTag t = super.getUpdateTag(); t.put("df", dfStorage.serializeNBT()); t.put("items", itemHandler.serializeNBT()); t.putInt("progress", progress); return t; }
    @Override public void handleUpdateTag(CompoundTag t) { super.handleUpdateTag(t); dfStorage.deserializeNBT(t.get("df")); itemHandler.deserializeNBT(t.getCompound("items")); progress = t.getInt("progress"); }
    @Nullable @Override public Packet<ClientGamePacketListener> getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }
    @Override public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) { CompoundTag t = pkt.getTag(); if (t != null) { dfStorage.deserializeNBT(t.get("df")); itemHandler.deserializeNBT(t.getCompound("items")); progress = t.getInt("progress"); } }

    @Override public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ENERGY) return energyHandler.cast();
        return super.getCapability(cap, side);
    }
    @Override public void invalidateCaps() { super.invalidateCaps(); energyHandler.invalidate(); }

    public static void tick(Level level, BlockPos pos, BlockState state, CentrifugeTileEntity te) {
        if (level.isClientSide()) return;

        // Pull DF from neighbors
        if (te.dfStorage.getDF() < te.dfStorage.getMaxDF()) {
            for (Direction d : Direction.values()) {
                int need = te.dfStorage.getMaxDF() - te.dfStorage.getDF();
                if (need <= 0) break;
                BlockEntity nb = level.getBlockEntity(pos.relative(d));
                if (nb == null) continue;
                int req = Math.min(DF_PER_TICK * 2, need);
                nb.getCapability(ForgeCapabilities.ENERGY, d.getOpposite()).ifPresent(h -> {
                    int got = h.extractEnergy(req, false);
                    if (got > 0) te.dfStorage.receiveEnergy(got, false);
                });
            }
        }

        ItemStack input = te.itemHandler.getStackInSlot(0);
        ItemStack output = te.itemHandler.getStackInSlot(1);

        if (input.isEmpty() || te.dfStorage.getDF() < DF_PER_TICK) {
            if (te.progress > 0) { te.progress = 0; te.setChanged(); }
            return;
        }

        boolean canOutput = output.isEmpty() || (output.is(input.getItem()) && output.getCount() < output.getMaxStackSize());
        if (!canOutput) {
            if (te.progress > 0) { te.progress = 0; te.setChanged(); }
            return;
        }

        te.dfStorage.consumeDF(DF_PER_TICK, false);
        te.progress++;
        if (te.progress >= PROGRESS_MAX) {
            input.shrink(1);
            if (output.isEmpty()) te.itemHandler.setStackInSlot(1, new ItemStack(input.getItem(), 1));
            else output.grow(1);
            te.progress = 0;
        }
        te.setChanged();
    }

    public DFStorage getDFStorage() { return dfStorage; }
    public ItemStackHandler getItemHandler() { return itemHandler; }
    public int getProgress() { return progress; }
    public int getMaxProgress() { return PROGRESS_MAX; }
}