package com.valence.valence.block.collector;

import com.valence.valence.Registration;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.fluids.FluidStack;

public class WaterCollectorMenu extends AbstractContainerMenu {
    private final WaterCollectorTileEntity tileEntity;
    private final DataSlot fluidAmountSlot = DataSlot.standalone();
    private final DataSlot fluidCapacitySlot = DataSlot.standalone();

    public WaterCollectorMenu(int id, Inventory inv, FriendlyByteBuf extraData) {
        this(id, inv, inv.player.level().getBlockEntity(extraData.readBlockPos()));
    }

    public WaterCollectorMenu(int id, Inventory inv, BlockEntity entity) {
        super(Registration.WATER_COLLECTOR_MENU.get(), id);
        this.tileEntity = (WaterCollectorTileEntity) entity;
        addDataSlot(fluidAmountSlot); addDataSlot(fluidCapacitySlot);
        if (tileEntity != null) {
            FluidStack fluid = tileEntity.getTank().getFluid();
            fluidAmountSlot.set(fluid.getAmount());
            fluidCapacitySlot.set(tileEntity.getTank().getCapacity());
        }
        for (int row = 0; row < 3; row++) for (int col = 0; col < 9; col++)
            this.addSlot(new Slot(inv, col + row * 9 + 9, 8 + col * 18, 96 + row * 18));
        for (int i = 0; i < 9; i++) this.addSlot(new Slot(inv, i, 8 + i * 18, 154));
    }

    @Override public void broadcastChanges() {
        if (tileEntity != null && tileEntity.getLevel() != null && !tileEntity.getLevel().isClientSide()) {
            FluidStack fluid = tileEntity.getTank().getFluid();
            fluidAmountSlot.set(fluid.getAmount());
            fluidCapacitySlot.set(tileEntity.getTank().getCapacity());
        }
        super.broadcastChanges();
    }

    @Override public ItemStack quickMoveStack(Player pl, int idx) {
        Slot slot = slots.get(idx);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();
        ItemStack result = stack.copy();
        if (idx < 27) { if (!moveItemStackTo(stack, 27, 36, false)) return ItemStack.EMPTY; }
        else if (idx < 36) { if (!moveItemStackTo(stack, 0, 27, false)) return ItemStack.EMPTY; }
        else return ItemStack.EMPTY;
        if (stack.isEmpty()) slot.set(ItemStack.EMPTY); else slot.setChanged();
        return result;
    }

    @Override public boolean stillValid(Player player) {
        return tileEntity != null && ContainerLevelAccess.create(tileEntity.getLevel(), tileEntity.getBlockPos()).evaluate(
            (level, pos) -> player.distanceToSqr((double)pos.getX() + 0.5D, (double)pos.getY() + 0.5D, (double)pos.getZ() + 0.5D) <= 64.0D, true);
    }

    public WaterCollectorTileEntity getTileEntity() { return tileEntity; }
    public int getFluidAmount() { return fluidAmountSlot.get(); }
    public int getFluidCapacity() { return fluidCapacitySlot.get(); }
}
