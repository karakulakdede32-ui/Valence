package com.valence.valence.block.collector;

import com.valence.valence.Registration;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.SlotItemHandler;

public class WaterCollectorMenu extends AbstractContainerMenu {
    private final WaterCollectorTileEntity tileEntity;

    public WaterCollectorMenu(int id, Inventory inv, FriendlyByteBuf extraData) {
        this(id, inv, inv.player.level().getBlockEntity(extraData.readBlockPos()));
    }

    public WaterCollectorMenu(int id, Inventory inv, BlockEntity entity) {
        super(Registration.WATER_COLLECTOR_MENU.get(), id);
        this.tileEntity = (WaterCollectorTileEntity) entity;

        // Player inventory
        for (int row = 0; row < 3; row++)
            for (int col = 0; col < 9; col++)
                this.addSlot(new Slot(inv, col + row * 9 + 9, 8 + col * 18, 96 + row * 18));
        // Hotbar
        for (int i = 0; i < 9; i++)
            this.addSlot(new Slot(inv, i, 8 + i * 18, 154));
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return tileEntity != null && ContainerLevelAccess.create(tileEntity.getLevel(), tileEntity.getBlockPos()).evaluate((level, pos) ->
            player.distanceToSqr((double)pos.getX() + 0.5D, (double)pos.getY() + 0.5D, (double)pos.getZ() + 0.5D) <= 64.0D, true);
    }

    public WaterCollectorTileEntity getTileEntity() {
        return tileEntity;
    }
}
