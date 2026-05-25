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

    private static final int PLAYER_INVENTORY_START = 9;
    private static final int HOTBAR_END = 45;

    public AdvancedMinerMenu(int id, Inventory playerInv, AdvancedMinerTileEntity te) {
        super(Registration.ADVANCED_MINER_MENU.get(), id);
        this.tileEntity = te;

        if (te != null) {
            // Fuel slot
            this.addSlot(new SlotItemHandler(te.getItemHandler(), 0, 80, 8));

            // Output slots: 2x4 grid
            for (int i = 0; i < 2; i++) {
                for (int j = 0; j < 4; j++) {
                    this.addSlot(new SlotItemHandler(te.getItemHandler(), 1 + j + i * 4, 26 + j * 27, 44 + i * 27));
                }
            }
        }

        // Player inventory
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                this.addSlot(new Slot(playerInv, j + i * 9 + 9, 8 + j * 18, 106 + i * 18));
            }
        }

        // Hotbar
        for (int i = 0; i < 9; i++) {
            this.addSlot(new Slot(playerInv, i, 8 + i * 18, 160));
        }
    }

    public AdvancedMinerMenu(int id, Inventory inv, FriendlyByteBuf buf) {
        this(id, inv, getTileEntity(inv, buf));
    }

    private static AdvancedMinerTileEntity getTileEntity(Inventory inv, FriendlyByteBuf buf) {
        BlockPos pos = buf.readBlockPos();
        Level level = inv.player.level();
        if (level.getBlockEntity(pos) instanceof AdvancedMinerTileEntity te) {
            return te;
        }
        return null;
    }

    @Override
    public boolean stillValid(Player player) {
        return tileEntity != null && ContainerLevelAccess.create(tileEntity.getLevel(), tileEntity.getBlockPos()).evaluate((level, pos) ->
            player.distanceToSqr((double)pos.getX() + 0.5D, (double)pos.getY() + 0.5D, (double)pos.getZ() + 0.5D) <= 64.0D, true);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();

            if (index < PLAYER_INVENTORY_START) {
                // From miner to player inventory
                if (!this.moveItemStackTo(itemstack1, PLAYER_INVENTORY_START, HOTBAR_END + 1, true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // From player to miner — only allow fuel in slot 0
                if (tileEntity != null && tileEntity.getItemHandler().isItemValid(0, itemstack1)) {
                    if (!this.moveItemStackTo(itemstack1, 0, 1, false)) {
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

    public AdvancedMinerTileEntity getTileEntity() {
        return tileEntity;
    }
}
