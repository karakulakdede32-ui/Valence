package com.valence.valence.block.quantumminer;

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
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.*;

public class QuantumMinerTileEntity extends BlockEntity implements MenuProvider, WorldlyContainer {
    public static final int DF_CAPACITY = 10000;
    public static final int DF_PER_SCAN_COLUMN = 5;
    public static final int MAX_OUTPUT_SLOTS = 12;

    private final DFStorage dfStorage = new DFStorage(DF_CAPACITY, 100, 0) {
        @Override protected void onEnergyChanged() { setChanged(); sync(); }
    };

    private int scanX = 0, scanZ = 0;
    private final Set<Block> foundOres = new HashSet<>();
    private final ItemStackHandler itemHandler = new ItemStackHandler(MAX_OUTPUT_SLOTS) {
        @Override public boolean isItemValid(int slot, ItemStack stack) { return false; } // output only
        @Override protected void onContentsChanged(int slot) { setChanged(); sync(); }
    };

    private final Map<Direction, LazyOptional<IItemHandler>> sidedWrappers = new EnumMap<>(Direction.class);
    private final LazyOptional<IItemHandler> unsidedWrapper = LazyOptional.of(() -> new InvWrapper(this));
    private final LazyOptional<IEnergyStorage> energyHandler = LazyOptional.of(() -> dfStorage);

    public QuantumMinerTileEntity(BlockPos pos, BlockState state) { super(Registration.QUANTUM_MINER_TE.get(), pos, state); }

    private void sync() { if (level != null && !level.isClientSide()) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3); }

    @Override public Component getDisplayName() { return Component.translatable("container.valence.quantum_miner"); }
    @Nullable @Override public AbstractContainerMenu createMenu(int id, Inventory inv, Player p) { return new QuantumMinerMenu(id, inv, this); }

    @Override public void load(CompoundTag t) {
        super.load(t); dfStorage.deserializeNBT(t.get("df")); itemHandler.deserializeNBT(t.getCompound("items"));
        scanX = t.getInt("sx"); scanZ = t.getInt("sz");
        foundOres.clear(); var list = t.getList("ores", 8);
        for (int i = 0; i < list.size(); i++) {
            Block b = net.minecraftforge.registries.ForgeRegistries.BLOCKS.getValue(new net.minecraft.resources.ResourceLocation(list.getString(i)));
            if (b != null && b != net.minecraft.world.level.block.Blocks.AIR) foundOres.add(b);
        }
    }
    @Override public void saveAdditional(CompoundTag t) {
        super.saveAdditional(t); t.put("df", dfStorage.serializeNBT()); t.put("items", itemHandler.serializeNBT());
        t.putInt("sx", scanX); t.putInt("sz", scanZ);
        var list = new net.minecraft.nbt.ListTag();
        for (Block b : foundOres) list.add(net.minecraft.nbt.StringTag.valueOf(net.minecraftforge.registries.ForgeRegistries.BLOCKS.getKey(b).toString()));
        t.put("ores", list);
    }
    @Override public CompoundTag getUpdateTag() { CompoundTag t = super.getUpdateTag(); t.put("df", dfStorage.serializeNBT()); t.put("items", itemHandler.serializeNBT()); t.putInt("sx", scanX); t.putInt("sz", scanZ); return t; }
    @Override public void handleUpdateTag(CompoundTag t) { super.handleUpdateTag(t); dfStorage.deserializeNBT(t.get("df")); itemHandler.deserializeNBT(t.getCompound("items")); scanX = t.getInt("sx"); scanZ = t.getInt("sz"); }
    @Nullable @Override public Packet<ClientGamePacketListener> getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }
    @Override public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) { CompoundTag t = pkt.getTag(); if (t != null) { dfStorage.deserializeNBT(t.get("df")); itemHandler.deserializeNBT(t.getCompound("items")); scanX = t.getInt("sx"); scanZ = t.getInt("sz"); } }

    @Override public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ENERGY) return energyHandler.cast();
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            if (side == null) return unsidedWrapper.cast();
            return sidedWrappers.computeIfAbsent(side, s -> LazyOptional.of(() -> new SidedInvWrapper(this, s))).cast();
        }
        return super.getCapability(cap, side);
    }
    @Override public void invalidateCaps() { super.invalidateCaps(); energyHandler.invalidate(); unsidedWrapper.invalidate(); sidedWrappers.values().forEach(LazyOptional::invalidate); sidedWrappers.clear(); }

    // Scans 4x faster than Advanced Miner (4 columns per tick) and outputs 2 of each ore
    public static void tick(Level level, BlockPos pos, BlockState state, QuantumMinerTileEntity te) {
        if (level.isClientSide()) {
            if (te.dfStorage.getDF() > 0 && level.random.nextInt(6) == 0) {
                double x = pos.getX() + 0.5 + (level.random.nextDouble() - 0.5) * 0.6;
                double y = pos.getY() + 0.5 + (level.random.nextDouble() - 0.5) * 0.6;
                double z = pos.getZ() + 0.5 + (level.random.nextDouble() - 0.5) * 0.6;
                level.addParticle(net.minecraft.core.particles.ParticleTypes.END_ROD, x, y, z, 0, 0.01, 0);
            }
            return;
        }

        // Pull DF from neighbors
        if (te.dfStorage.getDF() < te.dfStorage.getMaxDF()) {
            for (Direction d : Direction.values()) {
                int need = te.dfStorage.getMaxDF() - te.dfStorage.getDF();
                if (need <= 0) break;
                BlockEntity nb = level.getBlockEntity(pos.relative(d));
                if (nb == null) continue;
                int req = Math.min(50, need);
                nb.getCapability(ForgeCapabilities.ENERGY, d.getOpposite()).ifPresent(h -> {
                    int got = h.extractEnergy(req, false);
                    if (got > 0) te.dfStorage.receiveEnergy(got, false);
                });
            }
        }

        if (te.dfStorage.getDF() < DF_PER_SCAN_COLUMN) return;

        // Scan 4 columns per tick for speed
        for (int pass = 0; pass < 4; pass++) {
            if (te.scanZ >= 16) break;
            te.scanOneColumn((net.minecraft.server.level.ServerLevel) level);
        }
        te.setChanged();
    }

    private void scanOneColumn(net.minecraft.server.level.ServerLevel lvl) {
        BlockPos p = this.getBlockPos();
        int csx = (p.getX() >> 4) << 4, csz = (p.getZ() >> 4) << 4;

        for (int y = -64; y < 320; y++) {
            BlockState bs = lvl.getBlockState(new BlockPos(csx + scanX, y, csz + scanZ));
            if (isOre(bs) && !foundOres.contains(bs.getBlock()) && foundOres.size() < MAX_OUTPUT_SLOTS) {
                foundOres.add(bs.getBlock());
            }
        }

        dfStorage.consumeDF(DF_PER_SCAN_COLUMN, false);
        scanX++;
        if (scanX >= 16) { scanX = 0; scanZ++; }

        if (scanZ >= 16) {
            scanZ = 0;
            int slot = 0;
            for (Block ore : foundOres) {
                if (slot >= MAX_OUTPUT_SLOTS) break;
                // Try merge then empty slot — outputs 2 per ore type
                ItemStack stack = new ItemStack(ore, 2);
                boolean placed = false;
                for (int s = 0; s < MAX_OUTPUT_SLOTS; s++) {
                    ItemStack existing = itemHandler.getStackInSlot(s);
                    if (!existing.isEmpty() && existing.is(ore.asItem()) && existing.getCount() + 2 <= existing.getMaxStackSize()) {
                        existing.grow(2); placed = true; break;
                    }
                }
                if (!placed) {
                    for (int s = 0; s < MAX_OUTPUT_SLOTS; s++) {
                        if (itemHandler.getStackInSlot(s).isEmpty()) {
                            itemHandler.setStackInSlot(s, stack); placed = true; break;
                        }
                    }
                }
                slot++;
            }
            foundOres.clear();
        }
    }

    private boolean isOre(BlockState state) {
        Block blk = state.getBlock();
        return blk.builtInRegistryHolder().is(BlockTags.IRON_ORES) || blk.builtInRegistryHolder().is(BlockTags.GOLD_ORES) ||
               blk.builtInRegistryHolder().is(BlockTags.COPPER_ORES) || blk.builtInRegistryHolder().is(BlockTags.REDSTONE_ORES) ||
               blk.builtInRegistryHolder().is(BlockTags.LAPIS_ORES) || blk.builtInRegistryHolder().is(BlockTags.DIAMOND_ORES) ||
               blk.builtInRegistryHolder().is(BlockTags.EMERALD_ORES) || blk.builtInRegistryHolder().is(BlockTags.COAL_ORES);
    }

    public DFStorage getDFStorage() { return dfStorage; }
    public ItemStackHandler getItemHandler() { return itemHandler; }
    public int getScanProgress() { return scanZ * 16 + scanX; }
    public int getMaxScanProgress() { return 256; }

    // WorldlyContainer
    @Override public int[] getSlotsForFace(Direction s) { int[] o = new int[MAX_OUTPUT_SLOTS]; for (int i = 0; i < MAX_OUTPUT_SLOTS; i++) o[i] = i; return o; }
    @Override public boolean canPlaceItemThroughFace(int i, ItemStack s, Direction d) { return false; }
    @Override public boolean canTakeItemThroughFace(int i, ItemStack s, Direction d) { return true; }
    @Override public int getContainerSize() { return MAX_OUTPUT_SLOTS; }
    @Override public boolean isEmpty() { for (int i = 0; i < MAX_OUTPUT_SLOTS; i++) if (!itemHandler.getStackInSlot(i).isEmpty()) return false; return true; }
    @Override public ItemStack getItem(int i) { return itemHandler.getStackInSlot(i); }
    @Override public ItemStack removeItem(int i, int c) { return itemHandler.extractItem(i, c, false); }
    @Override public ItemStack removeItemNoUpdate(int i) { ItemStack s = itemHandler.getStackInSlot(i); itemHandler.setStackInSlot(i, ItemStack.EMPTY); return s; }
    @Override public void setItem(int i, ItemStack s) { itemHandler.setStackInSlot(i, s); }
    @Override public boolean stillValid(Player p) { return p.distanceToSqr(getBlockPos().getX()+0.5, getBlockPos().getY()+0.5, getBlockPos().getZ()+0.5) <= 64; }
    @Override public void clearContent() { for (int i = 0; i < itemHandler.getSlots(); i++) itemHandler.setStackInSlot(i, ItemStack.EMPTY); }

    public int getComparatorOutput() {
        // Returns 0-15 based on how full the output slots are
        boolean hasItems = false;
        int totalSlots = 0;
        int filledSlots = 0;
        for (int i = 0; i < getItemHandler().getSlots(); i++) {
            if (!getItemHandler().getStackInSlot(i).isEmpty()) {
                hasItems = true;
                filledSlots++;
            }
            totalSlots++;
        }
        if (!hasItems) return 0;
        // Scale: empty=0, half-filled=8, fully-filled=15
        return Math.max(1, filledSlots * 15 / Math.max(1, totalSlots));
    }

}