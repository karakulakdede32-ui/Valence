package com.valence.valence.block.miner;

import net.minecraft.core.BlockPos;
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

    public AdvancedMinerMenu(int id, Inventory playerInv, AdvancedMinerTileEntity te) {
        super(Registration.ADVANCED_MINER_MENU.get(), id);
        this.tileEntity = te;
        
        // 1 fuel slot (index 0)
        this.addSlot(new Slot(tileEntity, 0, 80, 20));
        
        // 8 output slots (indices 1-8)
        for (int i = 0; i < 4; i++) {
            this.addSlot(new Slot(tileEntity, 1 + i, 44 + i * 18, 50));
        }
        for (int i = 0; i < 4; i++) {
            this.addSlot(new Slot(tileEntity, 5 + i, 44 + i * 18, 68));
        }
        
        // Player inventory
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                this.addSlot(new Slot(playerInv, j + i * 9 + 9, 8 + j * 18, 100 + i * 18));
            }
        }
        
        // Hotbar
        for (int i = 0; i < 9; i++) {
            this.addSlot(new Slot(playerInv, i, 8 + i * 18, 158));
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
        return ItemStack.EMPTY;
    }
}