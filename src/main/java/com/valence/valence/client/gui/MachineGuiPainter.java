package com.valence.valence.client.gui;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

/**
 * Industrial cybernetic GUI painter with gradient backgrounds,
 * glowing accents, and polished 3D-style slot rendering.
 *
 * Color palette: dark charcoal (#0A0A0F) with cyan (#00BCD4)
 * and violet (#7C4DFF) tech accents.
 */
final class MachineGuiPainter {
    // ── Background & panel ──
    private static final int BG_DEEP       = 0xFF0A0A0F;
    private static final int BG_PANEL      = 0xFF13131C;
    private static final int BG_PANEL_LITE = 0xFF18182A;

    // ── Borders ──
    private static final int BORDER_OUTER   = 0xFF1E1E30;
    private static final int BORDER_INNER   = 0xFF282840;
    private static final int ACCENT         = 0xFF00BCD4;
    private static final int ACCENT_DIM     = 0xFF00838F;
    private static final int ACCENT_PURPLE  = 0xFF7C4DFF;

    // ── Slots ──
    private static final int SLOT_BORDER    = 0xFF2A2A44;
    private static final int SLOT_OUTER     = 0xFF1A1A2E;
    private static final int SLOT_INNER     = 0xFF10101C;
    private static final int SLOT_CENTER    = 0xFF14142A;
    private static final int SLOT_HIGHLIGHT = 0xFF2E2E4E;

    // ── Machine bay ──
    private static final int BAY_BORDER     = 0xFF1A1A30;
    private static final int BAY_BG         = 0xFF0E0E18;
    private static final int BAY_GRID       = 0x0800BCD4;

    // ── Progress arrow ──
    private static final int ARROW_BORDER   = 0xFF1E1E34;
    private static final int ARROW_BG       = 0xFF0D0D18;
    private static final int ARROW_FILL_A   = 0xFF00BCD4; // cyan
    private static final int ARROW_FILL_B   = 0xFF26C6DA;
    private static final int ARROW_GLOW     = 0x4400BCD4;
    private static final int ARROW_SHINE    = 0x66FFFFFF;

    // ── Text ──
    private static final int TXT_BRIGHT     = 0xFFD0D0DA;
    private static final int TXT_MUTED      = 0xFF707088;
    private static final int TXT_ACCENT     = 0xFF00BCD4;

    private MachineGuiPainter() {}

    // ═══════════════════════════════════════════════════════════
    //  PANEL – The main dialog background
    // ═══════════════════════════════════════════════════════════
    static void drawPanel(GuiGraphics g, int x, int y, int w, int h) {
        // Outer drop-shadow
        g.fill(x - 1, y - 1, x + w + 1, y,     0x66000000);
        g.fill(x - 1, y + h, x + w + 1, y + h + 1, 0x66000000);
        g.fill(x - 1, y, x, y + h,         0x66000000);
        g.fill(x + w, y, x + w + 1, y + h,     0x66000000);

        // Deep background
        g.fill(x, y, x + w, y + h, BG_DEEP);

        // Gradient illusion: layered panel fills
        g.fill(x + 1, y + 1, x + w - 1, y + h - 1, BG_PANEL);
        g.fill(x + 3, y + 14, x + w - 3, y + h - 3, BG_PANEL_LITE);

        // ── Top accent header bar ──
        g.fill(x + 1, y + 1, x + w - 1, y + 3,   ACCENT);
        g.fill(x + 1, y + 3, x + w - 1, y + 4,   ACCENT_DIM);

        // ── Corner glow tabs ──
        drawCornerGlow(g, x + 2, y + 2, true, true);
        drawCornerGlow(g, x + w - 10, y + 2, true, false);

        // ── Outer border ──
        g.fill(x, y, x + w, y + 1,               BORDER_OUTER);
        g.fill(x, y + h - 1, x + w, y + h,       BORDER_OUTER);
        g.fill(x, y, x + 1, y + h,               BORDER_OUTER);
        g.fill(x + w - 1, y, x + w, y + h,       BORDER_OUTER);

        // Inner glow border
        g.fill(x + 1, y + 1, x + 2, y + h - 1,  0x1800BCD4);
        g.fill(x + w - 2, y + 1, x + w - 1, y + h - 1, 0x1800BCD4);
    }

    private static void drawCornerGlow(GuiGraphics g, int x, int y, boolean left, boolean top) {
        int c = ACCENT;
        if (left) {
            g.fill(x, y, x + 6, y + 1, c);
            g.fill(x, y, x + 1, y + 6, c);
            g.fill(x + 1, y + 1, x + 4, y + 2, ACCENT_DIM);
            g.fill(x + 1, y + 1, x + 2, y + 4, ACCENT_DIM);
        } else {
            g.fill(x + 4, y, x + 10, y + 1, c);
            g.fill(x + 9, y, x + 10, y + 6, c);
            g.fill(x + 5, y + 1, x + 8, y + 2, ACCENT_DIM);
            g.fill(x + 8, y + 1, x + 9, y + 4, ACCENT_DIM);
        }
    }

    // ═══════════════════════════════════════════════════════════
    //  MACHINE BAY – recessed area where machine guts live
    // ═══════════════════════════════════════════════════════════
    static void drawMachineBay(GuiGraphics g, int x, int y, int w, int h) {
        // Outer recess shadow
        g.fill(x - 1, y - 1, x + w + 1, y,     0x44000000);
        g.fill(x - 1, y + h, x + w + 1, y + h + 1, 0x22000000);
        g.fill(x - 1, y, x, y + h,         0x44000000);

        // Bay background
        g.fill(x, y, x + w, y + h, BAY_BG);
        g.fill(x + 1, y + 1, x + w - 1, y + h - 1, 0xFF0C0C18);

        // Bay border (inset)
        g.fill(x, y, x + w, y + 1,               BAY_BORDER);
        g.fill(x, y + h - 1, x + w, y + h,       BAY_BORDER);
        g.fill(x, y, x + 1, y + h,               BAY_BORDER);
        g.fill(x + w - 1, y, x + w, y + h,       BAY_BORDER);

        // Subtle grid pattern
        for (int i = 0; i < h; i += 6) {
            g.fill(x + 3, y + 3 + i, x + w - 3, y + 4 + i, BAY_GRID);
        }

        // Inner glow at top
        g.fill(x + 2, y + 1, x + w - 2, y + 3, 0x0A00BCD4);
    }

    // ═══════════════════════════════════════════════════════════
    //  SLOT – polished recessed item slot
    // ═══════════════════════════════════════════════════════════
    static void drawSlot(GuiGraphics g, int x, int y) {
        // Outer shadow
        g.fill(x - 1, y - 1, x + 19, y,      0x44000000);
        g.fill(x - 1, y + 18, x + 19, y + 19,  0x22000000);
        g.fill(x - 1, y, x, y + 18,        0x44000000);

        // Slot border (dark)
        g.fill(x, y, x + 18, y + 1,               SLOT_BORDER);
        g.fill(x, y + 17, x + 18, y + 18,         SLOT_BORDER);
        g.fill(x, y, x + 1, y + 18,               SLOT_BORDER);
        g.fill(x + 17, y, x + 18, y + 18,         SLOT_BORDER);

        // Outer ring
        g.fill(x + 1, y + 1, x + 17, y + 17,      SLOT_OUTER);

        // Inner shadow – darker corners
        g.fill(x + 2, y + 2, x + 16, y + 16,      SLOT_INNER);

        // Center highlight
        g.fill(x + 3, y + 3, x + 15, y + 15,      SLOT_CENTER);

        // Top-left highlight for depth illusion
        g.fill(x + 2, y + 2, x + 14, y + 3,       SLOT_HIGHLIGHT);
        g.fill(x + 2, y + 2, x + 3, y + 14,       SLOT_HIGHLIGHT);

        // Cyan accent dot in corners
        g.fill(x + 2, y + 2, x + 3, y + 3, 0x4400BCD4);
        g.fill(x + 15, y + 2, x + 16, y + 3, 0x4400BCD4);
    }

    // ═══════════════════════════════════════════════════════════
    //  SLOT GRID
    // ═══════════════════════════════════════════════════════════
    static void drawSlotGrid(GuiGraphics g, int x, int y, int columns, int rows) {
        for (int row = 0; row < rows; row++)
            for (int col = 0; col < columns; col++)
                drawSlot(g, x + col * 18, y + row * 18);
    }

    // ═══════════════════════════════════════════════════════════
    //  PLAYER INVENTORY
    // ═══════════════════════════════════════════════════════════
    static void drawPlayerInventory(GuiGraphics g, int x, int y) {
        // Divider line with glow
        g.fill(x, y - 2, x + 162, y - 1, 0xFF1A1A2E);
        g.fill(x, y - 1, x + 162, y,     0x1800BCD4);

        drawSlotGrid(g, x, y, 9, 3);
        drawSlotGrid(g, x, y + 58, 9, 1);
    }

    // ═══════════════════════════════════════════════════════════
    //  PROGRESS ARROW – animated gradient fill
    // ═══════════════════════════════════════════════════════════
    static void drawProgressArrow(GuiGraphics g, int x, int y, float progress) {
        final int W = 36;
        final int H = 18;
        int fill = Math.max(0, Math.min(W, Math.round(W * progress)));

        // Low border
        g.fill(x, y + 5, x + W, y + 6, ARROW_BORDER);
        g.fill(x, y + 11, x + W, y + 12, ARROW_BORDER);

        // Arrow shaft background
        g.fill(x, y + 6, x + W - 9, y + 11, ARROW_BG);

        // Arrow head background
        g.fill(x + W - 13, y + 3, x + W - 9, y + 14, ARROW_BG);
        g.fill(x + W - 9, y + 5, x + W - 5, y + 12, ARROW_BG);
        g.fill(x + W - 5, y + 6, x + W - 2, y + 11, ARROW_BG);

        // Outer border for head
        g.fill(x + W - 12, y + 2, x + W - 9, y + 3, ARROW_BORDER);
        g.fill(x + W - 9, y + 4, x + W - 5, y + 5, ARROW_BORDER);
        g.fill(x + W - 5, y + 5, x + W - 2, y + 6, ARROW_BORDER);
        g.fill(x + W - 2, y + 6, x + W - 1, y + 11, ARROW_BORDER);
        g.fill(x + W - 5, y + 12, x + W - 2, y + 13, ARROW_BORDER);
        g.fill(x + W - 9, y + 13, x + W - 5, y + 14, ARROW_BORDER);
        g.fill(x + W - 12, y + 14, x + W - 9, y + 15, ARROW_BORDER);

        if (fill > 0) {
            // Filled arrow shaft
            drawFilledRect(g, x + 1, y + 7, x + 27, y + 10, x, fill, ARROW_FILL_A, ARROW_FILL_B);

            // Filled arrow head
            fillArrowHead(g, x, y, fill, true);

            // Glow at fill edge
            int glowX = x + fill - 1;
            if (fill < W - 1) {
                g.fill(glowX, y + 6, glowX + 2, y + 11, ARROW_GLOW);
            }

            // Shine line (top highlight)
            drawFilledRect(g, x + 1, y + 7, x + 27, y + 8, x, fill, ARROW_SHINE, ARROW_SHINE);
        }

        // Dark centerline for depth
        g.fill(x + 2, y + 8, x + W - 10, y + 9, 0x22000000);
    }

    private static void fillArrowHead(GuiGraphics g, int x, int y, int fill, boolean first) {
        // Arrow head starts at x + 27 in shaft space
        int headStart = 27;
        if (fill > headStart) {
            drawFilledRect(g, x + 23, y + 3, x + 29, y + 14, x, fill, ARROW_FILL_A, ARROW_FILL_B);
            drawFilledRect(g, x + 29, y + 5, x + 33, y + 12, x, fill, ARROW_FILL_A, ARROW_FILL_B);
            drawFilledRect(g, x + 33, y + 7, x + 36, y + 10, x, fill, ARROW_FILL_A, ARROW_FILL_B);
        }
    }

    private static void drawFilledRect(GuiGraphics g, int left, int top, int right, int bottom,
                                        int clipX, int fill, int colorA, int colorB) {
        int clippedRight = Math.min(right, clipX + fill);
        if (clippedRight > left) {
            // Simple gradient illusion: blend from colorA to colorB
            int mid = (left + clippedRight) / 2;
            g.fill(left, top, mid, bottom, colorA);
            g.fill(mid, top, clippedRight, bottom, colorB);
        }
    }

    // ═══════════════════════════════════════════════════════════
    //  FUEL GAUGE – optional vertical bar
    // ═══════════════════════════════════════════════════════════
    static void drawFuelGauge(GuiGraphics g, int x, int y, int height, float fuelPct) {
        int fill = Math.round(height * Math.min(1.0f, Math.max(0, fuelPct)));

        // Background
        g.fill(x, y, x + 6, y + height, 0xFF0D0D18);
        g.fill(x + 1, y + 1, x + 5, y + height - 1, 0xFF10101C);

        // Border
        g.fill(x, y, x + 6, y + 1, 0xFF1E1E30);
        g.fill(x, y + height - 1, x + 6, y + height, 0xFF1E1E30);
        g.fill(x, y, x + 1, y + height, 0xFF1E1E30);
        g.fill(x + 5, y, x + 6, y + height, 0xFF1E1E30);

        // Fill (gradient: orange at bottom → cyan at top)
        if (fill > 0) {
            int filledTop = y + height - fill;
            g.fill(x + 1, filledTop, x + 5, y + height - 1, 0xFF00BCD4);
            g.fill(x + 2, filledTop, x + 4, filledTop + 2, 0xFF26C6DA);
        }
    }

    // ═══════════════════════════════════════════════════════════
    //  TITLE – draws a centered glowing title
    // ═══════════════════════════════════════════════════════════
    static void drawTitle(GuiGraphics g, Font font, String title, int x, int y, int panelWidth) {
        int fontWidth = font.width(title);
        int titleX = x + (panelWidth - fontWidth) / 2;
        int titleY = y + 6;

        // Glow shadow
        g.drawString(font, title, titleX + 1, titleY + 1, 0x4400BCD4, false);
        // Main text
        g.drawString(font, title, titleX, titleY, TXT_ACCENT, false);

        // Underline accent
        int underlineW = Math.min(fontWidth + 12, panelWidth - 40);
        int underlineX = x + (panelWidth - underlineW) / 2;
        g.fill(underlineX, titleY + 10, underlineX + underlineW, titleY + 11, 0x1800BCD4);
        g.fill(underlineX + underlineW / 2 - 8, titleY + 11, underlineX + underlineW / 2 + 8, titleY + 12, ACCENT);
    }
}
