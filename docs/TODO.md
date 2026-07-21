# RetroLoader — Roadmap

## Phase 1: Documentation

- [x] Define project scope and goals
- [x] Choose technical stack (Mixin, mappings, event system, API design)
- [x] Document Classic 0.0.13a_03 game structure
- [x] Write architecture document
- [ ] Research Mixin compatibility with very old class files (Java 1.1 / Java 5 era)
- [ ] Define intermediary mapping format
- [ ] Define mod metadata format (finalize JSON vs TOML)
- [ ] Design registry system (blocks, items, entities)
- [ ] Document event list for Classic 0.0.13a_03
- [ ] Design Gradle plugin specification

## Phase 2: Core Loader (current)

- [x] Gradle project setup (wrapper, root build, gradle.properties)
- [x] Java agent entry point (`RetroAgent.premain`)
- [x] Class loading interception (delegating `RetroClassTransformer`)
- [ ] Mod discovery (scan `mods/` directory)
- [ ] Mod metadata parsing
- [ ] Dependency resolution and sorting
- [ ] Mixin integration (SpongePowered Mixin)
- [ ] Mapping system implementation
- [ ] Logging framework

## Phase 3: API Layer

- [ ] `retroloader.api` package structure
- [ ] `ModInitializer` interface and `ModContext`
- [ ] Event bus implementation (`@SubscribeEvent`, auto-discovery, type safety)
- [ ] Base event classes for Classic 0.0.13a_03:
  - [ ] Game lifecycle events (init, tick, shutdown)
  - [ ] Block events (place, break, interact)
  - [ ] Player events (move, join, leave)
  - [ ] World events (load, save, generate)
  - [ ] Render events (pre-render, post-render, HUD)
- [ ] Registry system (blocks, items, entities)
- [ ] Configuration API for mods

## Phase 4: Mappings for Classic 0.0.13a_03

- [ ] Decompile / analyze `com.mojang.minecraft.*` classes
- [ ] Create intermediary mapping file
- [ ] Verify mapping coverage (all public classes/methods)
- [ ] Document mapping for mod developers

## Phase 5: Developer Tooling

- [ ] Gradle plugin (`retroloader-gradle`)
  - [ ] Dev environment setup
  - [ ] Mapping application at build time
  - [ ] Mod packaging (jar with metadata)
  - [ ] Run configuration for testing
- [ ] Template mod project
- [ ] Documentation for mod developers

## Phase 6: Testing & Release

- [ ] Unit tests for loader internals
- [ ] Integration test: load a simple mod into Classic 0.0.13a_03
- [ ] Test multi-mod coexistence
- [ ] Test mixin application on real game classes
- [ ] Initial release (alpha) — Classic 0.0.13a_03 only

## Phase 7: Expand to More Versions

- [ ] Classic 0.0.14a → 0.30
- [ ] Indev versions
- [ ] Infdev versions
- [ ] Alpha versions
- [ ] Beta versions
- [ ] Release 1.0 → 1.6.x

## Backlog

- [ ] GUI mod list (in-game mod manager)
- [ ] Mod update checking
- [ ] Crash report integration
- [ ] Performance profiling hooks
- [ ] Cross-version mod compatibility layer (stretch goal)
