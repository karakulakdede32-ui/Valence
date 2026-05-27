package com.valence.valence.item;

import com.valence.valence.block.conduit.TransferConduitTileEntity;
import com.valence.valence.block.wireless.WirelessNodeTileEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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

    public LinkingTool(Properties props) { super(props); }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        Level level = ctx.getLevel();
        if (level.isClientSide) return InteractionResult.SUCCESS;

        Player player = ctx.getPlayer();
        BlockPos pos = ctx.getClickedPos();
        BlockEntity be = level.getBlockEntity(pos);
        if (player == null) return InteractionResult.FAIL;

        // ===== SHIFT + RIGHT-CLICK actions =====
        if (player.isShiftKeyDown()) {
            // Shift+right-click on Transfer Conduit → clear all links
            if (be instanceof TransferConduitTileEntity conduit) {
                conduit.clearAllLinks();
                player.displayClientMessage(Component.literal("§6[Linking Tool] §eCleared all links on this conduit"), true);
                return InteractionResult.SUCCESS;
            }

            // Shift+right-click on any machine → add as destination (push into this machine)
            boolean foundConduit = false;
            for (Direction dir : Direction.values()) {
                BlockPos adj = pos.relative(dir);
                if (level.getBlockEntity(adj) instanceof TransferConduitTileEntity conduit) {
                    conduit.addDest(pos);
                    foundConduit = true;
                }
            }
            if (foundConduit) {
                player.displayClientMessage(Component.literal("§6[Linking Tool] §bImport set: " + pos.getX() + ", " + pos.getY() + ", " + pos.getZ()), true);
            } else {
                player.displayClientMessage(Component.literal("§6[Linking Tool] §cNo conduit adjacent to this block"), true);
            }
            return InteractionResult.SUCCESS;
        }

        // ===== RIGHT-CLICK actions (no shift) =====

        // Right-click on Transfer Conduit → pass to block (opens GUI)
        if (be instanceof TransferConduitTileEntity) {
            return InteractionResult.PASS;
        }

        // Right-click on Wireless Node → toggle pairing
        if (be instanceof WirelessNodeTileEntity wireless) {
            if (wireless.getPairedNode() != null) {
                wireless.clearPairedNode();
                player.displayClientMessage(Component.literal("§6[Linking Tool] §eWireless link cleared"), true);
            } else {
                // Find another unpaired wireless node within range to pair with
                WirelessNodeTileEntity target = findUnpairedNodeInRange(level, pos, wireless);
                if (target != null) {
                    wireless.setPairedNode(target.getBlockPos());
                    target.setPairedNode(wireless.getBlockPos());
                    player.displayClientMessage(Component.literal("§6[Linking Tool] §aWireless link established!"), true);
                } else {
                    player.displayClientMessage(Component.literal("§6[Linking Tool] §cNo unpaired wireless node found within 32 blocks"), true);
                }
            }
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    @Nullable
    private WirelessNodeTileEntity findUnpairedNodeInRange(Level level, BlockPos origin, WirelessNodeTileEntity exclude) {
        int range = WirelessNodeTileEntity.MAX_RANGE;
        for (int x = -range; x <= range; x++) {
            for (int y = -range; y <= range; y++) {
                for (int z = -range; z <= range; z++) {
                    if (x == 0 && y == 0 && z == 0) continue;
                    BlockPos checkPos = origin.offset(x, y, z);
                    BlockEntity be = level.getBlockEntity(checkPos);
                    if (be instanceof WirelessNodeTileEntity node && node != exclude && node.getPairedNode() == null) {
                        // Check distance
                        if (origin.distSqr(checkPos) <= range * range) {
                            return node;
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * Called from ModEvents when player shift+left-clicks a block with the LinkingTool.
     * Adds the clicked block as a source (extract from it) on all adjacent conduits.
     */
    public static void handleShiftLeftClick(ItemStack stack, Level level, BlockPos pos, Player player) {
        boolean foundConduit = false;
        for (Direction dir : Direction.values()) {
            BlockPos adj = pos.relative(dir);
            if (level.getBlockEntity(adj) instanceof TransferConduitTileEntity conduit) {
                conduit.addSource(pos);
                foundConduit = true;
            }
        }
        if (foundConduit) {
            player.displayClientMessage(Component.literal("§6[Linking Tool] §eExport set: " + pos.getX() + ", " + pos.getY() + ", " + pos.getZ()), true);
        } else {
            player.displayClientMessage(Component.literal("§6[Linking Tool] §cNo conduit adjacent to this block"), true);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(Component.literal("§7Shift+Left-Click machine: Add as §eExport §7(source)"));
        tooltip.add(Component.literal("§7Shift+Right-Click machine: Add as §bImport §7(destination)"));
        tooltip.add(Component.literal("§7Shift+Right-Click conduit: §cClear §7all links"));
        tooltip.add(Component.literal("§7Right-Click conduit: Open GUI"));
        tooltip.add(Component.literal("§7Right-Click wireless node: Toggle pairing"));
    }
}
