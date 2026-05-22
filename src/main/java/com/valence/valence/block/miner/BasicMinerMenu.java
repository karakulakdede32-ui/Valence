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
    private static final int HOTBAR_END = 40;

    public BasicMinerMenu(int id, Inventory playerInv, BasicMinerTileEntity te) {
        super(Registration.BASIC_MINER_MENU.get(), id);
        this.tileEntity = te;
        
        if (te != null) {
            // Miner output slots
            for (int i = 0; i < 4; i++) {
                this.addSlot(new SlotItemHandler(te.getItemHandler(), i, 35 + i * 27, 17));
            }
        }
        
        // Player inventory
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                this.addSlot(new Slot(playerInv, j + i * 9 + 9, 8 + j * 18, 76 + i * 18));
            }
        }
        
        // Hotbar
        for (int i = 0; i < 9; i++) {
            this.addSlot(new Slot(playerInv, i, 8 + i * 18, 134));
        }
    }

    public BasicMinerMenu(int id, Inventory inv, FriendlyByteBuf buf) {
        this(id, inv, getTileEntityFromBuf(buf));
    }

    private static BasicMinerTileEntity getTileEntityFromBuf(FriendlyByteBuf buf) {
        BlockPos pos = buf.readBlockPos();
        Level level = getClientLevel();
        if (level != null && level.getBlockEntity(pos) instanceof BasicMinerTileEntity te) return te;
        return null;
    }

    private static Level getClientLevel() {
        try {
            Class<?> clazz = Class.forName("net.minecraft.client.Minecraft");
            Object minecraft = clazz.getMethod("getInstance").invoke(null);
            return (Level) clazz.getField("level").get(minecraft);
        } catch (Exception e) {
            return null;
        }
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
                // Basic Miner has only output slots, so we don't allow moving items into it
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
