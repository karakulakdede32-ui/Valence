package com.valence.valence.block.efurnace;

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
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class ElectricFurnaceTileEntity extends BlockEntity implements MenuProvider {
    public static final int SLOT_COUNT = 8;
    public static final int DF_CAPACITY = 500;
    public static final int DF_PER_TICK = 5;
    public static final int PROGRESS_MAX = 100;

    private final DFStorage dfStorage = new DFStorage(DF_CAPACITY, DF_PER_TICK * 2, 0) {
        @Override protected void onEnergyChanged() { setChanged(); sync(); }
    };

    private final ItemStackHandler itemHandler = new ItemStackHandler(SLOT_COUNT * 2) {
        @Override public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            if (slot >= SLOT_COUNT) return false; // output slots
            return true;
        }
        @Override protected void onContentsChanged(int slot) { setChanged(); }
    };

    private final int[] progress = new int[SLOT_COUNT];
    private boolean balanceMode = false;
    private final LazyOptional<IEnergyStorage> energyHandler = LazyOptional.of(() -> dfStorage);

    public ElectricFurnaceTileEntity(BlockPos pos, BlockState state) {
        super(Registration.ELECTRIC_FURNACE_TE.get(), pos, state);
    }

    private void sync() {
        if (level != null && !level.isClientSide()) level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
    }

    @Override public Component getDisplayName() { return Component.translatable("container.valence.electric_furnace"); }

    @Nullable @Override
    public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
        return new ElectricFurnaceMenu(id, inv, this);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        dfStorage.deserializeNBT(tag.get("df_storage"));
        itemHandler.deserializeNBT(tag.getCompound("items"));
        balanceMode = tag.getBoolean("balance_mode");
        int[] prog = tag.getIntArray("progress");
        for (int i = 0; i < SLOT_COUNT && i < prog.length; i++) progress[i] = prog[i];
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("df_storage", dfStorage.serializeNBT());
        tag.put("items", itemHandler.serializeNBT());
        tag.putBoolean("balance_mode", balanceMode);
        tag.putIntArray("progress", progress);
    }

    @Override public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        tag.put("df_storage", dfStorage.serializeNBT());
        tag.put("items", itemHandler.serializeNBT());
        tag.putIntArray("progress", progress);
        return tag;
    }

    @Override public void handleUpdateTag(CompoundTag tag) {
        super.handleUpdateTag(tag);
        dfStorage.deserializeNBT(tag.get("df_storage"));
        itemHandler.deserializeNBT(tag.getCompound("items"));
        balanceMode = tag.getBoolean("balance_mode");
        int[] prog = tag.getIntArray("progress");
        for (int i = 0; i < SLOT_COUNT && i < prog.length; i++) progress[i] = prog[i];
    }

    @Nullable @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        CompoundTag tag = pkt.getTag();
        if (tag != null) {
            dfStorage.deserializeNBT(tag.get("df_storage"));
            itemHandler.deserializeNBT(tag.getCompound("items"));
            balanceMode = tag.getBoolean("balance_mode");
        int[] prog = tag.getIntArray("progress");
            for (int i = 0; i < SLOT_COUNT && i < prog.length; i++) progress[i] = prog[i];
        }
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ENERGY) return energyHandler.cast();
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() { super.invalidateCaps(); energyHandler.invalidate(); }

    public static void tick(Level level, BlockPos pos, BlockState state, ElectricFurnaceTileEntity te) {
        if (level.isClientSide()) return;

        // Pull DF from neighbors
        if (te.dfStorage.getDF() < te.dfStorage.getMaxDF()) {
            for (Direction dir : Direction.values()) {
                int missing = te.dfStorage.getMaxDF() - te.dfStorage.getDF();
                if (missing <= 0) break;
                BlockEntity neighbor = level.getBlockEntity(pos.relative(dir));
                if (neighbor == null) continue;
                int request = Math.min(DF_PER_TICK, missing);
                neighbor.getCapability(ForgeCapabilities.ENERGY, dir.getOpposite()).ifPresent(handler -> {
                    int extracted = handler.extractEnergy(request, false);
                    if (extracted > 0) te.dfStorage.receiveEnergy(extracted, false);
                });
            }
        }

        boolean anyActive = false;

        for (int i = 0; i < SLOT_COUNT; i++) {
            int inputSlot = i;
            int outputSlot = i + SLOT_COUNT;

            ItemStack input = te.itemHandler.getStackInSlot(inputSlot);
            ItemStack output = te.itemHandler.getStackInSlot(outputSlot);

            if (input.isEmpty()) {
                te.progress[i] = 0;
                continue;
            }

            // Find smelting recipe
            SimpleContainer inv = new SimpleContainer(input);
            Optional<SmeltingRecipe> recipe = level.getRecipeManager().getRecipeFor(RecipeType.SMELTING, inv, level);

            if (recipe.isEmpty()) {
                te.progress[i] = 0;
                continue;
            }

            ItemStack result = recipe.get().getResultItem(level.registryAccess());
            if (result.isEmpty()) {
                te.progress[i] = 0;
                continue;
            }

            // Check output space
            boolean canOutput = output.isEmpty() ||
                (output.is(result.getItem()) && ItemStack.isSameItemSameTags(output, result) &&
                 output.getCount() + result.getCount() <= output.getMaxStackSize());

            if (!canOutput) {
                te.progress[i] = 0;
                continue;
            }

            anyActive = true;

            // Consume DF and progress
            if (te.dfStorage.getDF() >= DF_PER_TICK) {
                te.dfStorage.consumeDF(DF_PER_TICK, false);
                te.progress[i]++;

                if (te.progress[i] >= PROGRESS_MAX) {
                    // Smelt complete
                    if (output.isEmpty()) {
                        te.itemHandler.setStackInSlot(outputSlot, result.copy());
                    } else {
                        output.grow(result.getCount());
                    }
                    input.shrink(1);
                    if (input.isEmpty()) te.itemHandler.setStackInSlot(inputSlot, ItemStack.EMPTY);
                    te.progress[i] = 0;
                }
            }
        }

        if (anyActive) te.setChanged();
    }

    public void toggleBalanceMode() { balanceMode = !balanceMode; setChanged(); sync(); }
    public boolean isBalanceMode() { return balanceMode; }

    public int[] getProgress() { return progress; }
    public DFStorage getDFStorage() { return dfStorage; }
    public ItemStackHandler getItemHandler() { return itemHandler; }
}
