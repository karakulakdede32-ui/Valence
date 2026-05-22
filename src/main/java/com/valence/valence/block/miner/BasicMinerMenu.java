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

public class BasicMinerMenu extends AbstractContainerMenu {
    private final BasicMinerTileEntity tileEntity;

    public BasicMinerMenu(int id, Inventory playerInv, BasicMinerTileEntity te) {
        super(Registration.BASIC_MINER_MENU.get(), id);
        this.tileEntity = te;
        
        // 4 output slots in 2x2 grid
        this.addSlot(new Slot(tileEntity, 0, 80, 30));
        this.addSlot(new Slot(tileEntity, 1, 98, 30));
        this.addSlot(new Slot(tileEntity, 2, 80, 48));
        this.addSlot(new Slot(tileEntity, 3, 98, 48));
        
        // Player inventory
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                this.addSlot(new Slot(playerInv, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }
        
        // Hotbar
        for (int i = 0; i < 9; i++) {
            this.addSlot(new Slot(playerInv, i, 8 + i * 18, 142));
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
        return ItemStack.EMPTY;
    }
}