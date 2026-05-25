package com.valence.valence.client.gui;

import com.valence.valence.block.miner.BasicMinerMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class BasicMinerScreen extends AbstractContainerScreen<BasicMinerMenu> {
    private static final int PANEL = 0xFFB8B8B8;
    private static final int PANEL_DARK = 0xFF555555;
    private static final int PANEL_LIGHT = 0xFFFFFFFF;
    private static final int SLOT = 0xFF8B8B8B;

    public BasicMinerScreen(BasicMinerMenu menu, Inventory inventory, Component title) {
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
        drawSlotGrid(guiGraphics, x + 61, y + 16, 2, 2);
        drawPlayerInventory(guiGraphics, x + 7, y + 83);
    }

    private static void drawPanel(GuiGraphics guiGraphics, int x, int y, int width, int height) {
        guiGraphics.fill(x, y, x + width, y + height, PANEL_DARK);
        guiGraphics.fill(x + 1, y + 1, x + width - 1, y + height - 1, PANEL_LIGHT);
        guiGraphics.fill(x + 2, y + 2, x + width - 2, y + height - 2, PANEL);
    }

    private static void drawSlotGrid(GuiGraphics guiGraphics, int x, int y, int columns, int rows) {
        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
                drawSlot(guiGraphics, x + column * 18, y + row * 18);
            }
        }
    }

    private static void drawPlayerInventory(GuiGraphics guiGraphics, int x, int y) {
        drawSlotGrid(guiGraphics, x, y, 9, 3);
        drawSlotGrid(guiGraphics, x, y + 58, 9, 1);
    }

    private static void drawSlot(GuiGraphics guiGraphics, int x, int y) {
        guiGraphics.fill(x, y, x + 18, y + 18, PANEL_DARK);
        guiGraphics.fill(x + 1, y + 1, x + 17, y + 17, PANEL_LIGHT);
        guiGraphics.fill(x + 2, y + 2, x + 16, y + 16, SLOT);
    }
}
