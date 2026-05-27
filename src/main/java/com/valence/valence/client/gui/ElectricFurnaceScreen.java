package com.valence.valence.client.gui;

import com.valence.valence.block.efurnace.ElectricFurnaceMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class ElectricFurnaceScreen extends AbstractContainerScreen<ElectricFurnaceMenu> {
    private static final int PANEL_DARK = 0xFF373737, PANEL_MID = 0xFF8B8B8B, PANEL_LIGHT = 0xFFC6C6C6;
    private static final int SLOT_BG = 0xFF6B6B6B, SLOT_BORDER = 0xFF555555, SLOT_HI = 0xFFD0D0D0;
    private static final int TANK_BG = 0xFF2A2A2A, TANK_BORDER = 0xFF555555;
    private static final int DF_DARK = 0xFF8B6900, DF_MID = 0xFFD4A000, DF_BRIGHT = 0xFFFFD700;
    private static final int PROGRESS_BG = 0xFF444444, PROGRESS_FILL = 0xFFFFAA00;

    public ElectricFurnaceScreen(ElectricFurnaceMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = 176;
        this.imageHeight = 184;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override protected void init() {
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

        int x = leftPos, y = topPos;
        if (mx >= x + 151 && mx < x + 169 && my >= y + 17 && my < y + 71)
            gg.renderTooltip(font, Component.literal("DF: " + menu.getDF() + " / " + menu.getDFCapacity()), mx, my);
    }

    @Override protected void renderLabels(GuiGraphics gg, int mx, int my) {
        gg.drawString(font, title, titleLabelX, titleLabelY, 4210752, false);
        gg.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 4210752, false);
    }

    @Override protected void renderBg(GuiGraphics gg, float delta, int mx, int my) {
        int x = leftPos, y = topPos;
        drawPanel(gg, x, y, imageWidth, imageHeight);

        // DF gauge (right)
        int tx = x + 151, ty = y + 17, tw = 18, th = 54;
        gg.fill(tx, ty, tx + tw, ty + th, TANK_BORDER);
        gg.fill(tx + 1, ty + 1, tx + tw - 1, ty + th - 1, TANK_BG);
        int fh = menu.getDFCapacity() > 0 ? (int)((long)menu.getDF() * (th - 2) / menu.getDFCapacity()) : 0;
        if (fh > 0) {
            int fy = ty + th - 1 - fh;
            for (int i = 0; i < fh; i++) {
                float t = (float) i / fh;
                int c = t > 0.7f ? DF_BRIGHT : t > 0.3f ? DF_MID : DF_DARK;
                gg.fill(tx + 2, fy + i, tx + tw - 2, fy + i + 1, c);
            }
        }
        gg.drawString(font, Component.literal("DF"), x + 147, y + 75, 0x555555, false);

        // 8 input slots (top row)
        for (int col = 0; col < 8; col++) {
            drawSlot(gg, x + 7 + col * 18, y + 16);
        }

        // Progress bars under each input slot
        for (int col = 0; col < 8; col++) {
            int px = x + 7 + col * 18, py = y + 37;
            int pw = 16, ph = 4;
            gg.fill(px, py, px + pw, py + ph, PROGRESS_BG);
            int prog = menu.getProgress(col);
            int maxProg = menu.getMaxProgress();
            if (maxProg > 0 && prog > 0) {
                int fill = prog * pw / maxProg;
                if (fill > 0) gg.fill(px, py, px + fill, py + ph, PROGRESS_FILL);
            }
        }

        // Arrow from input to output
        String label = "5 DF/t";
        gg.drawString(font, Component.literal(label), x + 8, y + 44, 0xFFAA8800, false);

        // 8 output slots (bottom row)
        for (int col = 0; col < 8; col++) {
            drawSlot(gg, x + 7 + col * 18, y + 61);
        }

        // Player inventory
        for (int row = 0; row < 3; row++)
            for (int col = 0; col < 9; col++)
                drawSlot(gg, x + 7 + col * 18, y + 101 + row * 18);
        for (int col = 0; col < 9; col++)
            drawSlot(gg, x + 7 + col * 18, y + 159);
    }

    private static void drawPanel(GuiGraphics gg, int x, int y, int w, int h) {
        gg.fill(x, y, x + w, y + h, PANEL_DARK);
        gg.fill(x + 1, y + 1, x + w - 1, y + h - 1, PANEL_MID);
        gg.fill(x + 2, y + 2, x + w - 2, y + h - 2, PANEL_LIGHT);
    }

    private static void drawSlot(GuiGraphics gg, int x, int y) {
        gg.fill(x, y, x + 18, y + 18, SLOT_BORDER);
        gg.fill(x + 1, y + 1, x + 17, y + 17, SLOT_HI);
        gg.fill(x + 2, y + 2, x + 16, y + 16, SLOT_BG);
    }
}
