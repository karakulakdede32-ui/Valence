package com.valence.valence.item;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ChunkExcavator extends Item {
    private static final int MAX_DURABILITY = 100;

    public ChunkExcavator(Properties props) {
        super(props.durability(MAX_DURABILITY));
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        Level level = ctx.getLevel();
        if (level.isClientSide) return InteractionResult.SUCCESS;

        Player player = ctx.getPlayer();
        ItemStack stack = ctx.getItemInHand();
        if (player == null) return InteractionResult.FAIL;
        if (player.isShiftKeyDown()) return InteractionResult.FAIL;

        ServerLevel serverLevel = (ServerLevel) level;
        BlockPos pos = ctx.getClickedPos();
        LevelChunk chunk = serverLevel.getChunkAt(pos);

        int oresFound = 0;
        int oresExtracted = 0;
        boolean broken = false;

        // Scan the entire chunk (16x16 horizontal, full height)
        for (int sectionIdx = chunk.getSectionsCount() - 1; sectionIdx >= 0 && !broken; sectionIdx--) {
            LevelChunkSection section = chunk.getSection(sectionIdx);
            if (section == null || section.hasOnlyAir()) continue;

            int sectionY = chunk.getSectionYFromSectionIndex(sectionIdx);

            for (int x = 0; x < 16 && !broken; x++) {
                for (int z = 0; z < 16 && !broken; z++) {
                    for (int y = 0; y < 16 && !broken; y++) {
                        int worldY = (sectionY << 4) + y;
                        if (worldY < level.getMinBuildHeight() || worldY > level.getMaxBuildHeight()) continue;

                        BlockPos orePos = new BlockPos((chunk.getPos().x << 4) + x, worldY, (chunk.getPos().z << 4) + z);
                        BlockState state = level.getBlockState(orePos);

                        if (state.isAir()) continue;

                        boolean isOre = state.is(BlockTags.COAL_ORES)
                                || state.is(BlockTags.IRON_ORES)
                                || state.is(BlockTags.COPPER_ORES)
                                || state.is(BlockTags.GOLD_ORES)
                                || state.is(BlockTags.REDSTONE_ORES)
                                || state.is(BlockTags.LAPIS_ORES)
                                || state.is(BlockTags.DIAMOND_ORES)
                                || state.is(BlockTags.EMERALD_ORES)
                                || state.is(Blocks.NETHER_QUARTZ_ORE)
                                || state.is(Blocks.NETHER_GOLD_ORE)
                                || state.is(Blocks.ANCIENT_DEBRIS);

                        if (!isOre) continue;

                        oresFound++;

                        // Check durability before breaking
                        if (stack.getDamageValue() >= stack.getMaxDamage() - 1) {
                            broken = true;
                            break;
                        }

                        // Determine the drop item
                        BlockState dropState = state.getBlock().defaultBlockState();

                        // Handle redstone ore variants
                        if (state.is(Blocks.REDSTONE_ORE) || state.is(Blocks.DEEPSLATE_REDSTONE_ORE)) {
                            dropState = state.is(Blocks.DEEPSLATE_REDSTONE_ORE)
                                    ? Blocks.DEEPSLATE_REDSTONE_ORE.defaultBlockState()
                                    : Blocks.REDSTONE_ORE.defaultBlockState();
                        }

                        // Remove the block
                        level.destroyBlock(orePos, false, player);

                        // Try to add to player inventory, drop on ground if full
                        ItemStack oreStack = new ItemStack(dropState.getBlock().asItem(), 1);
                        if (!player.addItem(oreStack)) {
                            player.drop(oreStack, false);
                        }

                        oresExtracted++;

                        // Damage the item
                        stack.hurt(1, serverLevel.getRandom(), null);
                        if (stack.getDamageValue() >= stack.getMaxDamage()) {
                            stack.shrink(1);
                            player.displayClientMessage(Component.literal("§cChunk Excavator broke!"), true);
                            broken = true;
                            break;
                        }
                    }
                }
            }
        }

        if (oresFound > 0) {
            player.displayClientMessage(Component.literal("§aExtracted " + oresExtracted + "/" + oresFound + " ores from chunk"), true);
        } else {
            player.displayClientMessage(Component.literal("§7No ores found in this chunk"), true);
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(Component.literal("§7Right-click to extract all ores in this chunk"));
        tooltip.add(Component.literal("§7Durability: " + (stack.getMaxDamage() - stack.getDamageValue()) + "/" + stack.getMaxDamage()));
    }
}
