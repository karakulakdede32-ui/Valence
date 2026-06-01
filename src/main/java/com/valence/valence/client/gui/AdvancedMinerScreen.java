package com.valence.valence.client.gui;

import com.valence.valence.block.miner.AdvancedMinerMenu;
import com.valence.valence.block.miner.AdvancedMinerTileEntity;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class AdvancedMinerScreen extends AbstractContainerScreen<AdvancedMinerMenu> {
    public AdvancedMinerScreen(AdvancedMinerMenu menu, Inventory inv, Component title) {
        super(menu, inv, title); this.imageWidth = 176; this.imageHeight = 184; this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override protected void init() { super.init(); titleLabelX = (imageWidth - font.width(title))/2; titleLabelY = 6; inventoryLabelX = 8; }

    @Override public void render(GuiGraphics gg, int mx, int my, float delta) {
        renderBg(gg, delta, mx, my); super.render(gg, mx, my, delta); renderLabels(gg, mx, my); renderTooltip(gg, mx, my);
    }

    @Override protected void renderLabels(GuiGraphics gg, int mx, int my) {
        gg.drawString(font, title, titleLabelX, titleLabelY, 0xCCCCCC, false);
        gg.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0x888888, false);
    }

    @Override protected void renderBg(GuiGraphics gg, float delta, int mx, int my) {
        int x = leftPos, y = topPos;
        ValenceGui.drawPanel(gg, x, y, imageWidth, imageHeight);

        // Fuel slot
        ValenceGui.drawSlot(gg, x + 80, y + 8);
        ValenceGui.drawGaugeLabel(gg, font, "Fuel", x + 88, y + 2, 0x888888);
        AdvancedMinerTileEntity te = menu.getTileEntity();
        if (te != null) {
            // Fuel bar (coal/charcoal backup)
            int fuelBarH = 16;
            ValenceGui.drawFuelBar(gg, x + 106, y + 9, 6, fuelBarH, te.getFuel(), te.getMaxFuel());
            // DF gauge
            ValenceGui.drawGauge(gg, x + 116, y + 9, 6, fuelBarH,
                ValenceGui.DF_TOP, ValenceGui.DF_BOTTOM,
                te.getDFStorage().getDF(), te.getDFStorage().getMaxDF());
            ValenceGui.drawGaugeLabel(gg, font, "DF", x + 119, y + 2, ValenceGui.DF_TOP);
        }

        // Scan progress
        if (te != null && te.hasFuel()) {
            int scanned = te.getScanProgress();
            String scanText = "Scan: " + scanned + "/256";
            ValenceGui.drawProgressBar(gg, x + 52, y + 26, 70, 6, scanned, 256);
            ValenceGui.drawGaugeLabel(gg, font, scanText, x + 87, y + 18, 0x888888);
        }

        // Output 3x4 grid
        for (int i = 0; i < 3; i++) for (int j = 0; j < 4; j++)
            ValenceGui.drawSlot(gg, x + 26 + j * 27, y + 44 + i * 27);
        ValenceGui.drawGaugeLabel(gg, font, "Output", x + 88, y + 124, 0x888888);

        // Player inventory
        for (int r = 0; r < 3; r++) for (int c = 0; c < 9; c++) ValenceGui.drawSlot(gg, x+7+c*18, y+105+r*18);
        for (int c = 0; c < 9; c++) ValenceGui.drawSlot(gg, x+7+c*18, y+159);
    }
}
