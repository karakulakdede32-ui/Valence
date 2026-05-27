package com.valence.valence.block.conduit;

import com.valence.valence.Registration;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public class TransferConduitMenu extends AbstractContainerMenu {
    private final TransferConduitTileEntity tileEntity;
    public final DataSlot fluidAmount = DataSlot.standalone();
    public final DataSlot fluidCapacity = DataSlot.standalone();
    public final DataSlot dfAmount = DataSlot.standalone();
    public final DataSlot dfCapacity = DataSlot.standalone();
    public final DataSlot sourceCount = DataSlot.standalone();
    public final DataSlot destCount = DataSlot.standalone();

    public TransferConduitMenu(int id, Inventory inv, FriendlyByteBuf buf) { this(id, inv, inv.player.level().getBlockEntity(buf.readBlockPos())); }
    public TransferConduitMenu(int id, Inventory inv, BlockEntity entity) {
        super(Registration.TRANSFER_CONDUIT_MENU.get(), id);
        tileEntity = (TransferConduitTileEntity) entity;
        addDataSlot(fluidAmount); addDataSlot(fluidCapacity); addDataSlot(dfAmount); addDataSlot(dfCapacity);
        addDataSlot(sourceCount); addDataSlot(destCount);
        if (tileEntity != null) {
            fluidAmount.set(tileEntity.getFluidTank().getFluidAmount());
            fluidCapacity.set(tileEntity.getFluidTank().getCapacity());
            dfAmount.set(tileEntity.getDFStorage().getDF());
            dfCapacity.set(tileEntity.getDFStorage().getMaxDF());
            sourceCount.set(tileEntity.getLinkedSources().size());
            destCount.set(tileEntity.getLinkedDests().size());
        }
        for (int r = 0; r < 3; r++) for (int c = 0; c < 9; c++) addSlot(new Slot(inv, c+r*9+9, 8+c*18, 102+r*18));
        for (int i = 0; i < 9; i++) addSlot(new Slot(inv, i, 8+i*18, 160));
    }

    @Override public void broadcastChanges() {
        if (tileEntity != null && tileEntity.getLevel() != null && !tileEntity.getLevel().isClientSide()) {
            fluidAmount.set(tileEntity.getFluidTank().getFluidAmount());
            fluidCapacity.set(tileEntity.getFluidTank().getCapacity());
            dfAmount.set(tileEntity.getDFStorage().getDF());
            dfCapacity.set(tileEntity.getDFStorage().getMaxDF());
            sourceCount.set(tileEntity.getLinkedSources().size());
            destCount.set(tileEntity.getLinkedDests().size());
        }
        super.broadcastChanges();
    }

    @Override public ItemStack quickMoveStack(Player pl, int idx) { return ItemStack.EMPTY; }
    @Override public boolean stillValid(Player pl) {
        return tileEntity != null && ContainerLevelAccess.create(tileEntity.getLevel(), tileEntity.getBlockPos()).evaluate(
            (l, p) -> pl.distanceToSqr(p.getX()+0.5, p.getY()+0.5, p.getZ()+0.5) <= 64, true);
    }

    public TransferConduitTileEntity getTileEntity() { return tileEntity; }
    public int getFluidAmount() { return fluidAmount.get(); }
    public int getFluidCapacity() { return fluidCapacity.get(); }
    public int getDF() { return dfAmount.get(); }
    public int getDFCapacity() { return dfCapacity.get(); }
    public int getSourceCount() { return sourceCount.get(); }
    public int getDestCount() { return destCount.get(); }
}
