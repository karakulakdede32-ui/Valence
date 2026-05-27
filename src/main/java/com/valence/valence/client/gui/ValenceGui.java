package com.valence.valence.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.client.gui.Font;

/**
 * Unified clean UI helpers for all Valence machine GUIs.
 * Provides smooth gradients, clean panels, and polished slots.
 */
public class ValenceGui {

    // ── Clean dark panel palette ──
    public static final int DARKEST  = 0xFF1A1A1A;
    public static final int DARK     = 0xFF2A2A2A;
    public static final int MID      = 0xFF3A3A3A;
    public static final int LIGHT    = 0xFF4A4A4A;
    public static final int LIGHTEST = 0xFF5A5A5A;

    // ── Slot colors ──
    public static final int SLOT_OUTER = 0xFF1A1A1A;
    public static final int SLOT_INNER = 0xFF222222;
    public static final int SLOT_BG    = 0xFF2E2E2E;

    // ── Tank / gauge colors ──
    // Water / fluid
    public static final int FLUID_TOP    = 0xFF4080E0;
    public static final int FLUID_BOTTOM = 0xFF0F2A5A;
    // Steam (light blue-grey)
    public static final int STEAM_TOP    = 0xFFCCDDEE;
    public static final int STEAM_BOTTOM = 0xFF667788;
    // DF energy (gold)
    public static final int DF_TOP    = 0xFFFFD700;
    public static final int DF_BOTTOM = 0xFF8B5A00;
    // Progress bars
    public static final int PROG_LEFT   = 0xFFFF8800;
    public static final int PROG_RIGHT  = 0xFFFFDD44;
    // Fuel bar
    public static final int FUEL_TOP    = 0xFFFF6A00;
    public static final int FUEL_BOTTOM = 0xFF8B3A00;

    // ── Draw a clean machine panel with subtle bevel ──
    public static void drawPanel(GuiGraphics gg, int x, int y, int w, int h) {
        // Outer dark border
        gg.fill(x, y, x + w, y + h, DARKEST);
        // Thin bevel highlight (top & left)
        gg.fill(x + 1, y + 1, x + w - 1, y + 2, MID);
        gg.fill(x + 1, y + 2, x + 2, y + h - 1, MID);
        // Main background
        gg.fill(x + 2, y + 2, x + w - 2, y + h - 2, DARK);
        // Subtle inner shadow (bottom & right)
        gg.fill(x + 2, y + h - 2, x + w - 2, y + h - 1, LIGHT);
        gg.fill(x + w - 2, y + 2, x + w - 1, y + h - 2, LIGHT);
    }

    // ── Draw an inset slot ──
    public static void drawSlot(GuiGraphics gg, int x, int y) {
        gg.fill(x, y, x + 18, y + 18, SLOT_OUTER);
        gg.fill(x + 1, y + 1, x + 17, y + 17, SLOT_INNER);
        gg.fill(x + 2, y + 2, x + 16, y + 16, SLOT_BG);
    }

    // ── Draw a vertical gradient gauge (tank / energy bar) ──
    public static void drawGauge(GuiGraphics gg, int x, int y, int w, int h,
                                  int colorTop, int colorBottom,
                                  int fillAmount, int fillMax) {
        // Background
        gg.fill(x, y, x + w, y + h, DARKEST);
        gg.fill(x + 1, y + 1, x + w - 1, y + h - 1, 0xFF1A1A1A);

        if (fillMax <= 0 || fillAmount <= 0) return;

        int fillPx = Math.min(fillAmount * (h - 2) / fillMax, h - 2);
        if (fillPx <= 0) return;

        int gaugeY = y + h - 1 - fillPx;
        // Smooth vertical gradient from top color to bottom color
        gg.fillGradient(x + 2, gaugeY, x + w - 2, y + h - 1, colorTop, colorBottom);
    }

    // ── Draw a horizontal progress bar ──
    public static void drawProgressBar(GuiGraphics gg, int x, int y, int w, int h,
                                        int progress, int maxProgress) {
        gg.fill(x, y, x + w, y + h, 0xFF1A1A1A);
        gg.fill(x + 1, y + 1, x + w - 1, y + h - 1, 0xFF222222);

        if (maxProgress <= 0 || progress <= 0) return;

        int fillPx = Math.min(progress * (w - 2) / maxProgress, w - 2);
        if (fillPx <= 0) return;

        // Smooth gradient from left to right
        gg.fillGradient(x + 1, y + 1, x + 1 + fillPx, y + h - 1, PROG_LEFT, PROG_RIGHT);
    }

    // ── Draw a vertical fuel bar (like basic/advanced miner) ──
    public static void drawFuelBar(GuiGraphics gg, int x, int y, int w, int h,
                                    int fuel, int maxFuel) {
        gg.fill(x, y, x + w, y + h, 0xFF1A1A1A);
        gg.fill(x + 1, y + 1, x + w - 1, y + h - 1, 0xFF222222);

        if (maxFuel <= 0 || fuel <= 0) return;

        int fillPx = Math.min(fuel * (h - 2) / maxFuel, h - 2);
        if (fillPx <= 0) return;

        int gaugeY = y + h - 1 - fillPx;
        gg.fillGradient(x + 2, gaugeY, x + w - 2, y + h - 1, FUEL_TOP, FUEL_BOTTOM);
    }

    // ── Draw centered label ──
    public static void drawLabel(GuiGraphics gg, Font font, Component text, int cx, int y, int color) {
        gg.drawString(font, text, cx - font.width(text) / 2, y, color, false);
    }

    // ── Draw a small info label above/below a gauge ──
    public static void drawGaugeLabel(GuiGraphics gg, Font font, String text, int cx, int y, int color) {
        gg.drawString(font, Component.literal(text), cx - font.width(text) / 2, y, color, false);
    }
}
