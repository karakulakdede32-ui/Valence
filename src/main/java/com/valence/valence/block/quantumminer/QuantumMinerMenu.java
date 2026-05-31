package com.valence.valence.block.quantumminer;

import com.valence.valence.Registration;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.SlotItemHandler;

public class QuantumMinerMenu extends AbstractContainerMenu {
    private final QuantumMinerTileEntity te;
    private static final int SLOTS = 12;
    private static final int PLAYER_START = SLOTS;
    private static final int HOTBAR_END = SLOTS + 36;

    public QuantumMinerMenu(int id, Inventory inv, FriendlyByteBuf buf) { this(id, inv, inv.player.level().getBlockEntity(buf.readBlockPos())); }
    public QuantumMinerMenu(int id, Inventory inv, BlockEntity entity) {
        super(Registration.QUANTUM_MINER_MENU.get(), id);
        this.te = (QuantumMinerTileEntity) entity;
        if (te != null) {
            // 3x4 output grid
            for (int r = 0; r < 3; r++) for (int c = 0; c < 4; c++)
                addSlot(new SlotItemHandler(te.getItemHandler(), c + r * 4, 44 + c * 22, 17 + r * 22) {
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
            if (idx < SLOTS) { if (!moveItemStackTo(s1, PLAYER_START, HOTBAR_END, true)) return ItemStack.EMPTY; }
            else return ItemStack.EMPTY;
            if (s1.isEmpty()) slot.set(ItemStack.EMPTY); else slot.setChanged();
        } return stack;
    }
    public QuantumMinerTileEntity getTileEntity() { return te; }
}