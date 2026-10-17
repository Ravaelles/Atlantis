# CHALLENGE: a scripted opponent on the OpenBW harness

Owner's ask (2026-10-08): an opponent - `StationaryEnemyCpu` - that is Protoss, sits
in its base with 20 Hydralisks, and attacks only when our units come within 30
tiles, otherwise returning home. Purpose: a *passive* enemy so our own economy,
production and placement can be exercised without a rush deciding the game.

This file records what the harness can and cannot do, measured, so the next
attempt does not repeat the search.

## The blocker: one client, and the enemy is always the engine's AI

- **The harness accepts exactly ONE BWAPI client.**
  `BWAPI/Source/BWAPI/Server.cpp`, `checkForConnections()`: it polls the listening
  socket for 5 s, accepts a single connection, sets `connected = true`, unlinks the
  socket and never listens again. There is no second-client path. So
  `StationaryEnemyCpu` **cannot be a second Java process** attaching alongside the
  bot.
- **The enemy is spawned by the engine's auto-menu, by race only.**
  `BWAPI/Source/BWAPI/AutoMenuManager.cpp`: `auto_menu.enemy_race` (and
  `enemy_race_1..7`, `enemy_count`, up to 7 players) decides the enemy's race; the
  engine's own AI then plays it. There is no hook for a custom enemy module on the
  BWAPILauncher path.
- **OpenBW ships no melee AI scripts.** The classic "custom enemy via a UMS
  trigger" is available (`game_type=USE_MAP_SETTINGS` is already supported by our
  script), but it needs a map that *contains* the trigger and the units - we have
  no such map, and authoring one is a map-editor task, not a code task.
- **Scripted starting units exist, but only in the C++ test framework** - not on
  the launcher path we use. `test/BWTest.{h,cpp}` has
  `myInitialUnits`/`opponentInitialUnits` and `fork()`s the opponent as a linked
  C++ `AIModule`; that is the `tests-steamhammer` binary, which plays two **C++**
  bots and cannot host our Java jar (see `DOCS/HOW-TESTS-WORK.md` §4).

## What is therefore possible, in order of cost

1. **Use the engine AI as a passive opponent (cheapest, no harness change).**
   `auto_menu.enemy_race=Protoss` with a normal melee map gives an opponent that
   builds a base and grows - it is *not* stationary, but our economy runs for as
   long as we avoid it. Enough for "does the opening work, does the bot expand",
   not for "the bot is undisturbed".
2. **`enemy_count=0`** - ~~the cleanest "undisturbed economy" run~~ **DOES NOT
   WORK.** Measured 2026-10-08: the game hosts (every enemy slot closed, confirmed
   in `AutoMenuManager.cpp`), the client connects (`Connection successful`), and
   then **JBWAPI crashes**:

   ```
   Exception in thread "JBWAPI Client" java.lang.ArrayIndexOutOfBoundsException:
       Index -1 out of bounds for length 2
       at bwapi.Game.init(Game.java:202)
   ```

   The binding cannot initialise a game with no opponent - it looks up a player
   index that does not exist. So "play alone" is not available through this
   client either.
3. **A UMS map with the stationary units and their trigger** (medium cost). The
   owner's design, literally: 20 Hydralisks at the enemy base, attack when our
   units come within 30 tiles. Needs a map authored once (or a trigger tool); our
   side needs no code beyond hosting a UMS map, which the runner already supports.
4. **Teach the harness to host a second client** (highest cost, touches the
   shared platform). `Server.cpp` would need to accept a second connection and
   the launcher a "second AI" concept. CONVENTIONS treats `StardustDevEnvironment/`
   as shared: this is a decision for the owner, not a quiet change.

## Decision needed from the owner (updated 2026-10-17)

**Measured outcomes so far:**

- option 1 (engine AI as opponent) **does not spawn on the launcher
  path either** - 6+ consecutive games across two maps (TauCross,
  Python) and two enemy races show `enemy: ` empty, 0/0 kills, scouts
  finding nothing in 18 min. The auto-menu plumbing is CORRECT
  (env names match `LoadConfigString("auto_menu",...)`, defaults sane),
  so the failure is deeper: OpenBW spawns starting units only for
  `controller_occupied` / `controller_computer_game` slots
  (`bwgame.h` starting-units loop), and the launcher flow never gets
  the enemy slot there. Needs C++ host debugging (shared platform).
- **No spawn API exists anywhere**: checked JBWAPI `Game.java`,
  OpenBW `bwgame.h` (only internal `create_unit`), the bot tree.
  "Creating" enemy units from Java is impossible by construction.
- option 3 (UMS map with pre-placed units) is now the ONLY working
  path that does not touch the shared platform - but no map editor
  exists on this machine, and hand-authoring CHK/MPQ is its own
  project. No `.scm`/editor found (checked Wine tree, repo, disk).
- option 4 (second client) unchanged: impossible, one-client harness.
- What "previously" means could not be established: no spawn
  mechanism, UMS map, or editor exists anywhere reachable. If the
  owner remembers Wine games with grown Zerg bases, those came from
  real opponents, not from spawning - and Wine-vs-real-opponent
  remains the only fighting proof available TODAY.
- option 3 (a UMS map with the stationary Hydralisks and their trigger) is the
  design as stated, and needs a map authored once - **no code from us** beyond
  hosting UMS, which the runner already supports (`game_type=USE_MAP_SETTINGS`);
- option 4 (teach the harness to host a second client) is a change to the shared
  platform and needs the owner's agreement.
- option 5 (NEW: debug why the launcher flow leaves the enemy slot
  unoccupied): the single remaining unknown. Everything up to the slot
  setup checks out; the answer lives in C++ host debugging, not in our
  tree. If it turns out small (slot flag, branch order), enemy AI games
  come back for free - no map, no editor, no platform change.

So the practical shape of "StationaryEnemyCpu" here is **a map, not a bot**: a
UMS scenario with the base and units placed and triggers as designed.
That is also the cheapest thing that matches the intent exactly.

Until the owner picks, the Java side stays unbuilt **on purpose** - a
`StationaryEnemyCpu` class in our repo would be dead code on this harness, and
dead code that looks like a working feature is worse than an absent one.
