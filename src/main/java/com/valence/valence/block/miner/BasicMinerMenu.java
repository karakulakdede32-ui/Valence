package com.valence.valence.block.miner;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class BasicMinerMenu extends AbstractContainerMenu {
    public static final MenuType<BasicMinerMenu> TYPE = MenuType.create(
        (id, inv) -> new BasicMinerMenu(id, inv),
        (id, inv, buf) -> new BasicMinerMenu(id, inv)
    );

    public BasicMinerMenu(int id, Inventory inv) {
        super(TYPE, id);

        // Output slots for scanned ores (slots 0-3)
        // Row 1: results left (x=35, y=20) and right (x=125, y=20) - skip 18px for slot size
        // Actually we'll do 2x2 grid at typical center
        this.addSlot(new Slot(inv, 0, 80, 24));   // Slot 0: Top-left ore
        this.addSlot(new Slot(inv, 1, 116, 24));  // Slot 1: Top-right ore  
        this.addSlot(new Slot(inv, 2, 80, 60));  // Slot 2: Bottom-left ore
        this.addSlot(new Slot(inv, 3, 116, 60)); // Slot 3: Bottom-right ore

        // Player inventory (slots 9-35, 3 rows below)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(inv, 9 + row * 9 + col, 8 + col * 18, 96 + row * 18));
            }
        }

        // Hotbar (slots 0-8 at y=154)
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(inv, col, 8 + col * 18, 154));
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIdx) {
        return ItemStack.EMPTY;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
    }
}
