package com.valence.valence.block.growthchamber;

import com.valence.valence.Registration;
import com.valence.valence.energy.DFStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class TreeGrowthChamberTileEntity extends BlockEntity implements MenuProvider {
    public static final int DF_REQUIRED = 400;
    public static final int DF_PER_TICK = 2;
    public static final int PROCESS_TICKS = 200; // 10 seconds
    public static final int DF_CAPACITY = 500;

    private final DFStorage dfStorage = new DFStorage(DF_CAPACITY, DF_PER_TICK * 2, 0) {
        @Override protected void onEnergyChanged() { setChanged(); sync(); }
    };

    private final ItemStackHandler itemHandler = new ItemStackHandler(3) {
        @Override public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            if (slot == 0) return isSapling(stack);
            if (slot == 1) return isLog(stack) || stack.isEmpty();
            if (slot == 2) return isLeaf(stack) || stack.isEmpty();
            return false;
        }
        @Override protected void onContentsChanged(int slot) { setChanged(); }
    };

    private int progress = 0;
    private final LazyOptional<IEnergyStorage> energyHandler = LazyOptional.of(() -> dfStorage);

    // Side-aware: top = input, bottom = output, sides = input+output
    private final LazyOptional<IItemHandler> topHandler = LazyOptional.of(() -> new IItemHandler() {
        @Override public int getSlots() { return 1; }
        @Override public @NotNull ItemStack getStackInSlot(int slot) { return itemHandler.getStackInSlot(0); }
        @Override public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean sim) {
            if (isSapling(stack)) return itemHandler.insertItem(0, stack, sim);
            return stack;
        }
        @Override public @NotNull ItemStack extractItem(int slot, int amt, boolean sim) { return ItemStack.EMPTY; }
        @Override public int getSlotLimit(int slot) { return 64; }
        @Override public boolean isItemValid(int slot, @NotNull ItemStack stack) { return isSapling(stack); }
    });

    private final LazyOptional<IItemHandler> bottomHandler = LazyOptional.of(() -> new IItemHandler() {
        @Override public int getSlots() { return 2; }
        @Override public @NotNull ItemStack getStackInSlot(int slot) { return itemHandler.getStackInSlot(1 + slot); }
        @Override public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean sim) { return stack; }
        @Override public @NotNull ItemStack extractItem(int slot, int amt, boolean sim) { return itemHandler.extractItem(1 + slot, amt, sim); }
        @Override public int getSlotLimit(int slot) { return 64; }
        @Override public boolean isItemValid(int slot, @NotNull ItemStack stack) { return false; }
    });

    private static final Map<ItemStack, TreeResult> SAPLING_MAP = new HashMap<>();
    private record TreeResult(Block log, Block leaf, int logCount, int leafCount) {}

    static {
        add(Items.OAK_SAPLING, Blocks.OAK_LOG, Blocks.OAK_LEAVES);
        add(Items.SPRUCE_SAPLING, Blocks.SPRUCE_LOG, Blocks.SPRUCE_LEAVES);
        add(Items.BIRCH_SAPLING, Blocks.BIRCH_LOG, Blocks.BIRCH_LEAVES);
        add(Items.JUNGLE_SAPLING, Blocks.JUNGLE_LOG, Blocks.JUNGLE_LEAVES);
        add(Items.ACACIA_SAPLING, Blocks.ACACIA_LOG, Blocks.ACACIA_LEAVES);
        add(Items.DARK_OAK_SAPLING, Blocks.DARK_OAK_LOG, Blocks.DARK_OAK_LEAVES);
        add(Items.CHERRY_SAPLING, Blocks.CHERRY_LOG, Blocks.CHERRY_LEAVES);
        add(Items.MANGROVE_PROPAGULE, Blocks.MANGROVE_LOG, Blocks.MANGROVE_LEAVES);
    }

    private static void add(net.minecraft.world.item.Item sapling, Block log, Block leaf) {
        SAPLING_MAP.put(new ItemStack(sapling), new TreeResult(log, leaf, 10, 5));
    }

    public TreeGrowthChamberTileEntity(BlockPos pos, BlockState state) {
        super(Registration.TREE_GROWTH_CHAMBER_TE.get(), pos, state);
    }

    private void sync() {
        if (level != null && !level.isClientSide()) level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
    }

    @Override public Component getDisplayName() { return Component.translatable("container.valence.tree_growth_chamber"); }

    @Nullable @Override
    public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
        return new TreeGrowthChamberMenu(id, inv, this);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        dfStorage.deserializeNBT(tag.get("df_storage"));
        itemHandler.deserializeNBT(tag.getCompound("items"));
        progress = tag.getInt("progress");
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("df_storage", dfStorage.serializeNBT());
        tag.put("items", itemHandler.serializeNBT());
        tag.putInt("progress", progress);
    }

    @Override public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        tag.put("df_storage", dfStorage.serializeNBT());
        tag.put("items", itemHandler.serializeNBT());
        tag.putInt("progress", progress);
        return tag;
    }

    @Override public void handleUpdateTag(CompoundTag tag) {
        super.handleUpdateTag(tag);
        dfStorage.deserializeNBT(tag.get("df_storage"));
        itemHandler.deserializeNBT(tag.getCompound("items"));
        progress = tag.getInt("progress");
    }

    @Nullable @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        CompoundTag tag = pkt.getTag();
        if (tag != null) {
            dfStorage.deserializeNBT(tag.get("df_storage"));
            itemHandler.deserializeNBT(tag.getCompound("items"));
            progress = tag.getInt("progress");
        }
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ENERGY) return energyHandler.cast();
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            if (side == Direction.UP) return topHandler.cast();
            if (side == Direction.DOWN) return bottomHandler.cast();
            return topHandler.cast(); // sides: insert saplings too
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() { super.invalidateCaps(); energyHandler.invalidate(); }

    private static boolean isSapling(ItemStack stack) {
        if (stack.isEmpty()) return false;
        for (ItemStack key : SAPLING_MAP.keySet()) {
            if (stack.is(key.getItem())) return true;
        }
        return false;
    }

    private TreeResult findResult(ItemStack stack) {
        for (Map.Entry<ItemStack, TreeResult> e : SAPLING_MAP.entrySet()) {
            if (stack.is(e.getKey().getItem())) return e.getValue();
        }
        return null;
    }

    private static boolean isLog(ItemStack stack) {
        if (stack.isEmpty()) return false;
        for (TreeResult r : SAPLING_MAP.values()) {
            if (stack.is(r.log().asItem())) return true;
        }
        return false;
    }

    private static boolean isLeaf(ItemStack stack) {
        if (stack.isEmpty()) return false;
        for (TreeResult r : SAPLING_MAP.values()) {
            if (stack.is(r.leaf().asItem())) return true;
        }
        return false;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, TreeGrowthChamberTileEntity te) {
        if (level.isClientSide()) return;

        // Pull DF from neighbors
        if (te.dfStorage.getDF() < te.dfStorage.getMaxDF()) {
            for (Direction dir : Direction.values()) {
                int missing = te.dfStorage.getMaxDF() - te.dfStorage.getDF();
                if (missing <= 0) break;
                BlockEntity neighbor = level.getBlockEntity(pos.relative(dir));
                if (neighbor == null) continue;
                neighbor.getCapability(ForgeCapabilities.ENERGY, dir.getOpposite()).ifPresent(handler -> {
                    int pulled = te.dfStorage.receiveEnergy(handler.extractEnergy(missing, false), false);
                });
            }
        }

        ItemStack input = te.itemHandler.getStackInSlot(0);
        if (input.isEmpty()) {
            if (te.progress > 0) { te.progress = 0; te.setChanged(); }
            return;
        }

        TreeResult result = te.findResult(input);
        if (result == null) {
            if (te.progress > 0) { te.progress = 0; te.setChanged(); }
            return;
        }

        // Check output space
        ItemStack logStack = te.itemHandler.getStackInSlot(1);
        ItemStack leafStack = te.itemHandler.getStackInSlot(2);
        boolean logOk = logStack.isEmpty() || (logStack.is(result.log().asItem()) && logStack.getCount() + result.logCount() <= logStack.getMaxStackSize());
        boolean leafOk = leafStack.isEmpty() || (leafStack.is(result.leaf().asItem()) && leafStack.getCount() + result.leafCount() <= leafStack.getMaxStackSize());

        if (!logOk || !leafOk) {
            if (te.progress > 0) { te.progress = 0; te.setChanged(); }
            return;
        }

        // Consume DF progressively
        if (te.dfStorage.getDF() >= DF_PER_TICK) {
            te.dfStorage.consumeDF(DF_PER_TICK, false);
            te.progress++;
            te.setChanged();

            if (te.progress >= PROCESS_TICKS) {
                // Complete!
                input.shrink(1);
                if (input.isEmpty()) te.itemHandler.setStackInSlot(0, ItemStack.EMPTY);

                if (logStack.isEmpty()) {
                    te.itemHandler.setStackInSlot(1, new ItemStack(result.log(), result.logCount()));
                } else {
                    logStack.grow(result.logCount());
                }

                if (leafStack.isEmpty()) {
                    te.itemHandler.setStackInSlot(2, new ItemStack(result.leaf(), result.leafCount()));
                } else {
                    leafStack.grow(result.leafCount());
                }

                te.progress = 0;
                te.setChanged();
            }
        }
    }

    public DFStorage getDFStorage() { return dfStorage; }
    public ItemStackHandler getItemHandler() { return itemHandler; }
    public int getProgress() { return progress; }
    public int getMaxProgress() { return PROCESS_TICKS; }
}
