package com.valence.valence.block.miner;

import com.valence.valence.Registration;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.ItemStack;

import java.util.*;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import net.minecraftforge.items.wrapper.SidedInvWrapper;
import org.jetbrains.annotations.NotNull;

public class BasicMinerTileEntity extends BlockEntity implements WorldlyContainer, MenuProvider {
    private final List<ItemStack> scannedOres = new ArrayList<>();
    private boolean hasScanned = false;
    private int currentX = 0;
    private int currentZ = 0;
    private final Map<Block, Integer> oreCounts = new HashMap<>();
    private int fuel = 0;

    // Slot 0 = Fuel, Slots 1-4 = Output
    private final ItemStackHandler itemHandler = new ItemStackHandler(5) {
        @Override
        public void onContentsChanged(int slot) {
            setChanged();
            if (level != null && !level.isClientSide()) {
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
            }
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            if (slot == 0) {
                return stack.is(net.minecraft.world.item.Items.COAL) || stack.is(net.minecraft.world.item.Items.CHARCOAL);
            }
            return false;
        }
    };

    // Sided wrappers for mod compatibility
    private final Map<Direction, LazyOptional<IItemHandler>> sidedWrappers = new EnumMap<>(Direction.class);
    private final LazyOptional<IItemHandler> unsidedWrapper = LazyOptional.of(() -> new InvWrapper(this));

    public BasicMinerTileEntity(BlockPos pos, BlockState state) {
        super(Registration.BASIC_MINER_TE.get(), pos, state);
    }

    public BasicMinerTileEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("Items")) {
            itemHandler.deserializeNBT(tag.getCompound("Items"));
        }
        hasScanned = tag.getBoolean("hasScanned");
        currentX = tag.getInt("currentX");
        currentZ = tag.getInt("currentZ");
        fuel = tag.getInt("fuel");

    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Items", itemHandler.serializeNBT());
        tag.putBoolean("hasScanned", hasScanned);
        tag.putInt("currentX", currentX);
        tag.putInt("currentZ", currentZ);
        tag.putInt("fuel", fuel);


    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            if (side == null) {
                return unsidedWrapper.cast();
            }
            return sidedWrappers.computeIfAbsent(side, s ->
                LazyOptional.of(() -> new SidedInvWrapper(this, s))).cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        unsidedWrapper.invalidate();
        for (LazyOptional<IItemHandler> lo : sidedWrappers.values()) {
            lo.invalidate();
        }
        sidedWrappers.clear();
    }

    @Override
    public Component getDisplayName() {
        return Component.literal("Basic Miner");
    }

    @Override
    public net.minecraft.world.inventory.AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
        return new BasicMinerMenu(id, inv, this);
    }

    public void scanStep(ServerLevel lvl) {
        if (lvl == null) return;
        if (fuel <= 0) return;
        BlockPos p = this.getBlockPos();
        if (p == null) return;

        int chunkStartX = (p.getX() >> 4) << 4;
        int chunkStartZ = (p.getZ() >> 4) << 4;

        for (int y = -64; y < 320; y++) {
            BlockPos checkPos = new BlockPos(chunkStartX + currentX, y, chunkStartZ + currentZ);
            BlockState bs = lvl.getBlockState(checkPos);
            if (isOre(bs)) {
                oreCounts.merge(bs.getBlock(), 1, Integer::sum);
            }
        }

        currentX++;
        if (currentX >= 16) {
            currentX = 0;
            currentZ++;
        }

        if (currentZ >= 16) {
            finalizeScan();
            fuel--;
            setChanged();
            if (fuel <= 0) {
                tryConsumeFuel();
            }
        }
    }

    private void finalizeScan() {
        scannedOres.clear();
        List<Map.Entry<Block, Integer>> sorted = new ArrayList<>(oreCounts.entrySet());
        sorted.sort((a, b) -> Integer.compare(b.getValue(), a.getValue()));

        int count = 0;
        int slotIndex = 1;
        for (Map.Entry<Block, Integer> entry : sorted) {
            if (count >= 4) break;
            scannedOres.add(new ItemStack(entry.getKey(), entry.getValue()));

            // Place into output slot if empty
            if (slotIndex <= 4 && itemHandler.getStackInSlot(slotIndex).isEmpty()) {
                itemHandler.setStackInSlot(slotIndex, new ItemStack(entry.getKey(), 1));
            }
            slotIndex++;
            count++;
        }
        hasScanned = true;
        setChanged();
    }

    private boolean isOre(BlockState state) {
        Block blk = state.getBlock();
        return blk.builtInRegistryHolder().is(BlockTags.IRON_ORES) ||
               blk.builtInRegistryHolder().is(BlockTags.GOLD_ORES) ||
               blk.builtInRegistryHolder().is(BlockTags.COPPER_ORES) ||
               blk.builtInRegistryHolder().is(BlockTags.REDSTONE_ORES) ||
               blk.builtInRegistryHolder().is(BlockTags.LAPIS_ORES) ||
               blk.builtInRegistryHolder().is(BlockTags.DIAMOND_ORES) ||
               blk.builtInRegistryHolder().is(BlockTags.EMERALD_ORES) ||
               blk.builtInRegistryHolder().is(BlockTags.COAL_ORES);
    }

    private void tryConsumeFuel() {
        if (fuel > 0) return;
        ItemStack fuelStack = itemHandler.getStackInSlot(0);
        if (!fuelStack.isEmpty()) {
            fuel = 200;
            itemHandler.extractItem(0, 1, false);
            // Don't reset scan progress — resume from where we left off if interrupted
            hasScanned = false;
            setChanged();
        }
    }

    public List<ItemStack> getScannedOres() {
        return scannedOres;
    }

    public boolean hasScanned() {
        return hasScanned;
    }

    public ItemStackHandler getItemHandler() {
        return itemHandler;
    }

    public int getFuel() {
        return fuel;
    }

    public int getScanProgress() {
        return currentZ * 16 + currentX;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, BasicMinerTileEntity pEntity) {
        if (level.isClientSide()) return;

        if (pEntity.fuel <= 0) {
            pEntity.tryConsumeFuel();
        }

        if (!pEntity.hasScanned()) {
            if (pEntity.fuel > 0) {
                pEntity.scanStep((ServerLevel) level);
                setChanged(level, pos, state);
            }
        }
    }

    // ========== Container implementation ==========
    @Override
    public boolean isEmpty() {
        for (int i = 1; i < itemHandler.getSlots(); i++) {
            if (!itemHandler.getStackInSlot(i).isEmpty()) return false;
        }
        return true;
    }

    @Override
    public int getContainerSize() {
        return 5;
    }

    @Override
    public ItemStack getItem(int index) {
        return itemHandler.getStackInSlot(index);
    }

    @Override
    public ItemStack removeItem(int index, int count) {
        return itemHandler.extractItem(index, count, false);
    }

    @Override
    public ItemStack removeItemNoUpdate(int index) {
        ItemStack stack = itemHandler.getStackInSlot(index);
        itemHandler.setStackInSlot(index, ItemStack.EMPTY);
        return stack;
    }

    @Override
    public void setItem(int index, ItemStack stack) {
        itemHandler.setStackInSlot(index, stack);
    }

    @Override
    public boolean stillValid(Player player) {
        return player.distanceToSqr((double) getBlockPos().getX() + 0.5D,
                (double) getBlockPos().getY() + 0.5D,
                (double) getBlockPos().getZ() + 0.5D) <= 64.0D;
    }

    @Override
    public void clearContent() {
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            itemHandler.setStackInSlot(i, ItemStack.EMPTY);
        }
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        return side == Direction.DOWN ? new int[]{1, 2, 3, 4} : new int[]{0};
    }

    @Override
    public boolean canPlaceItemThroughFace(int index, ItemStack stack, Direction direction) {
        return direction != Direction.DOWN && index == 0 && (stack.is(net.minecraft.world.item.Items.COAL) || stack.is(net.minecraft.world.item.Items.CHARCOAL));
    }

    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
        return direction == Direction.DOWN && index > 0;
    }
}
