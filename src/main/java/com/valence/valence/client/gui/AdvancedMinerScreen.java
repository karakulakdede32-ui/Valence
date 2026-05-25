package com.valence.valence.client.gui;

import com.valence.valence.block.miner.AdvancedMinerMenu;
import com.valence.valence.block.miner.AdvancedMinerTileEntity;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class AdvancedMinerScreen extends AbstractContainerScreen<AdvancedMinerMenu> {
    // Machine panel colors
    private static final int PANEL_DARK = 0xFF373737;
    private static final int PANEL_MID = 0xFF8B8B8B;
    private static final int PANEL_LIGHT = 0xFFC6C6C6;
    private static final int SLOT_BG = 0xFF6B6B6B;
    private static final int SLOT_BORDER_DARK = 0xFF555555;
    private static final int SLOT_BORDER_LIGHT = 0xFFD0D0D0;
    private static final int FUEL_BAR_BG = 0xFF555555;
    private static final int FUEL_BAR_FILL = 0xFFFF6A00;

    public AdvancedMinerScreen(AdvancedMinerMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 176;
        this.imageHeight = 184;
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

        // Main machine panel
        drawMachinePanel(guiGraphics, x, y, imageWidth, imageHeight);

        // Fuel slot at top center (menu pos: 80, 8 → draw at 79, 7)
        drawSlot(guiGraphics, x + 79, y + 7);

        // Fuel bar
        AdvancedMinerTileEntity te = menu.getTileEntity();
        if (te != null) {
            int fuel = te.getFuel();
            int maxFuel = te.getMaxFuel();
            int barHeight = 16;
            int filled = maxFuel > 0 ? (fuel * barHeight) / maxFuel : 0;
            drawFuelBar(guiGraphics, x + 103, y + 8, barHeight, filled);
        }

        // Output slots: 2x4 grid (menu pos: 26 + j*27, 44 + i*27 → draw at 25 + j*27, 43 + i*27)
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 4; j++) {
                drawSlot(guiGraphics, x + 25 + j * 27, y + 43 + i * 27);
            }
        }

        // Player inventory (menu: main at 106, hotbar at 160)
        // Draw main inventory rows (menu: y=106,124,142 → draw at 105,123,141)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                drawSlot(guiGraphics, x + 7 + col * 18, y + 105 + row * 18);
            }
        }
        // Draw hotbar (menu: y=160 → draw at 159)
        for (int col = 0; col < 9; col++) {
            drawSlot(guiGraphics, x + 7 + col * 18, y + 159);
        }
    }

    private static void drawMachinePanel(GuiGraphics guiGraphics, int x, int y, int width, int height) {
        // Outer dark border
        guiGraphics.fill(x, y, x + width, y + height, PANEL_DARK);
        // Inner lighter border
        guiGraphics.fill(x + 1, y + 1, x + width - 1, y + height - 1, PANEL_MID);
        // Background
        guiGraphics.fill(x + 2, y + 2, x + width - 2, y + height - 2, PANEL_LIGHT);
    }

    private static void drawSlot(GuiGraphics guiGraphics, int x, int y) {
        // Slot border
        guiGraphics.fill(x, y, x + 18, y + 18, SLOT_BORDER_DARK);
        guiGraphics.fill(x + 1, y + 1, x + 17, y + 17, SLOT_BORDER_LIGHT);
        // Slot background
        guiGraphics.fill(x + 2, y + 2, x + 16, y + 16, SLOT_BG);
    }

    private static void drawFuelBar(GuiGraphics guiGraphics, int x, int y, int height, int filled) {
        // Background
        guiGraphics.fill(x, y, x + 6, y + height, FUEL_BAR_BG);
        // Fill (from bottom to top)
        if (filled > 0) {
            guiGraphics.fill(x + 1, y + height - filled, x + 5, y + height - 1, FUEL_BAR_FILL);
        }
    }
}
