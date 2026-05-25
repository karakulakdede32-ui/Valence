package com.valence.valence.block;

import com.valence.valence.Registration;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.SlotItemHandler;

public class GrinderMenu extends AbstractContainerMenu {
    private final GrinderTileEntity tileEntity;

    public GrinderMenu(int id, Inventory inv, FriendlyByteBuf extraData) {
        this(id, inv, inv.player.level().getBlockEntity(extraData.readBlockPos()));
    }

    public GrinderMenu(int id, Inventory inv, BlockEntity entity) {
        super(Registration.GRINDER_MENU.get(), id);
        this.tileEntity = (GrinderTileEntity) entity;

        if (tileEntity != null) {
            this.addSlot(new SlotItemHandler(tileEntity.getItemHandler(), 0, 44, 35)); // Input
            this.addSlot(new SlotItemHandler(tileEntity.getItemHandler(), 1, 116, 35) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return false; // Cannot put items into output slot
                }
            }); // Output
        }

        layoutPlayerInventorySlots(inv, 8, 84);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();
            if (index < 2) {
                if (!this.moveItemStackTo(itemstack1, 2, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(itemstack1, 0, 1, false)) {
                return ItemStack.EMPTY;
            }

            if (itemstack1.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return itemstack;
    }

    @Override
    public boolean stillValid(Player player) {
        return tileEntity != null && player.distanceToSqr((double)tileEntity.getBlockPos().getX() + 0.5D, (double)tileEntity.getBlockPos().getY() + 0.5D, (double)tileEntity.getBlockPos().getZ() + 0.5D) <= 64.0D;
    }

    public GrinderTileEntity getTileEntity() {
        return tileEntity;
    }

    private int addSlotRange(Inventory playerInventory, int index, int x, int y, int amount, int dx) {
        for (int i = 0; i < amount; i++) {
            addSlot(new Slot(playerInventory, index, x, y));
            x += dx;
            index++;
        }
        return index;
    }

    private int addSlotBox(Inventory playerInventory, int index, int x, int y, int horAmount, int dx, int verAmount, int dy) {
        for (int j = 0; j < verAmount; j++) {
            index = addSlotRange(playerInventory, index, x, y, horAmount, dx);
            y += dy;
        }
        return index;
    }

    private void layoutPlayerInventorySlots(Inventory playerInventory, int x, int y) {
        addSlotBox(playerInventory, 9, x, y, 9, 18, 3, 18);
        addSlotRange(playerInventory, 0, x, y + 58, 9, 18);
    }
}
