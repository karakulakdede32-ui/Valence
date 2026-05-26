package com.valence.valence.block.turbine;

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

public class SteamTurbineTileEntity extends BlockEntity implements MenuProvider {
    public static final int STEAM_CAPACITY = 50;
    public static final int STEAM_PER_TICK = 5;
    public static final int DF_PER_TICK = 5;
    public static final int DF_CAPACITY = 100;

    private final FluidTank steamTank = new FluidTank(STEAM_CAPACITY) {
        @Override public boolean isFluidValid(FluidStack stack) { return stack.getFluid() == Registration.STEAM.get(); }
        @Override protected void onContentsChanged() { setChanged(); sync(); }
    };

    private final DFStorage dfStorage = new DFStorage(DF_CAPACITY, DF_PER_TICK, 10) {
        @Override protected void onEnergyChanged() { setChanged(); sync(); }
    };

    private final LazyOptional<IFluidHandler> fluidHandler = LazyOptional.of(() -> steamTank);
    private final LazyOptional<IEnergyStorage> energyHandler = LazyOptional.of(() -> dfStorage);

    public SteamTurbineTileEntity(BlockPos pos, BlockState state) {
        super(Registration.STEAM_TURBINE_TE.get(), pos, state);
    }

    private void sync() {
        if (level != null && !level.isClientSide()) level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
    }

    @Override public Component getDisplayName() { return Component.translatable("container.valence.steam_turbine"); }

    @Nullable @Override
    public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
        return new SteamTurbineMenu(id, inv, this);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        steamTank.readFromNBT(tag.getCompound("steam_tank"));
        dfStorage.deserializeNBT(tag.get("df_storage"));
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("steam_tank", steamTank.writeToNBT(new CompoundTag()));
        tag.put("df_storage", dfStorage.serializeNBT());
    }

    @Override public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        tag.put("steam_tank", steamTank.writeToNBT(new CompoundTag()));
        tag.put("df_storage", dfStorage.serializeNBT());
        return tag;
    }

    @Override public void handleUpdateTag(CompoundTag tag) {
        super.handleUpdateTag(tag);
        steamTank.readFromNBT(tag.getCompound("steam_tank"));
        dfStorage.deserializeNBT(tag.get("df_storage"));
    }

    @Nullable @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        CompoundTag tag = pkt.getTag();
        if (tag != null) {
            steamTank.readFromNBT(tag.getCompound("steam_tank"));
            dfStorage.deserializeNBT(tag.get("df_storage"));
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

    public static void tick(Level level, BlockPos pos, BlockState state, SteamTurbineTileEntity te) {
        if (level.isClientSide()) return;

        // Pull steam from neighbors
        if (te.steamTank.getFluidAmount() < te.steamTank.getCapacity()) {
            for (Direction dir : Direction.values()) {
                BlockEntity neighbor = level.getBlockEntity(pos.relative(dir));
                if (neighbor == null) continue;
                int space = te.steamTank.getCapacity() - te.steamTank.getFluidAmount();
                neighbor.getCapability(ForgeCapabilities.FLUID_HANDLER, dir.getOpposite()).ifPresent(handler -> {
                    FluidStack sim = handler.drain(new FluidStack(Registration.STEAM.get(), space), IFluidHandler.FluidAction.SIMULATE);
                    if (!sim.isEmpty() && sim.getFluid() == Registration.STEAM.get()) {
                        int fill = te.steamTank.fill(sim, IFluidHandler.FluidAction.SIMULATE);
                        if (fill > 0) {
                            handler.drain(new FluidStack(Registration.STEAM.get(), fill), IFluidHandler.FluidAction.EXECUTE);
                            te.steamTank.fill(new FluidStack(Registration.STEAM.get(), fill), IFluidHandler.FluidAction.EXECUTE);
                        }
                    }
                });
                if (te.steamTank.getFluidAmount() >= te.steamTank.getCapacity()) break;
            }
        }

        // Generate DF from steam
        if (te.steamTank.getFluidAmount() >= STEAM_PER_TICK && te.dfStorage.getDF() < te.dfStorage.getMaxDF()) {
            te.steamTank.drain(STEAM_PER_TICK, IFluidHandler.FluidAction.EXECUTE);
            te.dfStorage.generateDF(DF_PER_TICK, false);
        }

        // Push DF to neighbors
        if (te.dfStorage.getDF() > 0) {
            for (Direction dir : Direction.values()) {
                BlockEntity neighbor = level.getBlockEntity(pos.relative(dir));
                if (neighbor == null) continue;
                neighbor.getCapability(ForgeCapabilities.ENERGY, dir.getOpposite()).ifPresent(handler -> {
                    int pushed = handler.receiveEnergy(te.dfStorage.getDF(), false);
                    te.dfStorage.consumeDF(pushed, false);
                });
            }
        }
    }

    public FluidTank getSteamTank() { return steamTank; }
    public DFStorage getDFStorage() { return dfStorage; }
}
