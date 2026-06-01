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
            player.displayClientMessage(Component.literal(" §7Tier 6 (Quantum): §fQuantum Miner (DF-powered)"), false);
            player.displayClientMessage(Component.literal(""), false);
            player.displayClientMessage(Component.literal("§eMachines:"), false);
            player.displayClientMessage(Component.literal(" §7Grinder: §fGrinds raw ores into powders. Input top, output bottom."), false);
            player.displayClientMessage(Component.literal(" §7Basic Miner: §fScans a 16x16 area for ores. Fuel: coal, charcoal, logs, sugar cane."), false);
            player.displayClientMessage(Component.literal(" §7Advanced Miner: §fScans 16x16, uses DF energy + fuel backup. 12 output slots."), false);
            player.displayClientMessage(Component.literal(" §7Quantum Miner: §fScans 16x16 using DF only. High speed, 12 output slots."), false);
            player.displayClientMessage(Component.literal(" §7Steam Furnace: §fSmelts using steam instead of fuel. Needs steam input."), false);
            player.displayClientMessage(Component.literal(" §7Steam Dynamo: §fConverts water → steam using heat. Water in, steam out."), false);
            player.displayClientMessage(Component.literal(" §7Steam Turbine: §fConverts steam → DF (Dark Force energy)."), false);
            player.displayClientMessage(Component.literal(" §7Steam Alloyer: §fFor alloy recipes (bronze, etc.). Uses steam."), false);
            player.displayClientMessage(Component.literal(" §7Electric Furnace: §fMulti-slot smelting using DF. 8 parallel inputs."), false);
            player.displayClientMessage(Component.literal(" §7Blast Furnace: §fMultiblock smelter. Needs lava below + brick ring. 2x speed."), false);
            player.displayClientMessage(Component.literal(" §7Ore Washer: §fWashes powders using water."), false);
            player.displayClientMessage(Component.literal(" §7Centrifuge: §fSpins to separate materials."), false);
            player.displayClientMessage(Component.literal(" §7Chemical Reactor: §fCombines inputs to produce new materials."), false);
            player.displayClientMessage(Component.literal(" §7Assembler: §fCrafts items from multiple ingredients."), false);
            player.displayClientMessage(Component.literal(""), false);
            player.displayClientMessage(Component.literal("§eEnergy:"), false);
            player.displayClientMessage(Component.literal(" §7Dark Force (DF) is the end-game energy system."), false);
            player.displayClientMessage(Component.literal(" §7Generate: §fSteam Dynamo → Steam Turbine (Water + heat = Steam = DF)"), false);
            player.displayClientMessage(Component.literal(" §7Store: §fDF Cell (1k DF), Mega Cell (100M DF)"), false);
            player.displayClientMessage(Component.literal(" §7Transmit: §fEnergy Cable (wired), Wireless Node (wireless, 32 block range)"), false);
            player.displayClientMessage(Component.literal(""), false);
            player.displayClientMessage(Component.literal("§eOre Processing Chain:"), false);
            player.displayClientMessage(Component.literal(" §fRaw Ore §7→ §fGrinder §7→ Powder §7→ §fFurnace §7→ Ingot"), false);
            player.displayClientMessage(Component.literal(" §fPowder §7→ §fOre Washer §7→ Pure Dust §7→ §fCentrifuge/Reactor §7→ §fAlloyer §7→ Bronze"), false);
            player.displayClientMessage(Component.literal(""), false);
            player.displayClientMessage(Component.literal("§eGetting Started:"), false);
            player.displayClientMessage(Component.literal(" §71. Sneak + right-click grass/dirt to find §fPebbles"), false);
            player.displayClientMessage(Component.literal(" §72. Craft 9 pebbles → §fCobblestone"), false);
            player.displayClientMessage(Component.literal(" §73. Build a §fGrinder §7to grind ores into powders"), false);
            player.displayClientMessage(Component.literal(" §74. Build a §fBasic Miner §7to find and extract ores"), false);
            player.displayClientMessage(Component.literal(" §75. Smelt powders in a vanilla furnace for §fBronze Ingots"), false);
            player.displayClientMessage(Component.literal(" §76. Enter the Steam Age: §fSteam Furnace, Dynamo, Turbine"), false);
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