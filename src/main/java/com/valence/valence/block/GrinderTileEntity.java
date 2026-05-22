package com.valence.valence.block;

import com.valence.valence.Registration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GrinderTileEntity extends BlockEntity implements MenuProvider {
    private final ItemStackHandler itemHandler = new ItemStackHandler(2) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return switch (slot) {
                case 0 -> true; // Input
                case 1 -> false; // Output
                default -> super.isItemValid(slot, stack);
            };
        }
    };

    private final LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.of(() -> itemHandler);

    private int progress = 0;
    private int maxProgress = 200;

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
            return lazyItemHandler.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        itemHandler.deserializeNBT(nbt.getCompound("inventory"));
        progress = nbt.getInt("grinder.progress");
    }

    @Override
    public void saveAdditional(CompoundTag nbt) {
        super.saveAdditional(nbt);
        nbt.put("inventory", itemHandler.serializeNBT());
        nbt.putInt("grinder.progress", progress);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, GrinderTileEntity pEntity) {
        if (level.isClientSide()) {
            return;
        }

        if (pEntity.hasRecipe()) {
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
    }

    private void craftItem() {
        ItemStack input = itemHandler.getStackInSlot(0);
        ItemStack result = getResultFor(input);

        itemHandler.extractItem(0, 1, false);
        itemHandler.setStackInSlot(1, new ItemStack(result.getItem(),
                itemHandler.getStackInSlot(1).getCount() + result.getCount()));

        resetProgress();
    }

    private boolean hasRecipe() {
        ItemStack input = itemHandler.getStackInSlot(0);
        ItemStack result = getResultFor(input);

        return !result.isEmpty() && canInsertAmountIntoOutputSlot(result.getCount())
                && canInsertItemIntoOutputSlot(result.getItem());
    }

    private ItemStack getResultFor(ItemStack input) {
        if (input.getItem() == Items.IRON_ORE || input.getItem() == Items.DEEPSLATE_IRON_ORE || input.getItem() == Items.RAW_IRON) {
            return new ItemStack(Registration.IRON_POWDER.get(), 2);
        } else if (input.getItem() == Items.GOLD_ORE || input.getItem() == Items.DEEPSLATE_GOLD_ORE || input.getItem() == Items.RAW_GOLD) {
            return new ItemStack(Registration.GOLD_POWDER.get(), 2);
        } else if (input.getItem() == Items.REDSTONE_ORE || input.getItem() == Items.DEEPSLATE_REDSTONE_ORE) {
            return new ItemStack(Registration.REDSTONE_POWDER.get(), 20); // User requested 18-20
        } else if (input.getItem() == Items.LAPIS_ORE || input.getItem() == Items.DEEPSLATE_LAPIS_ORE) {
            return new ItemStack(Registration.LAPIS_POWDER.get(), 20); // User requested 18-20
        }
        return ItemStack.EMPTY;
    }

    private boolean canInsertItemIntoOutputSlot(net.minecraft.world.item.Item item) {
        return itemHandler.getStackInSlot(1).isEmpty() || itemHandler.getStackInSlot(1).is(item);
    }

    private boolean canInsertAmountIntoOutputSlot(int count) {
        return itemHandler.getStackInSlot(1).getMaxStackSize() >=
                itemHandler.getStackInSlot(1).getCount() + count;
    }

    public ItemStackHandler getItemHandler() {
        return itemHandler;
    }
}
