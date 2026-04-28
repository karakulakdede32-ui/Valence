# VALENCE MOD - TEST BUILD DOCUMENTATION

## What's Included in This Test Build

### 🏗️ Core Structure
- ✅ Gradle build system (`build.gradle`)
- ✅ Main mod class with proper initialization
- ✅ Block registry system
- ✅ Item registry system  
- ✅ Recipe system

### 🧪 Blocks (Chemistry Machines)
1. **Chemical Reactor** - Core reaction vessel (5.0 hardness, 10.0 resistance)
   - Crafted from: Iron Block, Copper Block, Redstone Block
   
2. **Centrifuge** - Separates compounds (5.0 hardness, 10.0 resistance)
   - Crafted from: Iron Block, Copper Block, Stone
   
3. **Distillation Tower** - Separates liquids (4.0 hardness, 8.0 resistance)
   - Crafted from: Iron Block, Copper Block, Glass
   
4. **Electrolyzer** - Breaks compounds using electricity (5.0 hardness, 12.0 resistance)
   - Crafted from: Iron Block, Copper Block, Redstone Block

### 🧪 Items
**Chemicals:**
- Hydrogen Vial (H₂)
- Oxygen Vial (O₂)
- Sulfuric Acid (H₂SO₄)
- Nitric Acid (HNO₃)

**Materials:**
- Copper Dust
- Iron Dust
- Gold Dust
- Titanium Ingot
- Tungsten Ingot
- Chrome Ingot

### 🧬 Chemistry System
- **ChemicalElement** - Represents periodic table elements with atomic data
- **Compound** - Represents chemical compounds with formula and molar mass
- **ChemicalReaction** - Handles reaction requirements (temperature, energy)
- **PeriodicTable** - Predefined elements (H, O, N, S, C, Fe, Cu, Ti, W, Cr)

### 📋 Recipes
All 4 machines have crafting recipes defined in JSON format.

### 🎨 Models & Textures
- Block models for all 4 machines
- Item models for all items
- Placeholder texture references (ready for PNG files)
- Translation file for game text (en_us.json)

---

## How to Use This Test Build

1. **Clone the branch:**
   \`\`\`bash
   git checkout experiment/first-test
   \`\`\`

2. **Build the mod:**
   \`\`\`bash
   ./gradlew build
   \`\`\`

3. **Run in development:**
   \`\`\`bash
   ./gradlew runClient
   \`\`\`

4. **Find the JAR:**
   \`\`\`
   build/libs/valence-0.1.0.jar
   \`\`\`

---

## Next Steps to Improve

- [ ] Add actual PNG textures (currently placeholders)
- [ ] Add block entity classes for machine GUIs
- [ ] Add container/screen classes for machine interfaces
- [ ] Implement energy/EU system (like Gregtech)
- [ ] Add machine processing logic
- [ ] Add more chemicals and elements
- [ ] Add Gregtech-style difficulty (higher tier versions)
- [ ] Add custom crafting recipes for chemical reactions
- [ ] Add world generation for ores
- [ ] Add fluid handling system

---

## File Structure

\`\`\`
experiment/first-test/
├── build.gradle                          ← Build configuration
├── src/
│   ├── main/
│   │   ├── java/com/valence/
│   │   │   ├── ValenceMod.java          ← Main class
│   │   │   ├── block/
│   │   │   │   ├── ModBlocks.java       ← Block registry
│   │   │   │   ├── ChemicalReactorBlock.java
│   │   │   │   ├── CentrifugeBlock.java
│   │   │   │   ├── DistillationTowerBlock.java
│   │   │   │   └── ElectrolyzerBlock.java
│   │   │   ├── item/
│   │   │   │   └── ModItems.java        ← Item registry
│   │   │   ├── chemistry/
│   │   │   │   ├── ChemicalElement.java
│   │   │   │   ├── Compound.java
│   │   │   │   ├── ChemicalReaction.java
│   │   │   │   └── PeriodicTable.java
│   │   │   └── recipe/
│   │   │       └── ModRecipeSerializers.java
│   │   └── resources/
│   │       ├── assets/valence/
│   │       │   ├── models/
│   │       │   │   ├── block/
│   │       │   │   └── item/
│   │       │   ├── textures/
│   │       │   │   ├── block/
│   │       │   │   └── item/
│   │       │   └── lang/
│   │       │       └── en_us.json
│   │       ├── data/valence/
│   │       │   └── recipes/
│   │       └── META-INF/
│   │           └── mods.toml
│   └── test/
└── docs/
\`\`\`

---

Created: 2026-04-28
Branch: experiment/first-test
Status: Ready for testing!
