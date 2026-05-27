package com.valence.valence.block.megacell;

import com.valence.valence.Registration;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public class MegaCellMenu extends AbstractContainerMenu {
    private final MegaCellTileEntity tileEntity;
    private final DataSlot dfAmount = DataSlot.standalone();
    private final DataSlot dfCapacity = DataSlot.standalone();

    public MegaCellMenu(int id, Inventory inv, FriendlyByteBuf buf) { this(id, inv, inv.player.level().getBlockEntity(buf.readBlockPos())); }
    public MegaCellMenu(int id, Inventory inv, BlockEntity entity) {
        super(Registration.MEGA_CELL_MENU.get(), id);
        tileEntity = (MegaCellTileEntity) entity;
        addDataSlot(dfAmount); addDataSlot(dfCapacity);
        if (tileEntity != null) {
            dfAmount.set(tileEntity.getDFStorage().getDF());
            dfCapacity.set(tileEntity.getDFStorage().getMaxDF());
        }
        for (int r = 0; r < 3; r++) for (int c = 0; c < 9; c++) addSlot(new Slot(inv, c+r*9+9, 8+c*18, 102+r*18));
        for (int i = 0; i < 9; i++) addSlot(new Slot(inv, i, 8+i*18, 160));
    }

    @Override public void broadcastChanges() {
        if (tileEntity != null && tileEntity.getLevel() != null && !tileEntity.getLevel().isClientSide()) {
            dfAmount.set(tileEntity.getDFStorage().getDF());
            dfCapacity.set(tileEntity.getDFStorage().getMaxDF());
        }
        super.broadcastChanges();
    }

    @Override public ItemStack quickMoveStack(Player pl, int idx) {
        Slot slot = slots.get(idx);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem(); ItemStack result = stack.copy();
        if (idx < 27) { if (!moveItemStackTo(stack, 27, 36, false)) return ItemStack.EMPTY; }
        else if (idx < 36) { if (!moveItemStackTo(stack, 0, 27, false)) return ItemStack.EMPTY; }
        else return ItemStack.EMPTY;
        if (stack.isEmpty()) slot.set(ItemStack.EMPTY); else slot.setChanged();
        return result;
    }

    @Override public boolean stillValid(Player pl) { return true; }
    public MegaCellTileEntity getTileEntity() { return tileEntity; }
    public int getDF() { return dfAmount.get(); }
    public int getDFCapacity() { return dfCapacity.get(); }
}
