package com.valence.valence.event;

import com.valence.valence.Registration;
import com.valence.valence.ValenceMod;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Random;

@Mod.EventBusSubscriber(modid = ValenceMod.MODID)
public class PebbleEventHandler {
    private static final Random RANDOM = new Random();

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        BlockState state = level.getBlockState(pos);

        // Conditions: Sneaking, Main Hand, Empty Hand
        if (player.isShiftKeyDown() && event.getHand() == InteractionHand.MAIN_HAND && player.getMainHandItem().isEmpty()) {
            
            // Block conditions: Grass, Dirt, or Stone
            boolean isValidBlock = state.is(Blocks.GRASS_BLOCK) || 
                                 state.is(Blocks.DIRT) || 
                                 state.is(Blocks.STONE) ||
                                 state.is(Blocks.COARSE_DIRT) ||
                                 state.is(Blocks.ROOTED_DIRT);

            if (isValidBlock) {
                if (!level.isClientSide) {
                    // 50% chance
                    if (RANDOM.nextFloat() < 0.5f) {
                        ItemStack pebble = new ItemStack(Registration.STONE_PEBBLE.get());
                        if (!player.getInventory().add(pebble)) {
                            player.drop(pebble, false);
                        }
                        player.swing(InteractionHand.MAIN_HAND);
                    }
                }
                // Cancel event to prevent other interactions
                event.setCanceled(true);
            }
        }
    }
}
