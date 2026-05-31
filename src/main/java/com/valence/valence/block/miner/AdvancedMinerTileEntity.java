package com.valence.valence.block.miner;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
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
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import net.minecraftforge.items.wrapper.SidedInvWrapper;

public class AdvancedMinerTileEntity extends BlockEntity implements WorldlyContainer, MenuProvider {
    private int fuel = 0;
    private int scanX = 0;
    private int scanZ = 0;
    private final Set<Block> foundOres = new HashSet<>();

    // Slot 0 = Fuel input, Slots 1-8 = Output slots
    private final ItemStackHandler itemHandler = new ItemStackHandler(9) {
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
                return stack.getBurnTime(RecipeType.SMELTING) > 0;
            }
            return false;
        }
    };

    // Sided wrappers for mod compatibility (Create funnels, EnderIO conduits, etc.)
    private final Map<Direction, LazyOptional<IItemHandler>> sidedWrappers = new EnumMap<>(Direction.class);
    private final LazyOptional<IItemHandler> unsidedWrapper = LazyOptional.of(() -> new InvWrapper(this));

    public AdvancedMinerTileEntity(BlockPos pos, BlockState state) {
        super(Registration.ADVANCED_MINER_TE.get(), pos, state);
    }

    public AdvancedMinerTileEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("Items")) {
            itemHandler.deserializeNBT(tag.getCompound("Items"));
        }
        fuel = tag.getInt("fuel");
        scanX = tag.getInt("scanX");
        scanZ = tag.getInt("scanZ");

        foundOres.clear();
        if (tag.contains("FoundOres")) {
            ListTag list = tag.getList("FoundOres", 8);
            for (int i = 0; i < list.size(); i++) {
                String id = list.getString(i);
                Block block = net.minecraftforge.registries.ForgeRegistries.BLOCKS.getValue(
                    new net.minecraft.resources.ResourceLocation(id));
                if (block != null && block != net.minecraft.world.level.block.Blocks.AIR) {
                    foundOres.add(block);
                }
            }
        }
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Items", itemHandler.serializeNBT());
        tag.putInt("fuel", fuel);
        tag.putInt("scanX", scanX);
        tag.putInt("scanZ", scanZ);

        ListTag list = new ListTag();
        for (Block block : foundOres) {
            list.add(StringTag.valueOf(
                net.minecraftforge.registries.ForgeRegistries.BLOCKS.getKey(block).toString()));
        }
        tag.put("FoundOres", list);
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

    public int getScanProgress() {
        return scanZ * 16 + scanX;
    }

    public ItemStackHandler getItemHandler() {
        return itemHandler;
    }

    public boolean hasFuel() {
        return fuel > 0;
    }

    private void tryConsumeFuel() {
        if (fuel > 0) return;
        ItemStack fuelStack = itemHandler.getStackInSlot(0);
        if (!fuelStack.isEmpty()) {
            int burnTime = net.minecraftforge.common.ForgeHooks.getBurnTime(fuelStack, RecipeType.SMELTING);
            if (burnTime > 0) {
                fuel = burnTime;
                itemHandler.extractItem(0, 1, false);
                scanX = 0;
                scanZ = 0;
                foundOres.clear();
            }
        }
    }

    public void scanOneColumn(ServerLevel lvl) {
        if (lvl == null || fuel <= 0) return;

        BlockPos p = this.getBlockPos();
        if (p == null) return;

        int chunkStartX = (p.getX() >> 4) << 4;
        int chunkStartZ = (p.getZ() >> 4) << 4;

        // Scan one vertical column per call (384 blocks per tick)
        for (int y = -64; y < 320; y++) {
            BlockPos checkPos = new BlockPos(chunkStartX + scanX, y, chunkStartZ + scanZ);
            BlockState bs = lvl.getBlockState(checkPos);
            if (isOre(bs) && !foundOres.contains(bs.getBlock()) && foundOres.size() < 8) {
                foundOres.add(bs.getBlock());
            }
        }

        scanX++;
        if (scanX >= 16) {
            scanX = 0;
            scanZ++;
        }

        // Full 16x16 cycle complete — output ores and consume fuel
        if (scanZ >= 16) {
            scanZ = 0;
            for (Block ore : foundOres) {
                // Try to merge with existing stacks first, then find empty slot
                boolean placed = false;
                for (int slot = 1; slot < 9; slot++) {
                    ItemStack stack = itemHandler.getStackInSlot(slot);
                    if (!stack.isEmpty() && stack.is(ore.asItem()) && stack.getCount() < stack.getMaxStackSize()) {
                        itemHandler.setStackInSlot(slot, new ItemStack(ore, stack.getCount() + 1));
                        placed = true;
                        break;
                    }
                }
                if (!placed) {
                    for (int slot = 1; slot < 9; slot++) {
                        if (itemHandler.getStackInSlot(slot).isEmpty()) {
                            itemHandler.setStackInSlot(slot, new ItemStack(ore, 1));
                            placed = true;
                            break;
                        }
                    }
                }
            }
            foundOres.clear();
            fuel--;
            if (fuel <= 0) {
                tryConsumeFuel();
            }
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

        if (pEntity.fuel <= 0) {
            pEntity.tryConsumeFuel();
        }

        if (pEntity.fuel > 0) {
            pEntity.scanOneColumn((ServerLevel) level);
            setChanged(level, pos, state);
        }
    }

    // ========== Container implementation ==========
    @Override
    public boolean isEmpty() {
        for (int i = 1; i < 9; i++) {
            if (!itemHandler.getStackInSlot(i).isEmpty()) return false;
        }
        return true;
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        return side == Direction.DOWN ? new int[]{1, 2, 3, 4, 5, 6, 7, 8} : new int[]{0};
    }

    @Override
    public boolean canPlaceItemThroughFace(int index, ItemStack stack, Direction direction) {
        return direction != Direction.DOWN && index == 0;
    }

    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
        return direction == Direction.DOWN && index > 0;
    }

    @Override
    public int getContainerSize() {
        return 9;
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
