package com.valence.valence.block.miner;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;

public class BasicMinerMenu extends AbstractContainerMenu {
    public static final MenuType<BasicMinerMenu> TYPE = new MenuType<>(BasicMinerMenu::new);

    protected BasicMinerMenu(MenuType<BasicMinerMenu> type, int id) {
        super(type, id);
    }

    public BasicMinerMenu(int id, Inventory inv) {
        this(TYPE, id);
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }
}
