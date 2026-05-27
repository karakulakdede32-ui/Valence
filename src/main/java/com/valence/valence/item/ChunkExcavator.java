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
import net.minecraft.world.Containers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
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
        if (player.isShiftKeyDown()) return InteractionResult.FAIL; // prevent accidental use while sneaking

        ServerLevel serverLevel = (ServerLevel) level;
        BlockPos pos = ctx.getClickedPos();
        LevelChunk chunk = serverLevel.getChunkAt(pos);

        int oresFound = 0;
        int oresBroken = 0;

        // Scan the entire chunk (16x16 horizontal, full height)
        for (int sectionIdx = chunk.getSectionsCount() - 1; sectionIdx >= 0; sectionIdx--) {
            LevelChunkSection section = chunk.getSection(sectionIdx);
            if (section == null || section.hasOnlyAir()) continue;

            // Get the Y level for this section
            int sectionY = chunk.getSectionYFromSectionIndex(sectionIdx);

            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    for (int y = 0; y < 16; y++) {
                        int worldY = (sectionY << 4) + y;
                        if (worldY < level.getMinBuildHeight() || worldY > level.getMaxBuildHeight()) continue;

                        BlockPos orePos = new BlockPos((chunk.getPos().x << 4) + x, worldY, (chunk.getPos().z << 4) + z);
                        BlockState state = level.getBlockState(orePos);
                        BlockState defaultState = state.getBlock().defaultBlockState();

                        boolean isOre = state.is(BlockTags.COAL_ORES) ||
                                        state.is(BlockTags.IRON_ORES) ||
                                        state.is(BlockTags.COPPER_ORES) ||
                                        state.is(BlockTags.GOLD_ORES) ||
                                        state.is(BlockTags.REDSTONE_ORES) ||
                                        state.is(BlockTags.LAPIS_ORES) ||
                                        state.is(BlockTags.DIAMOND_ORES) ||
                                        state.is(BlockTags.EMERALD_ORES) ||
                                        state.is(Blocks.NETHER_QUARTZ_ORE) ||
                                        state.is(Blocks.NETHER_GOLD_ORE) ||
                                        state.is(Blocks.ANCIENT_DEBRIS);

                        if (!isOre) continue;
                        if (state.isAir()) continue;

                        oresFound++;

                        // Check durability
                        if (stack.getDamageValue() >= stack.getMaxDamage()) break;

                        // Silk-touch: drop the ore block itself so player can process it through the grinder
                        BlockState dropState = defaultState;

                        // Handle redstone ore - always use normal ore, not lit
                        if (state.is(Blocks.REDSTONE_ORE) || state.is(Blocks.DEEPSLATE_REDSTONE_ORE)) {
                            dropState = state.is(Blocks.DEEPSLATE_REDSTONE_ORE) ? Blocks.DEEPSLATE_REDSTONE_ORE.defaultBlockState() : Blocks.REDSTONE_ORE.defaultBlockState();
                        }

                        level.destroyBlock(orePos, false, player);
                        Containers.dropItemStack(level, orePos.getX() + 0.5, orePos.getY() + 0.5, orePos.getZ() + 0.5, new ItemStack(dropState.getBlock().asItem(), 1));
                        oresBroken++;

                        // Damage the item
                        stack.hurt(1, serverLevel.getRandom(), (net.minecraft.server.level.ServerPlayer)null);
                        if (stack.getDamageValue() >= stack.getMaxDamage()) {
                            stack.shrink(1);
                            player.displayClientMessage(Component.literal("Chunk Excavator broke!"), true);
                            break;
                        }
                    }
                    if (stack.isEmpty()) break;
                }
                if (stack.isEmpty()) break;
            }
            if (stack.isEmpty()) break;
        }

        if (oresFound > 0) {
            player.displayClientMessage(Component.literal("Extracted " + oresBroken + "/" + oresFound + " ores from chunk"), true);
        } else {
            player.displayClientMessage(Component.literal("No ores found in this chunk"), true);
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(Component.literal("Right-click to extract all ores in this chunk"));
        tooltip.add(Component.literal("Durability: " + (stack.getMaxDamage() - stack.getDamageValue()) + "/" + stack.getMaxDamage()));
    }
}
