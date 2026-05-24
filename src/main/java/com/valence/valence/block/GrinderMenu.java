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
            // Machine slots: input at (44, 35), output at (116, 35)
            this.addSlot(new SlotItemHandler(tileEntity.getItemHandler(), 0, 44, 35)); // Input
            this.addSlot(new SlotItemHandler(tileEntity.getItemHandler(), 1, 116, 35) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return false; // Cannot put items into output slot
                }
            }); // Output
        }

        // Player inventory: starts at y=84
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(inv, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }

        // Hotbar: y=142
        for (int i = 0; i < 9; i++) {
            this.addSlot(new Slot(inv, i, 8 + i * 18, 142));
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();
            if (index < 2) {
                // From machine to player inventory
                if (!this.moveItemStackTo(itemstack1, 2, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(itemstack1, 0, 1, false)) {
                // From player inventory to input slot only
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
        return tileEntity != null && player.distanceToSqr(
            (double) tileEntity.getBlockPos().getX() + 0.5D,
            (double) tileEntity.getBlockPos().getY() + 0.5D,
            (double) tileEntity.getBlockPos().getZ() + 0.5D) <= 64.0D;
    }
}
