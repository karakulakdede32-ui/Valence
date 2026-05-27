package com.valence.valence.client.gui;

import com.valence.valence.block.seeder.SeedDuplicatorMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class SeedDuplicatorScreen extends AbstractContainerScreen<SeedDuplicatorMenu> {
    public SeedDuplicatorScreen(SeedDuplicatorMenu menu, Inventory inv, Component title) {
        super(menu, inv, title); this.imageWidth = 176; this.imageHeight = 184; this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override protected void init() { super.init(); titleLabelX = (imageWidth - font.width(title))/2; titleLabelY = 6; inventoryLabelX = 8; }

    @Override public void render(GuiGraphics gg, int mx, int my, float delta) {
        renderBg(gg, delta, mx, my); super.render(gg, mx, my, delta); renderLabels(gg, mx, my); renderTooltip(gg, mx, my);
        int x = leftPos, y = topPos;
        if (mx >= x+7 && mx < x+25 && my >= y+17 && my < y+71)
            gg.renderTooltip(font, Component.literal("DF: "+menu.getDF()+"/"+menu.getDFCapacity()), mx, my);
    }

    @Override protected void renderLabels(GuiGraphics gg, int mx, int my) {
        gg.drawString(font, title, titleLabelX, titleLabelY, 0xCCCCCC, false);
        gg.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0x888888, false);
    }

    @Override protected void renderBg(GuiGraphics gg, float delta, int mx, int my) {
        int x = leftPos, y = topPos;
        ValenceGui.drawPanel(gg, x, y, imageWidth, imageHeight);

        // DF gauge
        ValenceGui.drawGauge(gg, x+7, y+17, 18, 54,
            ValenceGui.DF_TOP, ValenceGui.DF_BOTTOM,
            menu.getDF(), menu.getDFCapacity());
        ValenceGui.drawGaugeLabel(gg, font, "DF", x+16, y+75, 0x888888);

        // Input / Output
        ValenceGui.drawSlot(gg, x+55, y+34);
        ValenceGui.drawSlot(gg, x+115, y+34);

        ValenceGui.drawLabel(gg, font, Component.literal("50 DF"), x+85, y+30, 0xFFAA00);
        ValenceGui.drawLabel(gg, font, Component.literal("1►2"), x+85, y+44, 0x888888);

        ValenceGui.drawGaugeLabel(gg, font, "Seed In", x+64, y+55, 0x888888);
        ValenceGui.drawGaugeLabel(gg, font, "Dupe Out", x+124, y+55, 0x888888);

        for (int r = 0; r < 3; r++) for (int c = 0; c < 9; c++) ValenceGui.drawSlot(gg, x+7+c*18, y+101+r*18);
        for (int c = 0; c < 9; c++) ValenceGui.drawSlot(gg, x+7+c*18, y+159);
    }
}
