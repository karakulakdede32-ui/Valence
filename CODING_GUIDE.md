# 📚 Where to Put Your Code - A Beginner's Guide

Welcome! This guide explains exactly where to put different types of code for your Minecraft mod.

## 🗂️ Folder Structure Explained

### Main Location: `src/main/java/com/valence/`

This is where **ALL your Java code goes**. Think of it as the "brain" of your mod.

```
src/main/java/com/valence/
├── ValenceMod.java                    ← Main mod file (already created)
├── block/                             ← Your custom blocks go here
│   ├── ChemicalReactorBlock.java
│   └── LabBenchBlock.java
├── item/                              ← Your custom items go here
│   ├── ChemicalVialItem.java
│   └── ReagentBottleItem.java
├── entity/                            ← Your custom entities go here
│   └── ChemicalParticleEntity.java
├── recipe/                            ← Your crafting recipes go here
│   └── ChemicalRecipeRegistry.java
├── chemistry/                         ← Chemistry logic goes here
│   ├── ChemicalElement.java
│   ├── ChemicalReaction.java
│   └── ReactionCalculator.java
└── event/                             ← Event handlers go here
    └── PlayerEventHandler.java
```

## 📝 Types of Files & Where They Go

### 1. **Block Files** → `src/main/java/com/valence/block/`
Creates custom blocks in your mod

**Example: ChemicalReactorBlock.java**
```java
package com.valence.block;

import net.minecraft.world.level.block.Block;

public class ChemicalReactorBlock extends Block {
    public ChemicalReactorBlock(Properties properties) {
        super(properties);
    }
    
    // Block logic here
}
```

### 2. **Item Files** → `src/main/java/com/valence/item/`
Creates custom items in your mod

**Example: ChemicalVialItem.java**
```java
package com.valence.item;

import net.minecraft.world.item.Item;

public class ChemicalVialItem extends Item {
    public ChemicalVialItem(Item.Properties properties) {
        super(properties);
    }
    
    // Item logic here
}
```

### 3. **Chemistry Logic** → `src/main/java/com/valence/chemistry/`
Your complex chemistry calculations and reactions

**Example: ChemicalElement.java**
```java
package com.valence.chemistry;

public class ChemicalElement {
    private String name;
    private int atomicNumber;
    private double atomicMass;
    
    // Chemistry logic here
}
```

### 4. **Event Handlers** → `src/main/java/com/valence/event/`
Responds to game events (player joins, breaks blocks, etc.)

**Example: PlayerEventHandler.java**
```java
package com.valence.event;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;

@Mod.EventBusSubscriber(modid = "valence", bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class PlayerEventHandler {
    
    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        // Code that runs when player joins
    }
}
```

## 🎨 Resources: `src/main/resources/`

This is where **visual/config files** go:

```
src/main/resources/
├── assets/valence/                    ← Your mod's visuals
│   ├── textures/
│   │   ├── block/                    ← Block textures go here
│   │   │   └── chemical_reactor.png
��   │   └── item/                     ← Item textures go here
│   │       └── chemical_vial.png
│   ├── models/
│   │   ├── block/                    ← Block models
│   │   │   └── chemical_reactor.json
│   │   └── item/                     ← Item models
│   │       └── chemical_vial.json
│   └── lang/
│       └── en_us.json                ← Game text translations
├── data/valence/
│   ├── recipes/                      ← Crafting recipes
│   │   └── chemical_vial.json
│   └── worldgen/                     ← World generation
└── META-INF/
    └── mods.toml                     ← Already created (don't touch)
```

## 🔄 How It All Works Together

```
Player plays Minecraft
        ↓
Game triggers events → Event Handlers process them
        ↓
Player right-clicks block → Block code runs
        ↓
Chemistry calculation needed → Chemistry folder handles it
        ↓
Game renders → Uses textures from assets/
        ↓
Result shown to player
```

## 💡 Simple Example: Creating a Custom Block

### Step 1: Create the Java file
**File:** `src/main/java/com/valence/block/MyFirstBlock.java`
```java
package com.valence.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.Material;

public class MyFirstBlock extends Block {
    public MyFirstBlock() {
        super(Properties.of(Material.STONE)
            .sound(SoundType.STONE)
            .strength(3.0f, 5.0f));
    }
}
```

### Step 2: Register it
**File:** `src/main/java/com/valence/block/ModBlocks.java`
```java
package com.valence.block;

import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = 
        DeferredRegister.create(ForgeRegistries.BLOCKS, "valence");
    
    public static final RegistryObject<Block> MY_FIRST_BLOCK = 
        BLOCKS.register("my_first_block", MyFirstBlock::new);
}
```

### Step 3: Add a texture
**File:** `src/main/resources/assets/valence/textures/block/my_first_block.png`
(Add your PNG image here)

### Step 4: Add a model
**File:** `src/main/resources/assets/valence/models/block/my_first_block.json`
```json
{
  "parent": "block/cube_all",
  "textures": {
    "all": "valence:block/my_first_block"
  }
}
```

## 📦 Package Naming Convention

Always use: `com.valence.` + `category`

✅ **Good:**
- `com.valence.block`
- `com.valence.item`
- `com.valence.chemistry`

❌ **Bad:**
- `com.block` (missing mod name)
- `com.valence.stuff` (vague name)

## 🚀 Next Steps

1. **Learn Java basics** - Variables, loops, conditions, classes
2. **Create simple blocks** - Start with basic materials
3. **Add textures** - Use Photoshop or free tools like GIMP
4. **Test in-game** - See your creations work!
5. **Add complex features** - Chemistry, recipes, events

## 📚 Recommended Learning Resources

- [Minecraft Forge Documentation](https://docs.minecraftforge.net/)
- [Java for Beginners](https://www.oracle.com/java/technologies/javase/jdk-api-docs.html)
- [Minecraft Wiki](https://minecraft.fandom.com/)

---

**Remember:** Start simple! Create one block, then one item, then expand. You've got this! 🎮
