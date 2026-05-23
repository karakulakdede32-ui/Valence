package com.valence.valence.recipe;

import com.google.gson.JsonObject;
import com.valence.valence.ValenceMod;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class GrinderRecipe implements Recipe<SimpleContainer> {
    private final ResourceLocation id;
    private final Ingredient input;
    private final ItemStack output;
    private final int processingTime;

    public GrinderRecipe(ResourceLocation id, Ingredient input, ItemStack output, int processingTime) {
        this.id = id;
        this.input = input;
        this.output = output;
        this.processingTime = processingTime;
    }

    @Override
    public boolean matches(SimpleContainer pContainer, Level pLevel) {
        if (pLevel.isClientSide()) {
            return false;
        }
        return input.test(pContainer.getItem(0));
    }

    @Override
    public ItemStack assemble(SimpleContainer pContainer, RegistryAccess pRegistryAccess) {
        return output.copy();
    }

    @Override
    public boolean canCraftInDimensions(int pWidth, int pHeight) {
        return true;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess pRegistryAccess) {
        return output.copy();
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return Serializer.INSTANCE;
    }

    @Override
    public RecipeType<?> getType() {
        return Type.INSTANCE;
    }

    public Ingredient getInput() {
        return input;
    }

    public ItemStack getOutput() {
        return output;
    }

    public int getProcessingTime() {
        return processingTime;
    }

    public static class Type implements RecipeType<GrinderRecipe> {
        private Type() { }
        public static final Type INSTANCE = new Type();
        public static final String ID = "grinding";
    }

    public static class Serializer implements RecipeSerializer<GrinderRecipe> {
        public static final Serializer INSTANCE = new Serializer();
        public static final ResourceLocation ID = new ResourceLocation(ValenceMod.MODID, "grinding");

        @Override
        public GrinderRecipe fromJson(ResourceLocation pRecipeId, JsonObject pJson) {
            Ingredient input = Ingredient.fromJson(pJson.get("ingredients"));
            ItemStack output = ItemStack.fromJson(pJson.getAsJsonObject("output"));
            int processingTime = pJson.has("processingtime") ? pJson.get("processingtime").getAsInt() : 200; // Default to 200 ticks

            return new GrinderRecipe(pRecipeId, input, output, processingTime);
        }

        @Override
        public @Nullable GrinderRecipe fromNetwork(ResourceLocation pRecipeId, FriendlyByteBuf pBuffer) {
            Ingredient input = Ingredient.fromNetwork(pBuffer);
            ItemStack output = pBuffer.readItem();
            int processingTime = pBuffer.readInt();

            return new GrinderRecipe(pRecipeId, input, output, processingTime);
        }

        @Override
        public void toNetwork(FriendlyByteBuf pBuffer, GrinderRecipe pRecipe) {
            pRecipe.input.toNetwork(pBuffer);
            pBuffer.writeItemStack(pRecipe.output, false);
            pBuffer.writeInt(pRecipe.processingTime);
        }
    }
}
