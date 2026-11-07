# CherryVis trace library

Dependency-free game-trace library: it accumulates per-unit and board events
in memory during a game and renders the CherryVis viewer JSON once, at game
end. No filesystem, engine, or bot imports - only `java.util`.

## Layout

- `Json` - minimal string escaper for the trace format.
- `StringTable` - runtime string-to-id legend (`custom_names.manager`).
- `FirstSeen` - first-seen registry plus engine-id pairs and the deferred
  (position-settled) record policy. Pure primitives in and out.
- `GameTrace` - the per-game accumulator and the single owner of the viewer
  JSON shape (`units_updates`, `units_tooltips`, `units_logs`,
  `board_updates`, `game_values`, `logs`, `units_first_seen`, `unit_ids`).
- `GameSummary` - plain game-end data object (players, map, frames, result).
- `TypesNames` - static engine type-id to name table (viewer data, no logic).
- `TraceSink` - where the rendered trace goes. The library never touches the
  filesystem itself; the bot adapter implements this with its own file
  utilities (and optional compression).

## Performance contract

Appending is a map put on change-only rows; rendering and all file I/O happen
exactly once, after the game. Per-frame cost is effectively zero, and every
public entry point on the bot side is gated behind a single `enabled` flag,
so a normal (untraced) game pays nothing.

## Use from a bot

1. Create one `GameTrace` per game.
2. At the boundary, translate game objects to primitives
   (`trace.logManager(frame, unitId, name)`, `trace.logUnitLine(...)`,
   `trace.logTooltip(...)`, `trace.logState(...)`, `sampleValue(...)`,
   `firstSeen().record(...)`).
3. At game end, call `trace.toTraceJson(withTooltips)` once and write it
   through a `TraceSink`, plus `GameSummary.toJson()`.
