# Valence Mod — Developer Guide

## Code Navigation

```
src/main/java/com/valence/valence/
├── ValenceMod.java              # Entry point, mod bus setup
├── Registration.java            # All deferred registries (blocks, items, menus, etc.)
├── config/ValenceConfig.java    # Forge common config with tweakable values
├── energy/DFStorage.java        # Dark Force energy storage (extends Forge Energy)
├── fluid/SteamFluid.java        # Steam fluid type
├── block/                       # Block, TE, Menu (one subpackage per machine)
│   ├── GrinderBlock.java        # (machines directly in block/ are older)
│   ├── miner/                   # BasicMiner, AdvancedMiner
│   ├── furnace/                 # SteamFurnace
│   ├── alloyer/                 # SteamAlloyer
│   ├── dynamo/                  # SteamDynamo
│   ├── turbine/                 # SteamTurbine
│   ├── dfcell/                  # DFCell
│   ├── megacell/                # MegaCell
│   ├── efurnace/                # ElectricFurnace
│   ├── collector/               # WaterCollector
│   ├── conduit/                 # TransferConduit
│   ├── pipe/                    # FluidPipe, EnergyCable
│   ├── wireless/                # WirelessNode
│   ├── seeder/                  # SeedDuplicator
│   └── growthchamber/           # TreeGrowthChamber
├── client/gui/                  # All GUI screens
├── event/                       # Event handlers (pebbles, loot)
├── recipe/                      # Custom recipe types (GrinderRecipe)
└── item/                        # Custom items (LinkingTool, ChunkExcavator)
```

## Adding a New Machine

1. Create a subpackage under `block/` (e.g., `block/crusher/`)
2. Create files: `CrusherBlock.java`, `CrusherTileEntity.java`, `CrusherMenu.java`
3. Create GUI screen in `client/gui/CrusherScreen.java`
4. Register everything in `Registration.java` (blocks, items, block entities, menus)
5. Update `ClientSetup.java` to register the screen
6. Add recipe JSON under `data/valence/recipes/`
7. Add lang entries in `assets/valence/lang/en_us.json`
8. Add blockstate + model JSONs under `assets/valence/`

## Conventions

- **Blocks** extend `BaseEntityBlock` (or `BaseMachineBlock` if one gets created)
- **Tile entities** extend `BaseMachineTileEntity` for item-based machines
- **Tile entities** extend `BlockEntity` directly for fluid/energy machines
- Use `ValenceGui.draw*` helpers for consistent UI look
- Energy system uses Forge `IEnergyStorage` (wrapped via `DFStorage`)
- Fluid system uses Forge `IFluidHandler` (wrapped via `FluidTank`)
- All hardcoded tuning values should be moved to `ValenceConfig` when found

## Progression Tree (Recipe Gates)

- **Tier 1 (Handcraft):** Pebble, Grinder, Basic Miner, Bronze Ingot
- **Tier 2 (Steam Age):** Steam Furnace (needs: Grinder), Steam Dynamo (needs: Basic Miner), Water Collector (needs: Basic Miner)
- **Tier 3 (Advanced Steam):** Steam Alloyer (needs: Steam Furnace), Steam Turbine (needs: Steam Dynamo), Advanced Miner (needs: Basic Miner)
- **Tier 4 (Electric Age):** Electric Furnace (needs: Steam Furnace), DF Cell (needs: Electric Furnace), Transfer Conduit (needs: DF Cell)
- **Tier 5 (Late Game):** Energy Cable, Fluid Pipe, Wireless Node, Seed Duplicator, Tree Growth Chamber, Mega Cell, Chunk Excavator
