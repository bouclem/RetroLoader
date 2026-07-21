# RetroLoader

A modern modloader and API for old Minecraft versions.

## What is RetroLoader?

RetroLoader is a Java-based modloader that brings a **modern modding experience** — like NeoForge or Fabric — to vintage Minecraft versions. It targets versions **before release 1.7** (max ~1.6.x), specifically versions that are still accessible through the official launcher or archived but available.

The first supported version is **Classic 0.0.13a_03** (released May 22, 2009 — the earliest non-developer version in the launcher).

## Goals

- **Modern modding on old versions** — Mixin patching, event system, registry, mappings
- **Developer-friendly API** — mod developers never touch obfuscated game internals
- **Multi-mod coexistence** — mods are separate jars that load together cleanly
- **Per-version support** — each Minecraft version gets its own mapping set and adapter
- **Open source** — Apache 2.0 licensed, community-driven

## Key Features

| Feature | Description |
|---|---|
| **Mixin** | SpongePowered Mixin for high-level bytecode patching |
| **Mappings** | Intermediary mappings so mods use stable names, not obfuscated ones |
| **Event Bus** | `@SubscribeEvent` annotation-based, auto-discovery, compile-time type safety |
| **Registry** | Register custom blocks, items, entities, etc. |
| **Mod Metadata** | Structured mod definition with dependency resolution |
| **Gradle Plugin** | Dev environment setup, build tooling, mapping application |

## Supported Versions

| Version | Status |
|---|---|
| Classic 0.0.13a_03 | **In development** (first target) |
| Other Classic versions | Planned |
| Indev / Infdev | Planned |
| Alpha | Planned |
| Beta | Planned |
| Release 1.0 – 1.6.x | Planned |
| Release 1.7+ | **Not supported** (out of scope) |

Only versions available in the official launcher or archived-but-accessible will be supported.

## Links

- **GitHub**: [github.com/bouclem/RetroLoader](https://github.com/bouclem/RetroLoader)
- **License**: Apache 2.0

## Quick Start

> Documentation phase — no code yet. Quick start will be added once the loader is functional.

## Project Structure

Multi-module Gradle project:

```
RetroLoader/
├── .gitignore
├── LICENSE                          # Apache 2.0
├── docs/                            # Internal project documentation
├── wiki/                            # User & developer guides (how to use, how to mod)
│
├── retroloader-api/                 # Public API — @RetroMod, @SubscribeEvent,
│                                    #   ModInitializer, ModContext
├── retroloader-event/               # Event bus implementation, event base classes,
│                                    #   dispatch logic
├── retroloader-registry/            # Registry system — blocks, items, entities
├── retroloader-mixin/               # Mixin integration — wraps SpongePowered Mixin,
│                                    #   discovery, config
├── retroloader-mappings/            # Intermediary mapping system, remapping at
│                                    #   load/build time
├── retroloader-core/                # Mod discovery, metadata parsing, dependency
│                                    #   resolution, loading orchestration
├── retroloader-runtime/             # Java agent entry point, class loading
│                                    #   interception, bootstrap
├── retroloader-gradle/              # Gradle plugin — dev env, mapping application,
│                                    #   mod packaging
├── retroloader-tools/               # CLI utilities — mapping generation, decompile
│                                    #   helpers, diagnostics
│
└── versions/                        # Per-version adapters & mappings
    └── classic-0.0.13a_03/          #   Classic 0.0.13a_03 (first target)
        ├── mappings/                #     Intermediary mapping files
        └── adapter/                 #     Version-specific hooks & patches
```

See [PROJECT.md](PROJECT.md) for architecture and design details.
See [TODO.md](TODO.md) for the development roadmap.
