package com.valence.valence.fluid;

import com.valence.valence.Registration;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.fluids.ForgeFlowingFluid;

public abstract class SteamFluid extends ForgeFlowingFluid {
    private static final Properties PROPS = new Properties(
        Registration.STEAM_FLUID_TYPE,
        Registration.STEAM,
        Registration.STEAM_FLOWING
    ).block(null).bucket(null);

    protected SteamFluid() {
        super(PROPS);
    }

    public static class Source extends SteamFluid {
        @Override
        public boolean isSource(FluidState state) {
            return true;
        }

        @Override
        public int getAmount(FluidState state) {
            return 8;
        }
    }

    public static class Flowing extends SteamFluid {
        @Override
        public boolean isSource(FluidState state) {
            return false;
        }

        @Override
        public int getAmount(FluidState state) {
            return state.getValue(LEVEL);
        }
    }
}
