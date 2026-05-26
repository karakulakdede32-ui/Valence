package com.valence.valence.block.dfcell;

import com.valence.valence.Registration;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public class DFCellMenu extends AbstractContainerMenu {
    private final DFCellTileEntity tileEntity;
    private final DataSlot dfAmount = DataSlot.standalone();
    private final DataSlot dfCapacity = DataSlot.standalone();

    public DFCellMenu(int id, Inventory inv, FriendlyByteBuf buf) { this(id, inv, inv.player.level().getBlockEntity(buf.readBlockPos())); }
    public DFCellMenu(int id, Inventory inv, BlockEntity entity) {
        super(Registration.DF_CELL_MENU.get(), id);
        tileEntity = (DFCellTileEntity) entity;
        addDataSlot(dfAmount); addDataSlot(dfCapacity);
        if (tileEntity != null) { dfAmount.set(tileEntity.getDFStorage().getDF()); dfCapacity.set(tileEntity.getDFStorage().getMaxDF()); }
        for (int r = 0; r < 3; r++) for (int c = 0; c < 9; c++) addSlot(new Slot(inv, c+r*9+9, 8+c*18, 102+r*18));
        for (int i = 0; i < 9; i++) addSlot(new Slot(inv, i, 8+i*18, 160));
    }

    @Override public void broadcastChanges() {
        if (tileEntity != null && tileEntity.getLevel() != null && !tileEntity.getLevel().isClientSide()) {
            dfAmount.set(tileEntity.getDFStorage().getDF()); dfCapacity.set(tileEntity.getDFStorage().getMaxDF());
        }
        super.broadcastChanges();
    }

    @Override public ItemStack quickMoveStack(Player pl, int idx) { return ItemStack.EMPTY; }
    @Override public boolean stillValid(Player pl) {
        return tileEntity != null && ContainerLevelAccess.create(tileEntity.getLevel(), tileEntity.getBlockPos()).evaluate(
            (l, p) -> pl.distanceToSqr(p.getX()+0.5, p.getY()+0.5, p.getZ()+0.5) <= 64, true);
    }
    public DFCellTileEntity getTileEntity() { return tileEntity; }
    public int getDF() { return dfAmount.get(); }
    public int getDFCapacity() { return dfCapacity.get(); }
}
