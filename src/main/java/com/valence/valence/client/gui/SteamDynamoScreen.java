package com.valence.valence.client.gui;

import com.valence.valence.block.dynamo.SteamDynamoMenu;
import com.valence.valence.block.dynamo.SteamDynamoTileEntity;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class SteamDynamoScreen extends AbstractContainerScreen<SteamDynamoMenu> {
    public SteamDynamoScreen(SteamDynamoMenu menu, Inventory inv, Component title) {
        super(menu, inv, title); this.imageWidth = 176; this.imageHeight = 184; this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override protected void init() { super.init(); titleLabelX = (imageWidth - font.width(title))/2; titleLabelY = 6; inventoryLabelX = 8; }

    @Override public void render(GuiGraphics gg, int mx, int my, float delta) {
        renderBg(gg, delta, mx, my); super.render(gg, mx, my, delta); renderLabels(gg, mx, my); renderTooltip(gg, mx, my);
        int x = leftPos, y = topPos;
        if (mx >= x+26 && mx < x+44 && my >= y+17 && my < y+71)
            gg.renderTooltip(font, Component.literal("Water: "+menu.getWaterAmount()+"/"+menu.getWaterCapacity()+" mB"), mx, my);
        if (mx >= x+132 && mx < x+150 && my >= y+17 && my < y+71)
            gg.renderTooltip(font, Component.literal("Steam: "+menu.getSteamAmount()+"/"+menu.getSteamCapacity()+" mB"), mx, my);
    }

    @Override protected void renderLabels(GuiGraphics gg, int mx, int my) {
        gg.drawString(font, title, titleLabelX, titleLabelY, 0xCCCCCC, false);
        gg.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0x888888, false);
    }

    @Override protected void renderBg(GuiGraphics gg, float delta, int mx, int my) {
        int x = leftPos, y = topPos;
        ValenceGui.drawPanel(gg, x, y, imageWidth, imageHeight);

        SteamDynamoTileEntity te = menu.getTileEntity();
        boolean active = te != null && te.getWaterTank().getFluidAmount() >= 18 && te.getSteamTank().getFluidAmount() < te.getSteamTank().getCapacity();
        int wPx = te != null ? te.getWaterTank().getFluidAmount() : 0;
        int wMax = te != null ? te.getWaterTank().getCapacity() : 1;
        int sPx = te != null ? te.getSteamTank().getFluidAmount() : 0;
        int sMax = te != null ? te.getSteamTank().getCapacity() : 1;

        ValenceGui.drawGauge(gg, x+26, y+17, 18, 54,
            ValenceGui.FLUID_TOP, ValenceGui.FLUID_BOTTOM, wPx, wMax);
        ValenceGui.drawGaugeLabel(gg, font, "Water", x+35, y+75, 0x888888);
        if (wPx > 0) ValenceGui.drawActiveGlow(gg, x+26, y+17, 18, 54, wPx*52/wMax, ValenceGui.FLUID_TOP);

        ValenceGui.drawLabel(gg, font, Component.literal("18 mB/t"), x+88, y+37, 0xFFAA00);
        ValenceGui.drawLabel(gg, font, Component.literal("══►"), x+88, y+49, 0x888888);
        ValenceGui.drawStatus(gg, x+83, y+8, active ? 1 : (wPx > 0 ? 2 : 0));

        ValenceGui.drawGauge(gg, x+132, y+17, 18, 54,
            ValenceGui.STEAM_TOP, ValenceGui.STEAM_BOTTOM, sPx, sMax);
        ValenceGui.drawGaugeLabel(gg, font, "Steam", x+141, y+75, 0x888888);

        for (int r = 0; r < 3; r++) for (int c = 0; c < 9; c++) ValenceGui.drawSlot(gg, x+7+c*18, y+101+r*18);
        for (int c = 0; c < 9; c++) ValenceGui.drawSlot(gg, x+7+c*18, y+159);
    }
}
