package com.valence.valence.block.alloyer;

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
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class SteamAlloyerTileEntity extends BlockEntity implements MenuProvider {
    public static final int STEAM_CAPACITY = 100;
    public static final int STEAM_PER_ALLOY = 18;
    public static final int PROGRESS_MAX = 100;

    private final FluidTank steamTank = new FluidTank(STEAM_CAPACITY) {
        @Override public boolean isFluidValid(FluidStack stack) { return stack.getFluid() == Registration.STEAM.get(); }
        @Override protected void onContentsChanged() { setChanged(); sync(); }
    };

    private final ItemStackHandler itemHandler = new ItemStackHandler(3) {
        @Override public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            if (slot == 2) return false; // output
            return true;
        }
        @Override protected void onContentsChanged(int slot) { setChanged(); }
    };

    private int progress = 0;
    private final LazyOptional<IFluidHandler> fluidHandler = LazyOptional.of(() -> steamTank);

    private static final Map<List<ItemStack>, ItemStack> ALLOY_RECIPES = new HashMap<>();
    static {
        ALLOY_RECIPES.put(
            Arrays.asList(new ItemStack(Registration.IRON_POWDER.get(), 1), new ItemStack(Registration.COPPER_POWDER.get(), 1)),
            new ItemStack(Registration.BRONZE_INGOT.get(), 2)
        );
    }

    public SteamAlloyerTileEntity(BlockPos pos, BlockState state) {
        super(Registration.STEAM_ALLOYER_TE.get(), pos, state);
    }

    private void sync() {
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
    }

    @Override public Component getDisplayName() { return Component.translatable("container.valence.steam_alloyer"); }

    @Nullable @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new SteamAlloyerMenu(id, inventory, this);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        steamTank.readFromNBT(tag.getCompound("steam_tank"));
        itemHandler.deserializeNBT(tag.getCompound("items"));
        progress = tag.getInt("progress");
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("steam_tank", steamTank.writeToNBT(new CompoundTag()));
        tag.put("items", itemHandler.serializeNBT());
        tag.putInt("progress", progress);
    }

    @Override public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        tag.put("steam_tank", steamTank.writeToNBT(new CompoundTag()));
        tag.put("items", itemHandler.serializeNBT());
        tag.putInt("progress", progress);
        return tag;
    }

    @Override public void handleUpdateTag(CompoundTag tag) {
        super.handleUpdateTag(tag);
        steamTank.readFromNBT(tag.getCompound("steam_tank"));
        itemHandler.deserializeNBT(tag.getCompound("items"));
        progress = tag.getInt("progress");
    }

    @Nullable @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        CompoundTag tag = pkt.getTag();
        if (tag != null) {
            steamTank.readFromNBT(tag.getCompound("steam_tank"));
            itemHandler.deserializeNBT(tag.getCompound("items"));
            progress = tag.getInt("progress");
        }
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.FLUID_HANDLER) return fluidHandler.cast();
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        fluidHandler.invalidate();
    }

    public static void tick(Level level, BlockPos pos, BlockState state, SteamAlloyerTileEntity te) {
        if (level.isClientSide()) return;

        // Pull steam from neighbors
        te.pullSteam();

        // Process alloying
        if (te.canAlloy()) {
            te.progress++;
            if (te.progress >= PROGRESS_MAX) {
                te.doAlloy();
                te.progress = 0;
            }
            setChanged(level, pos, state);
        } else {
            if (te.progress > 0) {
                te.progress = 0;
                setChanged(level, pos, state);
            }
        }
    }

    private void pullSteam() {
        if (steamTank.getFluidAmount() >= steamTank.getCapacity()) return;
        for (Direction dir : Direction.values()) {
            BlockEntity neighbor = level.getBlockEntity(worldPosition.relative(dir));
            if (neighbor == null) continue;
            int space = steamTank.getCapacity() - steamTank.getFluidAmount();
            neighbor.getCapability(ForgeCapabilities.FLUID_HANDLER, dir.getOpposite()).ifPresent(handler -> {
                FluidStack sim = handler.drain(new FluidStack(Registration.STEAM.get(), space), IFluidHandler.FluidAction.SIMULATE);
                if (!sim.isEmpty() && sim.getFluid() == Registration.STEAM.get()) {
                    int filled = steamTank.fill(sim, IFluidHandler.FluidAction.SIMULATE);
                    if (filled > 0) {
                        handler.drain(new FluidStack(Registration.STEAM.get(), filled), IFluidHandler.FluidAction.EXECUTE);
                        steamTank.fill(new FluidStack(Registration.STEAM.get(), filled), IFluidHandler.FluidAction.EXECUTE);
                    }
                }
            });
        }
    }

    private boolean canAlloy() {
        if (steamTank.getFluidAmount() < STEAM_PER_ALLOY) return false;
        if (itemHandler.getStackInSlot(0).isEmpty() || itemHandler.getStackInSlot(1).isEmpty()) return false;

        ItemStack result = findRecipe();
        if (result == null || result.isEmpty()) return false;

        ItemStack output = itemHandler.getStackInSlot(2);
        return output.isEmpty() || (output.is(result.getItem()) && output.getCount() + result.getCount() <= output.getMaxStackSize());
    }

    private ItemStack findRecipe() {
        ItemStack slot0 = itemHandler.getStackInSlot(0);
        ItemStack slot1 = itemHandler.getStackInSlot(1);
        for (Map.Entry<List<ItemStack>, ItemStack> entry : ALLOY_RECIPES.entrySet()) {
            List<ItemStack> inputs = entry.getKey();
            if (inputs.size() != 2) continue;
            boolean match0 = slot0.is(inputs.get(0).getItem()) && slot0.getCount() >= inputs.get(0).getCount();
            boolean match1 = slot1.is(inputs.get(1).getItem()) && slot1.getCount() >= inputs.get(1).getCount();
            if ((match0 && match1) || (match1 && match0)) return entry.getValue().copy();
        }
        return null;
    }

    private void doAlloy() {
        if (!canAlloy()) return;
        ItemStack result = findRecipe();
        if (result == null) return;

        itemHandler.extractItem(0, 1, false);
        itemHandler.extractItem(1, 1, false);
        steamTank.drain(STEAM_PER_ALLOY, IFluidHandler.FluidAction.EXECUTE);

        ItemStack output = itemHandler.getStackInSlot(2);
        if (output.isEmpty()) {
            itemHandler.setStackInSlot(2, result);
        } else {
            output.grow(result.getCount());
        }
    }

    public FluidTank getSteamTank() { return steamTank; }
    public ItemStackHandler getItemHandler() { return itemHandler; }
    public int getProgress() { return progress; }
    public int getMaxProgress() { return PROGRESS_MAX; }

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
