package com.valence.valence.block.collector;

import com.valence.valence.Registration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
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

public class WaterCollectorTileEntity extends BlockEntity implements MenuProvider {
    public static final int CAPACITY = 16000; // 16 buckets
    private static final int GENERATION_RATE = 20; // mB per tick

    private final FluidTank tank = new FluidTank(CAPACITY) {
        @Override
        public boolean isFluidValid(FluidStack stack) {
            return stack.getFluid() == Fluids.WATER;
        }

        @Override
        protected void onContentsChanged() {
            setChanged();
            if (level != null && !level.isClientSide()) {
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
            }
        }
    };

    private final LazyOptional<IFluidHandler> fluidHandler = LazyOptional.of(() -> tank);

    public WaterCollectorTileEntity(BlockPos pos, BlockState state) {
        super(Registration.WATER_COLLECTOR_TE.get(), pos, state);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.valence.water_collector");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new WaterCollectorMenu(id, inventory, this);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        tank.readFromNBT(tag.getCompound("fluid_tank"));
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("fluid_tank", tank.writeToNBT(new CompoundTag()));
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.FLUID_HANDLER) {
            return fluidHandler.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        fluidHandler.invalidate();
    }

    public static void tick(Level level, BlockPos pos, BlockState state, WaterCollectorTileEntity te) {
        if (level.isClientSide()) return;

        // Passively generate water
        if (te.tank.getFluidAmount() < te.tank.getCapacity()) {
            int toFill = Math.min(GENERATION_RATE, te.tank.getCapacity() - te.tank.getFluidAmount());
            if (toFill > 0) {
                te.tank.fill(new FluidStack(Fluids.WATER, toFill), IFluidHandler.FluidAction.EXECUTE);
            }
        }
    }

    public FluidTank getTank() {
        return tank;
    }
}
