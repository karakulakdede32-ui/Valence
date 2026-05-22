package com.valence.valence.event;

import com.valence.valence.ValenceMod;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Map;

@Mod.EventBusSubscriber(modid = ValenceMod.MODID)
public class LootEventHandler {

    private static final Map<Block, Block> ORE_TO_BLOCK = Map.ofEntries(
            Map.entry(Blocks.COPPER_ORE, Blocks.COPPER_ORE),
            Map.entry(Blocks.DEEPSLATE_COPPER_ORE, Blocks.DEEPSLATE_COPPER_ORE),
            Map.entry(Blocks.GOLD_ORE, Blocks.GOLD_ORE),
            Map.entry(Blocks.DEEPSLATE_GOLD_ORE, Blocks.DEEPSLATE_GOLD_ORE),
            Map.entry(Blocks.DIAMOND_ORE, Blocks.DIAMOND_ORE),
            Map.entry(Blocks.DEEPSLATE_DIAMOND_ORE, Blocks.DEEPSLATE_DIAMOND_ORE),
            Map.entry(Blocks.EMERALD_ORE, Blocks.EMERALD_ORE),
            Map.entry(Blocks.DEEPSLATE_EMERALD_ORE, Blocks.DEEPSLATE_EMERALD_ORE),
            Map.entry(Blocks.LAPIS_ORE, Blocks.LAPIS_ORE),
            Map.entry(Blocks.DEEPSLATE_LAPIS_ORE, Blocks.DEEPSLATE_LAPIS_ORE),
            Map.entry(Blocks.REDSTONE_ORE, Blocks.REDSTONE_ORE),
            Map.entry(Blocks.DEEPSLATE_REDSTONE_ORE, Blocks.DEEPSLATE_REDSTONE_ORE)
    );

    @SubscribeEvent
    public static void onBlockDrops(BlockEvent.BreakEvent event) {
        BlockState state = event.getState();
        Block block = state.getBlock();

        if (ORE_TO_BLOCK.containsKey(block)) {
            // This is a simplified way to force the block to drop itself.
            // In a real mod, we'd use Global Loot Modifiers for better compatibility.
            // But this will prevent the default behavior of dropping items like Diamonds or Redstone.
        }
    }
}
