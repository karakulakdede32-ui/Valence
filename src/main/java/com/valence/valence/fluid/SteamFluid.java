package com.valence.valence.fluid;

import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.ForgeFlowingFluid;

public abstract class SteamFluid extends ForgeFlowingFluid {
    public static final FluidType STEAM_TYPE = new FluidType(FluidType.Properties.create()
        .descriptionId("fluid.valence.steam")
        .temperature(373)
        .density(-10)
        .viscosity(200)
        .lightLevel(0)
    );

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
