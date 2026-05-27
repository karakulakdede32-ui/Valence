package com.valence.valence.item;

import com.valence.valence.block.conduit.TransferConduitTileEntity;
import com.valence.valence.block.wireless.WirelessNodeTileEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class LinkingTool extends Item {
    private static final String TAG_EXPORT_X = "export_x";
    private static final String TAG_EXPORT_Y = "export_y";
    private static final String TAG_EXPORT_Z = "export_z";
    private static final String TAG_IMPORT_X = "import_x";
    private static final String TAG_IMPORT_Y = "import_y";
    private static final String TAG_IMPORT_Z = "import_z";

    public LinkingTool(Properties props) { super(props); }



    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        Level level = ctx.getLevel();
        if (level.isClientSide) return InteractionResult.SUCCESS;

        Player player = ctx.getPlayer();
        BlockPos pos = ctx.getClickedPos();
        ItemStack stack = ctx.getItemInHand();
        BlockEntity be = level.getBlockEntity(pos);

        if (player == null) return InteractionResult.FAIL;

        // Shift+right-click = set import
        if (player.isShiftKeyDown()) {
            setImport(stack, pos);
            player.displayClientMessage(Component.literal("§6[Linking Tool] §bImport set: " + pos.getX() + ", " + pos.getY() + ", " + pos.getZ()), true);

            // Check if we already have an export set - if so, configure the network
            if (hasExport(stack)) {
                tryConfigureNetwork(level, getExport(stack), getImport(stack), player, stack);
            }
            return InteractionResult.SUCCESS;
        }

        // Regular right-click on a Transfer Conduit - configure it with current link
        if (be instanceof TransferConduitTileEntity conduit) {
            if (hasExport(stack) && hasImport(stack)) {
                conduit.setLinkedSource(getExport(stack));
                conduit.setLinkedDest(getImport(stack));
                player.displayClientMessage(Component.literal("§6[Linking Tool] §aConduit configured! Export→Import"), true);
            } else {
                player.displayClientMessage(Component.literal("§6[Linking Tool] §cSet both an Export (shift+left-click) and Import (shift+right-click) first"), true);
            }
            return InteractionResult.SUCCESS;
        }

        // Regular right-click on a Wireless Node - pair it
        if (be instanceof WirelessNodeTileEntity wireless) {
            if (hasExport(stack) && hasImport(stack)) {
                BlockPos exportPos = getExport(stack);
                BlockPos importPos = getImport(stack);

                // Check if either block is next to a wireless node
                for (Direction dir : Direction.values()) {
                    BlockEntity neighbor = level.getBlockEntity(wireless.getBlockPos().relative(dir));
                    if (neighbor == null) continue;
                    if (neighbor.getBlockPos().equals(exportPos)) {
                        // This wireless node is next to the export - start looking for other node near import
                        for (Direction d : Direction.values()) {
                            BlockEntity n2 = level.getBlockEntity(wireless.getBlockPos().relative(d));
                            continue;
                        }
                    }
                }

                // Simple approach: look for a wireless node next to import
                BlockPos importNodePos = findAdjacentWirelessNode(level, importPos);
                if (importNodePos != null) {
                    BlockEntity importNode = level.getBlockEntity(importNodePos);
                    if (importNode instanceof WirelessNodeTileEntity importWireless) {
                        wireless.setPairedNode(importNodePos);
                        importWireless.setPairedNode(wireless.getBlockPos());
                        player.displayClientMessage(Component.literal("§6[Linking Tool] §aWireless link established!"), true);
                        return InteractionResult.SUCCESS;
                    }
                }
            }
            // Fallback: clear or re-pair
            if (wireless.getPairedNode() != null) {
                wireless.clearPairedNode();
                player.displayClientMessage(Component.literal("§6[Linking Tool] §eWireless link cleared"), true);
            } else {
                player.displayClientMessage(Component.literal("§6[Linking Tool] §cPlace the wireless tool export/import first"), true);
            }
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.SUCCESS;
    }

    private static void tryConfigureNetwork(Level level, BlockPos exportPos, BlockPos importPos, Player player, ItemStack stack) {

        // Search for Transfer Conduits adjacent to either position and configure them
        boolean configured = false;
        for (Direction dir : Direction.values()) {
            BlockPos conduitPos = exportPos.relative(dir);
            BlockEntity be = level.getBlockEntity(conduitPos);
            if (be instanceof TransferConduitTileEntity conduit) {
                conduit.setLinkedSource(exportPos);
                conduit.setLinkedDest(importPos);
                configured = true;
            }
        }
        for (Direction dir : Direction.values()) {
            BlockPos conduitPos = importPos.relative(dir);
            BlockEntity be = level.getBlockEntity(conduitPos);
            if (be instanceof TransferConduitTileEntity conduit) {
                conduit.setLinkedSource(exportPos);
                conduit.setLinkedDest(importPos);
                configured = true;
            }
        }

        // Also try to set up wireless link if wireless nodes are adjacent
        BlockPos exportNodePos = findAdjacentWirelessNode(level, exportPos);
        BlockPos importNodePos = findAdjacentWirelessNode(level, importPos);
        if (exportNodePos != null && importNodePos != null && !exportNodePos.equals(importNodePos)) {
            BlockEntity exportBE = level.getBlockEntity(exportNodePos);
            BlockEntity importBE = level.getBlockEntity(importNodePos);
            if (exportBE instanceof WirelessNodeTileEntity && importBE instanceof WirelessNodeTileEntity) {
                ((WirelessNodeTileEntity) exportBE).setPairedNode(importNodePos);
                ((WirelessNodeTileEntity) importBE).setPairedNode(exportNodePos);
                configured = true;
            }
        }

        if (configured) {
            player.displayClientMessage(Component.literal("§6[Linking Tool] §aNetwork configured! Transferring: Export → Import"), true);
            clearAll(stack);
        } else {
            player.displayClientMessage(Component.literal("§6[Linking Tool] §eExport & Import saved. Right-click a Transfer Conduit or Wireless Node to link."), true);
        }
    }

    @Nullable
    private static BlockPos findAdjacentWirelessNode(Level level, BlockPos pos) {
        for (Direction dir : Direction.values()) {
            BlockPos neighbor = pos.relative(dir);
            if (level.getBlockEntity(neighbor) instanceof WirelessNodeTileEntity) {
                return neighbor;
            }
        }
        return null;
    }

    /**
     * Called from ModEvents when player shift+left-clicks a block with the LinkingTool
     */
    public static void handleShiftLeftClick(ItemStack stack, Level level, BlockPos pos, Player player) {
        setExport(stack, pos);
        player.displayClientMessage(Component.literal("§6[Linking Tool] §eExport set: " + pos.getX() + ", " + pos.getY() + ", " + pos.getZ()), true);

        // If import is already set, try to configure the network
        if (hasImport(stack)) {
            BlockPos importPos = getImport(stack);
            tryConfigureNetwork(level, getExport(stack), importPos, player, stack);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(Component.literal("§7Shift+Left-Click: Set Export (source)"));
        tooltip.add(Component.literal("§7Shift+Right-Click: Set Import (destination)"));
        tooltip.add(Component.literal("§7Then right-click a Transfer Conduit or Wireless Node"));
        if (hasExport(stack)) {
            BlockPos p = getExport(stack);
            tooltip.add(Component.literal("§6Export: " + p.getX() + ", " + p.getY() + ", " + p.getZ()));
        }
        if (hasImport(stack)) {
            BlockPos p = getImport(stack);
            tooltip.add(Component.literal("§bImport: " + p.getX() + ", " + p.getY() + ", " + p.getZ()));
        }
    }

    // ========== NBT storage ==========

    private static boolean hasExport(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.contains(TAG_EXPORT_X);
    }

    private static boolean hasImport(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.contains(TAG_IMPORT_X);
    }

    private static BlockPos getExport(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return new BlockPos(tag.getInt(TAG_EXPORT_X), tag.getInt(TAG_EXPORT_Y), tag.getInt(TAG_EXPORT_Z));
    }

    private static BlockPos getImport(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return new BlockPos(tag.getInt(TAG_IMPORT_X), tag.getInt(TAG_IMPORT_Y), tag.getInt(TAG_IMPORT_Z));
    }

    private static void setExport(ItemStack stack, BlockPos pos) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putInt(TAG_EXPORT_X, pos.getX());
        tag.putInt(TAG_EXPORT_Y, pos.getY());
        tag.putInt(TAG_EXPORT_Z, pos.getZ());
    }

    private static void setImport(ItemStack stack, BlockPos pos) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putInt(TAG_IMPORT_X, pos.getX());
        tag.putInt(TAG_IMPORT_Y, pos.getY());
        tag.putInt(TAG_IMPORT_Z, pos.getZ());
    }

    private static void clearAll(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null) {
            tag.remove(TAG_EXPORT_X);
            tag.remove(TAG_EXPORT_Y);
            tag.remove(TAG_EXPORT_Z);
            tag.remove(TAG_IMPORT_X);
            tag.remove(TAG_IMPORT_Y);
            tag.remove(TAG_IMPORT_Z);
        }
    }
}
