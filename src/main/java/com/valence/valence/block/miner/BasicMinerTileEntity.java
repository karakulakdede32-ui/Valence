package com.valence.valence.block.miner;

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

public class BasicMinerTileEntity extends BlockEntity implements WorldlyContainer, MenuProvider {
    private final List<ItemStack> scannedOres = new ArrayList<>();
    private boolean hasScanned = false;
    private int currentX = 0;
    private int currentZ = 0;
    private final Map<Block, Integer> oreCounts = new HashMap<>();

    private final ItemStackHandler itemHandler = new ItemStackHandler(4) {
        @Override
        public void onContentsChanged(int slot) {
            setChanged();
            if (level != null && !level.isClientSide()) {
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
            }
        }
    };

    // Sided wrappers for mod compatibility
    private final Map<Direction, LazyOptional<IItemHandler>> sidedWrappers = new EnumMap<>(Direction.class);
    private final LazyOptional<IItemHandler> unsidedWrapper = LazyOptional.of(() -> new InvWrapper(this));

    public BasicMinerTileEntity(BlockPos pos, BlockState state) {
        this((BlockEntityType<BasicMinerTileEntity>) null, pos, state);
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

        scannedOres.clear();
        if (tag.contains("ScannedOres")) {
            ListTag oreList = tag.getList("ScannedOres", 10);
            for (int i = 0; i < oreList.size(); i++) {
                CompoundTag oreTag = oreList.getCompound(i);
                scannedOres.add(ItemStack.of(oreTag));
            }
        }

        oreCounts.clear();
        if (tag.contains("OreCounts")) {
            ListTag countList = tag.getList("OreCounts", 10);
            for (int i = 0; i < countList.size(); i++) {
                CompoundTag entry = countList.getCompound(i);
                Block block = net.minecraftforge.registries.ForgeRegistries.BLOCKS.getValue(
                    new net.minecraft.resources.ResourceLocation(entry.getString("block")));
                if (block != null) {
                    oreCounts.put(block, entry.getInt("count"));
                }
            }
        }
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Items", itemHandler.serializeNBT());
        tag.putBoolean("hasScanned", hasScanned);
        tag.putInt("currentX", currentX);
        tag.putInt("currentZ", currentZ);

        ListTag oreList = new ListTag();
        for (ItemStack ore : scannedOres) {
            CompoundTag oreTag = new CompoundTag();
            ore.save(oreTag);
            oreList.add(oreTag);
        }
        tag.put("ScannedOres", oreList);

        ListTag countList = new ListTag();
        for (Map.Entry<Block, Integer> entry : oreCounts.entrySet()) {
            CompoundTag entryTag = new CompoundTag();
            entryTag.putString("block", net.minecraftforge.registries.ForgeRegistries.BLOCKS.getKey(entry.getKey()).toString());
            entryTag.putInt("count", entry.getValue());
            countList.add(entryTag);
        }
        tag.put("OreCounts", countList);
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
        }
    }

    private void finalizeScan() {
        scannedOres.clear();
        List<Map.Entry<Block, Integer>> sorted = new ArrayList<>(oreCounts.entrySet());
        sorted.sort((a, b) -> Integer.compare(b.getValue(), a.getValue()));

        int count = 0;
        for (Map.Entry<Block, Integer> entry : sorted) {
            if (count >= 4) break;
            scannedOres.add(new ItemStack(entry.getKey(), entry.getValue()));
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

    public List<ItemStack> getScannedOres() {
        return scannedOres;
    }

    public boolean hasScanned() {
        return hasScanned;
    }

    public ItemStackHandler getItemHandler() {
        return itemHandler;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, BasicMinerTileEntity pEntity) {
        if (level.isClientSide()) return;

        if (!pEntity.hasScanned()) {
            pEntity.scanStep((ServerLevel) level);
            setChanged(level, pos, state);
        }
    }

    // ========== Container implementation ==========
    @Override
    public boolean isEmpty() {
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            if (!itemHandler.getStackInSlot(i).isEmpty()) return false;
        }
        return true;
    }

    @Override
    public int getContainerSize() {
        return 4;
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
        return new int[]{0, 1, 2, 3};
    }

    @Override
    public boolean canPlaceItemThroughFace(int index, ItemStack stack, Direction direction) {
        return false;
    }

    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
        return true;
    }
}
