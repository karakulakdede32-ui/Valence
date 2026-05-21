package com.valence.valence.block.miner;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ContainerOpenersCounter;

public class BasicMinerMenu extends net.minecraft.world.inventory.AbstractContainerMenu {
    private final BasicMinerTileEntity tileEntity;
    private final ContainerOpenersCounter openersCounter;

    public static final MenuType<BasicMinerMenu> TYPE = net.minecraft.world.inventory.MenuType.create(
        "basic_miner", BasicMinerMenu::create);

    public BasicMinerMenu(int id, Inventory playerInv, BasicMinerTileEntity te) {
        super(TYPE, id);
        this.tileEntity = te;
        this.openersCounter = te.openersCounter;
        
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

    private static BasicMinerMenu create(int id, Inventory inv, net.minecraft.world.inventory.ContainerLevelAccess access) {
        BlockEntity te = access.getBlockEntity();
        if (te instanceof BasicMinerTileEntity minerTE) {
            return new BasicMinerMenu(id, inv, minerTE);
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