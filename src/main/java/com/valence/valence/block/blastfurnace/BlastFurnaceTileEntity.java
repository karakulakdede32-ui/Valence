package com.valence.valence.block.blastfurnace;

import com.valence.valence.Registration;
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
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.Optional;

public class BlastFurnaceTileEntity extends BlockEntity implements MenuProvider {
    public static final int PROGRESS_MAX = 300;

    private final ItemStackHandler itemHandler = new ItemStackHandler(2) {
        @Override public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            if (slot == 1) return false;
            return true;
        }
        @Override protected void onContentsChanged(int slot) { setChanged(); }
    };
    private int progress = 0;
    private boolean multiblockValid = false;
    private int recheckDelay = 0;

    public BlastFurnaceTileEntity(BlockPos pos, BlockState state) { super(Registration.BLAST_FURNACE_TE.get(), pos, state); }

    @Override public Component getDisplayName() { return Component.translatable("container.valence.blast_furnace"); }
    @Nullable @Override public AbstractContainerMenu createMenu(int id, Inventory inv, Player p) { return new BlastFurnaceMenu(id, inv, this); }

    @Override public void load(CompoundTag t) { super.load(t); itemHandler.deserializeNBT(t.getCompound("items")); progress = t.getInt("progress"); }
    @Override public void saveAdditional(CompoundTag t) { super.saveAdditional(t); t.put("items", itemHandler.serializeNBT()); t.putInt("progress", progress); }
    @Override public CompoundTag getUpdateTag() { CompoundTag t = super.getUpdateTag(); t.put("items", itemHandler.serializeNBT()); t.putInt("progress", progress); return t; }
    @Override public void handleUpdateTag(CompoundTag t) { super.handleUpdateTag(t); itemHandler.deserializeNBT(t.getCompound("items")); progress = t.getInt("progress"); }
    @Nullable @Override public Packet<ClientGamePacketListener> getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }
    @Override public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) { CompoundTag t = pkt.getTag(); if (t != null) { itemHandler.deserializeNBT(t.getCompound("items")); progress = t.getInt("progress"); } }

    @Override public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) return LazyOptional.of(() -> itemHandler).cast();
        return super.getCapability(cap, side);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, BlastFurnaceTileEntity te) {
        if (level.isClientSide()) {
            // Client: smoke particles when running
            if (te.progress > 0 && level.random.nextInt(3) == 0) {
                double x = pos.getX() + 0.5;
                double y = pos.getY() + 1.0;
                double z = pos.getZ() + 0.5;
                level.addParticle(com.valence.valence.Registration.SMOKE.get(), x + (level.random.nextDouble()-0.5)*0.3, y, z + (level.random.nextDouble()-0.5)*0.3, 0, 0.04, 0);
            }
            return;
        }

        // Recheck multiblock every 40 ticks (2 seconds) to reduce CPU load
        te.recheckDelay--;
        if (te.recheckDelay <= 0) {
            te.multiblockValid = BlastFurnaceBlock.isFormed(level, pos);
            te.recheckDelay = 40;
        }

        if (!te.multiblockValid) {
            if (te.progress > 0) { te.progress = 0; te.setChanged(); }
            return;
        }

        ItemStack input = te.itemHandler.getStackInSlot(0);
        ItemStack output = te.itemHandler.getStackInSlot(1);

        if (input.isEmpty()) {
            if (te.progress > 0) { te.progress = 0; te.setChanged(); }
            return;
        }

        SimpleContainer inv = new SimpleContainer(input);
        Optional<SmeltingRecipe> recipe = level.getRecipeManager().getRecipeFor(RecipeType.SMELTING, inv, level);
        if (recipe.isEmpty()) { if (te.progress > 0) { te.progress = 0; te.setChanged(); } return; }

        ItemStack result = recipe.get().getResultItem(level.registryAccess());
        if (result.isEmpty()) { if (te.progress > 0) { te.progress = 0; te.setChanged(); } return; }

        boolean canOut = output.isEmpty() || (output.is(result.getItem()) && output.getCount() + result.getCount() <= output.getMaxStackSize());
        if (!canOut) { if (te.progress > 0) { te.progress = 0; te.setChanged(); } return; }

        te.progress++;
        if (te.progress >= PROGRESS_MAX) {
            input.shrink(1);
            if (output.isEmpty()) te.itemHandler.setStackInSlot(1, result.copy());
            else output.grow(result.getCount());
            te.progress = 0;
        }
        te.setChanged();
    }

    public int getComparatorOutput() {
        if (progress > 0) return (int)(15.0 * progress / PROGRESS_MAX);
        if (!itemHandler.getStackInSlot(1).isEmpty()) return 15;
        return 0;
    }

    public boolean isMultiblockValid() { return multiblockValid; }
    public ItemStackHandler getItemHandler() { return itemHandler; }
    public int getProgress() { return progress; }
    public int getMaxProgress() { return PROGRESS_MAX; }
}
