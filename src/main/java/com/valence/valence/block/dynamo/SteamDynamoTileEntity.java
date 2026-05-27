package com.valence.valence.block.dynamo;

import com.valence.valence.Registration;
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
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SteamDynamoTileEntity extends BlockEntity implements MenuProvider {
    private static final int WATER_TANK_CAPACITY = 2000;
    private static final int STEAM_TANK_CAPACITY = 100;
    private static final int CONVERSION_RATE = 18;

    private final FluidTank waterTank = new FluidTank(WATER_TANK_CAPACITY) {
        @Override public boolean isFluidValid(FluidStack stack) { return stack.getFluid() == Fluids.WATER; }
        @Override protected void onContentsChanged() { setChanged(); sync(); }
    };

    private final FluidTank steamTank = new FluidTank(STEAM_TANK_CAPACITY) {
        @Override public boolean isFluidValid(FluidStack stack) { return stack.getFluid() == Registration.STEAM.get(); }
        @Override protected void onContentsChanged() { setChanged(); sync(); }
    };

    private final LazyOptional<IFluidHandler> waterHandler = LazyOptional.of(() -> waterTank);
    private final LazyOptional<IFluidHandler> steamHandler = LazyOptional.of(() -> steamTank);

    public SteamDynamoTileEntity(BlockPos pos, BlockState state) {
        super(Registration.STEAM_DYNAMO_TE.get(), pos, state);
    }

    private void sync() {
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
    }

    @Override public Component getDisplayName() { return Component.translatable("container.valence.steam_dynamo"); }

    @Nullable @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new SteamDynamoMenu(id, inventory, this);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        waterTank.readFromNBT(tag.getCompound("water_tank"));
        steamTank.readFromNBT(tag.getCompound("steam_tank"));
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("water_tank", waterTank.writeToNBT(new CompoundTag()));
        tag.put("steam_tank", steamTank.writeToNBT(new CompoundTag()));
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        tag.put("water_tank", waterTank.writeToNBT(new CompoundTag()));
        tag.put("steam_tank", steamTank.writeToNBT(new CompoundTag()));
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        super.handleUpdateTag(tag);
        waterTank.readFromNBT(tag.getCompound("water_tank"));
        steamTank.readFromNBT(tag.getCompound("steam_tank"));
    }

    @Nullable @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        CompoundTag tag = pkt.getTag();
        if (tag != null) {
            waterTank.readFromNBT(tag.getCompound("water_tank"));
            steamTank.readFromNBT(tag.getCompound("steam_tank"));
        }
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.FLUID_HANDLER) {
            return LazyOptional.of(() -> new IFluidHandler() {
                @Override public int getTanks() { return 2; }
                @Override public @NotNull FluidStack getFluidInTank(int tank) { return tank == 0 ? waterTank.getFluid() : steamTank.getFluid(); }
                @Override public int getTankCapacity(int tank) { return tank == 0 ? waterTank.getCapacity() : steamTank.getCapacity(); }
                @Override public boolean isFluidValid(int tank, @NotNull FluidStack stack) { return tank == 0 ? waterTank.isFluidValid(stack) : steamTank.isFluidValid(stack); }
                @Override public int fill(FluidStack resource, FluidAction action) { return waterTank.fill(resource, action); }
                @Override public @NotNull FluidStack drain(FluidStack resource, FluidAction action) { return steamTank.drain(resource, action); }
                @Override public @NotNull FluidStack drain(int maxDrain, FluidAction action) { return steamTank.drain(maxDrain, action); }
            }).cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        waterHandler.invalidate();
        steamHandler.invalidate();
    }

    public static void tick(Level level, BlockPos pos, BlockState state, SteamDynamoTileEntity te) {
        if (level.isClientSide()) return;
        te.pullWaterFromNeighbors();
        te.convertWaterToSteam();
        te.pushSteamToNeighbors();
    }

    private void pullWaterFromNeighbors() {
        if (waterTank.getFluidAmount() >= waterTank.getCapacity() - CONVERSION_RATE) return;

        int space = waterTank.getCapacity() - waterTank.getFluidAmount();
        int remaining = Math.min(CONVERSION_RATE * 2, space);

        for (Direction dir : Direction.values()) {
            if (remaining <= 0) break;
            BlockPos neighborPos = worldPosition.relative(dir);

            // Try pulling from fluid inventory first
            boolean pulledFromBE = false;
            BlockEntity neighbor = level.getBlockEntity(neighborPos);
            if (neighbor != null) {
                int request = remaining;
                int pulled = neighbor.getCapability(ForgeCapabilities.FLUID_HANDLER, dir.getOpposite())
                    .map(handler -> {
                        FluidStack sim = handler.drain(new FluidStack(Fluids.WATER, request), IFluidHandler.FluidAction.SIMULATE);
                        if (sim.isEmpty() || sim.getFluid() != Fluids.WATER) return 0;
                        int fill = waterTank.fill(sim, IFluidHandler.FluidAction.SIMULATE);
                        if (fill <= 0) return 0;
                        handler.drain(new FluidStack(Fluids.WATER, fill), IFluidHandler.FluidAction.EXECUTE);
                        waterTank.fill(new FluidStack(Fluids.WATER, fill), IFluidHandler.FluidAction.EXECUTE);
                        return fill;
                    }).orElse(0);
                if (pulled > 0) {
                    remaining -= pulled;
                    pulledFromBE = true;
                }
            }

            // If no BE or BE didn't provide water, check for water source blocks
            if (!pulledFromBE && remaining > 0
                    && level.getFluidState(neighborPos).isSource()
                    && level.getFluidState(neighborPos).getType() == Fluids.WATER) {
                int fill = waterTank.fill(new FluidStack(Fluids.WATER, remaining), IFluidHandler.FluidAction.SIMULATE);
                if (fill > 0) {
                    waterTank.fill(new FluidStack(Fluids.WATER, fill), IFluidHandler.FluidAction.EXECUTE);
                    remaining -= fill;
                }
            }
        }
    }

    private void convertWaterToSteam() {
        if (waterTank.getFluidAmount() < CONVERSION_RATE) return;
        if (steamTank.getFluidAmount() >= steamTank.getCapacity()) return;

        // Only drain as much water as steam tank can accept
        int steamSpace = steamTank.getCapacity() - steamTank.getFluidAmount();
        int toConvert = Math.min(CONVERSION_RATE, steamSpace);
        if (toConvert <= 0) return;

        waterTank.drain(toConvert, IFluidHandler.FluidAction.EXECUTE);
        steamTank.fill(new FluidStack(Registration.STEAM.get(), toConvert), IFluidHandler.FluidAction.EXECUTE);
    }

    private void pushSteamToNeighbors() {
        if (steamTank.getFluidAmount() <= 0) return;
        for (Direction dir : Direction.values()) {
            BlockEntity neighbor = level.getBlockEntity(worldPosition.relative(dir));
            if (neighbor == null) continue;
            neighbor.getCapability(ForgeCapabilities.FLUID_HANDLER, dir.getOpposite()).ifPresent(handler -> {
                FluidStack toPush = steamTank.drain(steamTank.getFluidAmount(), IFluidHandler.FluidAction.SIMULATE);
                if (toPush.isEmpty()) return;
                int filled = handler.fill(toPush, IFluidHandler.FluidAction.SIMULATE);
                if (filled > 0) {
                    handler.fill(new FluidStack(Registration.STEAM.get(), filled), IFluidHandler.FluidAction.EXECUTE);
                    steamTank.drain(filled, IFluidHandler.FluidAction.EXECUTE);
                }
            });
        }
    }

    public FluidTank getWaterTank() { return waterTank; }
    public FluidTank getSteamTank() { return steamTank; }
}
