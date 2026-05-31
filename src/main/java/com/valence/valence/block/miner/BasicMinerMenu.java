package com.valence.valence.block.miner;

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

public class BasicMinerMenu extends AbstractContainerMenu {
    private final BasicMinerTileEntity tileEntity;

    private static final int FUEL_SLOT = 0;
    private static final int OUTPUT_SLOTS_END = 5; // slots 1-4
    private static final int PLAYER_INVENTORY_START = 5;
    private static final int HOTBAR_END = 41;

    public BasicMinerMenu(int id, Inventory inv, FriendlyByteBuf buf) {
        this(id, inv, getTe(inv, buf));
    }
    
    private static BasicMinerTileEntity getTe(Inventory inv, FriendlyByteBuf buf) {
        Level level = inv.player.level();
        if (level.getBlockEntity(buf.readBlockPos()) instanceof BasicMinerTileEntity be) return be;
        return null;
    }

    public BasicMinerMenu(int id, Inventory inv, BasicMinerTileEntity te) {
        super(Registration.BASIC_MINER_MENU.get(), id);
        this.tileEntity = te;

        if (te != null) {
            // Fuel slot — manually clickable
            this.addSlot(new SlotItemHandler(te.getItemHandler(), 0, 80, 8) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return getItemHandler().isItemValid(getSlotIndex(), stack);
                }
            });
            // Output slots — cannot place anything manually
            this.addSlot(new SlotItemHandler(te.getItemHandler(), 1, 62, 44) {
                @Override public boolean mayPlace(ItemStack s) { return false; }
            });
            this.addSlot(new SlotItemHandler(te.getItemHandler(), 2, 80, 44) {
                @Override public boolean mayPlace(ItemStack s) { return false; }
            });
            this.addSlot(new SlotItemHandler(te.getItemHandler(), 3, 62, 62) {
                @Override public boolean mayPlace(ItemStack s) { return false; }
            });
            this.addSlot(new SlotItemHandler(te.getItemHandler(), 4, 80, 62) {
                @Override public boolean mayPlace(ItemStack s) { return false; }
            });
        }

        for (int row = 0; row < 3; row++)
            for (int col = 0; col < 9; col++)
                addSlot(new Slot(inv, col + row * 9 + 9, 8 + col * 18, 102 + row * 18));
        for (int i = 0; i < 9; i++)
            addSlot(new Slot(inv, i, 8 + i * 18, 160));
    }

    @Override
    public boolean stillValid(Player player) {
        return tileEntity != null && ContainerLevelAccess.create(tileEntity.getLevel(), tileEntity.getBlockPos())
            .evaluate((level, pos) -> player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= 64, true);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();

            if (index < PLAYER_INVENTORY_START) {
                // From miner slots (0-4) to player inventory
                if (!this.moveItemStackTo(itemstack1, PLAYER_INVENTORY_START, HOTBAR_END + 1, true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // From player inventory to fuel slot only
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

    public BasicMinerTileEntity getTileEntity() {
        return tileEntity;
    }
}
