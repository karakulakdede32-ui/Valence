package com.valence.valence.client.gui;

import com.valence.valence.block.dfcell.DFCellMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class DFCellScreen extends AbstractContainerScreen<DFCellMenu> {
    private static final int PANEL_DARK = 0xFF373737, PANEL_MID = 0xFF8B8B8B, PANEL_LIGHT = 0xFFC6C6C6;
    private static final int TANK_BG = 0xFF2A2A2A, TANK_BORDER = 0xFF555555;
    private static final int DF_DARK = 0xFF8B6900, DF_MID = 0xFFD4A000, DF_BRIGHT = 0xFFFFD700;

    public DFCellScreen(DFCellMenu menu, Inventory inv, Component title) {
        super(menu, inv, title); this.imageWidth = 176; this.imageHeight = 184; this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override protected void init() { super.init(); titleLabelX = (imageWidth - font.width(title))/2; titleLabelY = 6; inventoryLabelX = 8; }

    @Override
    public void render(GuiGraphics gg, int mx, int my, float delta) {
        renderBg(gg, delta, mx, my); super.render(gg, mx, my, delta); renderLabels(gg, mx, my); renderTooltip(gg, mx, my);
        int x = leftPos, y = topPos;
        if (mx >= x+79 && mx < x+97 && my >= y+17 && my < y+71)
            gg.renderTooltip(font, Component.literal("DF: "+menu.getDF()+"/"+menu.getDFCapacity()), mx, my);
    }

    @Override protected void renderLabels(GuiGraphics gg, int mx, int my) {
        gg.drawString(font, title, titleLabelX, titleLabelY, 4210752, false);
        gg.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 4210752, false);
    }

    @Override protected void renderBg(GuiGraphics gg, float delta, int mx, int my) {
        int x = leftPos, y = topPos;
        gg.fill(x, y, x+imageWidth, y+imageHeight, PANEL_DARK);
        gg.fill(x+1, y+1, x+imageWidth-1, y+imageHeight-1, PANEL_MID);
        gg.fill(x+2, y+2, x+imageWidth-2, y+imageHeight-2, PANEL_LIGHT);

        // Large DF gauge in center
        int tx = x+79, ty = y+17, tw = 18, th = 54;
        gg.fill(tx, ty, tx+tw, ty+th, TANK_BORDER); gg.fill(tx+1, ty+1, tx+tw-1, ty+th-1, TANK_BG);
        int fh = menu.getDFCapacity() > 0 ? (int)((long)menu.getDF()*(th-2)/menu.getDFCapacity()) : 0;
        if (fh > 0) { int fy = ty+th-1-fh; for (int i = 0; i < fh; i++) { float t = (float)i/fh; gg.fill(tx+2, fy+i, tx+tw-2, fy+i+1, t>0.7f?DF_BRIGHT:t>0.3f?DF_MID:DF_DARK); } }
        gg.drawString(font, Component.literal("DF"), x+82, y+75, 0x555555, false);
        String info = menu.getDF()+"/"+menu.getDFCapacity()+" DF";
        gg.drawString(font, Component.literal(info), x+88-font.width(info)/2, y+86, 0x555555, false);

        for (int r = 0; r < 3; r++) for (int c = 0; c < 9; c++)
            gg.fill(x+7+c*18, y+101+r*18, x+25+c*18, y+119+r*18, 0xFF555555);
        for (int c = 0; c < 9; c++)
            gg.fill(x+7+c*18, y+159, x+25+c*18, y+177, 0xFF555555);
    }
}
