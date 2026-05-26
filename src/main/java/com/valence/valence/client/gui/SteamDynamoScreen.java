package com.valence.valence.client.gui;

import com.valence.valence.block.dynamo.SteamDynamoMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class SteamDynamoScreen extends AbstractContainerScreen<SteamDynamoMenu> {
    private static final int PANEL_DARK = 0xFF373737;
    private static final int PANEL_MID = 0xFF8B8B8B;
    private static final int PANEL_LIGHT = 0xFFC6C6C6;
    private static final int SLOT_BG = 0xFF6B6B6B;
    private static final int SLOT_BORDER_DARK = 0xFF555555;
    private static final int SLOT_BORDER_LIGHT = 0xFFD0D0D0;
    private static final int TANK_BG = 0xFF2A2A2A;
    private static final int TANK_BORDER = 0xFF555555;

    // Water colors
    private static final int WATER_DARK = 0xFF1A4FA0;
    private static final int WATER_MID = 0xFF2060C0;
    private static final int WATER_BRIGHT = 0xFF4080E0;
    // Steam colors (light gray/white)
    private static final int STEAM_DARK = 0xFFA0A0A0;
    private static final int STEAM_MID = 0xFFD0D0D0;
    private static final int STEAM_BRIGHT = 0xFFFFFFFF;

    public SteamDynamoScreen(SteamDynamoMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 176;
        this.imageHeight = 190;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = (imageWidth - font.width(title)) / 2;
        this.titleLabelY = 6;
        this.inventoryLabelX = 8;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        this.renderBg(guiGraphics, delta, mouseX, mouseY);
        super.render(guiGraphics, mouseX, mouseY, delta);
        this.renderLabels(guiGraphics, mouseX, mouseY);
        this.renderTooltip(guiGraphics, mouseX, mouseY);

        // Hover tooltips
        int x = this.leftPos, y = this.topPos;

        // Water tank tooltip
        int wtX = x + 26, wtY = y + 17, wtW = 18, wtH = 54;
        if (mouseX >= wtX && mouseX < wtX + wtW && mouseY >= wtY && mouseY < wtY + wtH) {
            guiGraphics.renderTooltip(this.font,
                Component.literal("Water: " + menu.getWaterAmount() + " / " + menu.getWaterCapacity() + " mB"),
                mouseX, mouseY);
        }

        // Steam tank tooltip
        int stX = x + 132, stY = y + 17, stW = 18, stH = 54;
        if (mouseX >= stX && mouseX < stX + stW && mouseY >= stY && mouseY < stY + stH) {
            guiGraphics.renderTooltip(this.font,
                Component.literal("Steam: " + menu.getSteamAmount() + " / " + menu.getSteamCapacity() + " mB"),
                mouseX, mouseY);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 4210752, false);
        guiGraphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 4210752, false);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float delta, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;

        drawMachinePanel(guiGraphics, x, y, imageWidth, imageHeight);

        // Labels
        guiGraphics.drawString(this.font, Component.literal("Water In"), x + 22, y + 75, 0x555555, false);
        guiGraphics.drawString(this.font, Component.literal("Steam Out"), x + 126, y + 75, 0x555555, false);

        // Arrow in the middle showing conversion
        drawArrow(guiGraphics, x + 76, y + 38);

        // Water tank (left)
        drawFluidTank(guiGraphics, x + 26, y + 17, 18, 54,
            menu.getWaterAmount(), menu.getWaterCapacity(),
            WATER_DARK, WATER_MID, WATER_BRIGHT);

        // Steam tank (right)
        drawFluidTank(guiGraphics, x + 132, y + 17, 18, 54,
            menu.getSteamAmount(), menu.getSteamCapacity(),
            STEAM_DARK, STEAM_MID, STEAM_BRIGHT);

        // Player inventory
        drawPlayerInventory(guiGraphics, x + 7, y + 107);
    }

    private static void drawFluidTank(GuiGraphics gg, int x, int y, int w, int h,
                                       int amount, int capacity, int dark, int mid, int bright) {
        gg.fill(x, y, x + w, y + h, TANK_BORDER);
        gg.fill(x + 1, y + 1, x + w - 1, y + h - 1, TANK_BG);

        int fillHeight = capacity > 0 ? (int) ((long) amount * (h - 2) / capacity) : 0;
        if (fillHeight > 0) {
            int fluidY = y + h - 1 - fillHeight;
            for (int i = 0; i < fillHeight; i++) {
                int color = dark;
                float t = (float) i / fillHeight;
                if (t > 0.7f) color = bright;
                else if (t > 0.3f) color = mid;
                gg.fill(x + 2, fluidY + i, x + w - 2, fluidY + i + 1, color);
            }
        }
    }

    private static void drawArrow(GuiGraphics gg, int x, int y) {
        int arrowW = 22, arrowH = 15;
        gg.fill(x, y, x + arrowW, y + arrowH, 0xFF555555);
        gg.fill(x + 1, y + 1, x + arrowW - 1, y + arrowH - 1, 0xFFC6C6C6);
        gg.fill(x + 2, y + 2, x + arrowW - 2, y + arrowH - 2, 0xFF8B8B8B);
        // Arrow head
        gg.fill(x + arrowW, y + 3, x + arrowW + 4, y + arrowH - 3, 0xFF555555);
        gg.fill(x + arrowW + 4, y + 5, x + arrowW + 6, y + arrowH - 5, 0xFF555555);
    }

    private static void drawMachinePanel(GuiGraphics gg, int x, int y, int width, int height) {
        gg.fill(x, y, x + width, y + height, PANEL_DARK);
        gg.fill(x + 1, y + 1, x + width - 1, y + height - 1, PANEL_MID);
        gg.fill(x + 2, y + 2, x + width - 2, y + height - 2, PANEL_LIGHT);
    }

    private static void drawSlot(GuiGraphics gg, int x, int y) {
        gg.fill(x, y, x + 18, y + 18, SLOT_BORDER_DARK);
        gg.fill(x + 1, y + 1, x + 17, y + 17, SLOT_BORDER_LIGHT);
        gg.fill(x + 2, y + 2, x + 16, y + 16, SLOT_BG);
    }

    private static void drawPlayerInventory(GuiGraphics gg, int x, int y) {
        for (int row = 0; row < 3; row++)
            for (int col = 0; col < 9; col++)
                drawSlot(gg, x + col * 18, y + row * 18);
        for (int col = 0; col < 9; col++)
            drawSlot(gg, x + col * 18, y + 58);
    }
}
