package com.valence.valence.client.gui;

import com.valence.valence.block.wireless.WirelessNodeMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class WirelessNodeScreen extends AbstractContainerScreen<WirelessNodeMenu> {
    public WirelessNodeScreen(WirelessNodeMenu menu, Inventory inv, Component title) {
        super(menu, inv, title); this.imageWidth = 176; this.imageHeight = 200; this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override protected void init() { super.init(); titleLabelX = (imageWidth - font.width(title))/2; titleLabelY = 6; inventoryLabelX = 8; }

    @Override public void render(GuiGraphics gg, int mx, int my, float delta) {
        renderBg(gg, delta, mx, my); super.render(gg, mx, my, delta); renderLabels(gg, mx, my); renderTooltip(gg, mx, my);
        int x = leftPos, y = topPos;
        if (mx >= x+79 && mx < x+97 && my >= y+17 && my < y+71)
            gg.renderTooltip(font, Component.literal("DF: "+menu.getDF()+"/"+menu.getDFCapacity()), mx, my);
    }

    @Override protected void renderLabels(GuiGraphics gg, int mx, int my) {
        gg.drawString(font, title, titleLabelX, titleLabelY, 0xCCCCCC, false);
        gg.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0x888888, false);
    }

    @Override protected void renderBg(GuiGraphics gg, float delta, int mx, int my) {
        int x = leftPos, y = topPos;
        ValenceGui.drawPanel(gg, x, y, imageWidth, imageHeight);

        // DF gauge center
        ValenceGui.drawGauge(gg, x+79, y+17, 18, 54,
            ValenceGui.DF_TOP, ValenceGui.DF_BOTTOM,
            menu.getDF(), menu.getDFCapacity());
        ValenceGui.drawLabel(gg, font, Component.literal("DF"), x+88, y+75, 0x888888);

        // Wireless icon (simple antenna)
        ValenceGui.drawLabel(gg, font, Component.literal("/\\"), x+88, y+30, 0x666666);
        ValenceGui.drawLabel(gg, font, Component.literal("/  \\"), x+88, y+40, 0x666666);

        String info = menu.getDF()+"/"+menu.getDFCapacity()+" DF";
        ValenceGui.drawLabel(gg, font, Component.literal(info), x+88, y+86, 0x666666);

        var paired = menu.getPairedPos();
        if (paired != null) {
            String pairInfo = "Paired: " + paired.getX() + "," + paired.getY() + "," + paired.getZ();
            ValenceGui.drawLabel(gg, font, Component.literal(pairInfo), x+88, y+97, 0x555555);
        } else {
            ValenceGui.drawLabel(gg, font, Component.literal("Not paired"), x+88, y+97, 0x555555);
        }

        for (int r = 0; r < 3; r++) for (int c = 0; c < 9; c++) ValenceGui.drawSlot(gg, x+7+c*18, y+119+r*18);
        for (int c = 0; c < 9; c++) ValenceGui.drawSlot(gg, x+7+c*18, y+177);
    }
}
