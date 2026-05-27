package com.valence.valence.client.gui;

import com.valence.valence.block.collector.WaterCollectorMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class WaterCollectorScreen extends AbstractContainerScreen<WaterCollectorMenu> {
    public WaterCollectorScreen(WaterCollectorMenu menu, Inventory inv, Component title) {
        super(menu, inv, title); this.imageWidth = 176; this.imageHeight = 178; this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override protected void init() { super.init(); titleLabelX = (imageWidth - font.width(title))/2; titleLabelY = 6; inventoryLabelX = 8; }

    @Override public void render(GuiGraphics gg, int mx, int my, float delta) {
        renderBg(gg, delta, mx, my); super.render(gg, mx, my, delta); renderLabels(gg, mx, my); renderTooltip(gg, mx, my);
        int x = leftPos, y = topPos;
        if (mx >= x+79 && mx < x+97 && my >= y+17 && my < y+71)
            gg.renderTooltip(font, Component.literal("Water: "+menu.getFluidAmount()+"/"+menu.getFluidCapacity()+" mB"), mx, my);
    }

    @Override protected void renderLabels(GuiGraphics gg, int mx, int my) {
        gg.drawString(font, title, titleLabelX, titleLabelY, 0xCCCCCC, false);
        gg.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0x888888, false);
    }

    @Override protected void renderBg(GuiGraphics gg, float delta, int mx, int my) {
        int x = leftPos, y = topPos;
        ValenceGui.drawPanel(gg, x, y, imageWidth, imageHeight);

        // Large water tank in center
        ValenceGui.drawGauge(gg, x + 79, y + 17, 18, 54,
            ValenceGui.FLUID_TOP, ValenceGui.FLUID_BOTTOM,
            menu.getFluidAmount(), menu.getFluidCapacity());
        ValenceGui.drawLabel(gg, font, Component.literal("Water"), x + 88, y + 75, 0x888888);

        // Capacity info
        String cap = menu.getFluidAmount()+"/"+menu.getFluidCapacity()+"mB";
        ValenceGui.drawLabel(gg, font, Component.literal(cap), x + 88, y + 86, 0x666666);

        for (int r = 0; r < 3; r++) for (int c = 0; c < 9; c++) ValenceGui.drawSlot(gg, x+7+c*18, y+95+r*18);
        for (int c = 0; c < 9; c++) ValenceGui.drawSlot(gg, x+7+c*18, y+153);
    }
}
