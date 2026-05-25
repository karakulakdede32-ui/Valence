package com.valence.valence.block.miner;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

import com.valence.valence.Registration;

public class BasicMinerMenu extends AbstractContainerMenu {
    private final BasicMinerTileEntity tileEntity;

    private static final int FUEL_SLOT = 0;
    private static final int OUTPUT_SLOTS_START = 1;
    private static final int OUTPUT_SLOTS_END = 5;
    private static final int PLAYER_INVENTORY_START = 5;
    private static final int HOTBAR_END = 41;

    public BasicMinerMenu(int id, Inventory playerInv, BasicMinerTileEntity te) {
        super(Registration.BASIC_MINER_MENU.get(), id);
        this.tileEntity = te;

        if (te != null) {
            // Fuel slot
            this.addSlot(new SlotItemHandler(te.getItemHandler(), 0, 80, 8));

            // Output slots: 2x2 grid
            this.addSlot(new SlotItemHandler(te.getItemHandler(), 1, 62, 44));
            this.addSlot(new SlotItemHandler(te.getItemHandler(), 2, 80, 44));
            this.addSlot(new SlotItemHandler(te.getItemHandler(), 3, 62, 62));
            this.addSlot(new SlotItemHandler(te.getItemHandler(), 4, 80, 62));
        }

        // Player inventory (standard vanilla layout)
        for (int row = 0; row < 3; row++)
            for (int col = 0; col < 9; col++)
                this.addSlot(new Slot(playerInv, col + row * 9 + 9, 8 + col * 18, 102 + row * 18));
        // Hotbar
        for (int i = 0; i < 9; i++)
            this.addSlot(new Slot(playerInv, i, 8 + i * 18, 160));
    }

    public BasicMinerMenu(int id, Inventory inv, FriendlyByteBuf buf) {
        this(id, inv, inv.player.level().getBlockEntity(buf.readBlockPos()) instanceof BasicMinerTileEntity te ? te : null);
    }

    @Override
    public boolean stillValid(Player player) {
        return tileEntity != null && ContainerLevelAccess.create(tileEntity.getLevel(), tileEntity.getBlockPos()).evaluate((level, pos) -> 
            player.distanceToSqr((double)pos.getX() + 0.5D, (double)pos.getY() + 0.5D, (double)pos.getZ() + 0.5D) <= 64.0D, true);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();

            if (index < PLAYER_INVENTORY_START) {
                // From miner to player inventory
                if (!this.moveItemStackTo(itemstack1, PLAYER_INVENTORY_START, HOTBAR_END + 1, true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // From player to miner — only allow coal/charcoal in fuel slot
                if (tileEntity != null && tileEntity.getItemHandler().isItemValid(0, itemstack1)) {
                    if (!this.moveItemStackTo(itemstack1, 0, 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else {
                    return ItemStack.EMPTY;
                }
            }

            if (itemstack1.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return itemstack;
    }

    public BasicMinerTileEntity getTileEntity() {
        return tileEntity;
    }
}
