package com.valence.valence.block.miner;

import com.valence.valence.Registration;
import com.valence.valence.energy.DFStorage;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.tags.BlockTags;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import net.minecraftforge.items.wrapper.SidedInvWrapper;

public class AdvancedMinerTileEntity extends BlockEntity implements WorldlyContainer, MenuProvider {
    public static final int DF_CAPACITY = 10000;
    public static final int DF_PER_SCAN = 5;
    public static final int MAX_OUTPUT_SLOTS = 12;

    private final DFStorage dfStorage = new DFStorage(DF_CAPACITY, 100, 0) {
        @Override protected void onEnergyChanged() { setChanged(); sync(); }
    };

    private int fuel = 0;
    private int scanX = 0;
    private int scanZ = 0;
    private final Set<Block> foundOres = new HashSet<>();

    // Slot 0 = Fuel input, Slots 1-12 = Output slots
    private final ItemStackHandler itemHandler = new ItemStackHandler(MAX_OUTPUT_SLOTS + 1) {
        @Override
        public void onContentsChanged(int slot) {
            setChanged();
            if (level != null && !level.isClientSide()) {
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
            }
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            if (slot == 0) {
                return stack.getBurnTime(RecipeType.SMELTING) > 0
                    || stack.is(Items.SUGAR_CANE)
                    || stack.is(Items.CHARCOAL)
                    || stack.is(Items.COAL)
                    || stack.is(Items.COAL_BLOCK);
            }
            return false;
        }
    };

    // Sided wrappers for mod compatibility
    private final Map<Direction, LazyOptional<IItemHandler>> sidedWrappers = new EnumMap<>(Direction.class);
    private final LazyOptional<IItemHandler> unsidedWrapper = LazyOptional.of(() -> new InvWrapper(this));
    private final LazyOptional<IEnergyStorage> energyHandler = LazyOptional.of(() -> dfStorage);

    public AdvancedMinerTileEntity(BlockPos pos, BlockState state) {
        super(Registration.ADVANCED_MINER_TE.get(), pos, state);
    }

    public AdvancedMinerTileEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    private void sync() {
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("Items")) {
            itemHandler.deserializeNBT(tag.getCompound("Items"));
        }
        dfStorage.deserializeNBT(tag.get("df"));
        fuel = tag.getInt("fuel");
        scanX = tag.getInt("scanX");
        scanZ = tag.getInt("scanZ");
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Items", itemHandler.serializeNBT());
        tag.put("df", dfStorage.serializeNBT());
        tag.putInt("fuel", fuel);
        tag.putInt("scanX", scanX);
        tag.putInt("scanZ", scanZ);
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
        if (cap == ForgeCapabilities.ENERGY) {
            return energyHandler.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        unsidedWrapper.invalidate();
        energyHandler.invalidate();
        for (LazyOptional<IItemHandler> lo : sidedWrappers.values()) {
            lo.invalidate();
        }
        sidedWrappers.clear();
    }

    @Override
    public Component getDisplayName() {
        return Component.literal("Advanced Miner");
    }

    @Override
    public net.minecraft.world.inventory.AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
        return new AdvancedMinerMenu(id, inv, this);
    }

    public int getMaxFuel() { return 1600; }

    public int getFuel() {
        return fuel;
    }

    public DFStorage getDFStorage() { return dfStorage; }

    public int getScanProgress() {
        return scanZ * 16 + scanX;
    }

    public ItemStackHandler getItemHandler() {
        return itemHandler;
    }

    public boolean hasFuel() {
        return fuel > 0 || dfStorage.getDF() >= DF_PER_SCAN;
    }

    private void tryConsumeFuel() {
        if (fuel > 0) return;
        ItemStack fuelStack = itemHandler.getStackInSlot(0);
        if (!fuelStack.isEmpty()) {
            int burnTime = net.minecraftforge.common.ForgeHooks.getBurnTime(fuelStack, RecipeType.SMELTING);
            if (burnTime <= 0 && fuelStack.is(Items.SUGAR_CANE)) burnTime = 100;
            // Also accept coal/charcoal directly
            if (burnTime <= 0 && (fuelStack.is(Items.COAL) || fuelStack.is(Items.CHARCOAL) || fuelStack.is(Items.COAL_BLOCK))) {
                burnTime = fuelStack.is(Items.COAL_BLOCK) ? 16000 : 1600;
            }
            fuel = Math.max(burnTime, 100);
            itemHandler.extractItem(0, 1, false);
            setChanged();
        }
    }

    public void scanOneColumn(ServerLevel lvl) {
        if (lvl == null) return;

        BlockPos p = this.getBlockPos();
        if (p == null) return;

        int chunkStartX = (p.getX() >> 4) << 4;
        int chunkStartZ = (p.getZ() >> 4) << 4;

        for (int y = -64; y < 320; y++) {
            BlockPos checkPos = new BlockPos(chunkStartX + scanX, y, chunkStartZ + scanZ);
            BlockState bs = lvl.getBlockState(checkPos);
            if (isOre(bs) && !foundOres.contains(bs.getBlock()) && foundOres.size() < MAX_OUTPUT_SLOTS) {
                foundOres.add(bs.getBlock());
            }
        }

        scanX++;
        if (scanX >= 16) {
            scanX = 0;
            scanZ++;
        }

        // Full 16x16 cycle complete — output ores and consume fuel/DF
        if (scanZ >= 16) {
            scanZ = 0;
            for (Block ore : foundOres) {
                boolean placed = false;
                for (int slot = 1; slot <= MAX_OUTPUT_SLOTS; slot++) {
                    ItemStack stack = itemHandler.getStackInSlot(slot);
                    if (!stack.isEmpty() && stack.is(ore.asItem()) && stack.getCount() < stack.getMaxStackSize()) {
                        itemHandler.setStackInSlot(slot, new ItemStack(ore, stack.getCount() + 1));
                        placed = true;
                        break;
                    }
                }
                if (!placed) {
                    for (int slot = 1; slot <= MAX_OUTPUT_SLOTS; slot++) {
                        if (itemHandler.getStackInSlot(slot).isEmpty()) {
                            itemHandler.setStackInSlot(slot, new ItemStack(ore, 1));
                            placed = true;
                            break;
                        }
                    }
                }
            }
            foundOres.clear();
        }

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

    public static void tick(Level level, BlockPos pos, BlockState state, AdvancedMinerTileEntity pEntity) {
        if (level.isClientSide()) return;

        // Pull DF from neighbors
        if (pEntity.dfStorage.getDF() < pEntity.dfStorage.getMaxDF()) {
            for (Direction d : Direction.values()) {
                int need = pEntity.dfStorage.getMaxDF() - pEntity.dfStorage.getDF();
                if (need <= 0) break;
                BlockEntity nb = level.getBlockEntity(pos.relative(d));
                if (nb == null) continue;
                int req = Math.min(50, need);
                nb.getCapability(ForgeCapabilities.ENERGY, d.getOpposite()).ifPresent(h -> {
                    int got = h.extractEnergy(req, false);
                    if (got > 0) pEntity.dfStorage.receiveEnergy(got, false);
                });
            }
        }

        // Try consuming fuel if DF is low
        if (pEntity.dfStorage.getDF() < DF_PER_SCAN) {
            if (pEntity.fuel <= 0) {
                pEntity.tryConsumeFuel();
            }
        }

        // Use DF first, then fuel
        boolean canScan = false;
        if (pEntity.dfStorage.getDF() >= DF_PER_SCAN) {
            pEntity.dfStorage.consumeDF(DF_PER_SCAN, false);
            canScan = true;
        } else if (pEntity.fuel > 0) {
            canScan = true;
        }

        if (canScan) {
            if (pEntity.scanZ >= 16) {
                pEntity.scanZ = 0;
            }
            pEntity.scanOneColumn((ServerLevel) level);
            // Consume fuel per tick
            if (pEntity.fuel > 0) {
                pEntity.fuel--;
                if (pEntity.fuel <= 0) {
                    pEntity.tryConsumeFuel();
                }
            }
            setChanged(level, pos, state);
        }
    }

    // ========== Container implementation ==========
    @Override
    public boolean isEmpty() {
        for (int i = 1; i <= MAX_OUTPUT_SLOTS; i++) {
            if (!itemHandler.getStackInSlot(i).isEmpty()) return false;
        }
        return true;
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        int[] outSlots = new int[MAX_OUTPUT_SLOTS];
        for (int i = 0; i < MAX_OUTPUT_SLOTS; i++) outSlots[i] = i + 1;
        return side == Direction.DOWN ? outSlots : new int[]{0};
    }

    @Override
    public boolean canPlaceItemThroughFace(int index, ItemStack stack, Direction direction) {
        return direction != Direction.DOWN && index == 0 &&
            (stack.getBurnTime(RecipeType.SMELTING) > 0 ||
             stack.is(Items.SUGAR_CANE) ||
             stack.is(Items.COAL) ||
             stack.is(Items.CHARCOAL) ||
             stack.is(Items.COAL_BLOCK));
    }

    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
        return direction == Direction.DOWN && index > 0;
    }

    @Override
    public int getContainerSize() {
        return MAX_OUTPUT_SLOTS + 1;
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
}
