package com.valence.valence.block.wireless;

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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class WirelessNodeTileEntity extends BlockEntity implements MenuProvider {
    public static final int DF_CAPACITY = 100;
    public static final int DF_TRANSFER = 10;
    public static final int MAX_RANGE = 32;
    public static final float EFFICIENCY = 0.95f; // 5% loss over the air

    private final DFStorage dfStorage = new DFStorage(DF_CAPACITY, DF_TRANSFER, DF_TRANSFER) {
        @Override protected void onEnergyChanged() { setChanged(); sync(); }
    };

    private BlockPos pairedNode = null;
    private final LazyOptional<IEnergyStorage> energyHandler = LazyOptional.of(() -> dfStorage);

    public WirelessNodeTileEntity(BlockPos pos, BlockState state) {
        super(Registration.WIRELESS_NODE_TE.get(), pos, state);
    }

    public BlockPos getPairedNode() { return pairedNode; }
    public void setPairedNode(BlockPos pos) { this.pairedNode = pos; setChanged(); sync(); }
    public void clearPairedNode() { this.pairedNode = null; setChanged(); sync(); }

    private void sync() {
        if (level != null && !level.isClientSide()) level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
    }

    @Override public Component getDisplayName() { return Component.translatable("container.valence.wireless_node"); }

    @Nullable @Override
    public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
        return new WirelessNodeMenu(id, inv, this);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        dfStorage.deserializeNBT(tag.get("df_storage"));
        if (tag.contains("paired")) pairedNode = BlockPos.of(tag.getLong("paired"));
        else pairedNode = null;
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("df_storage", dfStorage.serializeNBT());
        if (pairedNode != null) tag.putLong("paired", pairedNode.asLong());
    }

    @Override public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        tag.put("df_storage", dfStorage.serializeNBT());
        if (pairedNode != null) tag.putLong("paired", pairedNode.asLong());
        return tag;
    }

    @Override public void handleUpdateTag(CompoundTag tag) {
        super.handleUpdateTag(tag);
        dfStorage.deserializeNBT(tag.get("df_storage"));
        if (tag.contains("paired")) pairedNode = BlockPos.of(tag.getLong("paired"));
        else pairedNode = null;
    }

    @Nullable @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        CompoundTag tag = pkt.getTag();
        if (tag != null) {
            dfStorage.deserializeNBT(tag.get("df_storage"));
            if (tag.contains("paired")) pairedNode = BlockPos.of(tag.getLong("paired"));
            else pairedNode = null;
        }
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ENERGY) return energyHandler.cast();
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() { super.invalidateCaps(); energyHandler.invalidate(); }

    public static void tick(Level level, BlockPos pos, BlockState state, WirelessNodeTileEntity te) {
        if (level.isClientSide()) return;

        // Pull DF from adjacent machines
        if (te.dfStorage.getDF() < te.dfStorage.getMaxDF()) {
            for (Direction dir : Direction.values()) {
                int missing = te.dfStorage.getMaxDF() - te.dfStorage.getDF();
                if (missing <= 0) break;
                int request = Math.min(DF_TRANSFER, missing);

                BlockEntity neighbor = level.getBlockEntity(pos.relative(dir));
                if (neighbor == null) continue;

                neighbor.getCapability(ForgeCapabilities.ENERGY, dir.getOpposite()).ifPresent(handler -> {
                    int extracted = handler.extractEnergy(request, false);
                    if (extracted > 0) te.dfStorage.receiveEnergy(extracted, false);
                });
            }
        }

        // Send DF to paired node wirelessly
        if (te.dfStorage.getDF() > 0 && te.pairedNode != null) {
            // Check range
            double dist = Math.sqrt(pos.distSqr(te.pairedNode));
            if (dist > MAX_RANGE) {
                // Out of range, clear the pair
                te.clearPairedNode();
                return;
            }

            BlockEntity pairedBE = level.getBlockEntity(te.pairedNode);
            if (pairedBE instanceof WirelessNodeTileEntity pairedNode) {
                int toSend = Math.min(te.dfStorage.getDF(), DF_TRANSFER);
                int received = (int)(toSend * EFFICIENCY);
                if (received > 0) {
                    pairedNode.getCapability(ForgeCapabilities.ENERGY).ifPresent(handler -> {
                        int filled = handler.receiveEnergy(received, false);
                        if (filled > 0) te.dfStorage.consumeDF((int)(filled / EFFICIENCY), false);
                    });
                }
            } else {
                // Target is no longer a wireless node
                te.clearPairedNode();
            }
        }

        // Push DF to adjacent machines
        if (te.dfStorage.getDF() > 0) {
            for (Direction dir : Direction.values()) {
                if (te.dfStorage.getDF() <= 0) break;
                BlockEntity neighbor = level.getBlockEntity(pos.relative(dir));
                if (neighbor == null) continue;

                neighbor.getCapability(ForgeCapabilities.ENERGY, dir.getOpposite()).ifPresent(handler -> {
                    int pushed = handler.receiveEnergy(te.dfStorage.getDF(), false);
                    if (pushed > 0) te.dfStorage.consumeDF(pushed, false);
                });
            }
        }
    }

    public DFStorage getDFStorage() { return dfStorage; }
}
