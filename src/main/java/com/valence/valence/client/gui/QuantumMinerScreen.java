package com.valence.valence.client.gui;

import com.valence.valence.block.quantumminer.QuantumMinerMenu;
import com.valence.valence.block.quantumminer.QuantumMinerTileEntity;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class QuantumMinerScreen extends AbstractContainerScreen<QuantumMinerMenu> {
    public QuantumMinerScreen(QuantumMinerMenu menu, Inventory inv, Component title) {
        super(menu, inv, title); this.imageWidth = 176; this.imageHeight = 184;
    }
    @Override protected void init() { super.init(); titleLabelX = (imageWidth - font.width(title))/2; titleLabelY = 6; inventoryLabelX = 8; inventoryLabelY = imageHeight - 94; }
    @Override public void render(GuiGraphics gg, int mx, int my, float delta) { renderBg(gg, delta, mx, my); super.render(gg, mx, my, delta); renderLabels(gg, mx, my); renderTooltip(gg, mx, my); }
    @Override protected void renderLabels(GuiGraphics gg, int mx, int my) { gg.drawString(font, title, titleLabelX, titleLabelY, 0xCCCCCC, false); gg.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0x888888, false); }
    @Override protected void renderBg(GuiGraphics gg, float delta, int mx, int my) {
        int x = leftPos, y = topPos;
        ValenceGui.drawPanel(gg, x, y, imageWidth, imageHeight);

        // Scan progress
        QuantumMinerTileEntity te = menu.getTileEntity();
        if (te != null) {
            int scanned = te.getScanProgress();
            ValenceGui.drawProgressBar(gg, x + 52, y + 8, 70, 6, scanned, te.getMaxScanProgress());
            ValenceGui.drawGaugeLabel(gg, font, "Quantum Scan: " + scanned + "/256", x + 87, y + 2, 0x888888);
        }

        // 3x4 output grid
        for (int r = 0; r < 3; r++) for (int c = 0; c < 4; c++)
            ValenceGui.drawSlot(gg, x + 43 + c * 22, y + 16 + r * 22);
        ValenceGui.drawGaugeLabel(gg, font, "Quantum Output", x + 88, y + 84, 0x888888);

        for (int r = 0; r < 3; r++) for (int c = 0; c < 9; c++) ValenceGui.drawSlot(gg, x+7+c*18, y+101+r*18);
        for (int c = 0; c < 9; c++) ValenceGui.drawSlot(gg, x+7+c*18, y+159);
    }
}