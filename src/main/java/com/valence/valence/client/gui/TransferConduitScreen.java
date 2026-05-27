package com.valence.valence.client.gui;

import com.valence.valence.block.conduit.TransferConduitMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class TransferConduitScreen extends AbstractContainerScreen<TransferConduitMenu> {
    private static final int PANEL_DARK = 0xFF373737, PANEL_MID = 0xFF8B8B8B, PANEL_LIGHT = 0xFFC6C6C6;
    private static final int TANK_BG = 0xFF2A2A2A, TANK_BORDER = 0xFF555555;
    private static final int FLUID_DARK = 0xFF4040A0, FLUID_MID = 0xFF6060D0, FLUID_BRIGHT = 0xFF8888FF;
    private static final int DF_DARK = 0xFF8B6900, DF_MID = 0xFFD4A000, DF_BRIGHT = 0xFFFFD700;

    public TransferConduitScreen(TransferConduitMenu menu, Inventory inv, Component title) {
        super(menu, inv, title); this.imageWidth = 176; this.imageHeight = 184; this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override protected void init() { super.init(); titleLabelX = (imageWidth - font.width(title))/2; titleLabelY = 6; inventoryLabelX = 8; }

    @Override
    public void render(GuiGraphics gg, int mx, int my, float delta) {
        renderBg(gg, delta, mx, my); super.render(gg, mx, my, delta); renderLabels(gg, mx, my); renderTooltip(gg, mx, my);
        int x = leftPos, y = topPos;
        if (mx >= x+26 && mx < x+44 && my >= y+17 && my < y+71)
            gg.renderTooltip(font, Component.literal("Fluid: "+menu.getFluidAmount()+"/"+menu.getFluidCapacity()+" mB"), mx, my);
        if (mx >= x+132 && mx < x+150 && my >= y+17 && my < y+71)
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

        // Fluid tank (left)
        int fx = x+26, fy = y+17, fw = 18, fh = 54;
        gg.fill(fx, fy, fx+fw, fy+fh, TANK_BORDER); gg.fill(fx+1, fy+1, fx+fw-1, fy+fh-1, TANK_BG);
        int fH = menu.getFluidCapacity() > 0 ? (int)((long)menu.getFluidAmount()*(fh-2)/menu.getFluidCapacity()) : 0;
        if (fH > 0) { int fy2 = fy+fh-1-fH; for (int i=0; i<fH; i++) { float t = (float)i/fH; gg.fill(fx+2, fy2+i, fx+fw-2, fy2+i+1, t>0.7f?FLUID_BRIGHT:t>0.3f?FLUID_MID:FLUID_DARK); } }
        gg.drawString(font, Component.literal("Fluid"), x+22, y+75, 0x555555, false);

        // Center info: link counts
        String srcText = "Src: " + menu.getSourceCount();
        String dstText = "Dst: " + menu.getDestCount();
        gg.drawString(font, Component.literal(srcText), x+72, y+22, 0x444444, false);
        gg.drawString(font, Component.literal(dstText), x+72, y+36, 0x444444, false);
        gg.drawString(font, Component.literal("<=>"), x+78, y+50, 0x555555, false);

        // DF tank (right)
        int dx = x+132, dy = y+17, dw = 18, dh = 54;
        gg.fill(dx, dy, dx+dw, dy+dh, TANK_BORDER); gg.fill(dx+1, dy+1, dx+dw-1, dy+dh-1, TANK_BG);
        int dH = menu.getDFCapacity() > 0 ? (int)((long)menu.getDF()*(dh-2)/menu.getDFCapacity()) : 0;
        if (dH > 0) { int dy2 = dy+dh-1-dH; for (int i=0; i<dH; i++) { float t = (float)i/dH; gg.fill(dx+2, dy2+i, dx+dw-2, dy2+i+1, t>0.7f?DF_BRIGHT:t>0.3f?DF_MID:DF_DARK); } }
        gg.drawString(font, Component.literal("DF"), x+135, y+75, 0x555555, false);

        for (int r = 0; r < 3; r++) for (int c = 0; c < 9; c++)
            { int sx = x+7+c*18, sy = y+101+r*18; gg.fill(sx, sy, sx+18, sy+18, 0xFF555555); gg.fill(sx+1, sy+1, sx+17, sy+17, 0xFFD0D0D0); gg.fill(sx+2, sy+2, sx+16, sy+16, 0xFF6B6B6B); }
        for (int c = 0; c < 9; c++)
            { int sx = x+7+c*18, sy = y+159; gg.fill(sx, sy, sx+18, sy+18, 0xFF555555); gg.fill(sx+1, sy+1, sx+17, sy+17, 0xFFD0D0D0); gg.fill(sx+2, sy+2, sx+16, sy+16, 0xFF6B6B6B); }
    }
}
