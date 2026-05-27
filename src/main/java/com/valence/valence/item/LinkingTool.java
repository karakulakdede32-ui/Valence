package com.valence.valence.item;

import com.valence.valence.block.conduit.TransferConduitTileEntity;
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

    public LinkingTool(Properties props) { super(props); }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        Level level = ctx.getLevel();
        if (level.isClientSide) return InteractionResult.SUCCESS;

        BlockPos pos = ctx.getClickedPos();
        BlockEntity be = level.getBlockEntity(pos);
        ItemStack stack = ctx.getItemInHand();

        if (be instanceof TransferConduitTileEntity conduit) {
            // Clicking on a conduit - get the adjacent block from the face clicked
            BlockPos adjacentPos = pos.relative(ctx.getClickedFace());
            BlockEntity adjacent = level.getBlockEntity(adjacentPos);

            if (adjacent == null) {
                ctx.getPlayer().sendSystemMessage(Component.literal("No machine on this face of the conduit"));
                return InteractionResult.SUCCESS;
            }

            // Check if source is already set in the tool
            if (!hasSource(stack)) {
                // Set this conduit's source
                conduit.setLinkedSource(adjacentPos);
                ctx.getPlayer().sendSystemMessage(Component.literal("Source set: " + adjacentPos));
            } else {
                // Set this conduit's destination
                BlockPos sourcePos = getSource(stack);
                conduit.setLinkedSource(sourcePos);
                conduit.setLinkedDest(adjacentPos);
                clearSource(stack);
                ctx.getPlayer().sendSystemMessage(Component.literal("Linked! Source -> Destination. Conduit will transfer."));
            }
        } else {
            // Clicking on a regular machine - save as source in the tool
            setSource(stack, pos);
            ctx.getPlayer().sendSystemMessage(Component.literal("Source position saved: " + pos));
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(Component.literal("Links machines for fluid/energy transfer"));
        if (hasSource(stack)) {
            BlockPos p = getSource(stack);
            tooltip.add(Component.literal("Source: " + p.getX() + ", " + p.getY() + ", " + p.getZ()));
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
        }
    }
}
