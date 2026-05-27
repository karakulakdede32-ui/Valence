package com.valence.valence.client.gui;

import com.mojang.blaze3d.platform.InputConstants;
import com.valence.valence.block.efurnace.ElectricFurnaceMenu;
import com.valence.valence.block.efurnace.ElectricFurnaceTileEntity;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class ElectricFurnaceScreen extends AbstractContainerScreen<ElectricFurnaceMenu> {
    public ElectricFurnaceScreen(ElectricFurnaceMenu menu, Inventory inv, Component title) {
        super(menu, inv, title); this.imageWidth = 176; this.imageHeight = 184; this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override protected void init() { super.init(); titleLabelX = (imageWidth - font.width(title))/2; titleLabelY = 6; inventoryLabelX = 8; }

    @Override public void render(GuiGraphics gg, int mx, int my, float delta) {
        renderBg(gg, delta, mx, my); super.render(gg, mx, my, delta); renderLabels(gg, mx, my); renderTooltip(gg, mx, my);
        int x = leftPos, y = topPos;
        if (mx >= x+151 && mx < x+169 && my >= y+17 && my < y+71)
            gg.renderTooltip(font, Component.literal("DF: "+menu.getDF()+" / "+menu.getDFCapacity()), mx, my);
    }

    @Override protected void renderLabels(GuiGraphics gg, int mx, int my) {
        gg.drawString(font, title, titleLabelX, titleLabelY, 0xCCCCCC, false);
        if (menu.balanceMode.get() == 1)
            gg.drawString(font, Component.literal("[Balance: ON]"), leftPos+8, topPos+88, 0xFFAA00, false);
        gg.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0x888888, false);
    }

    @Override public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == InputConstants.KEY_K && menu != null) {
            minecraft.player.connection.send(new net.minecraft.network.protocol.game.ServerboundContainerButtonClickPacket(menu.containerId, 0));
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override protected void renderBg(GuiGraphics gg, float delta, int mx, int my) {
        int x = leftPos, y = topPos;
        ValenceGui.drawPanel(gg, x, y, imageWidth, imageHeight);

        // DF gauge
        ElectricFurnaceTileEntity te = menu.getTileEntity();
        int dfPx = te != null ? te.getDFStorage().getDF() : 0;
        int dfMax = te != null ? te.getDFStorage().getMaxDF() : 1;
        boolean active = te != null && te.getDFStorage().getDF() >= 5;

        ValenceGui.drawGauge(gg, x+151, y+17, 18, 54,
            ValenceGui.DF_TOP, ValenceGui.DF_BOTTOM, dfPx, dfMax);
        ValenceGui.drawGaugeLabel(gg, font, "DF", x+160, y+75, 0x888888);
        if (active) ValenceGui.drawActiveGlow(gg, x+151, y+17, 18, 54, dfPx*52/dfMax, ValenceGui.DF_TOP);

        // Status
        boolean anyActive = false;
        if (te != null) for (int p : te.getProgress()) if (p > 0) { anyActive = true; break; }
        ValenceGui.drawStatus(gg, x+8, y+8, anyActive ? 1 : (menu.getDF() >= 5 ? 2 : 0));

        // 8 input slots
        for (int c = 0; c < 8; c++) ValenceGui.drawSlot(gg, x+7+c*18, y+16);
        ValenceGui.drawGaugeLabel(gg, font, "Inputs", x+79, y+38, 0x888888);

        // 8 progress bars
        for (int c = 0; c < 8; c++)
            ValenceGui.drawArrow(gg, x+7+c*18, y+42, 16, 6, menu.getProgress(c), menu.getMaxProgress());

        // 8 output slots
        for (int c = 0; c < 8; c++) ValenceGui.drawSlot(gg, x+7+c*18, y+61);
        ValenceGui.drawGaugeLabel(gg, font, "Outputs", x+79, y+83, 0x888888);

        for (int r = 0; r < 3; r++) for (int c = 0; c < 9; c++) ValenceGui.drawSlot(gg, x+7+c*18, y+101+r*18);
        for (int c = 0; c < 9; c++) ValenceGui.drawSlot(gg, x+7+c*18, y+159);
    }
}
