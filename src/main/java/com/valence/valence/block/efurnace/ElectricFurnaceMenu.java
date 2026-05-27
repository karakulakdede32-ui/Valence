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
    public final DataSlot balanceMode = DataSlot.standalone();
    public final DataSlot[] progressSlots = new DataSlot[ElectricFurnaceTileEntity.SLOT_COUNT];

    public ElectricFurnaceMenu(int id, Inventory inv, FriendlyByteBuf buf) { this(id, inv, inv.player.level().getBlockEntity(buf.readBlockPos())); }
    public ElectricFurnaceMenu(int id, Inventory inv, BlockEntity entity) {
        super(Registration.ELECTRIC_FURNACE_MENU.get(), id);
        tileEntity = (ElectricFurnaceTileEntity) entity;
        addDataSlot(dfAmount); addDataSlot(dfCapacity); addDataSlot(balanceMode);
        for (int i = 0; i < ElectricFurnaceTileEntity.SLOT_COUNT; i++) {
            progressSlots[i] = DataSlot.standalone();
            addDataSlot(progressSlots[i]);
        }
        if (tileEntity != null) {
            dfAmount.set(tileEntity.getDFStorage().getDF());
            dfCapacity.set(tileEntity.getDFStorage().getMaxDF());
            balanceMode.set(tileEntity.isBalanceMode() ? 1 : 0);
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
            balanceMode.set(tileEntity.isBalanceMode() ? 1 : 0);
            int[] prog = tileEntity.getProgress();
            for (int i = 0; i < ElectricFurnaceTileEntity.SLOT_COUNT && i < prog.length; i++)
                progressSlots[i].set(prog[i]);
        }
        super.broadcastChanges();
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id == 0 && tileEntity != null) {
            tileEntity.toggleBalanceMode();
            return true;
        }
        return false;
    }

    @Override public ItemStack quickMoveStack(Player pl, int idx) {
        Slot slot = slots.get(idx);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();

        // Check if clicked in player inventory area (hotbar + main = slots 16 to 51)
        int playerStart = ElectricFurnaceTileEntity.SLOT_COUNT * 2; // 16 input+output slots
        int playerEnd = playerStart + 36; // 36 player inventory slots (27 main + 9 hotbar)

        if (idx >= playerStart && idx < playerEnd && balanceMode.get() == 1) {
            // Balance mode ON: distribute evenly across input slots
            int total = stack.getCount();
            int inputStart = 0;
            int inputEnd = ElectricFurnaceTileEntity.SLOT_COUNT;

            // First pass: how many can each slot take?
            int[] capacity = new int[ElectricFurnaceTileEntity.SLOT_COUNT];
            int totalCapacity = 0;
            for (int i = inputStart; i < inputEnd; i++) {
                ItemStack inSlot = tileEntity.getItemHandler().getStackInSlot(i);
                if (inSlot.isEmpty()) {
                    capacity[i] = 64;
                    totalCapacity += 64;
                } else if (inSlot.is(stack.getItem()) && ItemStack.isSameItemSameTags(stack, inSlot)) {
                    capacity[i] = 64 - inSlot.getCount();
                    if (capacity[i] < 0) capacity[i] = 0;
                    totalCapacity += capacity[i];
                }
            }
            if (totalCapacity <= 0) return ItemStack.EMPTY;
            int toDistribute = Math.min(total, totalCapacity);
            if (toDistribute <= 0) return ItemStack.EMPTY;

            // Distribute evenly
            int remaining = toDistribute;
            while (remaining > 0) {
                boolean placed = false;
                for (int i = inputStart; i < inputEnd && remaining > 0; i++) {
                    ItemStack inSlot = tileEntity.getItemHandler().getStackInSlot(i);
                    if (inSlot.isEmpty() || (inSlot.is(stack.getItem()) && ItemStack.isSameItemSameTags(stack, inSlot))) {
                        int space = 64 - (inSlot.isEmpty() ? 0 : inSlot.getCount());
                        if (space > 0) {
                            int add = Math.min(1, Math.min(space, remaining));
                            if (inSlot.isEmpty()) {
                                ItemStack newStack = stack.copy();
                                newStack.setCount(add);
                                tileEntity.getItemHandler().setStackInSlot(i, newStack);
                            } else {
                                inSlot.grow(add);
                            }
                            remaining -= add;
                            placed = true;
                        }
                    }
                }
                if (!placed) break; // no more room
            }

            int taken = toDistribute - remaining;
            stack.shrink(taken);
            return original;
        }

        // Default: single-slot shift-click (move between furnace and inventory)
        return ItemStack.EMPTY;
    }
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
