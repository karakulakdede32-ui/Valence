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

public class BasicMinerMenu extends AbstractContainerMenu {
    private final BasicMinerTileEntity tileEntity;
    
    // Slot indices
    private static final int MINER_SLOTS_COUNT = 4;
    private static final int PLAYER_INVENTORY_START = 4;
    private static final int PLAYER_INVENTORY_END = 31;
    private static final int HOTBAR_START = 31;
    private static final int HOTBAR_END = 40;

    public BasicMinerMenu(int id, Inventory playerInv, BasicMinerTileEntity te) {
        super(Registration.BASIC_MINER_MENU.get(), id);
        this.tileEntity = te;
        
        if (te != null) {
            // Arrange output slots in a 2x2 grid at the top of the GUI
            this.addSlot(new SlotItemHandler(te.getItemHandler(), 0, 62, 17));
            this.addSlot(new SlotItemHandler(te.getItemHandler(), 1, 80, 17));
            this.addSlot(new SlotItemHandler(te.getItemHandler(), 2, 62, 35));
            this.addSlot(new SlotItemHandler(te.getItemHandler(), 3, 80, 35));
        }
        
        // Player inventory (standard vanilla layout)
        for (int row = 0; row < 3; row++)
            for (int col = 0; col < 9; col++)
                this.addSlot(new Slot(playerInv, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
        // Hotbar
        for (int i = 0; i < 9; i++)
            this.addSlot(new Slot(playerInv, i, 8 + i * 18, 142));
    }

    public BasicMinerMenu(int id, Inventory inv, FriendlyByteBuf buf) {
        this(id, inv, inv.player.level().getBlockEntity(buf.readBlockPos()) instanceof BasicMinerTileEntity te ? te : null);
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

            if (index < MINER_SLOTS_COUNT) {
                // From miner to player inventory
                if (!this.moveItemStackTo(itemstack1, PLAYER_INVENTORY_START, HOTBAR_END, true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // Prevent moving items into miner output slots
                return ItemStack.EMPTY;
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
