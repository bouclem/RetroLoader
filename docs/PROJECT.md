# RetroLoader — Project Architecture

## Overview

RetroLoader is a modern modloader targeting Minecraft versions before release 1.7. It combines proven concepts from contemporary loaders (Fabric, NeoForge) with support for very old game versions that no other modern loader covers.

## Design Decisions

| Decision | Choice | Rationale |
|---|---|---|
| Language | Java | Minecraft is Java; modding ecosystem is Java |
| Patching | SpongePowered Mixin | Industry standard, developer-friendly, proven in Fabric/NeoForge |
| Mappings | Intermediary stable layer | Mods use stable names; game version updates don't break mods |
| Event listening | `@SubscribeEvent` (NeoForge-style) | Familiar, less boilerplate |
| Event creation | NeoForge-style event classes | Simple, extensible |
| Type safety | Compile-time enforced (Fabric-style) | Catch errors at build time, not runtime |
| Discoverability | Auto-scan registered classes | Harder to forget, less manual registration |
| API package | `retroloader.api` | Clean namespace |
| License | Apache 2.0 | Permissive, business-friendly, widely used |

## Architecture

### Layers (bottom to top)

```
┌─────────────────────────────────────────────┐
│              Mod (developer code)             │
├─────────────────────────────────────────────┤
│         retroloader.api (public API)          │
│  Events · Registry · Mixin support · Hooks    │
├─────────────────────────────────────────────┤
│       retroloader (loader internals)          │
│  Mod discovery · Dependency resolution ·      │
│  Mapping application · Class transformation   │
├─────────────────────────────────────────────┤
│          Mixin (SpongePowered)                │
│  Bytecode injection at runtime                │
├─────────────────────────────────────────────┤
│         Java Agent / Class Loader             │
│  Intercepts class loading                     │
├─────────────────────────────────────────────┤
│       Minecraft (target game version)         │
│  com.mojang.minecraft.* (obfuscated)          │
└─────────────────────────────────────────────┘
```

### Loading Sequence

1. **JVM starts** with RetroLoader as Java agent (`-javaagent:RetroLoader.jar`)
2. **RetroLoader initializes** — discovers mods in `mods/` directory
3. **Mappings loaded** — intermediary mappings for the target game version
4. **Dependency resolution** — mods sorted by dependencies, conflicts detected
5. **Mixin application** — registered mixins applied to game classes as they load
6. **Event bus initialized** — event classes registered
7. **Mod initialization** — mods receive init events in dependency order
8. **Game starts** — Minecraft runs with patches and mods applied

## Event System

### Design

- **Listening**: `@SubscribeEvent` annotation on methods, auto-discovered when class is registered on the event bus
- **Creating**: Event classes extend a base `RetroEvent` class
- **Type safety**: Event types are strongly typed at compile time — no string-based lookups
- **Posting**: `EventBus.post(event)` returns whether the event was cancelled

### Example (proposed API)

```java
// Register a listener
@RetroMod(id = "example_mod", version = "1.0.0")
public class ExampleMod implements ModInitializer {

    @Override
    public void onInitialize(ModContext ctx) {
        ctx.getEventBus().register(this);
    }

    @SubscribeEvent
    public void onBlockPlace(BlockPlaceEvent event) {
        // Type-safe, auto-discovered
        System.out.println("Block placed at " + event.getPosition());
    }
}
```

### Creating a Custom Event

```java
public class MyCustomEvent extends RetroEvent {
    private final String data;

    public MyCustomEvent(String data) {
        this.data = data;
    }

    public String getData() {
        return data;
    }

    @Override
    public boolean isCancellable() {
        return true;
    }
}
```

## Mappings

### Purpose

Classic 0.0.13a_03 uses unobfuscated class names (e.g., `com.mojang.minecraft.Minecraft`) but later versions are obfuscated. RetroLoader provides an **intermediary mapping layer** so mod code always references stable names regardless of the target version's obfuscation state.

### Mapping Structure

- **Game names** → actual class/method names in the target version's jar
- **Intermediary names** → stable names used by mods (e.g., `retroloader.minecraft.Minecraft`)
- Each supported version has its own mapping file

### For Classic 0.0.13a_03

This version is **not obfuscated** — class names are already readable (`com.mojang.minecraft.Minecraft`, `com.mojang.minecraft.level.Level`, etc.). The intermediary mapping for this version will be close to 1:1, but still provides a stable layer for future obfuscated versions.

## Mixin Usage

Mods use Mixin to patch game classes without modifying the original jar:

```java
@Mixin(com.mojang.minecraft.Minecraft.class)
public class MinecraftMixin {

    @Inject(method = "run()V", at = @At("HEAD"))
    private void onGameStart(CallbackInfo ci) {
        // Custom code runs before the game loop starts
    }
}
```

RetroLoader handles Mixin discovery and application automatically. Mods declare their mixin classes in mod metadata.

## Mod Metadata

Each mod includes a metadata file (format TBD — likely JSON or TOML):

```json
{
  "id": "example_mod",
  "version": "1.0.0",
  "name": "Example Mod",
  "description": "An example mod for RetroLoader",
  "author": "bouclem",
  "dependencies": {
    "retroloader": ">=0.1.0",
    "minecraft": "c0.0.13a_03"
  },
  "entrypoints": {
    "main": "com.example.ExampleMod"
  },
  "mixins": [
    "mixins.example_mod.json"
  ]
}
```

## Classic 0.0.13a_03 — Game Structure

### Package Layout

```
com.mojang.minecraft
├── gamemode      # Creative/Survival game modes
├── gui           # GUI screens, menus, HUD
├── item          # Item definitions
├── level         # Level (world) data, loading, generation
├── mob           # Mob entities (sheep, pig, zombie, etc.)
├── model         # 3D models for entities
├── net           # Network protocol (multiplayer)
├── particle      # Particle effects
├── phys          # Physics (AABB, collision)
├── player        # Player entity, movement
├── render        # Rendering (world, entities, HUD)
└── sound         # Sound system
```

### Key Classes

| Class | Role |
|---|---|
| `Minecraft` | Main game class, game loop, state management |
| `MinecraftApplet` | Applet wrapper for browser embedding |
| `Entity` | Base entity class |
| `GameSettings` | Configuration, key bindings |
| `KeyBinding` | Key binding definitions |
| `Timer` | Game tick timer |
| `Level` | World data (block array, dimensions) |
| `LevelRenderer` | World rendering |
| `Player` | Player entity |
| `TextureManager` | Texture loading and management |

### Level Format (0.0.13a_03)

| Field | Size | Description |
|---|---|---|
| Magic | 4 bytes | `27 1B B7 88` |
| Version | 1 byte | `01` |
| World Name | Variable | Length-prefixed ASCII string |
| Creator Name | Variable | Length-prefixed ASCII string |
| Timestamp | 8 bytes | Unix timestamp (long) |
| Width | 2 bytes | World width (x) |
| Height | 2 bytes | World height (z) |
| Depth | 2 bytes | World depth (y) |
| Block Array | 4,194,304 bytes | 256×256×64 block IDs, order: x → z → y |

### Known Bugs (relevant for modding)

- Choppy mouse input
- Auto-loads `level.dat` on startup — crashes if file is from a different version
- Diagonal water/lava swimming exploit

### Dependencies

- LWJGL (Lightweight Java Game Library) — rendering, input, audio
- Java (target: compatible with the era's JVM, but RetroLoader itself built on modern JDK)

## Development Phases

See [TODO.md](TODO.md) for the detailed roadmap.
