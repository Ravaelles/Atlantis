# STATUS — started but unfinished work

> Function (owner's ruling, 2026-10-14): this file holds work that is
> **started but not finished**, so it survives a context change. It is not
> a session log and not a second backlog:
> - open work lives in `_AI/NEXT.md` (stable numbers, `Closes #<n>`),
>   owner decisions in `_AI/DECISIONS.md`;
> - done work lives in git history; long post-mortems in
>   `_AI/CASE-STUDIES.md`.
>
> A line leaves this file when its work is done or when it is moved into
> `NEXT.md` as an actionable item. History before 2026-10-14 (milestones
> M0-M5, closed items #28-#80, per-session narratives) was removed in one
> pass - the git log is the archive.

## Scout repair (implemented, game verification open-ended)

- Roam timeout-advance, terminal fallback+release and per-game reset
  landed in `c14b767d` (`RoamAroundEnemyBase`, `ScoutCommander`,
  `ScoutState.reset()` wired in `OnGameStarted`; the `x>=3200` guard
  stays out). Tests: `ScoutRoamStuckTest` 4/4, `ScoutReleaseTest` 3/3;
  suite 494/494, ArchUnit store unchanged.
- Game verification is open-ended: no scout-hang game has been pinned to
  date (the repair was analytical - fall-through hole, same
  assigner+priority shape as PurpleWave `TacticWorkerScout`), so there is
  no failing gate to close. If a game ever shows a scout standing idle
  with no manager claiming it, read its `managerLogs()` first
  (CONVENTIONS §18) and file a `NEXT.md` item with that log.
