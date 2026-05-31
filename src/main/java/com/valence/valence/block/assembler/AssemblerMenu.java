package com.valence.valence.block.assembler;

import com.valence.valence.Registration;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.SlotItemHandler;

public class AssemblerMenu extends AbstractContainerMenu {
    private final AssemblerTileEntity te;
    public AssemblerMenu(int id, Inventory inv, FriendlyByteBuf buf) { this(id, inv, inv.player.level().getBlockEntity(buf.readBlockPos())); }
    public AssemblerMenu(int id, Inventory inv, BlockEntity entity) {
        super(Registration.ASSEMBLER_MENU.get(), id);
        this.te = (AssemblerTileEntity) entity;
        if (te != null) {
            // Input slots 0-4
            int[] inx = {26, 44, 62, 80, 98};
            for (int i = 0; i < 5; i++) addSlot(new SlotItemHandler(te.getItemHandler(), i, inx[i], 35));
            // Output slots 6-8
            for (int i = 6; i <= 8; i++) addSlot(new SlotItemHandler(te.getItemHandler(), i, 44 + (i-6)*27, 62) {
                @Override public boolean mayPlace(ItemStack s) { return false; }
            });
        }
        for (int r = 0; r < 3; r++) for (int c = 0; c < 9; c++) addSlot(new Slot(inv, c + r * 9 + 9, 8 + c * 18, 102 + r * 18));
        for (int i = 0; i < 9; i++) addSlot(new Slot(inv, i, 8 + i * 18, 160));
    }
    @Override public boolean stillValid(Player p) { return te != null && p.distanceToSqr(te.getBlockPos().getX()+0.5, te.getBlockPos().getY()+0.5, te.getBlockPos().getZ()+0.5) <= 64; }
    @Override public ItemStack quickMoveStack(Player p, int idx) {
        ItemStack stack = ItemStack.EMPTY; Slot slot = slots.get(idx);
        if (slot != null && slot.hasItem()) {
            ItemStack s1 = slot.getItem(); stack = s1.copy();
            int machineEnd = 9;
            if (idx < machineEnd) { if (!moveItemStackTo(s1, machineEnd, slots.size(), true)) return ItemStack.EMPTY; }
            else if (!moveItemStackTo(s1, 0, 5, false)) return ItemStack.EMPTY;
            if (s1.isEmpty()) slot.set(ItemStack.EMPTY); else slot.setChanged();
        } return stack;
    }
    public AssemblerTileEntity getTileEntity() { return te; }
}