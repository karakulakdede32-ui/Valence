package com.valence.valence.client.gui;

import com.valence.valence.block.GrinderMenu;
import com.valence.valence.block.GrinderTileEntity;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class GrinderScreen extends AbstractContainerScreen<GrinderMenu> {
    // Colors
    private static final int PANEL_DARK = 0xFF373737;
    private static final int PANEL_MID = 0xFF8B8B8B;
    private static final int PANEL_LIGHT = 0xFFC6C6C6;
    private static final int SLOT_BG = 0xFF6B6B6B;
    private static final int SLOT_BORDER_DARK = 0xFF555555;
    private static final int SLOT_BORDER_LIGHT = 0xFFD0D0D0;
    private static final int ARROW_BG = 0xFF555555;
    private static final int ARROW_FILL = 0xFFFFFFFF;

    public GrinderScreen(GrinderMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = (imageWidth - font.width(title)) / 2;
        this.titleLabelY = 6;
        this.inventoryLabelX = 8;
        this.inventoryLabelY = imageHeight - 96;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        this.renderBg(guiGraphics, delta, mouseX, mouseY);
        super.render(guiGraphics, mouseX, mouseY, delta);
        this.renderLabels(guiGraphics, mouseX, mouseY);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
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

        // Input slot (left)
        drawSlot(guiGraphics, x + 43, y + 34);

        // Progress arrow
        GrinderTileEntity te = menu.getTileEntity();
        int progress = 0;
        int maxProgress = 1;
        if (te != null) {
            progress = te.getProgress();
            maxProgress = te.getMaxProgress();
        }
        drawProgressArrow(guiGraphics, x + 72, y + 37, progress, maxProgress);

        // Output slot (right)
        drawSlot(guiGraphics, x + 115, y + 34);

        // Player inventory
        drawPlayerInventory(guiGraphics, x + 7, y + 83);
    }

    private static void drawMachinePanel(GuiGraphics guiGraphics, int x, int y, int width, int height) {
        guiGraphics.fill(x, y, x + width, y + height, PANEL_DARK);
        guiGraphics.fill(x + 1, y + 1, x + width - 1, y + height - 1, PANEL_MID);
        guiGraphics.fill(x + 2, y + 2, x + width - 2, y + height - 2, PANEL_LIGHT);
    }

    private static void drawSlot(GuiGraphics guiGraphics, int x, int y) {
        guiGraphics.fill(x, y, x + 18, y + 18, SLOT_BORDER_DARK);
        guiGraphics.fill(x + 1, y + 1, x + 17, y + 17, SLOT_BORDER_LIGHT);
        guiGraphics.fill(x + 2, y + 2, x + 16, y + 16, SLOT_BG);
    }

    private static void drawProgressArrow(GuiGraphics guiGraphics, int x, int y, int progress, int maxProgress) {
        int arrowWidth = 22;
        int arrowHeight = 15;

        // Arrow background
        guiGraphics.fill(x, y, x + arrowWidth, y + arrowHeight, ARROW_BG);

        // Arrow fill (from left to right)
        int filled = maxProgress > 0 ? (progress * (arrowWidth - 2)) / maxProgress : 0;
        if (filled > 0) {
            guiGraphics.fill(x + 1, y + 1, x + 1 + filled, y + arrowHeight - 1, ARROW_FILL);
        }

        // Arrow head
        guiGraphics.fill(x + arrowWidth, y + 3, x + arrowWidth + 4, y + arrowHeight - 3, ARROW_BG);
        guiGraphics.fill(x + arrowWidth + 4, y + 5, x + arrowWidth + 6, y + arrowHeight - 5, ARROW_BG);
    }

    private static void drawPlayerInventory(GuiGraphics guiGraphics, int x, int y) {
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                drawSlot(guiGraphics, x + column * 18, y + row * 18);
            }
        }
        for (int column = 0; column < 9; column++) {
            drawSlot(guiGraphics, x + column * 18, y + 58);
        }
    }
}
