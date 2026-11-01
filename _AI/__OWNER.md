# Owner requirements (agent-maintained, owner-decided)

What only you can do, per item, with context. Updated 2026-11-01.
(Everything else the agent does autonomously, including headless Wine runs.)

## Fixed already (no action)

- **IDE launches blocked by the wine-full gate** — the REFUSED banner
  fired on your own runs. Fixed 2026-11-01 (commit `f481ad8e`): the gate
  now applies only when `ATLANTIS_AGENT_RUN=1` is set (agent's own
  invocations). Your IDE/local runs are never gated. If you still see
  REFUSED anywhere, tell the agent - it means another gate exists.

## Decisions needed from you

### 1. Reviews due 2026-11-09 (confirm the date holds)
- **#156** (four reservation models): still belongs to #145/#147, no
  blind merge - agent recommends extending the park. Needs your call
  only if you want it lifted early.
- **#144–147** (parity runs): deletions stay owner-gated per WO order.
  The agent will bring per-gate game proofs; deletions themselves are
  your call, one gate at a time.
- **#160 / #185** (K-of-N tuner replicates): no data yet (duel noise
  dominates at K=3). Nothing to review until replicates exist -
  agent recommends keeping parked.

### 2. Doctrine choice — answered 2026-11-01, recorded here
- **#194** (which kinds change formation slots): your verdict is
  crescents on **all three chokes** (Main, Middle, Natural), rest
  unchanged. Agent's job now: sim-pin it, then a game proof.

### 3. Maps you need to author or pick (agent cannot)
- **Island/UMS map for #213** (Pylon #3-4 natural leak): the null-natural
  trigger cannot fire on any map in the library (all connected; Benzene
  verified connected 2026-11-01). Same class as #34: needs an
  owner-authored island/UMS map, or an explicit drop of the item.
- **#34 UMS opponent map**: unchanged, still owner-authored, tier stays
  owner-run.

### 4. Your live verifications (agent can't do these)
- **#150 PREPARING visual check**: open a game and confirm a PREPARING
  site reads as planned coverage, not a stuck builder (HP trail is
  wired; the twin is in place).
- **#96 Wine verification**: painter labels + one missions game.
- **#65 / #68 CherryVis**: open a real trace in the viewer (code side
  is done; what remains is your eyeballs + verdict).
- **#235 double-link fix verification (optional, recommended)**: the
  builder-handover fix for your Steamhammer loss is pinned sim-side.
  If you replay a similar game, watch for: builders no longer flap
  Manager↔Waiting every frame, first Gateway warps. If it still
  stalls, the agent needs a BuilderLog=1 game to see the issue path.

### 5. Vehicles only you can provide
- **#179 winnability**: all 16 retained wins are vs the native AI.
  Winnability vs real opponents needs real-bot games (your call
  how/when).
- **#198 full expression / #201 hunt proof / #203 live brake proof**:
  need a Stargate game, a lurker-with-gas game, a gas-rich midgame.
  The agent hunts these opportunistically in headless games (new
  attribution twins are in: `ZEALOT_BRAKE`, `V2_DETECTION_GOALS`).
  P_v_Lurkers.scx (your map pick): the agent runs it headless next -
  your uncommitted `src/main/Main.java` edit selecting it is yours,
  the agent will not touch that file.

## Standing rules the agent follows (so you know what to expect)
- Headless Wine only, via `scripts/atlantis.sh` (never `--visible`).
- One game at a time; never alongside the test suite.
- Shared `bwapi.ini` gets clobbered by every headless run (the scripts
  rewrite it per game). The agent backs it up before and restores it
  after each of its runs, so your IDE setup is never left pointing at
  the agent's map.
- Worktree may contain your uncommitted edits (e.g. Main.java map
  pick) - the agent commits only its own files and never touches yours.
