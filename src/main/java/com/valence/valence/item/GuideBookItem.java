package com.valence.valence.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class GuideBookItem extends Item {
    public GuideBookItem(Properties p) { super(p); }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide) {
            player.displayClientMessage(Component.literal(""), false);
            player.displayClientMessage(Component.literal("§6=== Valence Guide ==="), false);
            player.displayClientMessage(Component.literal("§eProgression Tree:"), false);
            player.displayClientMessage(Component.literal(" §7Tier 1 (Handcraft): §fPebbles → Grinder → Basic Miner"), false);
            player.displayClientMessage(Component.literal(" §7Tier 2 (Steam): §fSteam Furnace, Dynamo, Water Collector"), false);
            player.displayClientMessage(Component.literal(" §7Tier 3 (Adv. Steam): §fAlloyer, Turbine, Adv. Miner"), false);
            player.displayClientMessage(Component.literal(" §7Tier 4 (Electric): §fElectric Furnace, DF Cell, Ore Washer"), false);
            player.displayClientMessage(Component.literal(" §7Tier 5 (Late): §fCentrifuge, Reactor, Assembler, Mega Cell"), false);
            player.displayClientMessage(Component.literal(""), false);
            player.displayClientMessage(Component.literal("§eOre Processing Chain:"), false);
            player.displayClientMessage(Component.literal(" §7Raw Ore → §fGrinder §7→ Powder → §fWasher §7→ Pure Dust → §fCentrifuge/Reactor §7→ Alloy"), false);
            player.displayClientMessage(Component.literal(""), false);
            player.displayClientMessage(Component.literal("§eEnergy:"), false);
            player.displayClientMessage(Component.literal(" §7Steam Dynamo (Water → Steam) → Steam Turbine (Steam → DF)"), false);
            player.displayClientMessage(Component.literal(" §7DF stored in DF Cell / Mega Cell, transmitted via Cable or Wireless Node"), false);
            player.displayClientMessage(Component.literal(""), false);
            player.displayClientMessage(Component.literal("§7§o(Sneak + right-click grass/dirt for pebbles to get started)"), false);
        }
        return InteractionResultHolder.success(player.getItemInHand(hand));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("§7Right-click for the Valence guide"));
    }
}