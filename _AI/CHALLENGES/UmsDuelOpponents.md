# Who plays the enemy in UMS duels (OpenBW vs Wine)

Setup first, enemy second - the two platforms host the same map but a
different opponent. Verified 2026-10-21 on `ZealDrag_v_LingsUpg.scm`
(12 ours preplaced: 8 Dragoons + 3 Zealots + Observer; 32 upgraded
Lings; 6 triggers = Melee.trg only, nothing map-authored).

- Slots (both): p0 open (the bot), p1-3 computer. Atlantis hosts with
  ENEMY_COUNT=7 so the auto-menu closes nothing (`run-openbw-ums.sh`,
  `build-openbw.sh --smoke`).
- OpenBW enemy: NOBODY. There is no AIScript interpreter (other-model
  backlog A1.2b), and the bundled module for non-test maps is
  `DefaultTestModule` (draws text + `show me the money` / `operation
  cwal`, issues zero orders - `TestAIModule/Source/Dll.cpp`). What
  remains is engine default unit AI: sit until acquisition range, then
  every unit piles the nearest target. No waves, no retreat, no focus.
- Wine enemy: the map's own AI scripts (whatever the author set), or
  idle units if it sets none.
- Measured consequence: OpenBW opening wipes 4 ours for 0 ling kills
  in ~300 frames (UNIT_DIED twin), then both sides sit 5500+ frames -
  the map has bases so the baseless hunt never fires - and the bot
  force-leaves into a DEFEAT twin. Wine IDE on defaults: 32-8
  VICTORY in 36 game-sec. Same bot, same map, opposite games.
  Easy variant `ZealDrag_v_Lings2_easy.scm` (verified by smoke:
  same 12 ours, 26 lings instead of 32): Wine 3/5, same bimodal
  shape - fast full-wipe wins (20-43 s) or 202 s grinds with
  partial clears. Losers stay FIGHT throughout (FIGHT 127-156,
  FLEE 0-29 in the twin); the evaluator never refuses the open
  blob, and FLEE is banned baseless anyway.
- Do NOT compare W/L across platforms. OpenBW duels tune
  anti-blob-surround, Wine tunes anti-script. A Wine victory proves
  nothing about OpenBW and reverse.
