package com.valence.valence.block.blastfurnace;

import com.valence.valence.Registration;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.SlotItemHandler;

public class BlastFurnaceMenu extends AbstractContainerMenu {
    private final BlastFurnaceTileEntity te;
    public BlastFurnaceMenu(int id, Inventory inv, FriendlyByteBuf buf) { this(id, inv, inv.player.level().getBlockEntity(buf.readBlockPos())); }
    public BlastFurnaceMenu(int id, Inventory inv, BlockEntity entity) {
        super(Registration.BLAST_FURNACE_MENU.get(), id);
        this.te = (BlastFurnaceTileEntity) entity;
        if (te != null) {
            addSlot(new SlotItemHandler(te.getItemHandler(), 0, 44, 35));
            addSlot(new SlotItemHandler(te.getItemHandler(), 1, 116, 35) { @Override public boolean mayPlace(ItemStack s) { return false; } });
        }
        for (int r = 0; r < 3; r++) for (int c = 0; c < 9; c++) addSlot(new Slot(inv, c + r * 9 + 9, 8 + c * 18, 84 + r * 18));
        for (int i = 0; i < 9; i++) addSlot(new Slot(inv, i, 8 + i * 18, 142));
    }
    @Override public boolean stillValid(Player p) { return te != null && p.distanceToSqr(te.getBlockPos().getX()+0.5, te.getBlockPos().getY()+0.5, te.getBlockPos().getZ()+0.5) <= 64; }
    @Override public ItemStack quickMoveStack(Player p, int idx) {
        ItemStack stack = ItemStack.EMPTY; Slot slot = slots.get(idx);
        if (slot != null && slot.hasItem()) {
            ItemStack s1 = slot.getItem(); stack = s1.copy();
            if (idx < 2) { if (!moveItemStackTo(s1, 2, slots.size(), true)) return ItemStack.EMPTY; }
            else if (!moveItemStackTo(s1, 0, 1, false)) return ItemStack.EMPTY;
            if (s1.isEmpty()) slot.set(ItemStack.EMPTY); else slot.setChanged();
        } return stack;
    }
    public BlastFurnaceTileEntity getTileEntity() { return te; }
}