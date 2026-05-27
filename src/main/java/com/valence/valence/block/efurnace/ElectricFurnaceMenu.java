package com.valence.valence.block.efurnace;

import com.valence.valence.Registration;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.SlotItemHandler;

public class ElectricFurnaceMenu extends AbstractContainerMenu {
    private final ElectricFurnaceTileEntity tileEntity;
    public final DataSlot dfAmount = DataSlot.standalone();
    public final DataSlot dfCapacity = DataSlot.standalone();
    public final DataSlot[] progressSlots = new DataSlot[ElectricFurnaceTileEntity.SLOT_COUNT];

    public ElectricFurnaceMenu(int id, Inventory inv, FriendlyByteBuf buf) { this(id, inv, inv.player.level().getBlockEntity(buf.readBlockPos())); }
    public ElectricFurnaceMenu(int id, Inventory inv, BlockEntity entity) {
        super(Registration.ELECTRIC_FURNACE_MENU.get(), id);
        tileEntity = (ElectricFurnaceTileEntity) entity;
        addDataSlot(dfAmount); addDataSlot(dfCapacity);
        for (int i = 0; i < ElectricFurnaceTileEntity.SLOT_COUNT; i++) {
            progressSlots[i] = DataSlot.standalone();
            addDataSlot(progressSlots[i]);
        }
        if (tileEntity != null) {
            dfAmount.set(tileEntity.getDFStorage().getDF());
            dfCapacity.set(tileEntity.getDFStorage().getMaxDF());
            int[] prog = tileEntity.getProgress();
            for (int i = 0; i < ElectricFurnaceTileEntity.SLOT_COUNT && i < prog.length; i++)
                progressSlots[i].set(prog[i]);
        }

        // 8 input slots (top row) + 8 output slots (bottom row)
        if (tileEntity != null) {
            for (int col = 0; col < 8; col++) {
                addSlot(new SlotItemHandler(tileEntity.getItemHandler(), col, 8 + col * 18, 17));
            }
            for (int col = 0; col < 8; col++) {
                addSlot(new SlotItemHandler(tileEntity.getItemHandler(), col + 8, 8 + col * 18, 62));
            }
        }

        for (int r = 0; r < 3; r++) for (int c = 0; c < 9; c++) addSlot(new Slot(inv, c+r*9+9, 8+c*18, 102+r*18));
        for (int i = 0; i < 9; i++) addSlot(new Slot(inv, i, 8+i*18, 160));
    }

    @Override public void broadcastChanges() {
        if (tileEntity != null && tileEntity.getLevel() != null && !tileEntity.getLevel().isClientSide()) {
            dfAmount.set(tileEntity.getDFStorage().getDF());
            dfCapacity.set(tileEntity.getDFStorage().getMaxDF());
            int[] prog = tileEntity.getProgress();
            for (int i = 0; i < ElectricFurnaceTileEntity.SLOT_COUNT && i < prog.length; i++)
                progressSlots[i].set(prog[i]);
        }
        super.broadcastChanges();
    }

    @Override public ItemStack quickMoveStack(Player pl, int idx) { return ItemStack.EMPTY; }
    @Override public boolean stillValid(Player pl) {
        return tileEntity != null && ContainerLevelAccess.create(tileEntity.getLevel(), tileEntity.getBlockPos()).evaluate(
            (l, p) -> pl.distanceToSqr(p.getX()+0.5, p.getY()+0.5, p.getZ()+0.5) <= 64, true);
    }

    public ElectricFurnaceTileEntity getTileEntity() { return tileEntity; }
    public int getDF() { return dfAmount.get(); }
    public int getDFCapacity() { return dfCapacity.get(); }
    public int getProgress(int slot) { return slot >= 0 && slot < progressSlots.length ? progressSlots[slot].get() : 0; }
    public int getMaxProgress() { return ElectricFurnaceTileEntity.PROGRESS_MAX; }
}
