package com.valence.valence.block.growthchamber;

import com.valence.valence.Registration;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.SlotItemHandler;

public class TreeGrowthChamberMenu extends AbstractContainerMenu {
    private final TreeGrowthChamberTileEntity tileEntity;
    public final DataSlot dfAmount = DataSlot.standalone();
    public final DataSlot dfCapacity = DataSlot.standalone();
    public final DataSlot progress = DataSlot.standalone();
    public final DataSlot maxProgress = DataSlot.standalone();

    public TreeGrowthChamberMenu(int id, Inventory inv, FriendlyByteBuf buf) { this(id, inv, inv.player.level().getBlockEntity(buf.readBlockPos())); }
    public TreeGrowthChamberMenu(int id, Inventory inv, BlockEntity entity) {
        super(Registration.TREE_GROWTH_CHAMBER_MENU.get(), id);
        tileEntity = (TreeGrowthChamberTileEntity) entity;
        addDataSlot(dfAmount); addDataSlot(dfCapacity);
        addDataSlot(progress); addDataSlot(maxProgress);
        if (tileEntity != null) {
            dfAmount.set(tileEntity.getDFStorage().getDF());
            dfCapacity.set(tileEntity.getDFStorage().getMaxDF());
            this.progress.set(tileEntity.getProgress());
            this.maxProgress.set(tileEntity.getMaxProgress());
        }
        if (tileEntity != null) {
            addSlot(new SlotItemHandler(tileEntity.getItemHandler(), 0, 55, 34)); // sapling
            addSlot(new SlotItemHandler(tileEntity.getItemHandler(), 1, 106, 28)); // logs out
            addSlot(new SlotItemHandler(tileEntity.getItemHandler(), 2, 106, 52)); // leaves out
        }
        for (int r = 0; r < 3; r++) for (int c = 0; c < 9; c++) addSlot(new Slot(inv, c+r*9+9, 8+c*18, 102+r*18));
        for (int i = 0; i < 9; i++) addSlot(new Slot(inv, i, 8+i*18, 160));
    }

    @Override public void broadcastChanges() {
        if (tileEntity != null && tileEntity.getLevel() != null && !tileEntity.getLevel().isClientSide()) {
            dfAmount.set(tileEntity.getDFStorage().getDF());
            dfCapacity.set(tileEntity.getDFStorage().getMaxDF());
            progress.set(tileEntity.getProgress());
            maxProgress.set(tileEntity.getMaxProgress());
        }
        super.broadcastChanges();
    }

    @Override public ItemStack quickMoveStack(Player pl, int idx) { return ItemStack.EMPTY; }
    @Override public boolean stillValid(Player pl) {
        return tileEntity != null && ContainerLevelAccess.create(tileEntity.getLevel(), tileEntity.getBlockPos()).evaluate(
            (l, p) -> pl.distanceToSqr(p.getX()+0.5, p.getY()+0.5, p.getZ()+0.5) <= 64, true);
    }

    public TreeGrowthChamberTileEntity getTileEntity() { return tileEntity; }
    public int getDF() { return dfAmount.get(); }
    public int getDFCapacity() { return dfCapacity.get(); }
    public int getProgress() { return progress.get(); }
    public int getMaxProgress() { return maxProgress.get(); }
}
