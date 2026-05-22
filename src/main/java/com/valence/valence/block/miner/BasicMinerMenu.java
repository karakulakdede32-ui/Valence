package com.valence.valence.block.miner;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import com.valence.valence.Registration;

public class BasicMinerMenu extends AbstractContainerMenu {
    private final BasicMinerTileEntity tileEntity;

    public BasicMinerMenu(int id, Inventory playerInv, BasicMinerTileEntity te) {
        super(Registration.BASIC_MINER_MENU.get(), id);
        this.tileEntity = te;
        
        // ===== MINER OUTPUT SECTION =====
        // 4 output slots in a clean 2x2 grid, centered
        // Row 1
        this.addSlot(new Slot(tileEntity, 0, 35, 17));
        this.addSlot(new Slot(tileEntity, 1, 62, 17));
        this.addSlot(new Slot(tileEntity, 2, 89, 17));
        this.addSlot(new Slot(tileEntity, 3, 116, 17));
        
        // ===== PLAYER INVENTORY SECTION =====
        // Player inventory - 3 rows below miner
        // Row 1 (main inventory)
        for (int i = 0; i < 9; i++) {
            this.addSlot(new Slot(playerInv, i + 9, 8 + i * 18, 58 + 18));
        }
        // Row 2
        for (int i = 0; i < 9; i++) {
            this.addSlot(new Slot(playerInv, i + 18, 8 + i * 18, 58 + 36));
        }
        // Row 3
        for (int i = 0; i < 9; i++) {
            this.addSlot(new Slot(playerInv, i + 27, 8 + i * 18, 58 + 54));
        }
        
        // Hotbar - bottom row
        for (int i = 0; i < 9; i++) {
            this.addSlot(new Slot(playerInv, i, 8 + i * 18, 139));
        }
    }

    // Constructor for MenuType - receives ContainerLevelAccess
    public BasicMinerMenu(int id, Inventory inv, ContainerLevelAccess access) {
        this(id, inv, getTileEntity(access));
    }

    private static BasicMinerTileEntity getTileEntity(ContainerLevelAccess access) {
        return access.evaluate((level, pos) -> {
            if (level.getBlockEntity(pos) instanceof BasicMinerTileEntity te) return te;
            return null;
        }, null);
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();
            
            // Transfer from miner output to player inventory
            if (index < 4) {
                if (!this.moveItemStackTo(itemstack1, 4, 40, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(itemstack1, 0, 4, false)) {
                // Try to put back in miner
                return ItemStack.EMPTY;
            }
            
            if (itemstack1.getCount() == 0) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return itemstack;
    }
}