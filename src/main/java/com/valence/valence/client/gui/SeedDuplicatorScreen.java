package com.valence.valence.client.gui;

import com.valence.valence.block.seeder.SeedDuplicatorMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class SeedDuplicatorScreen extends AbstractContainerScreen<SeedDuplicatorMenu> {
    private static final int PANEL_DARK = 0xFF373737, PANEL_MID = 0xFF8B8B8B, PANEL_LIGHT = 0xFFC6C6C6;
    private static final int SLOT_BG = 0xFF6B6B6B, SLOT_BORDER_DARK = 0xFF555555, SLOT_BORDER_LIGHT = 0xFFD0D0D0;
    private static final int TANK_BG = 0xFF2A2A2A, TANK_BORDER = 0xFF555555;
    private static final int DF_DARK = 0xFF8B6900, DF_MID = 0xFFD4A000, DF_BRIGHT = 0xFFFFD700;

    public SeedDuplicatorScreen(SeedDuplicatorMenu menu, Inventory inv, Component title) {
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
        if (mx >= x + 79 && mx < x + 97 && my >= y + 17 && my < y + 71)
            gg.renderTooltip(font, Component.literal("DF: " + menu.getDF() + " / " + menu.getDFCapacity()), mx, my);
    }

    @Override protected void renderLabels(GuiGraphics gg, int mx, int my) {
        gg.drawString(font, title, titleLabelX, titleLabelY, 4210752, false);
        gg.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 4210752, false);
    }

    @Override protected void renderBg(GuiGraphics gg, float delta, int mx, int my) {
        int x = leftPos, y = topPos;
        drawMachinePanel(gg, x, y, imageWidth, imageHeight);

        // DF gauge (center)
        int tx = x + 79, ty = y + 17, tw = 18, th = 54;
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
        gg.drawString(font, Component.literal("DF"), x + 82, y + 75, 0x555555, false);

        // Info text
        String info = menu.getDF() + "/" + menu.getDFCapacity() + " DF";
        gg.drawString(font, Component.literal(info), x + 88 - font.width(info) / 2, y + 86, 0x555555, false);

        // Input slot (seed in)
        drawSlot(gg, x + 55, y + 34);

        // Arrow
        int arrowX = x + 83, arrowY = y + 37, arrowW = 22, arrowH = 15;
        gg.fill(arrowX, arrowY, arrowX + arrowW, arrowY + arrowH, 0xFF555555);
        gg.fill(arrowX + arrowW, arrowY + 3, arrowX + arrowW + 4, arrowY + arrowH - 3, 0xFF555555);
        gg.fill(arrowX + arrowW + 4, arrowY + 5, arrowX + arrowW + 6, arrowY + arrowH - 5, 0xFF555555);
        gg.drawString(font, Component.literal("50 DF"), x + 78, y + 56, 0xFFAA8800, false);

        // Output slot
        drawSlot(gg, x + 115, y + 34);

        // Player inventory
        for (int row = 0; row < 3; row++)
            for (int col = 0; col < 9; col++)
                drawSlot(gg, x + 7 + col * 18, y + 101 + row * 18);
        for (int col = 0; col < 9; col++)
            drawSlot(gg, x + 7 + col * 18, y + 159);
    }

    private static void drawMachinePanel(GuiGraphics gg, int x, int y, int w, int h) {
        gg.fill(x, y, x + w, y + h, PANEL_DARK);
        gg.fill(x + 1, y + 1, x + w - 1, y + h - 1, PANEL_MID);
        gg.fill(x + 2, y + 2, x + w - 2, y + h - 2, PANEL_LIGHT);
    }

    private static void drawSlot(GuiGraphics gg, int x, int y) {
        gg.fill(x, y, x + 18, y + 18, SLOT_BORDER_DARK);
        gg.fill(x + 1, y + 1, x + 17, y + 17, SLOT_BORDER_LIGHT);
        gg.fill(x + 2, y + 2, x + 16, y + 16, SLOT_BG);
    }
}
