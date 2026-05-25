package com.valence.valence.block;

import com.valence.valence.Registration;
import com.valence.valence.recipe.GrinderRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import net.minecraftforge.items.wrapper.SidedInvWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class GrinderTileEntity extends BlockEntity implements MenuProvider, WorldlyContainer {
    private final ItemStackHandler itemHandler = new ItemStackHandler(2) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            if (level != null && !level.isClientSide()) {
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
            }
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return switch (slot) {
                case 0 -> true;
                case 1 -> false;
                default -> super.isItemValid(slot, stack);
            };
        }
    };

    // Sided wrappers for mod compatibility (Create funnels, EnderIO conduits, etc.)
    private final Map<Direction, LazyOptional<IItemHandler>> sidedWrappers = new EnumMap<>(Direction.class);
    private final LazyOptional<IItemHandler> unsidedWrapper = LazyOptional.of(() -> new InvWrapper(this));

    private int progress = 0;
    private int maxProgress = 0;

    public GrinderTileEntity(BlockPos pos, BlockState state) {
        super(Registration.GRINDER_TE.get(), pos, state);
    }

    @Override
    public Component getDisplayName() {
        return Component.literal("Grinder");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new GrinderMenu(id, inventory, this);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
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
    public void load(CompoundTag nbt) {
        super.load(nbt);
        itemHandler.deserializeNBT(nbt.getCompound("inventory"));
        progress = nbt.getInt("grinder.progress");
        maxProgress = nbt.getInt("grinder.max_progress");
    }

    @Override
    public void saveAdditional(CompoundTag nbt) {
        super.saveAdditional(nbt);
        nbt.put("inventory", itemHandler.serializeNBT());
        nbt.putInt("grinder.progress", progress);
        nbt.putInt("grinder.max_progress", maxProgress);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, GrinderTileEntity pEntity) {
        if (level.isClientSide()) {
            return;
        }

        if (pEntity.hasRecipe()) {
            pEntity.getCurrentRecipe().ifPresent(recipe -> pEntity.maxProgress = recipe.getProcessingTime());
            pEntity.progress++;
            setChanged(level, pos, state);

            if (pEntity.progress >= pEntity.maxProgress) {
                pEntity.craftItem();
            }
        } else {
            pEntity.resetProgress();
            setChanged(level, pos, state);
        }
    }

    private void resetProgress() {
        this.progress = 0;
        this.maxProgress = 0;
    }

    private Optional<GrinderRecipe> getCurrentRecipe() {
        if (level == null) {
            return Optional.empty();
        }

        return level.getRecipeManager().getRecipeFor(GrinderRecipe.Type.INSTANCE, asRecipeContainer(), level);
    }

    private SimpleContainer asRecipeContainer() {
        return new SimpleContainer(itemHandler.getStackInSlot(0), itemHandler.getStackInSlot(1));
    }

    private void craftItem() {
        if (level == null) {
            return;
        }

        getCurrentRecipe().ifPresent(recipe -> {
            ItemStack result = recipe.assemble(asRecipeContainer(), level.registryAccess());
            if (result.isEmpty()) {
                resetProgress();
                return;
            }

            if (!canInsertItemIntoOutputSlot(result.getItem()) || !canInsertAmountIntoOutputSlot(result.getCount())) {
                resetProgress();
                return;
            }

            itemHandler.extractItem(0, 1, false);
            ItemStack output = itemHandler.getStackInSlot(1);
            if (output.isEmpty()) {
                itemHandler.setStackInSlot(1, result.copy());
            } else {
                itemHandler.setStackInSlot(1, new ItemStack(result.getItem(), output.getCount() + result.getCount()));
            }
            resetProgress();
        });
    }

    private boolean hasRecipe() {
        if (level == null) {
            return false;
        }

        return getCurrentRecipe().map(recipe -> {
            ItemStack result = recipe.assemble(asRecipeContainer(), level.registryAccess());
            return !result.isEmpty()
                    && canInsertItemIntoOutputSlot(result.getItem())
                    && canInsertAmountIntoOutputSlot(result.getCount());
        }).orElse(false);
    }

    private boolean canInsertItemIntoOutputSlot(net.minecraft.world.item.Item item) {
        return itemHandler.getStackInSlot(1).isEmpty() || itemHandler.getStackInSlot(1).is(item);
    }

    private boolean canInsertAmountIntoOutputSlot(int count) {
        ItemStack outputStack = itemHandler.getStackInSlot(1);
        if (outputStack.isEmpty()) {
            return true;
        }
        return outputStack.getMaxStackSize() >= outputStack.getCount() + count;
    }

    public int getProgress() { return progress; }
    public int getMaxProgress() { return maxProgress; }

    public ItemStackHandler getItemHandler() {
        return itemHandler;
    }

    @Override
    public int getContainerSize() {
        return 2;
    }

    @Override
    public boolean isEmpty() {
        return itemHandler.getStackInSlot(0).isEmpty() && itemHandler.getStackInSlot(1).isEmpty();
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
        setChanged();
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
        return side == Direction.DOWN ? new int[]{1} : new int[]{0};
    }

    @Override
    public boolean canPlaceItemThroughFace(int index, ItemStack stack, Direction direction) {
        return direction != Direction.DOWN && index == 0;
    }

    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
        return direction == Direction.DOWN && index == 1;
    }
}
