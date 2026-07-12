package com.valence.valence.block.endenergy;

import com.valence.valence.Registration;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public class EndEnergyCoreMenu extends AbstractContainerMenu {
    private final EndEnergyCoreTileEntity tileEntity;
    private final DataSlot dfAmount = DataSlot.standalone();
    private final DataSlot dfCapacity = DataSlot.standalone();
    private final DataSlot genPerTick = DataSlot.standalone();
    private final DataSlot transferRate = DataSlot.standalone();

    public EndEnergyCoreMenu(int id, Inventory inv, FriendlyByteBuf buf) {
        this(id, inv, inv.player.level().getBlockEntity(buf.readBlockPos()));
    }

    public EndEnergyCoreMenu(int id, Inventory inv, BlockEntity entity) {
        super(Registration.END_ENERGY_CORE_MENU.get(), id);
        tileEntity = (EndEnergyCoreTileEntity) entity;
        addDataSlot(dfAmount);
        addDataSlot(dfCapacity);
        addDataSlot(genPerTick);
        addDataSlot(transferRate);
        if (tileEntity != null) {
            dfAmount.set(tileEntity.getDFStorage().getDF());
            dfCapacity.set(tileEntity.getDFStorage().getMaxDF());
            genPerTick.set(tileEntity.getGenerationPerTickValue());
            transferRate.set(tileEntity.getTransferValue());
        }

        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 9; c++) {
                addSlot(new Slot(inv, c + r * 9 + 9, 8 + c * 18, 102 + r * 18));
            }
        }
        for (int i = 0; i < 9; i++) addSlot(new Slot(inv, i, 8 + i * 18, 160));
    }

    @Override
    public void broadcastChanges() {
        if (tileEntity != null && tileEntity.getLevel() != null && !tileEntity.getLevel().isClientSide()) {
            dfAmount.set(tileEntity.getDFStorage().getDF());
            dfCapacity.set(tileEntity.getDFStorage().getMaxDF());
            genPerTick.set(tileEntity.getGenerationPerTickValue());
            transferRate.set(tileEntity.getTransferValue());
        }
        super.broadcastChanges();
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();
        ItemStack result = stack.copy();
        if (index < 27) {
            if (!moveItemStackTo(stack, 27, 36, false)) return ItemStack.EMPTY;
        } else if (index < 36) {
            if (!moveItemStackTo(stack, 0, 27, false)) return ItemStack.EMPTY;
        } else return ItemStack.EMPTY;
        if (stack.isEmpty()) slot.set(ItemStack.EMPTY);
        else slot.setChanged();
        return result;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    public int getDF() {
        return dfAmount.get();
    }

    public int getDFCapacity() {
        return dfCapacity.get();
    }

    public int getGenerationPerTick() {
        return genPerTick.get();
    }

    public int getTransferRate() {
        return transferRate.get();
    }
}
