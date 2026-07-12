package com.valence.valence.block.endenergy;

import com.valence.valence.Registration;
import com.valence.valence.config.ValenceConfig;
import com.valence.valence.energy.DFStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
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

public class EndEnergyCoreTileEntity extends BlockEntity implements MenuProvider {
    private final DFStorage dfStorage = new DFStorage(getCapacity(), getTransfer(), getTransfer()) {
        @Override
        protected void onEnergyChanged() {
            setChanged();
            sync();
        }
    };

    private final LazyOptional<IEnergyStorage> energyHandler = LazyOptional.of(() -> dfStorage);

    public EndEnergyCoreTileEntity(BlockPos pos, BlockState state) {
        super(Registration.END_ENERGY_CORE_TE.get(), pos, state);
    }

    private static int getCapacity() {
        return Math.max(1, ValenceConfig.END_ENERGY_CORE_CAPACITY.get());
    }

    private static int getGenerationPerTick() {
        return DFStorage.clampAmount(ValenceConfig.END_ENERGY_CORE_GENERATION.get());
    }

    private static int getTransfer() {
        return DFStorage.clampAmount(ValenceConfig.END_ENERGY_CORE_TRANSFER.get());
    }

    private void sync() {
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.valence.end_energy_core");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new EndEnergyCoreMenu(id, inventory, this);
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

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        tag.put("df_storage", dfStorage.serializeNBT());
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        super.handleUpdateTag(tag);
        dfStorage.deserializeNBT(tag.get("df_storage"));
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

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
    public void invalidateCaps() {
        super.invalidateCaps();
        energyHandler.invalidate();
    }

    public static void tick(Level level, BlockPos pos, BlockState state, EndEnergyCoreTileEntity te) {
        if (level.isClientSide()) {
            if (level.random.nextInt(8) == 0) {
                level.addParticle(Registration.SPARK.get(),
                        pos.getX() + 0.5 + (level.random.nextDouble() - 0.5) * 0.6,
                        pos.getY() + 0.8,
                        pos.getZ() + 0.5 + (level.random.nextDouble() - 0.5) * 0.6,
                        (level.random.nextDouble() - 0.5) * 0.02, 0.03, (level.random.nextDouble() - 0.5) * 0.02);
            }
            return;
        }

        int missing = te.dfStorage.getMaxDF() - te.dfStorage.getDF();
        if (missing > 0) {
            te.dfStorage.generateDF(Math.min(getGenerationPerTick(), missing), false);
        }

        if (te.dfStorage.getDF() <= 0) return;

        int transferRate = getTransfer();
        for (Direction dir : Direction.values()) {
            if (te.dfStorage.getDF() <= 0) break;
            BlockEntity neighbor = level.getBlockEntity(pos.relative(dir));
            if (neighbor == null) continue;
            int toSend = Math.min(te.dfStorage.getDF(), transferRate);
            neighbor.getCapability(ForgeCapabilities.ENERGY, dir.getOpposite()).ifPresent(handler -> {
                int pushed = handler.receiveEnergy(toSend, false);
                if (pushed > 0) te.dfStorage.consumeDF(pushed, false);
            });
        }
    }

    public DFStorage getDFStorage() {
        return dfStorage;
    }

    public int getGenerationPerTickValue() {
        return getGenerationPerTick();
    }

    public int getTransferValue() {
        return getTransfer();
    }
}
