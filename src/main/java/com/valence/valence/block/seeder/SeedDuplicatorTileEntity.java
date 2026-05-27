package com.valence.valence.block.seeder;

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
import net.minecraft.world.item.BlockItem;
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

import java.util.HashSet;
import java.util.Set;

public class SeedDuplicatorTileEntity extends BlockEntity implements MenuProvider {
    public static final int DF_CAPACITY = 50;
    public static final int DF_PER_USE = 50;

    private final DFStorage dfStorage = new DFStorage(DF_CAPACITY, 10, 0) {
        @Override protected void onEnergyChanged() { setChanged(); sync(); }
    };

    private final ItemStackHandler itemHandler = new ItemStackHandler(2) {
        @Override public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            if (slot == 1) return false; // output slot — no manual insertion
            return isSeed(stack);
        }
        @Override protected void onContentsChanged(int slot) { setChanged(); }
    };

    // Side-aware item handlers
    // Top side: full access (insert input, extract output)
    // Bottom/sides: extraction-only from output slot
    private final LazyOptional<IEnergyStorage> energyHandler = LazyOptional.of(() -> dfStorage);
    private final LazyOptional<IItemHandler> fullHandler = LazyOptional.of(() -> itemHandler);
    private final LazyOptional<IItemHandler> outputOnlyHandler = LazyOptional.of(() -> new IItemHandler() {
        @Override public int getSlots() { return itemHandler.getSlots(); }
        @Override public @NotNull ItemStack getStackInSlot(int slot) { return itemHandler.getStackInSlot(slot); }
        @Override public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            // Only allow insertion into input slot (0)
            if (slot == 0 && isSeed(stack)) return itemHandler.insertItem(slot, stack, simulate);
            return stack;
        }
        @Override public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            // Only allow extraction from output slot (1) — protect input seeds
            if (slot != 1) return ItemStack.EMPTY;
            return itemHandler.extractItem(slot, amount, simulate);
        }
        @Override public int getSlotLimit(int slot) { return itemHandler.getSlotLimit(slot); }
        @Override public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return slot == 0 && isSeed(stack);
        }
    });

    public SeedDuplicatorTileEntity(BlockPos pos, BlockState state) {
        super(Registration.SEED_DUPLICATOR_TE.get(), pos, state);
    }

    private void sync() {
        if (level != null && !level.isClientSide()) level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
    }

    @Override public Component getDisplayName() { return Component.translatable("container.valence.seed_duplicator"); }

    @Nullable @Override
    public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
        return new SeedDuplicatorMenu(id, inv, this);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        dfStorage.deserializeNBT(tag.get("df_storage"));
        itemHandler.deserializeNBT(tag.getCompound("items"));
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("df_storage", dfStorage.serializeNBT());
        tag.put("items", itemHandler.serializeNBT());
    }

    @Override public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        tag.put("df_storage", dfStorage.serializeNBT());
        tag.put("items", itemHandler.serializeNBT());
        return tag;
    }

    @Override public void handleUpdateTag(CompoundTag tag) {
        super.handleUpdateTag(tag);
        dfStorage.deserializeNBT(tag.get("df_storage"));
        itemHandler.deserializeNBT(tag.getCompound("items"));
    }

    @Nullable @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        CompoundTag tag = pkt.getTag();
        if (tag != null) {
            dfStorage.deserializeNBT(tag.get("df_storage"));
            itemHandler.deserializeNBT(tag.getCompound("items"));
        }
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ENERGY) return energyHandler.cast();
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            if (side == Direction.UP) return fullHandler.cast(); // Top: full access
            return outputOnlyHandler.cast(); // Bottom/sides: extraction only
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        energyHandler.invalidate();
        fullHandler.invalidate();
        outputOnlyHandler.invalidate();
    }

    private static final Set<Block> SEED_BLOCKS = new HashSet<>();

    static {
        SEED_BLOCKS.add(Blocks.WHEAT);
        SEED_BLOCKS.add(Blocks.CARROTS);
        SEED_BLOCKS.add(Blocks.POTATOES);
        SEED_BLOCKS.add(Blocks.BEETROOTS);
        SEED_BLOCKS.add(Blocks.PUMPKIN_STEM);
        SEED_BLOCKS.add(Blocks.MELON_STEM);
        SEED_BLOCKS.add(Blocks.TORCHFLOWER_CROP);
        SEED_BLOCKS.add(Blocks.PITCHER_CROP);
        SEED_BLOCKS.add(Blocks.NETHER_WART);
    }

    private static boolean isSeed(ItemStack stack) {
        if (stack.isEmpty()) return false;
        if (stack.is(Items.WHEAT_SEEDS)) return true;
        if (stack.is(Items.CARROT)) return true;
        if (stack.is(Items.POTATO)) return true;
        if (stack.is(Items.BEETROOT_SEEDS)) return true;
        if (stack.is(Items.PUMPKIN_SEEDS)) return true;
        if (stack.is(Items.MELON_SEEDS)) return true;
        if (stack.is(Items.TORCHFLOWER_SEEDS)) return true;
        if (stack.is(Items.PITCHER_POD)) return true;
        if (stack.is(Items.NETHER_WART)) return true;
        if (stack.getItem() instanceof BlockItem bi && SEED_BLOCKS.contains(bi.getBlock())) return true;
        return false;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, SeedDuplicatorTileEntity te) {
        if (level.isClientSide()) return;

        // Pull DF from neighbors
        if (te.dfStorage.getDF() < te.dfStorage.getMaxDF()) {
            for (Direction dir : Direction.values()) {
                BlockEntity neighbor = level.getBlockEntity(pos.relative(dir));
                if (neighbor == null) continue;
                int missing = te.dfStorage.getMaxDF() - te.dfStorage.getDF();
                neighbor.getCapability(ForgeCapabilities.ENERGY, dir.getOpposite()).ifPresent(handler -> {
                    int pulled = te.dfStorage.receiveEnergy(handler.extractEnergy(missing, false), false);
                });
                if (te.dfStorage.getDF() >= te.dfStorage.getMaxDF()) break;
            }
        }

        // Duplicate seeds
        ItemStack input = te.itemHandler.getStackInSlot(0);
        ItemStack output = te.itemHandler.getStackInSlot(1);

        if (!input.isEmpty() && te.dfStorage.getDF() >= DF_PER_USE) {
            ItemStack result = input.copy();
            result.setCount(2);
            boolean canFit = output.isEmpty() || (output.is(result.getItem()) && ItemStack.isSameItemSameTags(input, output) && output.getCount() + 2 <= output.getMaxStackSize());

            if (canFit) {
                te.dfStorage.consumeDF(DF_PER_USE, false);

                if (output.isEmpty()) {
                    te.itemHandler.setStackInSlot(1, result);
                } else {
                    output.grow(2);
                }

                input.shrink(1);
                if (input.isEmpty()) te.itemHandler.setStackInSlot(0, ItemStack.EMPTY);
            }
        }
    }

    public DFStorage getDFStorage() { return dfStorage; }
    public ItemStackHandler getItemHandler() { return itemHandler; }
}
