package com.valence.valence.client.gui;

import com.valence.valence.block.turbine.SteamTurbineMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class SteamTurbineScreen extends AbstractContainerScreen<SteamTurbineMenu> {
    private static final int PANEL_DARK = 0xFF373737, PANEL_MID = 0xFF8B8B8B, PANEL_LIGHT = 0xFFC6C6C6;
    private static final int TANK_BG = 0xFF2A2A2A, TANK_BORDER = 0xFF555555;
    private static final int STEAM_DARK = 0xFFA0A0A0, STEAM_MID = 0xFFD0D0D0, STEAM_BRIGHT = 0xFFFFFFFF;
    private static final int DF_COLOR = 0xFFFFD700; // gold

    public SteamTurbineScreen(SteamTurbineMenu menu, Inventory inv, Component title) {
        super(menu, inv, title); this.imageWidth = 176; this.imageHeight = 184; this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override protected void init() { super.init(); titleLabelX = (imageWidth - font.width(title)) / 2; titleLabelY = 6; inventoryLabelX = 8; }

    @Override
    public void render(GuiGraphics gg, int mx, int my, float delta) {
        renderBg(gg, delta, mx, my); super.render(gg, mx, my, delta);
        renderLabels(gg, mx, my); renderTooltip(gg, mx, my);
        int x = leftPos, y = topPos;
        if (mx >= x+26 && mx < x+44 && my >= y+17 && my < y+71) gg.renderTooltip(font, Component.literal("Steam: "+menu.getSteamAmount()+"/"+menu.getSteamCapacity()+" mB"), mx, my);
        if (mx >= x+132 && mx < x+150 && my >= y+17 && my < y+71) gg.renderTooltip(font, Component.literal("DF: "+menu.getDF()+"/"+menu.getDFCapacity()), mx, my);
    }

    @Override protected void renderLabels(GuiGraphics gg, int mx, int my) {
        gg.drawString(font, title, titleLabelX, titleLabelY, 4210752, false);
        gg.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 4210752, false);
    }

    @Override protected void renderBg(GuiGraphics gg, float delta, int mx, int my) {
        int x = leftPos, y = topPos;
        drawPanel(gg, x, y, imageWidth, imageHeight);

        // Steam tank (left)
        drawTank(gg, x+26, y+17, 18, 54, menu.getSteamAmount(), menu.getSteamCapacity(), STEAM_DARK, STEAM_MID, STEAM_BRIGHT);
        gg.drawString(font, Component.literal("Steam"), x+22, y+75, 0x555555, false);

        // Arrow
        int ax = x+76, ay = y+37, aw = 24, ah = 15;
        gg.fill(ax, ay, ax+aw, ay+ah, 0xFF555555);
        gg.fill(ax+1, ay+1, ax+aw-1, ay+ah-1, 0xFF8B8B8B);
        // Arrow head
        gg.fill(ax+aw, ay+3, ax+aw+4, ay+ah-3, 0xFF555555);
        gg.fill(ax+aw+4, ay+5, ax+aw+6, ay+ah-5, 0xFF555555);
        // Label
        gg.drawString(font, Component.literal("5 DF/t"), x+78, y+56, 0xFFAA8800, false);

        // DF tank (right) - gold bar
        drawTank(gg, x+132, y+17, 18, 54, menu.getDF(), menu.getDFCapacity(), 0xFF8B6900, 0xFFD4A000, DF_COLOR);
        gg.drawString(font, Component.literal("DF"), x+135, y+75, 0x555555, false);

        for (int r = 0; r < 3; r++) for (int c = 0; c < 9; c++) drawSlot(gg, x+7+c*18, y+101+r*18);
        for (int c = 0; c < 9; c++) drawSlot(gg, x+7+c*18, y+159);
    }

    private static void drawTank(GuiGraphics gg, int x, int y, int w, int h, int amt, int cap, int dk, int md, int br) {
        gg.fill(x, y, x+w, y+h, TANK_BORDER); gg.fill(x+1, y+1, x+w-1, y+h-1, TANK_BG);
        int fh = cap > 0 ? (int)((long)amt * (h-2) / cap) : 0;
        if (fh > 0) { int fy = y + h - 1 - fh; for (int i = 0; i < fh; i++) { float t = (float)i/fh; gg.fill(x+2, fy+i, x+w-2, fy+i+1, t>0.7f?br:t>0.3f?md:dk); } }
    }

    private static void drawPanel(GuiGraphics gg, int x, int y, int w, int h) {
        gg.fill(x, y, x+w, y+h, PANEL_DARK); gg.fill(x+1, y+1, x+w-1, y+h-1, PANEL_MID); gg.fill(x+2, y+2, x+w-2, y+h-2, PANEL_LIGHT);
    }

    private static void drawSlot(GuiGraphics gg, int x, int y) {
        gg.fill(x, y, x+18, y+18, 0xFF555555); gg.fill(x+1, y+1, x+17, y+17, 0xFFD0D0D0); gg.fill(x+2, y+2, x+16, y+16, 0xFF6B6B6B);
    }
}
