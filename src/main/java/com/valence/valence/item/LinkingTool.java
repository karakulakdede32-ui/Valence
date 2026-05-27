package com.valence.valence.item;

import com.valence.valence.block.conduit.TransferConduitTileEntity;
import com.valence.valence.block.wireless.WirelessNodeTileEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class LinkingTool extends Item {
    private static final String TAG_SOURCE_X = "source_x";
    private static final String TAG_SOURCE_Y = "source_y";
    private static final String TAG_SOURCE_Z = "source_z";
    private static final String TAG_MODE = "tool_mode";

    private static final int MODE_WIRELESS = 1;

    public LinkingTool(Properties props) { super(props); }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        Level level = ctx.getLevel();
        if (level.isClientSide) return InteractionResult.SUCCESS;

        BlockPos pos = ctx.getClickedPos();
        BlockEntity be = level.getBlockEntity(pos);
        ItemStack stack = ctx.getItemInHand();

        // Wireless Node pairing
        if (be instanceof WirelessNodeTileEntity wireless) {
            if (wireless.getPairedNode() != null) {
                // Node already paired - clear it
                wireless.clearPairedNode();
                ctx.getPlayer().sendSystemMessage(Component.literal("Wireless link cleared"));
                return InteractionResult.SUCCESS;
            }

            BlockPos savedSource = getSource(stack);
            if (savedSource != null) {
                // Try to pair with saved source
                BlockEntity sourceBE = level.getBlockEntity(savedSource);
                if (sourceBE instanceof WirelessNodeTileEntity sourceWireless) {
                    if (sourceWireless.getPairedNode() == null && wireless.getPairedNode() == null) {
                        sourceWireless.setPairedNode(pos);
                        wireless.setPairedNode(savedSource);
                        clearSource(stack);
                        ctx.getPlayer().sendSystemMessage(Component.literal("Wireless link established! Range: 32 blocks, 95% efficiency"));
                    } else {
                        ctx.getPlayer().sendSystemMessage(Component.literal("One of the nodes is already paired"));
                    }
                } else {
                    // Saved source is not a wireless node, save as this node's source
                    ctx.getPlayer().sendSystemMessage(Component.literal("Saved position is not a Wireless Node"));
                }
            } else {
                // Nothing saved - mark this node as source for pairing
                setSource(stack, pos);
                setMode(stack, MODE_WIRELESS);
                ctx.getPlayer().sendSystemMessage(Component.literal("Wireless Node ready to pair - right-click another node"));
            }
            return InteractionResult.SUCCESS;
        }

        // Transfer Conduit linking (existing behavior)
        if (be instanceof TransferConduitTileEntity conduit) {
            BlockPos adjacentPos = pos.relative(ctx.getClickedFace());
            BlockEntity adjacent = level.getBlockEntity(adjacentPos);

            if (adjacent == null) {
                ctx.getPlayer().sendSystemMessage(Component.literal("No machine on this face of the conduit"));
                return InteractionResult.SUCCESS;
            }

            if (!hasSource(stack) || getMode(stack) == MODE_WIRELESS) {
                conduit.setLinkedSource(adjacentPos);
                clearSource(stack);
                ctx.getPlayer().sendSystemMessage(Component.literal("Source set on conduit: " + adjacentPos));
            } else {
                BlockPos sourcePos = getSource(stack);
                conduit.setLinkedSource(sourcePos);
                conduit.setLinkedDest(adjacentPos);
                clearSource(stack);
                ctx.getPlayer().sendSystemMessage(Component.literal("Conduit linked! Source -> Destination"));
            }
            return InteractionResult.SUCCESS;
        }

        // Regular machine - save as source
        if (hasSource(stack)) {
            ctx.getPlayer().sendSystemMessage(Component.literal("Source already saved at: " + getSource(stack)));
        } else {
            setSource(stack, pos);
            setMode(stack, 0);
            ctx.getPlayer().sendSystemMessage(Component.literal("Source saved: " + pos));
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(Component.literal("Links machines, conduits & wireless nodes"));
        if (hasSource(stack)) {
            BlockPos p = getSource(stack);
            String mode = getMode(stack) == MODE_WIRELESS ? " [Wireless]" : "";
            tooltip.add(Component.literal("Source: " + p.getX() + ", " + p.getY() + ", " + p.getZ() + mode));
        }
    }

    private static boolean hasSource(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.contains(TAG_SOURCE_X);
    }

    private static BlockPos getSource(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains(TAG_SOURCE_X)) {
            return new BlockPos(tag.getInt(TAG_SOURCE_X), tag.getInt(TAG_SOURCE_Y), tag.getInt(TAG_SOURCE_Z));
        }
        return null;
    }

    private static void setSource(ItemStack stack, BlockPos pos) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putInt(TAG_SOURCE_X, pos.getX());
        tag.putInt(TAG_SOURCE_Y, pos.getY());
        tag.putInt(TAG_SOURCE_Z, pos.getZ());
    }

    private static void clearSource(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null) {
            tag.remove(TAG_SOURCE_X);
            tag.remove(TAG_SOURCE_Y);
            tag.remove(TAG_SOURCE_Z);
            tag.remove(TAG_MODE);
        }
    }

    private static int getMode(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains(TAG_MODE)) return tag.getInt(TAG_MODE);
        return 0;
    }

    private static void setMode(ItemStack stack, int mode) {
        stack.getOrCreateTag().putInt(TAG_MODE, mode);
    }
}
