package com.valence.valence.block.miner;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.SlotItemHandler;

import com.valence.valence.Registration;

public class AdvancedMinerMenu extends AbstractContainerMenu {
    private final AdvancedMinerTileEntity tileEntity;

    // Slot indices
    private static final int FUEL_SLOT = 0;
    private static final int OUTPUT_SLOTS_START = 1;
    private static final int OUTPUT_SLOTS_COUNT = 8;
    private static final int PLAYER_INVENTORY_START = 9;
    private static final int HOTBAR_START = 36;
    private static final int HOTBAR_END = 45;

    public AdvancedMinerMenu(int id, Inventory playerInv, AdvancedMinerTileEntity te) {
        super(Registration.ADVANCED_MINER_MENU.get(), id);
        this.tileEntity = te;

        if (te != null) {
            // Fuel slot at top center
            this.addSlot(new SlotItemHandler(te.getItemHandler(), 0, 80, 8));

            // Output slots arranged in 2 rows of 4
            for (int row = 0; row < 2; row++) {
                for (int col = 0; col < 4; col++) {
                    this.addSlot(new SlotItemHandler(te.getItemHandler(), 1 + col + row * 4, 26 + col * 27, 44 + row * 27));
                }
            }
        }

        // Player inventory: standard layout starting at y=106
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInv, col + row * 9 + 9, 8 + col * 18, 106 + row * 18));
            }
        }

        // Hotbar: y=160
        for (int i = 0; i < 9; i++) {
            this.addSlot(new Slot(playerInv, i, 8 + i * 18, 160));
        }
    }

    public AdvancedMinerMenu(int id, Inventory inv, FriendlyByteBuf buf) {
        this(id, inv, (AdvancedMinerTileEntity) inv.player.level().getBlockEntity(buf.readBlockPos()));
    }

    @Override
    public boolean stillValid(Player player) {
        return tileEntity != null && ContainerLevelAccess.create(tileEntity.getLevel(), tileEntity.getBlockPos())
            .evaluate((level, pos) -> player.distanceToSqr(
                (double) pos.getX() + 0.5D,
                (double) pos.getY() + 0.5D,
                (double) pos.getZ() + 0.5D) <= 64.0D, true);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();

            if (index < OUTPUT_SLOTS_START + OUTPUT_SLOTS_COUNT) {
                // From miner to player inventory
                if (!this.moveItemStackTo(itemstack1, PLAYER_INVENTORY_START, HOTBAR_END, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (index >= PLAYER_INVENTORY_START && index < HOTBAR_END) {
                // From player inventory to fuel slot only
                if (tileEntity.getItemHandler().isItemValid(0, itemstack1)) {
                    if (!this.moveItemStackTo(itemstack1, FUEL_SLOT, FUEL_SLOT + 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else {
                    return ItemStack.EMPTY;
                }
            } else if (index >= HOTBAR_START && index < HOTBAR_END) {
                // From hotbar to fuel slot
                if (tileEntity.getItemHandler().isItemValid(0, itemstack1)) {
                    if (!this.moveItemStackTo(itemstack1, FUEL_SLOT, FUEL_SLOT + 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else {
                    return ItemStack.EMPTY;
                }
            }

            if (itemstack1.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return itemstack;
    }
}
