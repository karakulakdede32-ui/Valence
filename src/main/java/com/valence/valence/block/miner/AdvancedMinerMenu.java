package com.valence.valence.block.miner;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;

public class AdvancedMinerMenu extends AbstractContainerMenu {
    public static final MenuType<AdvancedMinerMenu> TYPE = new MenuType<>(AdvancedMinerMenu::new);

    protected AdvancedMinerMenu(MenuType<AdvancedMinerMenu> type, int id) {
        super(type, id);
    }

    public AdvancedMinerMenu(int id, Inventory inv) {
        this(TYPE, id);
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }
}
