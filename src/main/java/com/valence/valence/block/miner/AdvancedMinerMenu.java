package com.valence.valence.block.miner;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ContainerOpenersCounter;

public class AdvancedMinerMenu extends net.minecraft.world.inventory.AbstractContainerMenu {
    private final AdvancedMinerTileEntity tileEntity;

    public static final MenuType<AdvancedMinerMenu> TYPE = net.minecraft.world.inventory.MenuType.create(
        "advanced_miner", AdvancedMinerMenu::create);

    public AdvancedMinerMenu(int id, Inventory playerInv, AdvancedMinerTileEntity te) {
        super(TYPE, id);
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

    private static AdvancedMinerMenu create(int id, Inventory inv, net.minecraft.world.inventory.ContainerLevelAccess access) {
        BlockEntity te = access.getBlockEntity();
        if (te instanceof AdvancedMinerTileEntity minerTE) {
            return new AdvancedMinerMenu(id, inv, minerTE);
        }
        return null;
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