package com.valence.valence.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

public class ValenceConfig {

    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    // Grinder
    public static final ForgeConfigSpec.IntValue GRINDER_DEFAULT_PROCESSING_TIME;
    public static final ForgeConfigSpec.IntValue GRINDER_MAX_STACK_SIZE;

    // Miners
    public static final ForgeConfigSpec.IntValue BASIC_MINER_FUEL_DURATION;
    public static final ForgeConfigSpec.IntValue ADVANCED_MINER_FUEL_DURATION;
    public static final ForgeConfigSpec.IntValue BASIC_MINER_OUTPUT_SLOTS;
    public static final ForgeConfigSpec.IntValue ADVANCED_MINER_OUTPUT_SLOTS;

    // Steam machines
    public static final ForgeConfigSpec.IntValue STEAM_TANK_CAPACITY;
    public static final ForgeConfigSpec.IntValue STEAM_PER_OPERATION;

    // Electric
    public static final ForgeConfigSpec.IntValue ELECTRIC_FURNACE_CAPACITY;
    public static final ForgeConfigSpec.IntValue ELECTRIC_FURNACE_CONSUMPTION;
    public static final ForgeConfigSpec.IntValue ELECTRIC_FURNACE_SPEED;

    // Pebble
    public static final ForgeConfigSpec.DoubleValue PEBBLE_DROP_CHANCE;

    // DF energy
    public static final ForgeConfigSpec.IntValue DF_CELL_CAPACITY;
    public static final ForgeConfigSpec.IntValue MEGA_CELL_CAPACITY;
    public static final ForgeConfigSpec.IntValue DF_TRANSFER_RATE;

    static {
        BUILDER.comment("Valence Mod Configuration").push("general");

        BUILDER.comment("Grinder settings").push("grinder");
        GRINDER_DEFAULT_PROCESSING_TIME = BUILDER
                .comment("Default processing time in ticks for grinding recipes (20 ticks = 1 second)")
                .defineInRange("default_processing_time", 200, 20, 72000);
        GRINDER_MAX_STACK_SIZE = BUILDER
                .comment("Maximum items that can stack in a single grinder output slot")
                .defineInRange("max_output_stack", 64, 1, 64);
        BUILDER.pop();

        BUILDER.comment("Miner settings").push("miners");
        BASIC_MINER_FUEL_DURATION = BUILDER
                .comment("How many ticks one piece of coal/charcoal lasts in the Basic Miner")
                .defineInRange("basic_miner_fuel_duration", 200, 20, 72000);
        ADVANCED_MINER_FUEL_DURATION = BUILDER
                .comment("How many ticks one piece of fuel lasts in the Advanced Miner")
                .defineInRange("advanced_miner_fuel_duration", 200, 20, 72000);
        BASIC_MINER_OUTPUT_SLOTS = BUILDER
                .comment("Number of output slots in the Basic Miner")
                .defineInRange("basic_miner_output_slots", 4, 1, 9);
        ADVANCED_MINER_OUTPUT_SLOTS = BUILDER
                .comment("Number of output slots in the Advanced Miner")
                .defineInRange("advanced_miner_output_slots", 8, 1, 16);
        BUILDER.pop();

        BUILDER.comment("Steam machine settings").push("steam");
        STEAM_TANK_CAPACITY = BUILDER
                .comment("Steam tank capacity in millibuckets for steam machines")
                .defineInRange("steam_tank_capacity", 100, 10, 10000);
        STEAM_PER_OPERATION = BUILDER
                .comment("Steam consumed per operation (smelt, alloy, etc.)")
                .defineInRange("steam_per_operation", 18, 1, 1000);
        BUILDER.pop();

        BUILDER.comment("Electric Furnace settings").push("electric_furnace");
        ELECTRIC_FURNACE_CAPACITY = BUILDER
                .comment("DF energy buffer capacity")
                .defineInRange("df_capacity", 500, 50, 50000);
        ELECTRIC_FURNACE_CONSUMPTION = BUILDER
                .comment("DF consumed per tick when smelting")
                .defineInRange("df_per_tick", 5, 1, 500);
        ELECTRIC_FURNACE_SPEED = BUILDER
                .comment("Ticks per smelting operation")
                .defineInRange("progress_max", 100, 10, 72000);
        BUILDER.pop();

        BUILDER.comment("Pebble settings").push("pebble");
        PEBBLE_DROP_CHANCE = BUILDER
                .comment("Chance (0.0 to 1.0) of getting a pebble when sneaking on grass/dirt/stone")
                .defineInRange("drop_chance", 0.5, 0.0, 1.0);
        BUILDER.pop();

        BUILDER.comment("DF Energy settings").push("energy");
        DF_CELL_CAPACITY = BUILDER
                .comment("DF Cell energy capacity")
                .defineInRange("df_cell_capacity", 1000, 100, 1000000);
        MEGA_CELL_CAPACITY = BUILDER
                .comment("Mega Cell energy capacity")
                .defineInRange("mega_cell_capacity", 100000000, 1000, 1000000000);
        DF_TRANSFER_RATE = BUILDER
                .comment("Maximum DF transfer rate per tick between machines")
                .defineInRange("transfer_rate", 20, 1, 100000);
        BUILDER.pop();

        BUILDER.pop();
        SPEC = BUILDER.build();
    }

    public static void register() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, SPEC, "valence-common.toml");
    }
}
