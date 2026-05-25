package com.valence.valence.client.gui;

import com.valence.valence.block.GrinderMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class GrinderScreen extends AbstractContainerScreen<GrinderMenu> {
    private static final int PANEL = 0xFFB8B8B8;
    private static final int PANEL_DARK = 0xFF555555;
    private static final int PANEL_LIGHT = 0xFFFFFFFF;
    private static final int SLOT = 0xFF8B8B8B;

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
        drawPanel(guiGraphics, x, y, imageWidth, imageHeight);
        drawSlot(guiGraphics, x + 43, y + 34);
        drawArrow(guiGraphics, x + 76, y + 38);
        drawSlot(guiGraphics, x + 115, y + 34);
        drawPlayerInventory(guiGraphics, x + 7, y + 83);
    }

    private static void drawPanel(GuiGraphics guiGraphics, int x, int y, int width, int height) {
        guiGraphics.fill(x, y, x + width, y + height, PANEL_DARK);
        guiGraphics.fill(x + 1, y + 1, x + width - 1, y + height - 1, PANEL_LIGHT);
        guiGraphics.fill(x + 2, y + 2, x + width - 2, y + height - 2, PANEL);
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

    private static void drawArrow(GuiGraphics guiGraphics, int x, int y) {
        guiGraphics.fill(x, y + 5, x + 24, y + 9, PANEL_DARK);
        guiGraphics.fill(x + 20, y + 1, x + 24, y + 13, PANEL_DARK);
        guiGraphics.fill(x + 24, y + 3, x + 28, y + 11, PANEL_DARK);
    }

    private static void drawSlot(GuiGraphics guiGraphics, int x, int y) {
        guiGraphics.fill(x, y, x + 18, y + 18, PANEL_DARK);
        guiGraphics.fill(x + 1, y + 1, x + 17, y + 17, PANEL_LIGHT);
        guiGraphics.fill(x + 2, y + 2, x + 16, y + 16, SLOT);
    }
}
