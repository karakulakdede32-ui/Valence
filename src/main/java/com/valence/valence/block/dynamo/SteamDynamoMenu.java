package com.valence.valence.block.dynamo;

import com.valence.valence.Registration;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public class SteamDynamoMenu extends AbstractContainerMenu {
    private final SteamDynamoTileEntity tileEntity;
    private final DataSlot waterAmount = DataSlot.standalone();
    private final DataSlot waterCapacity = DataSlot.standalone();
    private final DataSlot steamAmount = DataSlot.standalone();
    private final DataSlot steamCapacity = DataSlot.standalone();

    public SteamDynamoMenu(int id, Inventory inv, FriendlyByteBuf buf) { this(id, inv, inv.player.level().getBlockEntity(buf.readBlockPos())); }
    public SteamDynamoMenu(int id, Inventory inv, BlockEntity entity) {
        super(Registration.STEAM_DYNAMO_MENU.get(), id);
        tileEntity = (SteamDynamoTileEntity) entity;
        addDataSlot(waterAmount); addDataSlot(waterCapacity); addDataSlot(steamAmount); addDataSlot(steamCapacity);
        if (tileEntity != null) {
            waterAmount.set(tileEntity.getWaterTank().getFluidAmount());
            waterCapacity.set(tileEntity.getWaterTank().getCapacity());
            steamAmount.set(tileEntity.getSteamTank().getFluidAmount());
            steamCapacity.set(tileEntity.getSteamTank().getCapacity());
        }
        for (int r = 0; r < 3; r++) for (int c = 0; c < 9; c++) addSlot(new Slot(inv, c+r*9+9, 8+c*18, 102+r*18));
        for (int i = 0; i < 9; i++) addSlot(new Slot(inv, i, 8+i*18, 160));
    }

    @Override public void broadcastChanges() {
        if (tileEntity != null && tileEntity.getLevel() != null && !tileEntity.getLevel().isClientSide()) {
            waterAmount.set(tileEntity.getWaterTank().getFluidAmount());
            waterCapacity.set(tileEntity.getWaterTank().getCapacity());
            steamAmount.set(tileEntity.getSteamTank().getFluidAmount());
            steamCapacity.set(tileEntity.getSteamTank().getCapacity());
        }
        super.broadcastChanges();
    }

    @Override public ItemStack quickMoveStack(Player pl, int idx) {
        Slot slot = slots.get(idx);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();
        ItemStack result = stack.copy();
        if (idx < 27) { if (!moveItemStackTo(stack, 27, 36, false)) return ItemStack.EMPTY; }
        else if (idx < 36) { if (!moveItemStackTo(stack, 0, 27, false)) return ItemStack.EMPTY; }
        else return ItemStack.EMPTY;
        if (stack.isEmpty()) slot.set(ItemStack.EMPTY); else slot.setChanged();
        return result;
    }

    @Override public boolean stillValid(Player player) {
        return tileEntity != null && ContainerLevelAccess.create(tileEntity.getLevel(), tileEntity.getBlockPos()).evaluate(
            (level, pos) -> player.distanceToSqr((double)pos.getX() + 0.5D, (double)pos.getY() + 0.5D, (double)pos.getZ() + 0.5D) <= 64.0D, true);
    }

    public SteamDynamoTileEntity getTileEntity() { return tileEntity; }
    public int getWaterAmount() { return waterAmount.get(); }
    public int getWaterCapacity() { return waterCapacity.get(); }
    public int getSteamAmount() { return steamAmount.get(); }
    public int getSteamCapacity() { return steamCapacity.get(); }
}
