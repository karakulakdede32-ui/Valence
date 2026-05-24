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
import com.valence.valence.recipe.GrinderRecipe;
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
            return slot == 0; // Only input slot accepts items
        }
    };

    private final LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.of(() -> itemHandler);

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
            level.getRecipeManager().getRecipeFor(GrinderRecipe.Type.INSTANCE, pEntity, level).ifPresent(recipe -> {
                pEntity.maxProgress = recipe.getProcessingTime();
                pEntity.progress++;
                setChanged(level, pos, state);

                if (pEntity.progress >= pEntity.maxProgress) {
                    pEntity.craftItem();
                }
            });
        } else {
            pEntity.progress = 0;
            setChanged(level, pos, state);
        }
    }

    private void craftItem() {
        level.getRecipeManager().getRecipeFor(GrinderRecipe.Type.INSTANCE, this, level).ifPresent(recipe -> {
            ItemStack input = itemHandler.getStackInSlot(0);
            ItemStack result = recipe.assemble(this, level.registryAccess());

            itemHandler.extractItem(0, 1, false);
            itemHandler.setStackInSlot(1, new ItemStack(result.getItem(),
                    itemHandler.getStackInSlot(1).getCount() + result.getCount()));
        });

        resetProgress();
    }

    private void resetProgress() {
        this.progress = 0;
    }

    private boolean hasRecipe() {
        return level.getRecipeManager().getRecipeFor(GrinderRecipe.Type.INSTANCE, this, level).isPresent();
    }

    public ItemStackHandler getItemHandler() {
        return itemHandler;
    }
}
