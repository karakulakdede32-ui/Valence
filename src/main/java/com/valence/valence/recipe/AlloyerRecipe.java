package com.valence.valence.recipe;

import com.google.gson.JsonObject;
import com.valence.valence.ValenceMod;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class AlloyerRecipe implements Recipe<SimpleContainer> {
    private final ResourceLocation id;
    private final Ingredient inputA;
    private final Ingredient inputB;
    private final ItemStack output;
    private final int processingTime;
    private final int steamCost;

    public AlloyerRecipe(ResourceLocation id, Ingredient inputA, Ingredient inputB, ItemStack output, int processingTime, int steamCost) {
        this.id = id; this.inputA = inputA; this.inputB = inputB;
        this.output = output; this.processingTime = processingTime; this.steamCost = steamCost;
    }

    @Override public boolean matches(SimpleContainer c, Level l) {
        boolean a0 = inputA.test(c.getItem(0)) && inputB.test(c.getItem(1));
        boolean a1 = inputA.test(c.getItem(1)) && inputB.test(c.getItem(0));
        return a0 || a1;
    }
    @Override public ItemStack assemble(SimpleContainer c, RegistryAccess r) { return output.copy(); }
    @Override public boolean canCraftInDimensions(int w, int h) { return true; }
    @Override public ItemStack getResultItem(RegistryAccess r) { return output.copy(); }
    @Override public ResourceLocation getId() { return id; }
    @Override public RecipeSerializer<?> getSerializer() { return Serializer.INSTANCE; }
    @Override public RecipeType<?> getType() { return Type.INSTANCE; }

    public Ingredient getInputA() { return inputA; }
    public Ingredient getInputB() { return inputB; }
    public ItemStack getOutput() { return output; }
    public int getProcessingTime() { return processingTime; }
    public int getSteamCost() { return steamCost; }

    public static class Type implements RecipeType<AlloyerRecipe> {
        private Type() {}
        public static final Type INSTANCE = new Type();
        public static final String ID = "alloying";
    }

    public static class Serializer implements RecipeSerializer<AlloyerRecipe> {
        public static final Serializer INSTANCE = new Serializer();
        public static final ResourceLocation ID = new ResourceLocation(ValenceMod.MODID, "alloying");

        @Override public AlloyerRecipe fromJson(ResourceLocation id, JsonObject json) {
            Ingredient a = Ingredient.fromJson(json.get("input_a"));
            Ingredient b = Ingredient.fromJson(json.get("input_b"));
            ItemStack out = ShapedRecipe.itemStackFromJson(json.getAsJsonObject("output"));
            int time = json.has("processingtime") ? json.get("processingtime").getAsInt() : 100;
            int steam = json.has("steam") ? json.get("steam").getAsInt() : 18;
            return new AlloyerRecipe(id, a, b, out, time, steam);
        }
        @Override public @Nullable AlloyerRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
            return new AlloyerRecipe(id, Ingredient.fromNetwork(buf), Ingredient.fromNetwork(buf), buf.readItem(), buf.readInt(), buf.readInt());
        }
        @Override public void toNetwork(FriendlyByteBuf buf, AlloyerRecipe r) {
            r.inputA.toNetwork(buf); r.inputB.toNetwork(buf);
            buf.writeItemStack(r.output, false); buf.writeInt(r.processingTime); buf.writeInt(r.steamCost);
        }
    }
}