package com.valence.valence.block.assembler;

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

public class AssemblerTileEntity extends BlockEntity implements MenuProvider {
    public static final int DF_CAPACITY = 10000;
    public static final int DF_PER_TICK = 20;

    private final DFStorage dfStorage = new DFStorage(DF_CAPACITY, DF_PER_TICK * 2, 0) {
        @Override protected void onEnergyChanged() { setChanged(); sync(); }
    };
    // 10 slots: 0-4 input, 5 fuel, 6-8 output, 9 crafting result
    private final ItemStackHandler itemHandler = new ItemStackHandler(10) {
        @Override public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            if (slot >= 6 && slot <= 8) return false; // output
            return true;
        }
        @Override protected void onContentsChanged(int slot) { setChanged(); }
    };
    private int progress = 0;
    private String selectedRecipe = "";
    private final LazyOptional<IEnergyStorage> energyHandler = LazyOptional.of(() -> dfStorage);

    public AssemblerTileEntity(BlockPos pos, BlockState state) { super(Registration.ASSEMBLER_TE.get(), pos, state); }
    private void sync() { if (level != null && !level.isClientSide()) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3); }

    @Override public Component getDisplayName() { return Component.translatable("container.valence.assembler"); }
    @Nullable @Override public AbstractContainerMenu createMenu(int id, Inventory inv, Player p) { return new AssemblerMenu(id, inv, this); }

    @Override public void load(CompoundTag t) { super.load(t); dfStorage.deserializeNBT(t.get("df")); itemHandler.deserializeNBT(t.getCompound("items")); progress = t.getInt("progress"); selectedRecipe = t.getString("recipe"); }
    @Override public void saveAdditional(CompoundTag t) { super.saveAdditional(t); t.put("df", dfStorage.serializeNBT()); t.put("items", itemHandler.serializeNBT()); t.putInt("progress", progress); t.putString("recipe", selectedRecipe); }
    @Override public CompoundTag getUpdateTag() { CompoundTag t = super.getUpdateTag(); t.put("df", dfStorage.serializeNBT()); t.put("items", itemHandler.serializeNBT()); t.putInt("progress", progress); t.putString("recipe", selectedRecipe); return t; }
    @Override public void handleUpdateTag(CompoundTag t) { super.handleUpdateTag(t); dfStorage.deserializeNBT(t.get("df")); itemHandler.deserializeNBT(t.getCompound("items")); progress = t.getInt("progress"); selectedRecipe = t.getString("recipe"); }
    @Nullable @Override public Packet<ClientGamePacketListener> getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }
    @Override public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) { CompoundTag t = pkt.getTag(); if (t != null) { dfStorage.deserializeNBT(t.get("df")); itemHandler.deserializeNBT(t.getCompound("items")); progress = t.getInt("progress"); selectedRecipe = t.getString("recipe"); } }

    @Override public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ENERGY) return energyHandler.cast();
        return super.getCapability(cap, side);
    }
    @Override public void invalidateCaps() { super.invalidateCaps(); energyHandler.invalidate(); }

    // Tries smelting recipes as a universal recipe handler
    public static void tick(Level level, BlockPos pos, BlockState state, AssemblerTileEntity te) {
        if (level.isClientSide()) return;

        // Pull DF
        if (te.dfStorage.getDF() < te.dfStorage.getMaxDF()) {
            for (Direction d : Direction.values()) {
                int need = te.dfStorage.getMaxDF() - te.dfStorage.getDF();
                if (need <= 0) break;
                BlockEntity nb = level.getBlockEntity(pos.relative(d));
                if (nb == null) continue;
                int req = Math.min(DF_PER_TICK * 2, need);
                nb.getCapability(ForgeCapabilities.ENERGY, d.getOpposite()).ifPresent(h -> {
                    int got = h.extractEnergy(req, false);
                    if (got > 0) te.dfStorage.receiveEnergy(got, false);
                });
            }
        }

        // Find first non-empty input slot (0-4)
        ItemStack input = ItemStack.EMPTY;
        int inputSlot = -1;
        for (int i = 0; i < 5; i++) {
            if (!te.itemHandler.getStackInSlot(i).isEmpty()) {
                input = te.itemHandler.getStackInSlot(i);
                inputSlot = i;
                break;
            }
        }

        if (input.isEmpty() || te.dfStorage.getDF() < DF_PER_TICK) {
            if (te.progress > 0) { te.progress = 0; te.setChanged(); }
            return;
        }

        // Find a smelting recipe
        SimpleContainer inv = new SimpleContainer(input);
        Optional<SmeltingRecipe> recipe = level.getRecipeManager().getRecipeFor(RecipeType.SMELTING, inv, level);
        if (recipe.isEmpty()) {
            if (te.progress > 0) { te.progress = 0; te.setChanged(); }
            return;
        }

        ItemStack result = recipe.get().getResultItem(level.registryAccess());
        if (result.isEmpty()) {
            if (te.progress > 0) { te.progress = 0; te.setChanged(); }
            return;
        }

        // Find output slot (6-8)
        int outSlot = -1;
        for (int i = 6; i <= 8; i++) {
            ItemStack s = te.itemHandler.getStackInSlot(i);
            if (s.isEmpty() || (s.is(result.getItem()) && ItemStack.isSameItemSameTags(s, result) && s.getCount() + result.getCount() <= s.getMaxStackSize())) {
                outSlot = i; break;
            }
        }
        if (outSlot < 0) { if (te.progress > 0) { te.progress = 0; te.setChanged(); } return; }

        te.dfStorage.consumeDF(DF_PER_TICK, false);
        te.progress++;
        int maxProg = 80;
        if (te.progress >= maxProg) {
            input.shrink(1);
            ItemStack out = te.itemHandler.getStackInSlot(outSlot);
            if (out.isEmpty()) te.itemHandler.setStackInSlot(outSlot, result.copy());
            else out.grow(result.getCount());
            te.progress = 0;
        }
        te.setChanged();
    }

    public String getSelectedRecipe() { return selectedRecipe; }
    public void setSelectedRecipe(String r) { selectedRecipe = r; setChanged(); }
    public DFStorage getDFStorage() { return dfStorage; }
    public ItemStackHandler getItemHandler() { return itemHandler; }
    public int getProgress() { return progress; }
    public int getMaxProgress() { return 80; }
}