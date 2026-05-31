package com.valence.valence.client.gui;

import com.valence.valence.block.reactor.ChemicalReactorMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class ChemicalReactorScreen extends AbstractContainerScreen<ChemicalReactorMenu> {
    public ChemicalReactorScreen(ChemicalReactorMenu menu, Inventory inv, Component title) {
        super(menu, inv, title); this.imageWidth = 176; this.imageHeight = 166;
    }
    @Override protected void init() { super.init(); titleLabelX = (imageWidth - font.width(title))/2; titleLabelY = 6; inventoryLabelX = 8; inventoryLabelY = imageHeight - 94; }
    @Override public void render(GuiGraphics gg, int mx, int my, float delta) { renderBg(gg, delta, mx, my); super.render(gg, mx, my, delta); renderLabels(gg, mx, my); renderTooltip(gg, mx, my); }
    @Override protected void renderLabels(GuiGraphics gg, int mx, int my) { gg.drawString(font, title, titleLabelX, titleLabelY, 0xCCCCCC, false); gg.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0x888888, false); }
    @Override protected void renderBg(GuiGraphics gg, float delta, int mx, int my) {
        int x = leftPos, y = topPos;
        ValenceGui.drawPanel(gg, x, y, imageWidth, imageHeight);
        for (int i = 0; i < 4; i++) ValenceGui.drawSlot(gg, x + 25 + i * 18, y + 34);
        ValenceGui.drawSlot(gg, x + 133, y + 34);
        ValenceGui.drawArrow(gg, x + 104, y + 38, 26, 8, menu.getProgress(), menu.getMaxProgress());
        ValenceGui.drawGaugeLabel(gg, font, "Inputs", x + 53, y + 55, 0x888888);
        ValenceGui.drawGaugeLabel(gg, font, "Out", x + 142, y + 55, 0x888888);
        ValenceGui.drawGaugeLabel(gg, font, "React", x + 8, y + 8, 0x888888);
        for (int r = 0; r < 3; r++) for (int c = 0; c < 9; c++) ValenceGui.drawSlot(gg, x+7+c*18, y+83+r*18);
        for (int c = 0; c < 9; c++) ValenceGui.drawSlot(gg, x+7+c*18, y+141);
    }
}