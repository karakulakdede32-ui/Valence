package com.valence.valence.client.gui;

import com.valence.valence.block.endenergy.EndEnergyCoreMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class EndEnergyCoreScreen extends AbstractContainerScreen<EndEnergyCoreMenu> {
    public EndEnergyCoreScreen(EndEnergyCoreMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 176;
        this.imageHeight = 184;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void init() {
        super.init();
        titleLabelX = (imageWidth - font.width(title)) / 2;
        titleLabelY = 6;
        inventoryLabelX = 8;
    }

    @Override
    public void render(GuiGraphics gg, int mx, int my, float delta) {
        renderBg(gg, delta, mx, my);
        super.render(gg, mx, my, delta);
        renderLabels(gg, mx, my);
        renderTooltip(gg, mx, my);
        int x = leftPos;
        int y = topPos;
        if (mx >= x + 79 && mx < x + 97 && my >= y + 17 && my < y + 71) {
            gg.renderTooltip(font, Component.literal("FE: " + menu.getDF() + "/" + menu.getDFCapacity()), mx, my);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics gg, int mx, int my) {
        gg.drawString(font, title, titleLabelX, titleLabelY, 0xCCCCCC, false);
        gg.drawString(font, Component.literal(String.format("%,d / %,d FE", menu.getDF(), menu.getDFCapacity())), leftPos + 8, topPos + 80, 0xFFAA00, false);
        gg.drawString(font, Component.literal(String.format("+%,d FE/t", menu.getGenerationPerTick())), leftPos + 8, topPos + 90, 0x55FF55, false);
        gg.drawString(font, Component.literal(String.format("TX %,d FE/t", menu.getTransferRate())), leftPos + 8, topPos + 100, 0x66CCFF, false);
        gg.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0x888888, false);
    }

    @Override
    protected void renderBg(GuiGraphics gg, float delta, int mx, int my) {
        int x = leftPos;
        int y = topPos;
        ValenceGui.drawPanel(gg, x, y, imageWidth, imageHeight);

        int fe = menu.getDF();
        int cap = menu.getDFCapacity();
        ValenceGui.drawGauge(gg, x + 79, y + 17, 18, 54, ValenceGui.DF_TOP, ValenceGui.DF_BOTTOM, fe, cap);
        if (fe > 0 && cap > 0) {
            int fillPx = Math.min(fe * 52 / cap, 52);
            ValenceGui.drawActiveGlow(gg, x + 79, y + 17, 18, 54, fillPx, ValenceGui.DF_TOP);
        }
        ValenceGui.drawLabel(gg, font, Component.literal("END"), x + 88, y + 75, 0xFFAA00);
        int pct = cap > 0 ? fe * 100 / cap : 0;
        ValenceGui.drawLabel(gg, font, Component.literal(pct + "%"), x + 88, y + 86, 0x888888);

        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 9; c++) ValenceGui.drawSlot(gg, x + 7 + c * 18, y + 101 + r * 18);
        }
        for (int c = 0; c < 9; c++) ValenceGui.drawSlot(gg, x + 7 + c * 18, y + 159);
    }
}
