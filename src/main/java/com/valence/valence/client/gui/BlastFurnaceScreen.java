package com.valence.valence.client.gui;

import com.valence.valence.block.blastfurnace.BlastFurnaceMenu;
import com.valence.valence.block.blastfurnace.BlastFurnaceTileEntity;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class BlastFurnaceScreen extends AbstractContainerScreen<BlastFurnaceMenu> {
    public BlastFurnaceScreen(BlastFurnaceMenu menu, Inventory inv, Component title) {
        super(menu, inv, title); this.imageWidth = 176; this.imageHeight = 166;
    }
    @Override protected void init() { super.init(); titleLabelX = (imageWidth - font.width(title))/2; titleLabelY = 6; inventoryLabelX = 8; inventoryLabelY = imageHeight - 94; }
    @Override public void render(GuiGraphics gg, int mx, int my, float delta) { renderBg(gg, delta, mx, my); super.render(gg, mx, my, delta); renderLabels(gg, mx, my); renderTooltip(gg, mx, my); }
    @Override protected void renderLabels(GuiGraphics gg, int mx, int my) { gg.drawString(font, title, titleLabelX, titleLabelY, 0xCCCCCC, false); gg.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0x888888, false); }
    @Override protected void renderBg(GuiGraphics gg, float delta, int mx, int my) {
        int x = leftPos, y = topPos;
        ValenceGui.drawPanel(gg, x, y, imageWidth, imageHeight);
        ValenceGui.drawSlot(gg, x + 43, y + 34); ValenceGui.drawSlot(gg, x + 115, y + 34);
        BlastFurnaceTileEntity te = menu.getTileEntity();
        int prog = te != null ? te.getProgress() : 0;
        int maxP = te != null ? te.getMaxProgress() : 1;
        ValenceGui.drawArrow(gg, x + 74, y + 38, 30, 8, prog, maxP);
        int status = (te != null && te.isMultiblockValid()) ? 1 : 3;
        ValenceGui.drawStatus(gg, x + 8, y + 8, status);
        ValenceGui.drawGaugeLabel(gg, font, te != null && te.isMultiblockValid() ? "Lava OK" : "Need lava", x + 16, y + 18, te != null && te.isMultiblockValid() ? 0x44DD44 : 0xDD4444);
        for (int r = 0; r < 3; r++) for (int c = 0; c < 9; c++) ValenceGui.drawSlot(gg, x+7+c*18, y+83+r*18);
        for (int c = 0; c < 9; c++) ValenceGui.drawSlot(gg, x+7+c*18, y+141);
    }
}