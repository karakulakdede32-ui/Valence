package com.valence.valence.client.gui;

import com.valence.valence.block.assembler.AssemblerMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class AssemblerScreen extends AbstractContainerScreen<AssemblerMenu> {
    public AssemblerScreen(AssemblerMenu menu, Inventory inv, Component title) {
        super(menu, inv, title); this.imageWidth = 176; this.imageHeight = 184;
    }
    @Override protected void init() { super.init(); titleLabelX = (imageWidth - font.width(title))/2; titleLabelY = 6; inventoryLabelX = 8; inventoryLabelY = imageHeight - 94; }
    @Override public void render(GuiGraphics gg, int mx, int my, float delta) { renderBg(gg, delta, mx, my); super.render(gg, mx, my, delta); renderLabels(gg, mx, my); renderTooltip(gg, mx, my); }
    @Override protected void renderLabels(GuiGraphics gg, int mx, int my) { gg.drawString(font, title, titleLabelX, titleLabelY, 0xCCCCCC, false); gg.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0x888888, false); }
    @Override protected void renderBg(GuiGraphics gg, float delta, int mx, int my) {
        int x = leftPos, y = topPos;
        ValenceGui.drawPanel(gg, x, y, imageWidth, imageHeight);
        // Input row
        ValenceGui.drawGaugeLabel(gg, font, "Inputs", x + 62, y + 8, 0x888888);
        for (int i = 0; i < 5; i++) ValenceGui.drawSlot(gg, x + 25 + i * 18, y + 34);
        // Arrow
        ValenceGui.drawArrow(gg, x + 62, y + 55, 50, 8, 0, 1);
        // Outputs
        ValenceGui.drawGaugeLabel(gg, font, "Outputs", x + 62, y + 74, 0x888888);
        for (int i = 0; i < 3; i++) ValenceGui.drawSlot(gg, x + 43 + i * 27, y + 61);
        for (int r = 0; r < 3; r++) for (int c = 0; c < 9; c++) ValenceGui.drawSlot(gg, x+7+c*18, y+101+r*18);
        for (int c = 0; c < 9; c++) ValenceGui.drawSlot(gg, x+7+c*18, y+159);
    }
}