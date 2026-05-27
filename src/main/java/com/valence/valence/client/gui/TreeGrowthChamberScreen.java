package com.valence.valence.client.gui;

import com.valence.valence.block.growthchamber.TreeGrowthChamberMenu;
import com.valence.valence.block.growthchamber.TreeGrowthChamberTileEntity;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class TreeGrowthChamberScreen extends AbstractContainerScreen<TreeGrowthChamberMenu> {
    public TreeGrowthChamberScreen(TreeGrowthChamberMenu menu, Inventory inv, Component title) {
        super(menu, inv, title); this.imageWidth = 176; this.imageHeight = 184; this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override protected void init() { super.init(); titleLabelX = (imageWidth - font.width(title))/2; titleLabelY = 6; inventoryLabelX = 8; }

    @Override public void render(GuiGraphics gg, int mx, int my, float delta) {
        renderBg(gg, delta, mx, my); super.render(gg, mx, my, delta); renderLabels(gg, mx, my); renderTooltip(gg, mx, my);
        int x = leftPos, y = topPos;
        if (mx >= x+151 && mx < x+169 && my >= y+17 && my < y+71)
            gg.renderTooltip(font, Component.literal("DF: "+menu.getDF()+"/"+menu.getDFCapacity()), mx, my);
    }

    @Override protected void renderLabels(GuiGraphics gg, int mx, int my) {
        gg.drawString(font, title, titleLabelX, titleLabelY, 0xCCCCCC, false);
        gg.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0x888888, false);
    }

    @Override protected void renderBg(GuiGraphics gg, float delta, int mx, int my) {
        int x = leftPos, y = topPos;
        ValenceGui.drawPanel(gg, x, y, imageWidth, imageHeight);

        TreeGrowthChamberTileEntity te = menu.getTileEntity();
        boolean active = te != null && te.getProgress() > 0;
        boolean hasSap = te != null && !te.getItemHandler().getStackInSlot(0).isEmpty();

        // DF gauge
        ValenceGui.drawGauge(gg, x+151, y+17, 18, 54,
            ValenceGui.DF_TOP, ValenceGui.DF_BOTTOM,
            menu.getDF(), menu.getDFCapacity());
        ValenceGui.drawGaugeLabel(gg, font, "DF", x+160, y+75, 0x888888);

        // Status
        ValenceGui.drawStatus(gg, x+8, y+8, active ? 1 : (hasSap ? 2 : 0));

        // Input slot
        ValenceGui.drawSlot(gg, x+54, y+33);
        ValenceGui.drawGaugeLabel(gg, font, "Sapling", x+63, y+55, 0x888888);

        // Animated arrow + progress
        ValenceGui.drawArrow(gg, x+72, y+36, 30, 8, menu.getProgress(), menu.getMaxProgress());

        // Outputs
        ValenceGui.drawSlot(gg, x+106, y+28);
        ValenceGui.drawSlot(gg, x+106, y+50);
        ValenceGui.drawGaugeLabel(gg, font, "Logs", x+115, y+46, 0x888888);
        ValenceGui.drawGaugeLabel(gg, font, "Leaves", x+115, y+68, 0x888888);

        for (int r = 0; r < 3; r++) for (int c = 0; c < 9; c++) ValenceGui.drawSlot(gg, x+7+c*18, y+106+r*18);
        for (int c = 0; c < 9; c++) ValenceGui.drawSlot(gg, x+7+c*18, y+164);
    }
}
