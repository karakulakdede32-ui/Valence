package com.valence.valence.block.dynamo;

import com.valence.valence.Registration;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.fluids.FluidStack;

public class SteamDynamoMenu extends AbstractContainerMenu {
    private final SteamDynamoTileEntity tileEntity;
    private final DataSlot waterAmountSlot = DataSlot.standalone();
    private final DataSlot waterCapacitySlot = DataSlot.standalone();
    private final DataSlot steamAmountSlot = DataSlot.standalone();
    private final DataSlot steamCapacitySlot = DataSlot.standalone();

    public SteamDynamoMenu(int id, Inventory inv, FriendlyByteBuf extraData) {
        this(id, inv, inv.player.level().getBlockEntity(extraData.readBlockPos()));
    }

    public SteamDynamoMenu(int id, Inventory inv, BlockEntity entity) {
        super(Registration.STEAM_DYNAMO_MENU.get(), id);
        this.tileEntity = (SteamDynamoTileEntity) entity;

        addDataSlot(waterAmountSlot);
        addDataSlot(waterCapacitySlot);
        addDataSlot(steamAmountSlot);
        addDataSlot(steamCapacitySlot);

        if (tileEntity != null) {
            waterAmountSlot.set(tileEntity.getWaterTank().getFluidAmount());
            waterCapacitySlot.set(tileEntity.getWaterTank().getCapacity());
            steamAmountSlot.set(tileEntity.getSteamTank().getFluidAmount());
            steamCapacitySlot.set(tileEntity.getSteamTank().getCapacity());
        }

        // Player inventory
        for (int row = 0; row < 3; row++)
            for (int col = 0; col < 9; col++)
                this.addSlot(new Slot(inv, col + row * 9 + 9, 8 + col * 18, 108 + row * 18));
        for (int i = 0; i < 9; i++)
            this.addSlot(new Slot(inv, i, 8 + i * 18, 166));
    }

    @Override
    public void broadcastChanges() {
        if (tileEntity != null && tileEntity.getLevel() != null && !tileEntity.getLevel().isClientSide()) {
            waterAmountSlot.set(tileEntity.getWaterTank().getFluidAmount());
            waterCapacitySlot.set(tileEntity.getWaterTank().getCapacity());
            steamAmountSlot.set(tileEntity.getSteamTank().getFluidAmount());
            steamCapacitySlot.set(tileEntity.getSteamTank().getCapacity());
        }
        super.broadcastChanges();
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

    public SteamDynamoTileEntity getTileEntity() { return tileEntity; }
    public int getWaterAmount() { return waterAmountSlot.get(); }
    public int getWaterCapacity() { return waterCapacitySlot.get(); }
    public int getSteamAmount() { return steamAmountSlot.get(); }
    public int getSteamCapacity() { return steamCapacitySlot.get(); }
}
