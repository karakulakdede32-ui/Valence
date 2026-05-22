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

public class AdvancedMinerMenu extends AbstractContainerMenu {
    private final AdvancedMinerTileEntity tileEntity;
    
    // Slot indices
    private static final int FUEL_SLOT = 0;
    private static final int OUTPUT_SLOTS_START = 1;
    private static final int OUTPUT_SLOTS = 8;
    private static final int PLAYER_INVENTORY_START = 9;
    private static final int PLAYER_INVENTORY_END = 36;
    private static final int HOTBAR_START = 36;

    public AdvancedMinerMenu(int id, Inventory playerInv, AdvancedMinerTileEntity te) {
        super(Registration.ADVANCED_MINER_MENU.get(), id);
        this.tileEntity = te;
        
        // ===== TOP SECTION: FUEL SLOT =====
        // Single fuel slot prominently displayed at top-center
        this.addSlot(new Slot(tileEntity, 0, 80, 8));
        
        // ===== MIDDLE SECTION: OUTPUT SLOTS =====
        // 8 output slots in a 4x2 grid below the fuel slot
        // Row 1 of outputs
        this.addSlot(new Slot(tileEntity, 1, 26, 44));
        this.addSlot(new Slot(tileEntity, 2, 53, 44));
        this.addSlot(new Slot(tileEntity, 3, 80, 44));
        this.addSlot(new Slot(tileEntity, 4, 107, 44));
        // Row 2 of outputs
        this.addSlot(new Slot(tileEntity, 5, 26, 71));
        this.addSlot(new Slot(tileEntity, 6, 53, 71));
        this.addSlot(new Slot(tileEntity, 7, 80, 71));
        this.addSlot(new Slot(tileEntity, 8, 107, 71));
        
        // ===== PLAYER INVENTORY SECTION =====
        // Player inventory - 3 rows below the miner section
        // Row 1 (main inventory)
        for (int i = 0; i < 9; i++) {
            this.addSlot(new Slot(playerInv, i + 9, 8 + i * 18, 106));
        }
        // Row 2
        for (int i = 0; i < 9; i++) {
            this.addSlot(new Slot(playerInv, i + 18, 8 + i * 18, 124));
        }
        // Row 3
        for (int i = 0; i < 9; i++) {
            this.addSlot(new Slot(playerInv, i + 27, 8 + i * 18, 142));
        }
        
        // Hotbar - bottom row
        for (int i = 0; i < 9; i++) {
            this.addSlot(new Slot(playerInv, i, 8 + i * 18, 160));
        }
    }

    // Constructor for MenuType - receives ContainerLevelAccess
    public AdvancedMinerMenu(int id, Inventory inv, ContainerLevelAccess access) {
        this(id, inv, getTileEntity(access));
    }

    private static AdvancedMinerTileEntity getTileEntity(ContainerLevelAccess access) {
        return access.evaluate((level, pos) -> {
            if (level.getBlockEntity(pos) instanceof AdvancedMinerTileEntity te) return te;
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
            if (index < OUTPUT_SLOTS_START + OUTPUT_SLOTS) {
                // From fuel (0) or output slots (1-8) to player inventory
                if (!this.moveItemStackTo(itemstack1, PLAYER_INVENTORY_START, PLAYER_INVENTORY_END + 9, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (index >= PLAYER_INVENTORY_START && index < PLAYER_INVENTORY_END) {
                // From player inventory to miner slots
                if (index == PLAYER_INVENTORY_START && this.slots.get(FUEL_SLOT).mayPlace(itemstack1)) {
                    // Fuel slot - can only accept fuel items
                    if (!this.moveItemStackTo(itemstack1, FUEL_SLOT, FUEL_SLOT + 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (!this.moveItemStackTo(itemstack1, OUTPUT_SLOTS_START, OUTPUT_SLOTS_START + OUTPUT_SLOTS, false)) {
                    // Try to put in output slots
                    return ItemStack.EMPTY;
                }
            } else {
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