package com.valence.valence.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


/**
 * Base class for all Valence machine tile entities.
 * Handles:
 * - ItemStackHandler with sync callback
 * - Sided inventory wrappers (for hoppers/pipes)
 * - NBT save/load for items
 * - Client sync packets (getUpdateTag, handleUpdateTag, getUpdatePacket, onDataPacket)
 * - Capability invalidation
 *
 * Subclasses implement:
 * - createMenu() for the GUI
 * - tick() for machine logic
 * - createItemHandler() to configure slots
 * - Additional capabilities via overrideCapability()
 */
public abstract class BaseMachineTileEntity extends BlockEntity implements MenuProvider {

    protected ItemStackHandler itemHandler;
    private final LazyOptional<IItemHandler> unsidedWrapper;

    protected BaseMachineTileEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        this.itemHandler = createItemHandler();
        this.unsidedWrapper = LazyOptional.empty();
    }

    // ---------- Subclass hooks ----------

    /** Create the ItemStackHandler with desired slot count and validation. */
    protected abstract ItemStackHandler createItemHandler();

    /** Override to provide additional capabilities (e.g. FLUID_HANDLER, ENERGY). */
    protected @NotNull <T> LazyOptional<T> overrideCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return LazyOptional.empty();
    }

    /** Called when the inventory changes — triggers save and sync. */
    protected void onInventoryChanged() {
        setChanged();
        sync();
    }

    // ---------- Sync helpers ----------

    protected void sync() {
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
    }

    // ---------- NBT ----------

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("items")) {
            itemHandler.deserializeNBT(tag.getCompound("items"));
        }
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("items", itemHandler.serializeNBT());
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        tag.put("items", itemHandler.serializeNBT());
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        super.handleUpdateTag(tag);
        if (tag.contains("items")) {
            itemHandler.deserializeNBT(tag.getCompound("items"));
        }
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        CompoundTag tag = pkt.getTag();
        if (tag != null && tag.contains("items")) {
            itemHandler.deserializeNBT(tag.getCompound("items"));
        }
    }

    // ---------- Capabilities ----------

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            @SuppressWarnings("unchecked")
            LazyOptional<T> result = (LazyOptional<T>) unsidedWrapper;
            return result;
        }
        LazyOptional<T> override = overrideCapability(cap, side);
        if (override.isPresent()) return override;
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        unsidedWrapper.invalidate();
    }

    // ---------- Default container methods (delegated to itemHandler) ----------

    @Override
    public Component getDisplayName() {
        return Component.literal("Machine");
    }

    public ItemStackHandler getItemHandler() {
        return itemHandler;
    }

    // Subclasses still need to implement:
    // - createMenu(int id, Inventory inv, Player player)
    // - static void tick(Level, BlockPos, BlockState, SubclassType)
}
