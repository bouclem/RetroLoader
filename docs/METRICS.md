# RetroLoader — Project Metrics

Track project metrics over time. Compare to previous state, flag regressions.

## Current State (Jul 22, 2026)

| Metric | Value |
|---|---|
| Phase | 2 — Core Loader |
| Source files | 7 (RetroAgent, ModMetadata, ModContainer, ModMetadataParser, ModDiscovery, VersionDownloader, VersionDecompiler) |
| Documentation files | 6 |
| Other files | 6 (LICENSE, .gitignore, settings.gradle, build.gradle, gradle.properties, retroloader-core/build.gradle) |
| Planned modules | 9 + versions/ |
| Implemented modules | 2 (retroloader-runtime, retroloader-core) |
| Test files | 0 |
| Test coverage | N/A |
| Dependencies | 2 (night-config:toml 3.8.1, vineflower 1.12.0) |
| Supported MC versions | 0 (1 in development) |
| Lines of code | ~450 |
| Lines of documentation | ~330 |

## History

| Date | Phase | LOC | Docs | Tests | Notes |
|---|---|---|---|---|---|
| 2026-07-21 | 1 — Docs | 0 | 6 | 0 | Project started, documentation phase |
| 2026-07-21 | 2 — Core Loader | ~110 | 6 | 0 | Gradle setup, Java agent (RetroAgent), class loading interception |
| 2026-07-22 | 2 — Core Loader | ~450 | 6 | 0 | retroloader-core: mod discovery, metadata parsing, version download, decompile |
