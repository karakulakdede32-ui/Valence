package com.valence.valence.fluid;

import com.valence.valence.ValenceMod;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.ForgeFlowingFluid;

public abstract class SteamFluid extends ForgeFlowingFluid {

    // Proper FluidType with textures and gas-like behavior
    public static final FluidType STEAM_TYPE = new FluidType(FluidType.Properties.create()
        .descriptionId("fluid.valence.steam")
        .temperature(373)
        .density(-100)          // Very negative = lighter than air → rises
        .viscosity(100)          // Lower viscosity = flows easily
        .lightLevel(8)           // Slightly glowing
        .canSwim(false)          // Can't swim in gas
        .canDrown(false)         // Can't drown in gas
        .canExtinguish(false)    // Can't put out fires
        .canConvertToSource(false)
        .supportsBoating(false)
        .motionScale(0.002D)
    ) {
        @Override
        public void initializeClient(java.util.function.Consumer<net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions> consumer) {
            consumer.accept(new net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions() {
                private static final ResourceLocation STILL = new ResourceLocation(ValenceMod.MODID, "fluid/steam_still");
                private static final ResourceLocation FLOW = new ResourceLocation(ValenceMod.MODID, "fluid/steam_flow");
                
                @Override
                public ResourceLocation getStillTexture() { return STILL; }
                @Override
                public ResourceLocation getFlowingTexture() { return FLOW; }
                @Override
                public ResourceLocation getOverlayTexture() { return STILL; }
            });
        }
    };

    private static final Properties PROPS = new Properties(
        () -> STEAM_TYPE,
        com.valence.valence.Registration.STEAM,
        com.valence.valence.Registration.STEAM_FLOWING
    ).block(null).bucket(null);

    protected SteamFluid() {
        super(PROPS);
    }

    @Override
    public FluidType getFluidType() {
        return STEAM_TYPE;
    }

    // Make steam flow upward (gas behavior)
    @Override
    protected boolean canBeReplacedWith(FluidState state, BlockGetter level, BlockPos pos, Fluid fluid, Direction dir) {
        // Steam rises: can be replaced from below
        return dir == Direction.UP || super.canBeReplacedWith(state, level, pos, fluid, dir);
    }

    @Override
    protected int getSlopeFindDistance(LevelReader level) {
        return 3; // Spreads faster vertically
    }

    @Override
    protected int getDropOff(LevelReader level) {
        return 1; // Slower drop-off = travels further
    }

    @Override
    public Vec3 getFlow(BlockGetter level, BlockPos pos, FluidState state) {
        // Upward flow for steam
        return new Vec3(0, 0.1, 0);
    }

    @Override
    public int getTickDelay(LevelReader level) {
        return 5; // Faster than water
    }

    public static class Source extends SteamFluid {
        @Override
        public boolean isSource(FluidState state) { return true; }
        @Override
        public int getAmount(FluidState state) { return 8; }
    }

    public static class Flowing extends SteamFluid {
        @Override
        public boolean isSource(FluidState state) { return false; }
        @Override
        public int getAmount(FluidState state) { return state.getValue(LEVEL); }
    }
}
