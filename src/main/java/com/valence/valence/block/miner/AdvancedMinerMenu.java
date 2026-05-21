package com.valence.valence.block.miner;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class AdvancedMinerMenu extends AbstractContainerMenu {
    public static final MenuType<AdvancedMinerMenu> TYPE = MenuType.create(
        (id, inv) -> new AdvancedMinerMenu(id, inv),
        (id, inv, buf) -> new AdvancedMinerMenu(id, inv)
    );

    public AdvancedMinerMenu(int id, Inventory inv) {
        super(TYPE, id);

        // Slot 0: Fuel input (coal/charcoal)
        this.addSlot(new Slot(inv, 0, 44, 49));  

        // Slots 1-8: Output for up to 8 ore types (2x4 grid)
        int[] ox = {80, 107, 134, 161, 80, 107, 134, 161};
        int[] oy = {24, 24, 24, 24, 60, 60, 60, 60};
        for (int i = 0; i < 8; i++) {
            this.addSlot(new Slot(inv, 1 + i, ox[i], oy[i]));
        }

        // Player inventory (3 rows = 27 slots)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(inv, 9 + row * 9 + col, 8 + col * 18, 100 + row * 18));
            }
        }

        // Hotbar (9 slots)
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(inv, col, 8 + col * 18, 158));
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
