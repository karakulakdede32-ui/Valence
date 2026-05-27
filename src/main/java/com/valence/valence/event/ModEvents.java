package com.valence.valence.event;

import com.valence.valence.ValenceMod;
import com.valence.valence.item.LinkingTool;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ValenceMod.MODID)
public class ModEvents {

    @SubscribeEvent
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        if (event.getLevel().isClientSide()) return;

        ItemStack stack = event.getItemStack();
        if (stack.isEmpty()) return;
        if (!(stack.getItem() instanceof LinkingTool)) return;
        if (!event.getEntity().isShiftKeyDown()) return;

        // Shift+left-click with LinkingTool: set export
        LinkingTool.handleShiftLeftClick(stack, event.getLevel(), event.getPos(), event.getEntity());
        event.setCanceled(true);
    }
}
