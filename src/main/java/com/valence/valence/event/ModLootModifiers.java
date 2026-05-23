package com.valence.valence.event;

import com.valence.valence.ValenceMod;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.tags.BlockTags;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class ModLootModifiers {
    public static final DeferredRegister<Codec<? extends IGlobalLootModifier>> LOOT_MODIFIER_SERIALIZERS = 
            DeferredRegister.create(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, ValenceMod.MODID);

    public static final RegistryObject<Codec<? extends IGlobalLootModifier>> ORE_RESTRICTION_MODIFIER = 
            LOOT_MODIFIER_SERIALIZERS.register("ore_restriction", () -> OreRestrictionModifier.CODEC);

    public static class OreRestrictionModifier extends LootModifier {
        public static final Codec<OreRestrictionModifier> CODEC = RecordCodecBuilder.create(inst -> 
            codecStart(inst).apply(inst, OreRestrictionModifier::new));

        protected OreRestrictionModifier(LootItemCondition[] conditionsIn) {
            super(conditionsIn);
        }

        @Override
        protected @NotNull ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
            BlockState state = context.getParamOrNull(LootContextParams.BLOCK_STATE);
            if (state != null && (state.builtInRegistryHolder().is(BlockTags.IRON_ORES) ||
                                 state.builtInRegistryHolder().is(BlockTags.GOLD_ORES) ||
                                 state.builtInRegistryHolder().is(BlockTags.COPPER_ORES) ||
                                 state.builtInRegistryHolder().is(BlockTags.REDSTONE_ORES) ||
                                 state.builtInRegistryHolder().is(BlockTags.LAPIS_ORES) ||
                                 state.builtInRegistryHolder().is(BlockTags.DIAMOND_ORES) ||
                                 state.builtInRegistryHolder().is(BlockTags.EMERALD_ORES) ||
                                 state.builtInRegistryHolder().is(BlockTags.COAL_ORES))) {
                generatedLoot.clear();
                generatedLoot.add(new ItemStack(state.getBlock()));
            }
            return generatedLoot;
        }

        @Override
        public Codec<? extends IGlobalLootModifier> codec() {
            return ORE_RESTRICTION_MODIFIER.get();
        }
    }
}
