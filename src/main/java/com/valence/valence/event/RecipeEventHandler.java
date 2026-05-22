package com.valence.valence.event;

import com.valence.valence.ValenceMod;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ValenceMod.MODID)
public class RecipeEventHandler {
    // This is a placeholder. To truly disable recipes, we should use a Recipe Serializer 
    // or a JSON-based recipe removal system via DataPacks.
}
