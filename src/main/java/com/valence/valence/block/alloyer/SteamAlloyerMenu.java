package com.valence.valence.block.alloyer;

import com.valence.valence.Registration;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.SlotItemHandler;

public class SteamAlloyerMenu extends AbstractContainerMenu {
    private final SteamAlloyerTileEntity tileEntity;
    private final DataSlot steamAmountSlot = DataSlot.standalone();
    private final DataSlot steamCapacitySlot = DataSlot.standalone();
    private final DataSlot progressSlot = DataSlot.standalone();
    private final DataSlot maxProgressSlot = DataSlot.standalone();

    public SteamAlloyerMenu(int id, Inventory inv, FriendlyByteBuf buf) { this(id, inv, inv.player.level().getBlockEntity(buf.readBlockPos())); }

    public SteamAlloyerMenu(int id, Inventory inv, BlockEntity entity) {
        super(Registration.STEAM_ALLOYER_MENU.get(), id);
        this.tileEntity = (SteamAlloyerTileEntity) entity;

        addDataSlot(steamAmountSlot); addDataSlot(steamCapacitySlot);
        addDataSlot(progressSlot); addDataSlot(maxProgressSlot);

        if (tileEntity != null) {
            steamAmountSlot.set(tileEntity.getSteamTank().getFluidAmount());
            steamCapacitySlot.set(tileEntity.getSteamTank().getCapacity());
            progressSlot.set(tileEntity.getProgress());
            maxProgressSlot.set(tileEntity.getMaxProgress());

            addSlot(new SlotItemHandler(tileEntity.getItemHandler(), 0, 26, 35));
            addSlot(new SlotItemHandler(tileEntity.getItemHandler(), 1, 44, 35));
            addSlot(new SlotItemHandler(tileEntity.getItemHandler(), 2, 116, 35) {
                @Override public boolean mayPlace(ItemStack stack) { return false; }
            });
        }

        for (int row = 0; row < 3; row++)
            for (int col = 0; col < 9; col++)
                addSlot(new Slot(inv, col + row * 9 + 9, 8 + col * 18, 102 + row * 18));
        for (int i = 0; i < 9; i++)
            addSlot(new Slot(inv, i, 8 + i * 18, 160));
    }

    @Override
    public void broadcastChanges() {
        if (tileEntity != null && tileEntity.getLevel() != null && !tileEntity.getLevel().isClientSide()) {
            steamAmountSlot.set(tileEntity.getSteamTank().getFluidAmount());
            steamCapacitySlot.set(tileEntity.getSteamTank().getCapacity());
            progressSlot.set(tileEntity.getProgress());
            maxProgressSlot.set(tileEntity.getMaxProgress());
        }
        super.broadcastChanges();
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack stack = ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stack1 = slot.getItem();
            stack = stack1.copy();
            if (index < 3) { if (!moveItemStackTo(stack1, 3, 39, true)) return ItemStack.EMPTY; }
            else if (!moveItemStackTo(stack1, 0, 2, false)) return ItemStack.EMPTY;
            if (stack1.isEmpty()) slot.set(ItemStack.EMPTY);
            else slot.setChanged();
        }
        return stack;
    }

    @Override
    public boolean stillValid(Player player) {
        return tileEntity != null && ContainerLevelAccess.create(tileEntity.getLevel(), tileEntity.getBlockPos()).evaluate(
            (level, pos) -> player.distanceToSqr(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) <= 64.0D, true);
    }

    public SteamAlloyerTileEntity getTileEntity() { return tileEntity; }
    public int getSteamAmount() { return steamAmountSlot.get(); }
    public int getSteamCapacity() { return steamCapacitySlot.get(); }
    public int getProgress() { return progressSlot.get(); }
    public int getMaxProgress() { return maxProgressSlot.get(); }
}
