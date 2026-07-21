# RetroLoader — Changelog

## [0.0.1] - 2026-07-21

### Added
- Project documentation (README, PROJECT, TODO, CHANGELOG, LESSONS, METRICS)
- Defined project scope: modern modloader for pre-1.7 Minecraft versions
- Chose technical stack: Java, custom bytecode patching (from scratch), intermediary mappings, `@SubscribeEvent` event system
- Documented Classic 0.0.13a_03 game structure and level format
- Designed architecture: Java agent → loader → API → mod layers
- Designed event system: NeoForge-style listening, Fabric-style type safety, auto-discovery
- Designed mod metadata format (draft)
- Created development roadmap (7 phases)
- Apache 2.0 LICENSE and .gitignore
- Gradle project setup (wrapper 8.14.3, root build.gradle, gradle.properties with Zulu 21)
- `retroloader-runtime` module: Java agent entry point (`RetroAgent`)
  - `premain()` and `agentmain()` agent lifecycle
  - Delegating `RetroClassTransformer` for class loading interception
  - Transformer registration API (`registerTransformer`)
  - Jar manifest with `Premain-Class`, `Can-Redefine-Classes`, `Can-Retransform-Classes`
- Modular project layout (9 modules + versions/ + wiki/)
