package com.valence.valence.client.gui;

import com.valence.valence.block.alloyer.SteamAlloyerMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class SteamAlloyerScreen extends AbstractContainerScreen<SteamAlloyerMenu> {
    private static final int PANEL_DARK = 0xFF373737;
    private static final int PANEL_MID = 0xFF8B8B8B;
    private static final int PANEL_LIGHT = 0xFFC6C6C6;
    private static final int SLOT_BG = 0xFF6B6B6B;
    private static final int SLOT_BORDER_DARK = 0xFF555555;
    private static final int SLOT_BORDER_LIGHT = 0xFFD0D0D0;
    private static final int TANK_BG = 0xFF2A2A2A;
    private static final int TANK_BORDER = 0xFF555555;
    private static final int STEAM_DARK = 0xFFA0A0A0;
    private static final int STEAM_MID = 0xFFD0D0D0;
    private static final int STEAM_BRIGHT = 0xFFFFFFFF;
    private static final int PROGRESS_BG = 0xFF555555;
    private static final int PROGRESS_FILL = 0xFF80C080;

    public SteamAlloyerScreen(SteamAlloyerMenu menu, Inventory inv, Component title) {
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
        if (mx >= x + 151 && mx < x + 169 && my >= y + 17 && my < y + 71) {
            gg.renderTooltip(font, Component.literal("Steam: " + menu.getSteamAmount() + " / " + menu.getSteamCapacity() + " mB"), mx, my);
        }
    }

    @Override protected void renderLabels(GuiGraphics gg, int mx, int my) {
        gg.drawString(font, title, titleLabelX, titleLabelY, 4210752, false);
        gg.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 4210752, false);
    }

    @Override protected void renderBg(GuiGraphics gg, float delta, int mx, int my) {
        int x = leftPos, y = topPos;
        drawMachinePanel(gg, x, y, imageWidth, imageHeight);

        // Steam tank (right side)
        int tankX = x + 151, tankY = y + 17, tankW = 18, tankH = 54;
        gg.fill(tankX, tankY, tankX + tankW, tankY + tankH, TANK_BORDER);
        gg.fill(tankX + 1, tankY + 1, tankX + tankW - 1, tankY + tankH - 1, TANK_BG);
        int fillH = menu.getSteamCapacity() > 0 ? (int)((long)menu.getSteamAmount() * (tankH - 2) / menu.getSteamCapacity()) : 0;
        if (fillH > 0) {
            int fy = tankY + tankH - 1 - fillH;
            for (int i = 0; i < fillH; i++) {
                float t = (float)i / fillH;
                int c = t > 0.7f ? STEAM_BRIGHT : t > 0.3f ? STEAM_MID : STEAM_DARK;
                gg.fill(tankX + 2, fy + i, tankX + tankW - 2, fy + i + 1, c);
            }
        }
        gg.drawString(font, Component.literal("Steam"), x + 147, y + 75, 0x555555, false);

        // Input slots
        drawSlot(gg, x + 25, y + 34);
        drawSlot(gg, x + 43, y + 34);

        // Plus sign
        gg.drawString(font, Component.literal("+"), x + 67, y + 38, 0x555555, false);

        // Equals sign
        gg.drawString(font, Component.literal("="), x + 99, y + 38, 0x555555, false);

        // Output slot
        drawSlot(gg, x + 115, y + 34);

        // Progress bar
        int pbX = x + 25, pbY = y + 57, pbW = 106, pbH = 10;
        gg.fill(pbX, pbY, pbX + pbW, pbY + pbH, PROGRESS_BG);
        int fill = menu.getMaxProgress() > 0 ? menu.getProgress() * (pbW - 2) / menu.getMaxProgress() : 0;
        if (fill > 0) gg.fill(pbX + 1, pbY + 1, pbX + 1 + fill, pbY + pbH - 1, PROGRESS_FILL);

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
