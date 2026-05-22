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
            // Implementation would go here to restrict drops
            return generatedLoot;
        }

        @Override
        public Codec<? extends IGlobalLootModifier> codec() {
            return ORE_RESTRICTION_MODIFIER.get();
        }
    }
}
