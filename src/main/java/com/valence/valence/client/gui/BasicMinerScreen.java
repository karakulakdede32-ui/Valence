package com.valence.valence.client.gui;

import com.valence.valence.block.miner.BasicMinerMenu;
import com.valence.valence.block.miner.BasicMinerTileEntity;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class BasicMinerScreen extends AbstractContainerScreen<BasicMinerMenu> {
    public BasicMinerScreen(BasicMinerMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 176; this.imageHeight = 184; this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override protected void init() { super.init(); titleLabelX = (imageWidth - font.width(title))/2; titleLabelY = 6; inventoryLabelX = 8; }

    @Override
    public void render(GuiGraphics gg, int mx, int my, float delta) {
        renderBg(gg, delta, mx, my); super.render(gg, mx, my, delta); renderLabels(gg, mx, my); renderTooltip(gg, mx, my);
    }

    @Override protected void renderLabels(GuiGraphics gg, int mx, int my) {
        gg.drawString(font, title, titleLabelX, titleLabelY, 0xCCCCCC, false);
        gg.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0x888888, false);
    }

    @Override protected void renderBg(GuiGraphics gg, float delta, int mx, int my) {
        int x = leftPos, y = topPos;
        ValenceGui.drawPanel(gg, x, y, imageWidth, imageHeight);

        // Fuel
        ValenceGui.drawSlot(gg, x + 79, y + 7);
        ValenceGui.drawGaugeLabel(gg, font, "Fuel", x + 88, y + 2, 0x888888);
        BasicMinerTileEntity te = menu.getTileEntity();
        if (te != null) {
            int barH = 6;
            ValenceGui.drawFuelBar(gg, x + 106, y + 9, barH, 16, te.getFuel(), 200);
        }

        // Output 2x2
        ValenceGui.drawSlot(gg, x + 61, y + 43); ValenceGui.drawSlot(gg, x + 79, y + 43);
        ValenceGui.drawSlot(gg, x + 61, y + 61); ValenceGui.drawSlot(gg, x + 79, y + 61);
        ValenceGui.drawGaugeLabel(gg, font, "Output", x + 70, y + 82, 0x888888);

        // Player inventory
        for (int r = 0; r < 3; r++) for (int c = 0; c < 9; c++) ValenceGui.drawSlot(gg, x+7+c*18, y+101+r*18);
        for (int c = 0; c < 9; c++) ValenceGui.drawSlot(gg, x+7+c*18, y+159);
    }
}
