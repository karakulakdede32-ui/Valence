package com.valence.valence.block.washer;

import com.valence.valence.Registration;
import com.valence.valence.config.ValenceConfig;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OreWasherTileEntity extends BlockEntity implements MenuProvider {
    public static final int WATER_CAPACITY = 4000;
    public static final int WATER_PER_WASH = 250;
    public static final int PROGRESS_MAX = 80;

    private final FluidTank waterTank = new FluidTank(WATER_CAPACITY) {
        @Override public boolean isFluidValid(FluidStack stack) { return stack.getFluid().isSame(net.minecraft.world.level.material.Fluids.WATER); }
        @Override protected void onContentsChanged() { setChanged(); sync(); }
    };
    private final ItemStackHandler itemHandler = new ItemStackHandler(2) {
        @Override public boolean isItemValid(int slot, @NotNull ItemStack stack) { return slot != 1; }
        @Override protected void onContentsChanged(int slot) { setChanged(); }
    };
    private int progress = 0;
    private final LazyOptional<IFluidHandler> fluidHandler = LazyOptional.of(() -> waterTank);

    public OreWasherTileEntity(BlockPos pos, BlockState state) { super(Registration.ORE_WASHER_TE.get(), pos, state); }

    private void sync() { if (level != null && !level.isClientSide()) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3); }

    @Override public Component getDisplayName() { return Component.translatable("container.valence.ore_washer"); }
    @Nullable @Override public AbstractContainerMenu createMenu(int id, Inventory inv, Player p) { return new OreWasherMenu(id, inv, this); }

    @Override public void load(CompoundTag t) { super.load(t); waterTank.readFromNBT(t.getCompound("tank")); itemHandler.deserializeNBT(t.getCompound("items")); progress = t.getInt("progress"); }
    @Override public void saveAdditional(CompoundTag t) { super.saveAdditional(t); t.put("tank", waterTank.writeToNBT(new CompoundTag())); t.put("items", itemHandler.serializeNBT()); t.putInt("progress", progress); }
    @Override public CompoundTag getUpdateTag() { CompoundTag t = super.getUpdateTag(); t.put("tank", waterTank.writeToNBT(new CompoundTag())); t.put("items", itemHandler.serializeNBT()); t.putInt("progress", progress); return t; }
    @Override public void handleUpdateTag(CompoundTag t) { super.handleUpdateTag(t); waterTank.readFromNBT(t.getCompound("tank")); itemHandler.deserializeNBT(t.getCompound("items")); progress = t.getInt("progress"); }
    @Nullable @Override public Packet<ClientGamePacketListener> getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }
    @Override public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) { CompoundTag t = pkt.getTag(); if (t != null) { waterTank.readFromNBT(t.getCompound("tank")); itemHandler.deserializeNBT(t.getCompound("items")); progress = t.getInt("progress"); } }

    @Override public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.FLUID_HANDLER) return fluidHandler.cast();
        if (cap == ForgeCapabilities.ITEM_HANDLER) return LazyOptional.of(() -> itemHandler).cast();
        return super.getCapability(cap, side);
    }
    @Override public void invalidateCaps() { super.invalidateCaps(); fluidHandler.invalidate(); }

    public static void tick(Level level, BlockPos pos, BlockState state, OreWasherTileEntity te) {
        if (level.isClientSide()) return;

        // Pull water from neighbors
        if (te.waterTank.getFluidAmount() < te.waterTank.getCapacity()) {
            for (Direction d : Direction.values()) {
                int space = te.waterTank.getCapacity() - te.waterTank.getFluidAmount();
                if (space <= 0) break;
                BlockEntity neighbor = level.getBlockEntity(pos.relative(d));
                if (neighbor == null) continue;
                neighbor.getCapability(ForgeCapabilities.FLUID_HANDLER, d.getOpposite()).ifPresent(h -> {
                    FluidStack drained = h.drain(new FluidStack(net.minecraft.world.level.material.Fluids.WATER, space), IFluidHandler.FluidAction.SIMULATE);
                    if (!drained.isEmpty() && drained.getFluid().isSame(net.minecraft.world.level.material.Fluids.WATER)) {
                        int filled = te.waterTank.fill(drained, IFluidHandler.FluidAction.SIMULATE);
                        if (filled > 0) { h.drain(new FluidStack(net.minecraft.world.level.material.Fluids.WATER, filled), IFluidHandler.FluidAction.EXECUTE); te.waterTank.fill(new FluidStack(net.minecraft.world.level.material.Fluids.WATER, filled), IFluidHandler.FluidAction.EXECUTE); }
                    }
                });
            }
        }

        ItemStack input = te.itemHandler.getStackInSlot(0);
        ItemStack output = te.itemHandler.getStackInSlot(1);

        if (input.isEmpty() || te.waterTank.getFluidAmount() < WATER_PER_WASH) {
            if (te.progress > 0) { te.progress = 0; te.setChanged(); }
            return;
        }

        // Check if input is a grindable dust (any powder)
        boolean isPowder = input.is(com.valence.valence.Registration.IRON_POWDER.get()) ||
                           input.is(com.valence.valence.Registration.GOLD_POWDER.get()) ||
                           input.is(com.valence.valence.Registration.COPPER_POWDER.get()) ||
                           input.is(com.valence.valence.Registration.REDSTONE_POWDER.get()) ||
                           input.is(com.valence.valence.Registration.LAPIS_POWDER.get());

        if (!isPowder) {
            if (te.progress > 0) { te.progress = 0; te.setChanged(); }
            return;
        }

        // Check output space
        boolean canOutput = output.isEmpty() || (output.getCount() < output.getMaxStackSize());

        if (!canOutput) {
            if (te.progress > 0) { te.progress = 0; te.setChanged(); }
            return;
        }

        te.progress++;
        if (te.progress >= PROGRESS_MAX) {
            te.waterTank.drain(WATER_PER_WASH, IFluidHandler.FluidAction.EXECUTE);
            input.shrink(1);
            if (output.isEmpty()) te.itemHandler.setStackInSlot(1, new ItemStack(input.getItem(), 1));
            else output.grow(1);
            te.progress = 0;
        }
        te.setChanged();
    }

    public FluidTank getWaterTank() { return waterTank; }
    public ItemStackHandler getItemHandler() { return itemHandler; }
    public int getProgress() { return progress; }
    public int getMaxProgress() { return PROGRESS_MAX; }
}