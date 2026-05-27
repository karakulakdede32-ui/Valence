package com.valence.valence.block.megacell;

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

public class MegaCellTileEntity extends BlockEntity implements MenuProvider {
    public static final int CAPACITY = 100_000_000;
    public static final int MAX_TRANSFER = 100_000;

    private final DFStorage dfStorage = new DFStorage(CAPACITY, MAX_TRANSFER, MAX_TRANSFER) {
        @Override protected void onEnergyChanged() { setChanged(); sync(); }
    };

    private final LazyOptional<IEnergyStorage> energyHandler = LazyOptional.of(() -> dfStorage);

    public MegaCellTileEntity(BlockPos pos, BlockState state) {
        super(Registration.MEGA_CELL_TE.get(), pos, state);
    }

    private void sync() {
        if (level != null && !level.isClientSide()) level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
    }

    @Override public Component getDisplayName() { return Component.translatable("container.valence.mega_cell"); }
    @Nullable @Override
    public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
        return new MegaCellMenu(id, inv, this);
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

    public static void tick(Level level, BlockPos pos, BlockState state, MegaCellTileEntity te) {
        if (level.isClientSide()) return;
        if (te.dfStorage.getDF() < te.dfStorage.getMaxDF()) {
            for (Direction dir : Direction.values()) {
                int missing = te.dfStorage.getMaxDF() - te.dfStorage.getDF();
                if (missing <= 0) break;
                BlockEntity neighbor = level.getBlockEntity(pos.relative(dir));
                if (neighbor == null) continue;
                int request = Math.min(MAX_TRANSFER, missing);
                neighbor.getCapability(ForgeCapabilities.ENERGY, dir.getOpposite()).ifPresent(handler -> {
                    int pulled = handler.extractEnergy(request, false);
                    if (pulled > 0) te.dfStorage.receiveEnergy(pulled, false);
                });
            }
        }
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
