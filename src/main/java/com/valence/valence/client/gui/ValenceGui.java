package com.valence.valence.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

/**
 * Unified clean UI helpers for all Valence machine GUIs.
 * Smooth gradients, animated arrows, status indicators.
 */
public class ValenceGui {

    // ── Panel colors ──
    public static final int DARKEST  = 0xFF1A1A1A;
    public static final int DARK     = 0xFF2A2A2A;
    public static final int MID      = 0xFF3A3A3A;
    public static final int LIGHT    = 0xFF4A4A4A;

    // ── Slot colors ──
    public static final int SLOT_OUTER = 0xFF1A1A1A;
    public static final int SLOT_INNER = 0xFF222222;
    public static final int SLOT_BG    = 0xFF2E2E2E;

    // ── Gauge colors ──
    public static final int FLUID_TOP    = 0xFF4080E0;
    public static final int FLUID_BOTTOM = 0xFF0F2A5A;
    public static final int STEAM_TOP    = 0xFFCCDDEE;
    public static final int STEAM_BOTTOM = 0xFF667788;
    public static final int DF_TOP    = 0xFFFFD700;
    public static final int DF_BOTTOM = 0xFF8B5A00;
    public static final int PROG_LEFT   = 0xFFFF8800;
    public static final int PROG_RIGHT  = 0xFFFFDD44;
    public static final int FUEL_TOP    = 0xFFFF6A00;
    public static final int FUEL_BOTTOM = 0xFF8B3A00;

    // ── Status indicator colors ──
    public static final int STATUS_ACTIVE   = 0xFF44DD44;
    public static final int STATUS_IDLE     = 0xFFDDDD44;
    public static final int STATUS_BLOCKED  = 0xFFDD4444;
    public static final int STATUS_OFF      = 0xFF444444;

    // ── Draw panel with bevel ──
    public static void drawPanel(GuiGraphics gg, int x, int y, int w, int h) {
        gg.fill(x, y, x + w, y + h, DARKEST);
        gg.fill(x + 1, y + 1, x + w - 1, y + 2, MID);
        gg.fill(x + 1, y + 2, x + 2, y + h - 1, MID);
        gg.fill(x + 2, y + 2, x + w - 2, y + h - 2, DARK);
        gg.fill(x + 2, y + h - 2, x + w - 2, y + h - 1, LIGHT);
        gg.fill(x + w - 2, y + 2, x + w - 1, y + h - 2, LIGHT);
    }

    // ── Draw slot ──
    public static void drawSlot(GuiGraphics gg, int x, int y) {
        gg.fill(x, y, x + 18, y + 18, SLOT_OUTER);
        gg.fill(x + 1, y + 1, x + 17, y + 17, SLOT_INNER);
        gg.fill(x + 2, y + 2, x + 16, y + 16, SLOT_BG);
    }

    // ── Vertical gradient gauge ──
    public static void drawGauge(GuiGraphics gg, int x, int y, int w, int h,
                                  int colorTop, int colorBottom,
                                  int fillAmount, int fillMax) {
        gg.fill(x, y, x + w, y + h, DARKEST);
        gg.fill(x + 1, y + 1, x + w - 1, y + h - 1, 0xFF1C1C1C);
        if (fillMax <= 0 || fillAmount <= 0) return;
        int fillPx = Math.min(fillAmount * (h - 2) / fillMax, h - 2);
        if (fillPx <= 0) return;
        int gaugeY = y + h - 1 - fillPx;
        gg.fillGradient(x + 2, gaugeY, x + w - 2, y + h - 1, colorTop, colorBottom);
    }

    // ── Horizontal progress bar ──
    public static void drawProgressBar(GuiGraphics gg, int x, int y, int w, int h,
                                        int progress, int maxProgress) {
        gg.fill(x, y, x + w, y + h, 0xFF1A1A1A);
        gg.fill(x + 1, y + 1, x + w - 1, y + h - 1, 0xFF222222);
        if (maxProgress <= 0 || progress <= 0) return;
        int fillPx = Math.min(progress * (w - 2) / maxProgress, w - 2);
        if (fillPx <= 0) return;
        gg.fillGradient(x + 1, y + 1, x + 1 + fillPx, y + h - 1, PROG_LEFT, PROG_RIGHT);
    }

    // ── Vertical fuel bar ──
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

    // ── Animated progress arrow (like vanilla furnace) ──
    // Draws an arrow that fills from left to right based on progress
    public static void drawArrow(GuiGraphics gg, int x, int y, int w, int h,
                                  int progress, int maxProgress) {
        gg.fill(x, y, x + w, y + h, 0xFF1A1A1A);
        gg.fill(x + 1, y + 1, x + w - 1, y + h - 1, 0xFF222222);
        if (maxProgress <= 0 || progress <= 0) return;
        int fillPx = Math.min(progress * (w - 2) / maxProgress, w - 2);
        if (fillPx <= 0) return;
        // Arrow shape: wider at right end
        gg.fillGradient(x + 1, y + 1, x + 1 + fillPx, y + h - 1, PROG_RIGHT, PROG_LEFT);
        // Arrow head (triangle at the front)
        int headX = x + 1 + fillPx;
        if (headX < x + w - 1) {
            gg.fillGradient(headX, y + 1, Math.min(headX + 2, x + w - 1), y + h - 1, 0xFFFFAA00, 0xFFFFDD44);
        }
    }

    // ── Status indicator (colored dot) ──
    // status: 0=off, 1=active, 2=idle, 3=blocked
    public static void drawStatus(GuiGraphics gg, int x, int y, int status) {
        int color = switch (status) {
            case 1 -> STATUS_ACTIVE;
            case 2 -> STATUS_IDLE;
            case 3 -> STATUS_BLOCKED;
            default -> STATUS_OFF;
        };
        gg.fill(x, y, x + 6, y + 6, 0xFF111111);
        gg.fill(x + 1, y + 1, x + 5, y + 5, color);
        // Subtle glow
        gg.fill(x, y, x + 6, y + 1, color);
    }

    // ── Pulsing gauge glow for active machines ──
    // Uses game time for smooth animation
    public static void drawActiveGlow(GuiGraphics gg, int x, int y, int w, int h,
                                       int gaugeFillPx, int color) {
        if (gaugeFillPx <= 0) return;
        long t = Minecraft.getInstance().level != null ? Minecraft.getInstance().level.getGameTime() : 0;
        float phase = (float) Math.sin(t * 0.05) * 0.3f + 0.7f;
        int alpha = Math.min(255, Math.max(40, (int)(phase * 80)));
        int glowColor = (alpha << 24) | (color & 0x00FFFFFF);
        int gaugeY = y + h - 1 - gaugeFillPx;
        gg.fill(x, gaugeY, x + w, y + h - 1, glowColor);
    }

    // ── Centered label ──
    public static void drawLabel(GuiGraphics gg, Font font, Component text, int cx, int y, int color) {
        gg.drawString(font, text, cx - font.width(text) / 2, y, color, false);
    }
    public static void drawGaugeLabel(GuiGraphics gg, Font font, String text, int cx, int y, int color) {
        gg.drawString(font, Component.literal(text), cx - font.width(text) / 2, y, color, false);
    }
}
