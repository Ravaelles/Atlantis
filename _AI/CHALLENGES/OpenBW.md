# OpenBW: headless-engine traps

Read before touching `scripts/run-openbw-e2e.sh`, `build-bot-jar.sh`'s
library list, or anything that starts a game. One insight per bullet, no
narrative - every line below cost a research cycle (CONVENTIONS §11).

Status: attach solved and bounded (§17: 120 s wall, 20 game minutes);
placement builds since the 2026-10-11 library patch. The one property
this engine never gives us is replay fidelity (verified upstream, §5).

## 1. Attach

- Registry exists only with `BWAPI_CONFIG_CONFIG__SHARED_MEMORY=ON`
  (resolved before any `bwapi.ini`); without it the client loops on
  "No server proc ID" against a healthy socket. Script sets it.
  Pinned by `OpenBWLauncherTest`.
- Clear stale state **before** hosting (`pkill -9 -x BWAPILauncher`,
  `rm -f /dev/shm/bwapi_shared_memory_* /tmp/bwapi_socket_*`): a dead
  host's PID is indistinguishable from a live one, and a stale segment
  makes the next launch a silent no-op (`localOnly`, no socket, no log).
- `BWAPILauncher` forks: compare `pgrep -x` against the registry PID,
  never trust `$!`. One command owns host+client+teardown (§15).
- `pkill -x`, never `pkill -f` (the shell SIGKILLs itself).
- One client only (5 s poll, then socket unlinked): a second bot cannot
  attach; scripted opponents need a UMS map, `enemy_count=0` crashes
  JBWAPI at `Game.init`.
- junixsocket: both stocked jars bundle 1.0.x (`setCreated`, dead on
  Java 9+); `build-bot-jar.sh` replaces the whole `org/newsclub/` tree
  with 2.10.1 plus native alias, descriptors and `Multi-Release: true`
  (all asserted). A jar that cannot attach is a build failure - silent
  as "bot never joins".
- Layer split that located the packaging bug: 20-line socket program
  with clean classpath (CONNECTED OK) vs with `Atlantis.jar`
  (`UnsatisfiedLinkError`). Without it the log says "socket", the
  socket exists, and the hunt circles.
- Ours, not the harness: launcher killing its own host; ENV/build_orders
  written to the wrong directory (bot played with no production, silent).

## 2. Fields OpenBW fills differently (all five measured)

Same shape every time: StarCraft fills it, OpenBW does not fill it the
way BWAPI expects. Work around our side, never "fix the engine".

| Field | Reads on OpenBW | Working signal |
|---|---|---|
| `regionCount` | 0, always | terrain fallback, never `Game.hasPath` |
| `isOccupied` grid | only for currently visible tiles | unit list, not the grid |
| `buildUnit` | null, always | `isConstructing` + building-at-site |
| `isCompleted` | false at attach (workers!), true later | decline uncompleted units, wait it out |
| `isExplored` | per-player shared memory | explored 4/4 at issue; never a refusal cause |

- `regionCount = 0` ⇒ `hasPath` false for every pair. Answered from
  terrain everywhere (`MapTiles.hasPathBetween`); the library's own
  `canBuild` got the same fallback as a source patch, shipped as
  `lib/jbwapi-2.2.0-atlantis.jar` (source: `/sc-ai/JBWAPI-Atlantis`,
  rebuild in `build-bot-jar.sh`).
- `isBuildable(x,y,true)` says free on unwatched tiles whatever stands
  there; `Game.canBuildHere` (client-side Java!) checks the unit
  rectangle minus builder. They answer different questions - their
  disagreement is not a defect. Rule: builder may stand on the
  footprint, nobody else may.
- Status advances on `isConstructing`, finish on warp
  (`ProtossWarping` FINISHED + positional-miss adoption); never assign
  or order `!isCompleted()` units.
- Map guard: `MAP_ANALYSIS areas=.. chokes=..` or the model (not
  placement) is the suspect; empty exits 43/42, never silent.
  `src/bwem/BWMap` shadows the jar's (no `getChokePoints()` there).

## 3. Placement doctrine (resolved, do not re-open)

The search finds tiles; since the `hasPath` patch the command path
builds (`engineAccepted=true`, Pylons/Gateways/Assimilators in rosters).
Remaining builder-side stalls are queue/builder issues (NEXT #53 and
friends), never placement until a bounded run shows `Can't find place`
again.

## 4. Replays desync from the live game (verified upstream 2026-10-15)

- OpenBW replays re-simulate from recorded actions; a dropped action
  desyncs the replay early while the live game played correctly.
  Upstream issue [OpenBW/openbw#27](https://github.com/OpenBW/openbw/issues/27)
  ("openbw creates desynced replay"): bmnielsen, Mar 2019 ("everything
  looks correct in the logs... replays desync shortly into the game"),
  seconded Aug 2021 (Steamhammer-vs-itself on Linux). Open,
  unassigned, no fix - nothing to pull.
- Doctrine: live log, roster and trace are truth; replays are
  position-viewing only, never unit-existence truth. Recorded locally
  (NEXT #78): 28/28 exists+completed raw dump matches the roster of a
  game whose replay desyncs; pairing mixup excluded.
- Consequences: E2E gates read live truth (valid); viewer fuzzy-match
  residual misses on `--save-replay` pairs are expected (direct
  `unit_ids` bypass matching); engine surgery is out of scope (NEXT
  #79 cheap-first plan stands if ever revisited).

## 5. Dead ends (measured, do not reopen without new evidence)

Invisibility, unbuildable-terrain-area, fog blocking builds, fixing
regions from application code, patching the C++ harness (refusal is
inside the JVM), swapping the jar alone, reinstalling OpenBW, the
manual-game vision-disc story (invented, withdrawn), re-proving
placement over a replay mismatch.

## 6. Stale pointers (history, not tasks)

`_AI/STATUS.md` deleted 2026-10-14 (§20); `__PLAN-DECISIONS.md` deleted
2026-10-18 (all boxes landed, absorption continues as NEXT item 2);
`_AI/MASTER-PLAN.md` folded into `NEXT.md` phases (same change); NEXT
#47/#48/#49/#53 closed, narratives in `_AI/CASE-STUDIES.md`;
`JBWAPI-Rav.jar` removed for stock 2.2.0; run artefacts live in
`/sc-ai/BOTS/AtlantisOpenBW/E2E_LOGS/` as `E2E_LOG` (game),
`E2E_DEBUG` (diagnostics), `E2E_DECISIONS` (audit) plus the stable
`E2E_LAST_*` triple; replays in `/sc-ai/REPLAYS/`.
