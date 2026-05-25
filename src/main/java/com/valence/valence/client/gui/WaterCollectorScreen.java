package com.valence.valence.client.gui;

import com.valence.valence.block.collector.WaterCollectorMenu;
import com.valence.valence.block.collector.WaterCollectorTileEntity;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.fluids.FluidStack;

public class WaterCollectorScreen extends AbstractContainerScreen<WaterCollectorMenu> {
    // Panel colors
    private static final int PANEL_DARK = 0xFF373737;
    private static final int PANEL_MID = 0xFF8B8B8B;
    private static final int PANEL_LIGHT = 0xFFC6C6C6;
    private static final int SLOT_BG = 0xFF6B6B6B;
    private static final int SLOT_BORDER_DARK = 0xFF555555;
    private static final int SLOT_BORDER_LIGHT = 0xFFD0D0D0;
    // Water colors
    private static final int WATER_DARK = 0xFF1A4FA0;
    private static final int WATER_MID = 0xFF2060C0;
    private static final int WATER_BRIGHT = 0xFF4080E0;
    // Tank background
    private static final int TANK_BG = 0xFF2A2A2A;
    private static final int TANK_BORDER = 0xFF555555;

    public WaterCollectorScreen(WaterCollectorMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 176;
        this.imageHeight = 178;
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

        // Hover tooltip for fluid tank
        WaterCollectorTileEntity te = menu.getTileEntity();
        if (te != null) {
            int tankX = this.leftPos + 79;
            int tankY = this.topPos + 17;
            int tankW = 18;
            int tankH = 54;
            if (mouseX >= tankX && mouseX < tankX + tankW && mouseY >= tankY && mouseY < tankY + tankH) {
                FluidStack fluid = te.getTank().getFluid();
                int amount = fluid.getAmount();
                int capacity = te.getTank().getCapacity();
                guiGraphics.renderTooltip(this.font,
                    Component.literal(amount + " / " + capacity + " mB"),
                    mouseX, mouseY);
            }
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

        // Draw the fluid tank (vertical gauge)
        WaterCollectorTileEntity te = menu.getTileEntity();
        int tankX = x + 79;
        int tankY = y + 17;
        int tankW = 18;
        int tankH = 54;

        // Tank background
        guiGraphics.fill(tankX, tankY, tankX + tankW, tankY + tankH, TANK_BORDER);
        guiGraphics.fill(tankX + 1, tankY + 1, tankX + tankW - 1, tankY + tankH - 1, TANK_BG);

        // Fluid fill
        if (te != null) {
            int amount = te.getTank().getFluidAmount();
            int capacity = te.getTank().getCapacity();
            int fillHeight = (int) ((long) amount * (tankH - 2) / capacity);
            if (fillHeight > 0) {
                int fluidY = tankY + tankH - 1 - fillHeight;
                // Draw water gradient
                for (int i = 0; i < fillHeight; i++) {
                    int color = WATER_DARK;
                    float t = (float) i / fillHeight;
                    if (t > 0.7f) color = WATER_BRIGHT;
                    else if (t > 0.3f) color = WATER_MID;
                    guiGraphics.fill(tankX + 2, fluidY + i, tankX + tankW - 2, fluidY + i + 1, color);
                }
            }
        }

        // Label
        guiGraphics.drawString(this.font, Component.literal("Water"), x + 65 - font.width("Water") / 2, y + 75, 0x555555, false);

        // Player inventory
        drawPlayerInventory(guiGraphics, x + 7, y + 95);
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
