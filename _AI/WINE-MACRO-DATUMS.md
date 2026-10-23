# Wine macro datum log

Extracted from `_AI/NEXT.md` (owner-ordered, CONVENTIONS §26): per-game
datum chains live here, one line/record per game per chain, anchor =
previous line. `_AI/NEXT.md` keeps item verdicts, live tails and the
single deciding numbers only. Archive-only datums (EVENTS.jsonl + WINE_LOG
tail + BUILDINGS.json); game time = archived `Total time`.

## §0 — pending ground truth (absorbed handoff)

archive ground truth (Total time verdicts; fed counts by realpath-dedup script):
- 084039 (B49): 366 s VICTORY 1/0 Lost 0 — 3 feds (zeal f4980, drag f7260, pylon f7890)
- 084359 (B50): 420 s VICTORY 5/2 Lost 2 (Dragoon#172 f8642, Zealot#165 f8651) — 2 feds (pylon f6750, drag f7560)
- 091041 (B51): 367 s VICTORY 1/0 Lost 0 — 0 feds; STARTED 14 (8P/4G/1C/1A); CANCEL 1 (Pylon [97,114] f4324→[98,115] f4341, 17f gap); DEMOTE/BREAKDOWN/deaths 0; NexusQ 0; warps 13; P1 f964 [92,113] bottom; Cyber STARTED f3961 warp f4204; Assim f4866; STUCK 218x/23; GW PREP only oid29/47 (GW#2/#3 instant)
- 091345 (FOREIGN, no B-number): 525 s VICTORY 6/7 Lost 7 — 1 fed (drag f10050); STARTED 16 (7P/6G/1C/1A/1N); CANCEL/DEMOTE 0; BREAKDOWN 9; deaths 7; NexusQ 51; warps 16
- 092241 (B52): 496 s VICTORY 6/8 Lost 8 — 2 feds (zeal f5130, drag f9930); STARTED 14 (7P/4G/1C/1A/1N); CANCEL 1 (Pylon [112,100] f10941); DEMOTE 2 (GW [90,108] f6474, Pylon f7674, both 90f); deaths 8; NexusQ 39; STUCK 306x/32
- global: 182 unique logs / 74 BOARD_FED rows (realpath-dedup)
- next: full B52 9-datum set + foreign-game datum + re-sweep recount lines (180/71 → +091345 +092241 = 182/74)


## §1 — item 1 tally chain (BOARD_FED verdicts, cumulative)

   no candidate yet (history in git: 10-16 zero per-gate rows, 10-17
   nine sustained-army rows, 10-18 one supply-pylon rescue in 214926 -
   never routine). Re-measured 2026-10-19 across the 3 fresh runs:
   0 BOARD_FED everywhere, 83 BUILD rows all legacy/demand-direct -
   the feeder still fires nothing; no candidate. 4th run same day
   (`105921` long, 16546f/11:34, exit 143 wall-cap): BOARD_FED 1
   (sustained-dragoon 5:16, pumps already training since 4:12, zero
   BOARD-authored DECISIONS) - rescue, not routine; no candidate. 5th
   run 2026-10-20 (Wine FE short): BOARD_FED 3x on 3 different
   demands - gateway 2:45, assimilator 4:53, supply-pylon 5:38 -
   every one built within 600 frames (ORDER_NOT_READY clears, 6th datum 2026-10-21 TauCross macro: single BOARD_FED sustained-dragoon 6:31 with pumps training through it and zero BOARD-authored DECISIONS - rescue again, not routine; no candidate),
   CONSTRUCTION_REQUESTED +13f, BUILDING_STARTED +113f); pumps
   silent on all three. First routine-shaped evidence: nominates
   the legacy demand producers behind those rows for per-gate
   deletion (proof game after the cut still required). 7th datum 2026-10-21 Wine macro: BOARD_FED x2 (sustained-dragoon 5:18 with 29 DRAGOON_TRAINs in +/-600f, sustained-zealot 12:57 with 7 TRAIN_ATTEMPTs + 1 train in +/-1500f) - pumps present both times (attempting counts as present: a blocked pump is still rescue, not routine); still no candidate. 8th datum 2026-10-21 second Wine macro: BOARD_FED x1 (sustained-dragoon 6:05, 3 DRAGOON_TRAINs + 4 TRAIN_ATTEMPTs in +/-1500f - pumps present, rescue again). Deletions
   wait for routine feeder coverage, never batch rewrites;
   board-invisible production (Forge, cannons) is capability,
   not duplication. 2026-10-21 tooling: `scripts/attribute-board-fed.py`
   automates the verdict (army: TRAIN rows in +/-1500f; buildings:
   REQUESTED+STARTED+ORDER_NOT_READY in +/-600f - ORDER_NOT_READY is
   the building analogue of a blocked TRAIN_ATTEMPT). Sweep over all
   11 retained TauCross macros: 21 rescue, 0 routine - still no
   candidate, now mechanically instead of by hand. 2026-10-21 re-sweep
   with the 2 newest flagged runs in (13 macros): 24 rescue, 0 routine -
   still no candidate. 2026-10-22 re-sweep (retained archive holds only
   today's macro: 2 BOARD_FED, both rescue - pumps present 8 and 14):
   26 rescue, 0 routine - still no candidate. 2026-10-22 tooling fix:
   `attribute-board-fed.py` with several files silently read only the
   first (`args[0]`) - now iterates all files with a run prefix; re-sweep
   over all 52 retained logs: 2 BOARD_FED, both rescue (pumps 8 and 14) -
   tally 26 rescue, 0 routine, still no candidate.

 2026-10-23 Wine TauCross macro 203305 (VICTORY 1/0, 374 s, supply-cap
 exit at 53): BOARD_FED 2x, both rescue (supply-pylon 4:38, pumps
 present 1; sustained-dragoon 5:26, pumps present 7) - tally 28
 rescue, 0 routine, still no candidate. Dragoon demand-side rule
 live proof #2 (NEXT #45): Core warped 2:48, DRAGOON_TRAIN from 4:24,
 3 Dragoons warped.

 2026-10-23 long Wine TauCross macro 212818 (18.5 game-min, Defeat
 54/158, lurker contain late): BOARD_FED 3x, all rescue
 (sustained-zealot, sustained-dragoon, supply-pylon - pumps present
 53/52/1) - tally 31 rescue, 0 routine, still no candidate.

 2026-10-23 Wine TauCross macro 224120 (VICTORY 4/0, 419 s, supply-cap
 exit at 51): BOARD_FED 1x sustained-dragoon (pumps present 20) -
 rescue; tally 32 rescue, 0 routine, still no candidate.

 2026-10-23 Wine TauCross macro 225319 (VICTORY 3/0, 419 s, supply-cap
 exit at 51): BOARD_FED 1x sustained-dragoon (pumps present 29) -
 rescue; tally 33 rescue, 0 routine, still no candidate.

 2026-10-23 Wine TauCross macro 231335 (VICTORY 1/1, 370 s, supply-cap
 exit at 50): BOARD_FED 1x sustained-zealot (pumps present 54) -
 rescue; tally 34 rescue, 0 routine, still no candidate.

 2026-10-23 Wine TauCross macro 235430 (VICTORY 1/0, 380 s, supply-cap
 exit at 51): BOARD_FED 2x (sustained-zealot f5040 pumps 7,
 sustained-dragoon f7830 pumps 14) - both rescue; tally 36 rescue,
 0 routine, still no candidate.

 2026-10-23 Wine TauCross macro 001819 (VICTORY 1/0, 380 s, supply-cap
 exit at 51): BOARD_FED 2x (sustained-zealot f4950 pumps 8,
 sustained-dragoon f7860 pumps 9) - both rescue; tally 38 rescue,
 0 routine, still no candidate.
 2026-10-23 Wine TauCross DRY_RUN macro 002228 (VICTORY 9/1, 432 s,
 V2 planning alongside, legacy playing): BOARD_FED 2x
 (sustained-zealot f5550 pumps 9, sustained-dragoon f7680 pumps 8) -
 both rescue; tally 40 rescue, 0 routine, still no candidate.

 2026-10-26 Wine TauCross macro 003012 (VICTORY 4/0, 427 s, supply-cap
 exit at 51): BOARD_FED 1x sustained-dragoon f7530 (pumps present 29) -
 rescue; tally 41 rescue, 0 routine, still no candidate.
 Tooling: multi-file sweep deduped by realpath (the `latest` symlink double-counted one run before).

 2026-10-26 Wine TauCross macro 003419 (VICTORY 1/1, 384s game, 1 own death): BOARD_FED 3x (sustained-zealot f5010 pumps 7,
 supply-pylon f6750 pumps 1, sustained-dragoon f7530 pumps 8) - all
 rescue; tally 44 rescue, 0 routine, still no candidate.

 2026-10-26 Wine TauCross macro 045503 (VICTORY 1/0, 372s game, zero own deaths): BOARD_FED 1x
 sustained-dragoon f7470 (pumps present 10) - rescue; tally 45 rescue,
 0 routine, still no candidate.

 2026-10-26 Wine TauCross macro 045940 (VICTORY 1/0, 375s game, zero own deaths): BOARD_FED 1x
 sustained-dragoon f7230 (pumps present 11) - rescue; tally 46 rescue,
 0 routine, still no candidate.

 2026-10-26 Wine TauCross macro 050409 (VICTORY 1/0, 388s game, zero own deaths): BOARD_FED 3x
 (sustained-zealot f4980 pumps 8, sustained-dragoon f7350 pumps 8, supply-pylon f8040 pumps 3) - all
 rescue; tally 49 rescue, 0 routine, still no candidate.

 2026-10-26 Wine TauCross macro 051600 (VICTORY 1/1, 394s game, 1 own death): BOARD_FED 2x
 (supply-pylon f6690 pumps 1, sustained-dragoon f8580 pumps 5) - both rescue; tally 51 rescue,
 0 routine, still no candidate.

 2026-10-26 Wine TauCross macro 055715 (VICTORY 1/0, 370s game, zero own deaths): BOARD_FED 2x
 (supply-pylon f6630 pumps 1, sustained-dragoon f7770 pumps 8) - both rescue; tally 53 rescue,
 0 routine, still no candidate.

 2026-10-26 Wine TauCross macro 060047 (VICTORY 7/1, 376s game, 1 own death): BOARD_FED 2x
 (supply-pylon f6660 pumps 1, sustained-dragoon f8580 pumps 5) - both rescue; tally 55 rescue,
 0 routine, still no candidate.

 2026-10-26 Wine TauCross macro 060458 (VICTORY 0/0, 343s game, zero own deaths): BOARD_FED 0x -
 first macro with no fed rows at all (shortest game, 343s, pumps never starved); tally holds at
 55 rescue, 0 routine, still no candidate.

 2026-10-26 Wine TauCross macro 060844 (VICTORY 1/0, 394s game, zero own deaths): BOARD_FED 1x
 sustained-zealot f4830 (pumps present 7) - rescue; tally 56 rescue,
 0 routine, still no candidate.

 2026-10-26 Wine TauCross macro 061711 (VICTORY 0/0, 366s game, zero own deaths): BOARD_FED 2x
 (sustained-zealot f4920 pumps 8, sustained-dragoon f7290 pumps 18) - both rescue; tally 58 rescue,
 0 routine, still no candidate.

 2026-10-26 Wine TauCross macro 061958 (VICTORY 1/0, 379s game, zero own deaths): BOARD_FED 2x
 (sustained-zealot f5010 pumps 8, sustained-dragoon f7650 pumps 10) - both rescue; tally 60 rescue,
 0 routine, still no candidate.

 2026-10-26 Wine TauCross macro 062321: ABNORMAL EXIT, no verdict - JVM gone ~f8661 with no VICTORY/Defeat/ForceExit/tail (last ledger warp Dragoon f8661, last stream event 6:06); twin stream lost (32 startup rows retained, cause undetermined - no OOM in dmesg); contributes 0 BOARD_FED rows; tally holds at 60 rescue, 0 routine.

 2026-10-26 Wine TauCross macro 062612 (VICTORY 1/0, 350s game, zero own deaths): BOARD_FED 1x
 supply-pylon f6660 (pumps present 1) - rescue; tally 61 rescue,
 0 routine, still no candidate.

 2026-10-26 Wine TauCross macro 062958 (VICTORY 1/0, 364s game, zero own deaths): BOARD_FED 2x
 (sustained-zealot f4950 pumps 9, sustained-dragoon f8280 pumps 5) - both rescue; tally 63 rescue
 (062321's twin stream lost - contributes 0), 0 routine, still no candidate.

 2026-10-26 Wine TauCross macro 063315 (VICTORY 6/1, 361s game, 1 own death): BOARD_FED 1x
 sustained-zealot f4920 (pumps present 8) - rescue; tally 64 rescue,
 0 routine, still no candidate.

 2026-10-26 Wine TauCross macro 063610: ABNORMAL EXIT, no verdict - JVM gone ~f6963 with no VICTORY/Defeat/ForceExit/tail (last ledger warp Dragoon f6963, last stream event 6:02); twin stream lost (32 startup rows, same signature as 062321); contributes 0 BOARD_FED rows; tally holds at 64 rescue, 0 routine. Second abnormal exit - pattern, not one-off (see 062321 datum).

 2026-10-26 Wine TauCross macro 064036 (VICTORY 1/0, 371s game, zero own deaths): BOARD_FED 0x -
 second macro with no fed rows (pumps never starved again); tally holds at 64 rescue,
 0 routine, still no candidate.

 2026-10-26 Wine TauCross macro 064330 (VICTORY 1/0, 340s game, zero own deaths): BOARD_FED 1x
 supply-pylon f6660 (pumps present 3) - rescue; tally 65 rescue,
 0 routine, still no candidate (shortest macro on record - beats 060458's 343s).

 2026-10-26 Wine TauCross macro 064641 (VICTORY 1/0, 476s game, zero own deaths): BOARD_FED 2x
 (sustained-zealot f5010 pumps 7, sustained-dragoon f7740 pumps 24) - both rescue; tally 67 rescue,
 0 routine, still no candidate (longest recent macro, 476s game).

 2026-10-26 Wine TauCross macro 065027 (VICTORY 4/1, 379s game, 1 own death): BOARD_FED 2x
 (sustained-zealot f4950 pumps 5, sustained-dragoon f7800 pumps 76) - both rescue; tally 69 rescue,
 0 routine, still no candidate.

 2026-10-26 Wine TauCross macro 065812 (VICTORY 0/0, 365s game, zero own deaths): BOARD_FED 2x
 (supply-pylon f6720 pumps 1, sustained-dragoon f8070 pumps 8) - both rescue; tally 71 rescue,
 0 routine, still no candidate.

 2026-10-26 Wine TauCross macro 071321 (Defeat 6/22, 564s game, 22 own deaths): BOARD_FED 2x
 (sustained-zealot f5820 pumps 8, sustained-dragoon f12060 pumps 1) - both rescue; tally 73 rescue,
 0 routine, still no candidate (first defeat does not feed the board - both rows are rescues).

 2026-10-26 Wine TauCross macro 072350 (VICTORY 0/1, 377s game, zero own deaths): BOARD_FED 1x
 supply-pylon f4770 (pumps present 5) - rescue; tally 74 rescue,
 0 routine, still no candidate.

 2026-10-26 Wine TauCross macro 074823 (VICTORY 0/0, 371s game, zero own deaths): BOARD_FED 1x
 sustained-dragoon f7290 (pumps present 25) - rescue; tally 75 rescue,
 0 routine, still no candidate.

 2026-10-26 Wine TauCross macro 075608 (VICTORY 0/0, 366s game, zero own deaths): BOARD_FED 0x -
 third macro with no fed rows at all (pumps never starved); tally holds at
 75 rescue, 0 routine, still no candidate.

 2026-10-26 Wine TauCross macro 080452 (VICTORY 11/1, 446s game, 1 own death): BOARD_FED 2x
 (sustained-zealot f5760 pumps 21, sustained-dragoon f7950 pumps 17) - both rescue; tally 77 rescue,
 0 routine, still no candidate (first fight game in the chain - hydra brawl f9800-10150, Dragoon#166 lost f10145).

 2026-10-26 Wine TauCross macro 080801: ABNORMAL EXIT, no verdict - JVM gone ~f8550 (last ledger warp Pylon f8541 [116,21], last live-log event 5:53 Gateway PREPARING [108,4] = ledger warp f8526; twin stream wiped - 32-row artifact, 4th instance, Gateway tile [56,23]); unit suite ran overlapping this game and the exact predicted corruption is observed live: stream replaced by the artifact + 10 foreign fixture rows interleaved with 3 own rows in DECISIONS. Process rule going forward: never overlap suite and game runs. Contributes 0 BOARD_FED rows; tally holds at 77 rescue, 0 routine. Third abnormal exit (all three top-spawn [113,15]; healthy top-spawn games exist - noted, not claimed). Death cause still open - the overlap explains the wipe, not the JVM death.

 2026-10-26 Wine TauCross macro 081145 (VICTORY 1/0, 371s game, zero own deaths): BOARD_FED 1x
 sustained-dragoon f7440 (pumps present 10) - rescue; tally 78 rescue,
 0 routine, still no candidate (BO audit 4 off-plan, matches ledger).

 2026-10-26 Wine TauCross macro 081641 (VICTORY 0/0, 365s game, zero own deaths): BOARD_FED 1x
 supply-pylon f6720 (pumps present 1) - rescue; tally 79 rescue,
 0 routine, still no candidate.

 2026-10-26 Wine TauCross macro 081913 (VICTORY 19/7, 525s game, 7 own deaths): BOARD_FED 2x
 (sustained-zealot f6120 pumps 9, sustained-dragoon f9000 pumps 16) - both rescue; tally 81 rescue,
 0 routine, still no candidate (second fight game - early zergling rush killed 6 Probes f3866-4167, hydra brawl 7:57 Dragoon#177 lost).

 2026-10-26 Wine TauCross macro 082330 (VICTORY 7/3, 388s game, 3 own deaths): BOARD_FED 1x
 sustained-zealot f6360 (pumps present 9) - rescue; tally 82 rescue,
 0 routine, still no candidate (early zergling rush killed 3 Probes f3900-4100).

 2026-10-26 Wine TauCross macro 082738 (VICTORY 1/0, 374s game, zero own deaths): BOARD_FED 4x
 (sustained-zealot f5400 pumps 50, supply-pylon f6930 pumps 1, sustained-dragoon f7680 pumps 57, supply-pylon f8250 pumps 1) - all rescue; tally 86 rescue,
 0 routine, still no candidate (most feds in one game - pumps starved four times, all rescued).

 2026-10-26 Wine TauCross macro 082947 (VICTORY 6/5, 423s game, 5 own deaths): BOARD_FED 2x
 (sustained-zealot f4980 pumps 6, sustained-dragoon f9240 pumps 9) - both rescue; tally 88 rescue,
 0 routine, still no candidate (early rush killed 3 Probes + 1 scout Zealot f3900-4400).

 2026-10-26 Wine TauCross macro 083202 (VICTORY 1/0, 355s game, zero own deaths): BOARD_FED 1x
 sustained-dragoon f7530 (pumps present 10) - rescue; tally 89 rescue,
 0 routine, still no candidate.

 2026-10-26 Wine TauCross macro 083539 (VICTORY 1/0, 355s game, zero own deaths): BOARD_FED 1x
 sustained-zealot f5400 (pumps present 6) - rescue; tally 90 rescue,
 0 routine, still no candidate.

 2026-10-26 Wine TauCross macro 084039 (VICTORY 1/0, 366s game, zero own deaths): BOARD_FED 3x
 (sustained-zealot f4980 pumps 15, sustained-dragoon f7260 pumps 15, supply-pylon f7890 pumps 1) - all rescue; tally 93 rescue,
 0 routine, still no candidate.

 2026-10-26 Wine TauCross macro 084359 (VICTORY 5/2, 420s game, 2 own deaths): BOARD_FED 2x
 (supply-pylon f6750 pumps 1, sustained-dragoon f7560 pumps 5) - both rescue; tally 95 rescue,
 0 routine, still no candidate (took-too-long Gateway cancel + REQUEUE_VETO + Nexus#2 saga + Assimilator#2 demand).

 2026-10-26 Wine TauCross macro 091041 (VICTORY 1/0, 367s game, zero own deaths): BOARD_FED 0x -
 fourth macro with no fed rows at all (pumps never starved); tally holds at
 95 rescue, 0 routine, still no candidate.


 2026-10-26 Wine TauCross macro 092241 (VICTORY 6/8, 496s game, 8 own deaths): BOARD_FED 2x (sustained-zealot f5130 pumps 6, sustained-dragoon f9930 pumps 7) - both rescue; tally 97 rescue, 0 routine, still no candidate (29th/30th live demotes; Cyber sup14 earliest on record; Nexus#2 warped f10769 [51,113] per ledger, roster Nexus=2).
 2026-10-26 Wine TauCross macro 110209 (VICTORY 6/11, 613s game, 11 own deaths): BOARD_FED 1x (sustained-zealot f8250 pumps 8) - rescue; tally 98 rescue, 0 routine, still no candidate (CyberneticsC took-too-long cancel + 50 core-single-instance refusals, never warped; gasless Dragoon-less game; Nexus=3).
## §2 — item 1 re-sweep chain (retained-log recounts)

 2026-10-23 re-sweep over 64 unique retained logs (latest-symlink
 deduped): BOARD_FED still 2, both rescue - tally unchanged, no
 candidate; the 4 newest runs are UMS duels (no construction twins,
 no new #150/#151/#92/#84/#74 instances).

 2026-10-23 re-sweep over 97 unique retained logs: 9 BOARD_FED rows,
 all rescue, 0 routine - cumulative tally holds at 33 rescue / 0
 routine, still no candidate (the newest runs are UMS duels, no
 construction twins).

 2026-10-23 re-sweep over 110 unique retained logs: 10 BOARD_FED rows,
 all rescue, 0 routine - cumulative tally holds at 34 rescue / 0
 routine, still no candidate.

 2026-10-23 re-sweep over 116 unique retained logs: 12 BOARD_FED rows,
 all rescue, 0 routine - cumulative tally holds at 36 rescue / 0
 routine, still no candidate (newest runs are UMS duels + latch
 batches, no construction twins there).

 2026-10-23 re-sweep over 140 unique retained logs: 14 BOARD_FED rows,
 all rescue, 0 routine - cumulative tally holds at 38 rescue / 0
 routine, still no candidate.

 2026-10-26 re-sweep over 141 unique retained logs: 16 BOARD_FED rows,
 all rescue, 0 routine - cumulative tally holds at 40 rescue / 0
 routine, still no candidate.

 2026-10-26 re-sweep over 142 unique retained logs: 17 BOARD_FED rows,
 all rescue, 0 routine - cumulative tally holds at 41 rescue / 0
 routine, still no candidate.

 2026-10-26 re-sweep over 143 unique retained logs: 20 BOARD_FED rows,
 all rescue, 0 routine - cumulative tally holds at 44 rescue / 0
 routine, still no candidate.

 2026-10-26 re-sweep over 144 unique retained logs: 21 BOARD_FED rows,
 all rescue, 0 routine - cumulative tally holds at 45 rescue / 0
 routine, still no candidate.

 2026-10-26 re-sweep over 145 unique retained logs: 22 BOARD_FED rows,
 all rescue, 0 routine - cumulative tally holds at 46 rescue / 0
 routine, still no candidate.

 2026-10-26 re-sweep over 146 unique retained logs: 25 BOARD_FED rows,
 all rescue, 0 routine - cumulative tally holds at 49 rescue / 0
 routine, still no candidate.

 2026-10-26 re-sweep over 147 unique retained logs: 27 BOARD_FED rows,
 all rescue, 0 routine - cumulative tally holds at 51 rescue / 0
 routine, still no candidate.

 2026-10-26 re-sweep over 148 unique retained logs: 29 BOARD_FED rows,
 all rescue, 0 routine - cumulative tally holds at 53 rescue / 0
 routine, still no candidate.

 2026-10-26 re-sweep over 149 unique retained logs: 31 BOARD_FED rows,
 all rescue, 0 routine - cumulative tally holds at 55 rescue / 0
 routine, still no candidate.

 2026-10-26 re-sweep over 150 unique retained logs: 31 BOARD_FED rows,
 all rescue, 0 routine - cumulative tally holds at 55 rescue / 0
 routine, still no candidate.

 2026-10-26 re-sweep over 151 unique retained logs: 32 BOARD_FED rows,
 all rescue, 0 routine - cumulative tally holds at 56 rescue / 0
 routine, still no candidate.

 2026-10-26 re-sweep over 152 unique retained logs: 34 BOARD_FED rows,
 all rescue, 0 routine - cumulative tally holds at 58 rescue / 0
 routine, still no candidate.

 2026-10-26 re-sweep over 153 unique retained logs: 36 BOARD_FED rows,
 all rescue, 0 routine - cumulative tally holds at 60 rescue / 0
 routine, still no candidate.

 2026-10-26 re-sweep over 154 unique retained logs: 36 BOARD_FED rows,
 all rescue, 0 routine - cumulative tally holds at 60 rescue / 0
 routine, still no candidate (062321's twin stream lost in the abnormal exit - 0 rows from that game).

 2026-10-26 re-sweep over 155 unique retained logs: 37 BOARD_FED rows,
 all rescue, 0 routine - cumulative tally holds at 61 rescue / 0
 routine, still no candidate.

 2026-10-26 re-sweep over 156 unique retained logs: 39 BOARD_FED rows,
 all rescue, 0 routine - cumulative tally holds at 63 rescue / 0
 routine, still no candidate (062321 contributes 0 - twin stream lost).

 2026-10-26 re-sweep over 157 unique retained logs: 40 BOARD_FED rows,
 all rescue, 0 routine - cumulative tally holds at 64 rescue / 0
 routine, still no candidate.

 2026-10-26 re-sweep over 158 unique retained logs: 40 BOARD_FED rows,
 all rescue, 0 routine - cumulative tally holds at 64 rescue / 0
 routine, still no candidate (063610 contributes 0 - twin stream lost, second abnormal exit).

 2026-10-26 re-sweep over 159 unique retained logs: 40 BOARD_FED rows,
 all rescue, 0 routine - cumulative tally holds at 64 rescue / 0
 routine, still no candidate (second zero-fed macro after 060458).

 2026-10-26 re-sweep over 160 unique retained logs: 41 BOARD_FED rows,
 all rescue, 0 routine - cumulative tally holds at 65 rescue / 0
 routine, still no candidate.

 2026-10-26 re-sweep over 161 unique retained logs: 43 BOARD_FED rows,
 all rescue, 0 routine - cumulative tally holds at 67 rescue / 0
 routine, still no candidate.

 2026-10-26 re-sweep over 162 unique retained logs: 45 BOARD_FED rows,
 all rescue, 0 routine - cumulative tally holds at 69 rescue / 0
 routine, still no candidate.

 2026-10-26 re-sweep over 163 unique retained logs: 47 BOARD_FED rows,
 all rescue, 0 routine - cumulative tally holds at 71 rescue / 0
 routine, still no candidate.

 2026-10-26 re-sweep over 164 unique retained logs: 49 BOARD_FED rows,
 all rescue, 0 routine - cumulative tally holds at 73 rescue / 0
 routine, still no candidate (first defeat contributes 0 routine rows).

 2026-10-26 re-sweep over 165 unique retained logs: 50 BOARD_FED rows,
 all rescue, 0 routine - cumulative tally holds at 74 rescue / 0
 routine, still no candidate.

 2026-10-26 re-sweep over 166 unique retained logs: 51 BOARD_FED rows,
 all rescue, 0 routine - cumulative tally holds at 75 rescue / 0
 routine, still no candidate.

 2026-10-26 re-sweep over 167 unique retained logs: 51 BOARD_FED rows,
 all rescue, 0 routine - cumulative tally holds at 75 rescue / 0
 routine, still no candidate (third zero-fed macro).

 2026-10-26 re-sweep over 168 unique retained logs: 53 BOARD_FED rows,
 all rescue, 0 routine - cumulative tally holds at 77 rescue / 0
 routine, still no candidate.

 2026-10-26 re-sweep over 169 unique retained logs: 53 BOARD_FED rows,
 all rescue, 0 routine - cumulative tally holds at 77 rescue / 0
 routine, still no candidate (080801 contributes 0 - twin stream wiped in the abnormal exit).

 2026-10-26 re-sweep over 170 unique retained logs: 54 BOARD_FED rows,
 all rescue, 0 routine - cumulative tally holds at 78 rescue / 0
 routine, still no candidate.

 2026-10-26 re-sweep over 171 unique retained logs: 55 BOARD_FED rows,
 all rescue, 0 routine - cumulative tally holds at 79 rescue / 0
 routine, still no candidate.

 2026-10-26 re-sweep over 172 unique retained logs: 57 BOARD_FED rows,
 all rescue, 0 routine - cumulative tally holds at 81 rescue / 0
 routine, still no candidate.

 2026-10-26 re-sweep over 173 unique retained logs: 58 BOARD_FED rows,
 all rescue, 0 routine - cumulative tally holds at 82 rescue / 0
 routine, still no candidate.

 2026-10-26 re-sweep over 174 unique retained logs: 62 BOARD_FED rows,
 all rescue, 0 routine - cumulative tally holds at 86 rescue / 0
 routine, still no candidate.

 2026-10-26 re-sweep over 175 unique retained logs: 64 BOARD_FED rows,
 all rescue, 0 routine - cumulative tally holds at 88 rescue / 0
 routine, still no candidate.

 2026-10-26 re-sweep over 176 unique retained logs: 65 BOARD_FED rows,
 all rescue, 0 routine - cumulative tally holds at 89 rescue / 0
 routine, still no candidate.

 2026-10-26 re-sweep over 177 unique retained logs: 66 BOARD_FED rows,
 all rescue, 0 routine - cumulative tally holds at 90 rescue / 0
 routine, still no candidate.

 2026-10-26 re-sweep over 178 unique retained logs: 69 BOARD_FED rows,
 all rescue, 0 routine - cumulative tally holds at 93 rescue / 0
 routine, still no candidate.

 2026-10-26 re-sweep over 179 unique retained logs: 71 BOARD_FED rows,
 all rescue, 0 routine - cumulative tally holds at 95 rescue / 0
 routine, still no candidate.

 2026-10-26 re-sweep over 180 unique retained logs: 71 BOARD_FED rows,
 all rescue, 0 routine - cumulative tally holds at 95 rescue / 0
 routine, still no candidate (fourth zero-fed macro).


 2026-10-26 foreign macro 091345 (NOT our run, no B-number; VICTORY 6/7, 525s, 7 own deaths): BOARD_FED 1x (sustained-dragoon f10050 pumps 8) - rescue, counted in re-sweep rows only, never in tally; 16 STARTED (7P/6G/1C/1A/1N), 0 CANCEL, 0 DEMOTE, 9 BREAKDOWN (late-Gateway exit-cutoff cluster f11940-12510), deaths 7 (Zealot + 6 Probe), NexusQ 51, 16 warps incl. Nexus#2 f10618, GW#2 PREPARING twin f1684 (not instant).
 2026-10-26 re-sweep over 181 unique retained logs: 72 BOARD_FED rows, all rescue, 0 routine - tally holds at 95 rescue / 0 routine (091345 foreign +1 row in rows-total only).
 2026-10-26 re-sweep over 182 unique retained logs: 74 BOARD_FED rows, all rescue, 0 routine - tally 97 rescue / 0 routine (B52 +2; foreign row in rows-total only).
 2026-10-26 re-sweep over 183 unique retained logs: 75 BOARD_FED rows, all rescue, 0 routine - tally 98 rescue / 0 routine (B53 +1).
## §3 — #150 adopt/cancel ledger

 2026-10-23 Wine TauCross macro 203305 (VICTORY 1/0, 374 s, supply-cap exit): same quiet shape - 0 CANCELs, 0 ADOPTED, builder-rich; no overlap rows.

2026-10-23 long macro 212818 (18.5 game-min, Defeat 54/158): 0 ADOPTED; Assimilator null-buildPosition CANCELs x4 (f11905-12354, siteless-mint death, gas instance of the known mechanism); 3 Nexus warps (6:39/11:38/15:55, later overrun) - adoption/resite seams unfired again.

2026-10-23 macro 224120 (419 s): 0 ADOPTED; 1 CANCEL (Pylon refresh-cancel f8991, re-requested tile warped f9193 - cancel-and-replace fires live, both pylons warped); Nexus#2 STARTED f9354 [53,114], no CANCEL, unwarped at the supply-cap exit (final Nexus=1).

2026-10-23 macro 225319 (419 s): 0 ADOPTED; 1 CANCEL (Gateway refresh-cancel f2668 with buildable:true - builder #6 stood ON the site [14,51] PlayerGuard, footprint self-blocked; re-request warped f2691); Nexus#2 STARTED f8098 [16,14], warped (final Nexus=2).

2026-10-23 macro 231335 (370 s): 0 ADOPTED; 0 CANCELs; DEMOTE_STALLED_IN_PROGRESS console x2 (first live sighting, BUILDER_LOG-gated, untwinned - Gateway [18,47] and Pylon [13,58], both recovered: 12 STARTED, final Pylon=6/Gateway=4); Gateway [14,51] self-block resited to [16,52] with NO cancel row (refresh path without CANCEL twin - the 225319 class, third occurrence of the tile).

2026-10-23 macro 235430 (380 s): 0 ADOPTED; 0 CANCELs; 0 DEMOTE rows (twin live from this game on, nothing fired - pipeline healthy); Pylon [118,19] refresh at 3:45 with no cancel (third resite-without-cancel shape); Nexus#2 queued 5:43 READY but unwarped at the supply-cap exit (final Nexus=1, the 224120 shape).

2026-10-26 DRY_RUN macro 002228 (VICTORY 9/1, 432 s, V2 planning alongside): 0 ADOPTED; 0 CANCELs; DEMOTE_STALLED twin fired 2x live (FIRST live rows: CyberneticsC [13,54] f4571 stoodStill 90f, Gateway [17,47] f9079 stoodStill 90f - both recovered, re-armed and warped, no stuck freeze); Pylon#1 f947; Nexus#2 STARTED f8764 but unwarped at the supply-cap exit (final Nexus=1).

2026-10-26 macro 003419 (VICTORY 1/1, 384s, 1 own death): 0 ADOPTED; 0 CANCELs; DEMOTE_STALLED twin 2x (Pylon [118,16] f4589 + Gateway [116,23] f6015, stoodStill 90f; both re-armed and warped per ledger f4669/f6147 - fourth/fifth live demotes); Pylon#1 f957 [113,15]; Gateway [116,23] breakdown f5760 (travelling builder, stoodStill 75f) then demoted+re-armed; Nexus queued once (NOT_READY f8113) never started (final Nexus=1). Dragoon#174 died STANDING at (56,109) under contact: 98 consecutive stuck-no-step, 16 hits all moving=false, zero displacement for 90f+, 0 FROZEN rows, latch releasing throughout - the #182 freeze shape recurring live (pocket-vs-logic since decided by sim 2026-10-23: PocketVsOpenFieldKiteTest 2/2, geometry alone explains stand-and-trade; SHOT twin kept firing, so it died trading, not idle).

2026-10-26 macro 045503 (ended f8880, 370s game, zero own deaths): 0 ADOPTED; 1 CANCEL (Gateway siteless f4610, Null position - third siteless-mint game after 212818 (x4 Assimilator) and 003012 (x1 Gateway); terminal without origin, no REQUESTED row); DEMOTE_STALLED 1x (Gateway [17,41] f4789 stoodStill 90f, re-armed and warped f4948 per ledger - sixth live demote, recovered); Pylon#1 f952 [10,52]; Nexus queued once (NOT_READY f8113) never started (final Nexus=1).

2026-10-26 macro 045940 (VICTORY 1/0, 375s game, zero own deaths): 0 ADOPTED; 0 CANCELs; 0 DEMOTEs - pipeline quiet, nothing stalled; Pylon#1 f964 [92,113]; Nexus never queued (final Nexus=1).

2026-10-26 macro 050409 (VICTORY 1/0, 388s game, zero own deaths): 0 ADOPTED; 1 CANCEL (Gateway refresh f7981 [17,40] "position still not good after refresh", re-STARTED f7999 and warped f8288 per ledger - cancel-and-replace live on the same site); DEMOTE_STALLED twin 3x (Pylon [15,45] f4640 + Gateway [17,44] f5973 stoodStill 90f + Gateway [17,40] f8283 stoodStill 127f; all re-armed and warped per ledger f4648/f5978/f8288 - seventh/eighth/ninth live demotes, all recovered); Pylon#1 f952 [10,52]; Nexus queued once (NOT_READY f9230) never started (final Nexus=1).

2026-10-26 macro 051600 (VICTORY 1/1, 394s game, 1 own death): 0 ADOPTED; 0 CANCELs; DEMOTE_STALLED twin 1x (Gateway [6,58] f8166 stoodStill 90f, re-armed and warped f8189 per ledger - tenth live demote, recovered); Pylon#1 f952 [10,52]; Nexus queued once (NOT_READY f9372) never started (final Nexus=1). The tenth demote closes the "first unrecovered" follow-up: the console text suggested no re-warp, but the archive ledger shows the warp 23f after the demote at the same tile - all 10 twin demotes recovered.

2026-10-26 macro 055715 (VICTORY 1/0, 370s game, zero own deaths): 0 ADOPTED; 0 CANCELs; DEMOTE_STALLED twin 1x (Gateway [17,44] f6008 stoodStillFor=136f, re-armed and warped f6039 per ledger - eleventh live demote, recovered; the 136f is the 150f hold-after-attempt dominating the 90f stillness trip, not pass cadence: last BUILD_ATTEMPT f5858 by the same probe #161, demote f6008 = +150f exactly while 90f stillness already held since f5962. Same arithmetic in 050409 GW [17,40]: attempt f8133, demote f8283 = +150f, stillness 127f. Rule pinned x2 from archives: demote fires at max(90f still, 150f since attempt) and the twin reports stillness, not eligibility; standard 90f demotes are stale-attempt cases where stillness trips first); Pylon#1 f952 [10,52]; Nexus never queued (final Nexus=1). Same site's full chain: STARTED f5738, BREAKDOWN f5760 (travelling-gathering builder, stoodStill 62f), demote f6008, warp f6039 - the 003419 breakdown-to-demote-to-rearm class again.

2026-10-26 macro 060047 (VICTORY 7/1, 376s game, 1 own death): 0 ADOPTED; 0 CANCELs; DEMOTE_STALLED twin 1x (Gateway [83,110] f8611 stoodStill 90f, re-armed and warped f8616 per ledger - twelfth live demote, recovered); Pylon#1 f964 [92,113] (bottom spawn); Nexus never queued (final Nexus=1). Death archive-verified: Zealot#144 f8495, killed by Zergling#182 at (38,43) - first Zerg kill on record (7 kills this game; previous macros met no real fight). End-game stand, not a freeze: dragoons #170/#173 row 29+11 stuck-no-step f8840-8869 glued at (26-27,20), foe in range at 3.5 tiles, cooling, latch releasing, HIT f8840/f8869 - and 0 FROZEN rows, correctly (trading-exclusion: target in weapon range). Both survived to the supply-cap exit 100f later - the brawl-clump spectrum, not the #182 died-standing shape.

2026-10-26 macro 060458 (VICTORY 0/0, 343s game, zero own deaths): 0 ADOPTED; 0 CANCELs; 0 DEMOTEs; BREAKDOWN twin 2x Pylon, both unexplored sites ([94,101] f3180 explored 2/4, recovered and warped f3355; [112,100] f7590 explored 0/4, no construction ever STARTED there - attempt died scouting); Gateway [83,107] STARTED f8018 unwarped at the supply-cap exit (cut off, no cancel/demote - the Nexus#2 class); 11 of 12 STARTED warped; Pylon#1 f964 [92,113] (bottom spawn); Nexus never queued (final Nexus=1).

2026-10-26 macro 061711 (VICTORY 0/0, 366s game, zero own deaths): 0 ADOPTED; 0 CANCELs; 0 DEMOTEs; BREAKDOWN twin 1x Pylon [35,65] f8730 (STARTED f8725, game ended f8730 unwarped - stranded by exit, the 060458-Gateway class); CyberneticsC STARTED f3840 and warped f4198 (no cancel, no lock - the negative control for the B22 zombie-row lock: without a refresh-cancel there is no zombie row and no core-single-instance block); 12 of 13 STARTED warped; Pylon#1 f952 [10,52]; Nexus queued once (NOT_READY f7847) never started (final Nexus=1).

2026-10-26 macro 060844 (VICTORY 1/0, 394s game, zero own deaths): 0 ADOPTED; 8 CANCELs (7 refresh, all [117,12]-class: Pylon x6 f4393-f5336 + CyberneticsC f4393 [112,13]; 1 siteless Gateway f9017 "Null position" - fourth siteless-mint game after 212818/003012/045503); 0 DEMOTEs; BREAKDOWN x6 (Gateway [120,19] f5280 + [108,11] f5910 gathering-travelling; Pylon [123,23] f7770 x3 units same frame - first triple; Gateway [116,23] f8670 stoodStill 291f - longest stall on record, warped f8799 with no demote, dist gate held: builder never within 3.5); all 7 Gateway + 5 Pylon STARTED warped; CyberneticsC STARTED f4331 (cid 52, oid 9), refresh-cancelled f4393, NEVER re-warped; Pylon#1 f957 [113,15] (top spawn); Nexus#2 STARTED f6969 and warped [103,37] - FIRST 2-base game (final Nexus=2, queued from f5966, ORDER_WAITING Nexus-for-CyberneticsC funding contention on the way). THE CYBER LOCK: the cancelled oid 9 was never removed from the production queue - it lived on as a siteless zombie row, and it + the re-request oid 26 refused each other via core-single-instance every 300f (17 pairs f4408-f9208, both oids named in the same frames) - the exact mutual lock documented in BuildGates.java. Consequence terminal: no Core all game, no Dragoons (ledger: 0 CyberneticsC, 0 Dragoon, 11 Zealots). Gate-specific, not cancel-specific: the zombie-row leak is general (same family as the duplicate-reservation minting noted in RefreshConstructionPosition), but only CyberneticsC has a single-instance gate - 050409's Gateway refresh-cancel (oid 46 -> 47) re-requested cleanly with zero refusals after, because no gate blocks double Gateway rows. Duplicate-mint path pinned: OnlyOneAllowedOfType counts withPlanned (constructions/units), which no longer sees the cancelled bare row - so the re-request is minted alongside the zombie; the gate then sees both bare queue rows and locks. First live fires of TWO of the four stub-pinned guards (sweep-verified 2026-10-26 across all 153 retained logs: only this game rows either guard; core-single-instance 34x terminal, pylon-overstock 6x benign - Pylons warped anyway).

2026-10-26 macro 001819 (380 s): 0 ADOPTED; 1 CANCEL (Pylon refresh-cancel f5244 buildable:true, re-requested tile [113,8] warped f5371 ~135f later - cancel-and-replace live); 15 STARTED, Nexus=1 all game (Nexus#2 never queued); Pylon#1 f958. V2_GOAL_SOURCE twin silent all game - root-caused, not a twin bug: PRODUCTION_V2 defaults OFF and no Wine env sets DRY_RUN, so the engine never plans (twin wired and pinned, engine simply off - see #144 datum).

2026-10-26 macro 003012 (VICTORY 4/0, 427 s): 0 ADOPTED; 2 CANCELs (Pylon refresh f8316; Gateway siteless f8937 "Not powered by a Pylon", the known mint-death mechanism - second live instance after 212818 Assimilator x4); DEMOTE_STALLED 1x (Forge [83,124] f9371 stoodStill 90f, recovered - third live demote, second stalled type); BO audit only 3 mismatches; Nexus#2 STARTED f8940 unwarped at the cap (final Nexus=1); ORDER_WAITING Nexus-for-Forge f8937 (funding priority, part of the Nexus#2 delay).


2026-10-26 macro 092241 (VICTORY 6/8, 496s game, 8 own deaths): 0 ADOPTED; 1 CANCEL (Pylon refresh [112,100] f10941 never re-warped, 13 of 14 STARTED warped); DEMOTE_STALLED twin 2x ([90,108] Gateway f6474 90f warped f6479 - 5f re-arm ties B49's record; [79,121] Pylon f7674 90f warped f7733) - 29th/30th live demotes (30 total across retained logs).
2026-10-26 macro 110209 (VICTORY 6/11, 613s game, 11 own deaths): 0 ADOPTED; 2 CANCELs (CyberneticsC [98,111] f7405 took-too-long 116s - second took-too-long on record after B50's Gateway; Pylon resite [90,107] f8604 -> [79,114] warped - cancel-and-replace).
## §4 — #71 lottery (700 s+ run)

 2026-10-23 macro 203305: supply-cap exit at 374 s, Nexus#2 queued 6:11 but unwarped at the cap - too short and multi-base trajectory, not the lottery shape.

 2026-10-23 long macro 212818 (18.5 game-min, multi-base from 6:39, supply collapse 53->9 late): ORDER_SAVING 0, correctly silent (predicate needs 1 base + bank<550); lottery continues.

 2026-10-23 macro 224120 (419 s VICTORY, supply-cap exit at 51, Nexus#2 STARTED f9354 but unwarped at the cap): too short, multi-base trajectory - not the 1-base 700 s+ lottery shape.

 2026-10-23 macro 225319 (419 s VICTORY, supply-cap exit at 51, Nexus#2 warped 5:3x): multi-base again - not the lottery shape.

 2026-10-23 macro 231335 (370 s VICTORY, supply-cap exit at 50, Nexus=1 all game): right shape (1 base), 330 s short of 700 s - lottery continues.

 2026-10-23 macro 235430 (380 s VICTORY, supply-cap exit at 51, Nexus=1 all game): right shape (1 base), 320 s short of 700 s - lottery continues.

 2026-10-26 DRY_RUN macro 002228 (432 s VICTORY, supply-cap exit at 51, Nexus#2 STARTED f8764 but unwarped at the cap): multi-base trajectory - not the 1-base 700 s+ lottery shape.

 2026-10-26 macro 001819 (380 s VICTORY, supply-cap exit at 51, Nexus=1 all game, no Nexus queue at all): right shape (1 base), 320 s short of 700 s - lottery continues.

 2026-10-26 macro 003419 (ended f9150, 381s game, 1 base, Nexus queued once never started): right shape (1 base), far too short for the 700 s+ lottery - exit cause and verdict unlogged in the archive.

 2026-10-26 macro 045503 (ended f8880, 370s game, 1 base, Nexus queued once never started): right shape, still far too short of the 700 s+ lottery - exit cause and verdict unlogged in the archive.

 2026-10-26 macro 045940 (375s VICTORY 1/0, 1 base, Nexus never queued): right shape, still far too short of the 700 s+ lottery.

 2026-10-26 macro 050409 (388s VICTORY 1/0, 1 base, Nexus queued once never started): right shape, still far too short of the 700 s+ lottery.

 2026-10-26 macro 051600 (394s VICTORY 1/1, 1 base, Nexus queued once never started): right shape, still far too short of the 700 s+ lottery.

 2026-10-26 macro 055715 (370s VICTORY 1/0, 1 base, Nexus never queued): right shape, still far too short of the 700 s+ lottery.

 2026-10-26 macro 060047 (376s VICTORY 7/1, 1 base, Nexus never queued): right shape, still far too short of the 700 s+ lottery - but the first real fight (7 kills, Zerg enemy).

 2026-10-26 macro 060458 (343s VICTORY 0/0, 1 base, Nexus never queued): right shape, still far too short of the 700 s+ lottery - shortest macro on record.

 2026-10-26 macro 061711 (366s VICTORY 0/0, 1 base, Nexus queued once never started): right shape, still far too short of the 700 s+ lottery.

 2026-10-26 macro 061958 (379s VICTORY 1/0, 1 base, Nexus queued once never started): right shape, still far too short of the 700 s+ lottery.

 2026-10-26 macro 062321: no lottery info - abnormal JVM exit ~f8661 (~6:04), no verdict, twin stream lost; not a lottery data point. Follow-up 2026-10-26: second abnormal exit in B29 (062321 f8661 + 063610 f6963, identical signature: no verdict/tail, twin stream = 32 startup rows, ledger full to near-death) - NOT a one-off flake; pattern open, cause undetermined (no OOM, B22 completed at the same spawn, so not spawn-determined either).

 2026-10-26 macro 062612 (350s VICTORY 1/0, 1 base, Nexus queued once never started): right shape, still far too short of the 700 s+ lottery.

 2026-10-26 macro 062958 (364s VICTORY 1/0, 1 base, Nexus queued once never started): right shape, still far too short of the 700 s+ lottery.

 2026-10-26 macro 063610: no lottery info - second abnormal JVM exit (~f6963), no verdict, twin stream lost; not a lottery data point.

 2026-10-26 macro 080801: no lottery info - third abnormal JVM exit (~f8550), no verdict, twin stream wiped; not a lottery data point.

 2026-10-26 macro 081145 (371s VICTORY 1/0, 1 base, Nexus queued once NOT_READY f8056 never started, Nexus=1, no Forge): right shape, still far too short of the 700 s+ lottery.

 2026-10-26 macro 081641 (365s VICTORY 0/0, 1 base, Nexus never queued, Nexus=1, no Forge): right shape, still far too short of the 700 s+ lottery.

 2026-10-26 macro 081913 (525s VICTORY 19/7, Nexus#2 warp placed f10631 [51,113] per ledger, archived roster Nexus=2, Forge warped f11347 per ledger, roster Forge=1): multi-base trajectory - not the 1-base lottery shape; longest game since the 071321 defeat (525s vs 564s).

 2026-10-26 macro 082330 (388s VICTORY 7/3, 1 base, Nexus queued once NOT_READY f7980 never started, Nexus=1, no Forge): right shape, still far too short of the 700 s+ lottery.

 2026-10-26 macro 082738 (374s VICTORY 1/0, 1 base, Nexus queued once NOT_READY f8875 never started, Nexus=1, no Forge): right shape, still far too short of the 700 s+ lottery.

 2026-10-26 macro 082947 (423s VICTORY 6/5, 1 base, Nexus never queued, Nexus=1, no Forge): right shape, still far too short of the 700 s+ lottery.

 2026-10-26 macro 083202 (355s VICTORY 1/0, 1 base, Nexus never queued, Nexus=1, no Forge): right shape, still far too short of the 700 s+ lottery.

 2026-10-26 macro 083539 (355s VICTORY 1/0, Nexus#2 warp placed f8145 [14,13] per ledger, archived roster Nexus=2): multi-base - not the 1-base lottery shape; fifth completed 2-base game (B22/B32/B36/B43/B48).

 2026-10-26 macro 084039 (366s VICTORY 1/0, 1 base, Nexus never queued, Nexus=1, no Forge): right shape, still far too short of the 700 s+ lottery.

 2026-10-26 macro 084359 (420s VICTORY 5/2, Nexus#2 warp placed f7963 [14,13] per ledger, archived roster Nexus=2): multi-base - not the 1-base lottery shape; sixth completed 2-base game (B22/B32/B36/B43/B48/B50).

 2026-10-26 macro 064330 (340s VICTORY 1/0, 1 base, Nexus never queued): right shape, still far too short of the 700 s+ lottery - shortest macro on record (beats 060458's 343s).

2026-10-26 macro 065027 (379s VICTORY 4/1, 1 base, Nexus#2 STARTED f9794 and warp placed [51,113] per ledger - uncompleted at exit): right shape, still far too short of the 700 s+ lottery.

2026-10-26 macro 065812 (365s VICTORY 0/0, 1 base, Nexus never queued): right shape, still far too short of the 700 s+ lottery.

2026-10-26 macro 071321 (564s Defeat 6/22, 1 base + Nexus#2 warp placed f11495, archived roster Nexus=2): NOT a lottery game - first defeat (Zerg rush f3900 + hydra brawl f9300+, army wiped f9700-10000, 22 own deaths); longest game on record by far (564s vs 476s).

2026-10-26 macro 072350 (377s VICTORY 0/1, 2 bases - Nexus#2 warped f6511, archived roster Nexus=2): NOT the 1-base lottery shape; third completed 2-base game (B22 and B32 completed too, only B33 did not finish warping).

2026-10-26 macro 074823 (371s VICTORY 0/0, supply-cap exit at 53, 1 base, Nexus queued once NOT_READY f7847 never started, final Nexus=1): right shape, still far too short of the 700 s+ lottery.

2026-10-26 macro 075608 (366s VICTORY 0/0, supply-cap exit at 51, 1 base, Nexus never queued, final Nexus=1): right shape, still far too short of the 700 s+ lottery.

2026-10-26 macro 080452 (446s VICTORY 11/1, supply-cap exit at 51, Nexus#2 warp placed f9123 [51,113] IN_PROGRESS at exit, roster Nexus=1 - uncompleted, needs ~f12000): multi-base trajectory, not the 1-base 700 s+ lottery shape; longest game since the 071321 defeat.

 2026-10-26 macro 064641 (476s VICTORY 1/0, 2 bases, Nexus#2 warped f9444): multi-base trajectory now (like 060844) - not the 1-base 700 s+ lottery shape; longest recent macro.

 2026-10-26 macro 064036 (371s VICTORY 1/0, 1 base, Nexus never queued): right shape, still far too short of the 700 s+ lottery.

 2026-10-26 macro 063315 (361s VICTORY 6/1, 1 base, Nexus never queued): right shape, still far too short of the 700 s+ lottery - second real fight (6 kills, Zerg rush f4600+).

 2026-10-26 macro 060844 (394s VICTORY 1/0, 2 bases, Nexus#2 warped f6969): NOT the lottery shape anymore (multi-base) - and the first game that outgrew the 1-base lottery frame entirely.

 2026-10-26 macro 003012 (427 s VICTORY, supply-cap exit at 51, Nexus#2 STARTED f8940 but unwarped at the cap): multi-base trajectory - not the 1-base 700 s+ lottery shape.


 2026-10-26 macro 092241 (496s VICTORY 6/8, 2 bases - Nexus#2 warped f10769 [51,113] per ledger, archived roster Nexus=2): multi-base, not the lottery shape; second 2-base with a sub-500s clock.
 2026-10-26 macro 110209 (613s VICTORY 6/11, 3 bases - Nexus#2 warped f11948 [51,113] + Nexus#3 warped f12250 per ledger, archived roster Nexus=3): multi-base, not the lottery shape.
## §5 — #84 pipeline + cancel ledger

 2026-10-23 macro 203305: pipeline healthy (13 STARTED, 0 CANCELs); one stall-with-recovery (Assimilator builder 121f, GasWorker assigned after, warped) - known class, not a new instance. STUCK_WORKER twin rows 221x at ~30f cadence per worker are the cyclic diagnostic, not 221 stalls.

 2026-10-23 macro 224120 (VICTORY 4/0, BUILDER_LOG=1): pipeline healthy - Pylon#1 STARTED f964 (~0:40, BO At 8), 16 STARTED, 1 CANCEL (Pylon refresh-cancel f8991, re-requested tile warped f9193); BUILD_BREAKDOWN x2 on Pylon [112,114] (worker #20, dist 11.7, canBuild=false all variants, explored 1/4) then warped f6664 - stall-with-recovery, not a new instance. STUCK_WORKER 229x across 24 workers at ~30f cadence - the cyclic diagnostic, not stalls.

 2026-10-23 macro 225319 (VICTORY 3/0, BUILDER_LOG=1): pipeline healthy - Pylon#1 STARTED f952 (~0:40, BO At 8), 14 STARTED, 1 CANCEL (Gateway refresh-cancel, re-request warped); BUILD_BREAKDOWN x2, both self-recovered (Gateway [14,51] f2040: builder #6 PlayerGuard ON the site, dist 1.3, canBuild=true all - footprint self-block, then cancel+warp; Pylon [13,58] f4380: worker #139 on ReturnMinerals, dist 10.8, warped anyway) - not new instances. STUCK_WORKER 230x across 27 workers at ~30f cadence - the cyclic diagnostic, not stalls.

 2026-10-23 macro 231335 (VICTORY 1/1, BUILDER_LOG=1): pipeline healthy - Pylon#1 f952, 12 STARTED, 0 CANCELs; BREAKDOWN x2 self-recovered (Gateway [14,51] f2040: same builder-on-site PlayerGuard shape, third occurrence, deterministic opening tile; Pylon [24,44] f3300/3330: worker gathering at dist 20.3, unexplored canBuild=false, resited [15,45] warped f4138). STUCK_WORKER 218x across 24 workers at ~30f cadence - cyclic, not stalls.

 2026-10-23 macro 235430 (VICTORY 1/0, BUILDER_LOG=1): pipeline healthy - Pylon#1 f957, 13 STARTED, 0 CANCELs, 0 BREAKDOWNs (quietest opening yet - no builder-on-site stall this game); Nexus#2 queued 5:43 READY but unwarped at the cap (final Nexus=1). STUCK_WORKER 222x across 25 workers at ~30f cadence - cyclic, not stalls.

 2026-10-26 DRY_RUN macro 002228 (VICTORY 9/1, BUILDER_LOG=1, V2 planning): pipeline healthy - Pylon#1 f947, 16 STARTED, 0 CANCELs, BREAKDOWN x2 Pylon (f3390/f8100, travelling builders, self-recovered); the 2 demotes were CyberneticsC + Gateway (see #150 twin datum). Nexus#2 STARTED f8764 unwarped at the cap (final Nexus=1). STUCK_WORKER 230x across 23 workers at ~30f cadence - cyclic, not stalls.

 2026-10-26 macro 001819 (VICTORY 1/0, BUILDER_LOG=1): pipeline healthy - Pylon#1 f958, 15 STARTED, 1 CANCEL (Pylon refresh [117,12]->[113,8], both warped), 0 BREAKDOWNs; Nexus never queued (final Nexus=1). STUCK_WORKER 220x across 24 workers at ~30f cadence - cyclic, not stalls.

 2026-10-26 macro 003419 (BUILDER_LOG=1): pipeline healthy - Pylon#1 f957 [113,15], 13 STARTED, 0 CANCELs, BREAKDOWN x1 Gateway [116,23] f5760 (travelling builder, stoodStill 75f) then demoted f6015 + re-armed; DEMOTE twin 2x, both recovered per ledger. STUCK_WORKER 228x across 23 workers at ~30f cadence - cyclic, not stalls.

 2026-10-26 macro 045503 (BUILDER_LOG=1): pipeline healthy - Pylon#1 f952 [10,52], 12 STARTED, 1 CANCEL (siteless Gateway, known mint-death shape), BREAKDOWN x0; DEMOTE twin 1x, recovered per ledger. STUCK_WORKER 224x across 23 workers at ~30f cadence - cyclic, not stalls.

 2026-10-26 macro 045940 (VICTORY 1/0, BUILDER_LOG=1): pipeline healthy - Pylon#1 f964 [92,113] (bottom spawn), 13 STARTED, 0 CANCELs, BREAKDOWN x0, DEMOTE x0 (quietest pipeline since 235430); all 13 STARTED warped per ledger. STUCK_WORKER 217x across 24 workers at ~30f cadence - cyclic, not stalls.

 2026-10-26 macro 050409 (VICTORY 1/0, BUILDER_LOG=1): pipeline healthy - Pylon#1 f952 [10,52], 15 STARTED, 1 CANCEL (Gateway refresh [17,40] f7981, cancel-and-replace, warped f8288), BREAKDOWN x0; DEMOTE twin 3x, all recovered per ledger (14 of 15 STARTED warped, 1 cancelled). STUCK_WORKER 231x across 24 workers at ~30f cadence - cyclic, not stalls.

 2026-10-26 macro 051600 (VICTORY 1/1, BUILDER_LOG=1): pipeline healthy - Pylon#1 f952 [10,52], 15 STARTED, 0 CANCELs, BREAKDOWN x1 Gateway [14,51] f2070 (builder-on-site self-block, stoodStill 156f, warped f2074 - fourth occurrence of the deterministic opening tile); DEMOTE twin 1x, recovered per ledger; all 15 STARTED warped. STUCK_WORKER 220x across 23 workers at ~30f cadence - cyclic, not stalls.

 2026-10-26 macro 055715 (VICTORY 1/0, BUILDER_LOG=1): pipeline healthy - Pylon#1 f952 [10,52], 13 STARTED, 0 CANCELs, BREAKDOWN x3 (Gateway [17,44] f5760 gathering-travelling, stoodStill 62f, then demoted f6008 + re-armed - the 003419 class; Pylon [35,65] f7560/f7740 unexplored site, builder gathering far, both self-recovered, warped f8452); DEMOTE twin 1x, recovered per ledger; all 13 STARTED warped. STUCK_WORKER 206x across 23 workers at ~30f cadence - cyclic, not stalls.

 2026-10-26 macro 060047 (VICTORY 7/1, BUILDER_LOG=1): pipeline healthy - Pylon#1 f964 [92,113] (bottom spawn), 15 STARTED, 0 CANCELs, BREAKDOWN x0; DEMOTE twin 1x (Gateway [83,110] f8611, warped f8616), recovered per ledger; all 15 STARTED warped. STUCK_WORKER 221x across 24 workers at ~30f cadence - cyclic, not stalls.

 2026-10-26 macro 060458 (VICTORY 0/0, BUILDER_LOG=1): pipeline healthy - Pylon#1 f964 [92,113] (bottom spawn), 12 STARTED, 0 CANCELs, BREAKDOWN x2 Pylon both unexplored ([94,101] f3180 recovered/warped f3355; [112,100] f7590 attempt-only, never STARTED); DEMOTE x0; 11 of 12 STARTED warped (Gateway [83,107] cut off by the supply-cap exit). STUCK_WORKER 216x across 24 workers at ~30f cadence - cyclic, not stalls.

 2026-10-26 macro 061711 (VICTORY 0/0, BUILDER_LOG=1): pipeline healthy - Pylon#1 f952 [10,52], 13 STARTED, 0 CANCELs, BREAKDOWN x1 Pylon [35,65] f8730 (STARTED f8725, stranded by the supply-cap exit 5f later); DEMOTE x0; 12 of 13 STARTED warped. STUCK_WORKER 220x across 25 workers at ~30f cadence - cyclic, not stalls.

 2026-10-26 macro 061958 (VICTORY 1/0, BUILDER_LOG=1): pipeline healthy - Pylon#1 f952 [10,52], 13 STARTED, 0 CANCELs, BREAKDOWN x1 Gateway [14,51] f2040 (builder-on-site, fifth occurrence, self-recovered); DEMOTE twin 1x Assimilator (first of its type), recovered per ledger; all 13 STARTED warped. STUCK_WORKER 231x across 24 workers at ~30f cadence - cyclic, not stalls.

 2026-10-26 macro 062612 (VICTORY 1/0, BUILDER_LOG=1): pipeline healthy - Pylon#1 f952 [10,52], 12 STARTED, 1 CANCEL (Gateway refresh [8,54] f2668, re-sited [4,55] warped f3759 - cancel-and-replace), BREAKDOWN x1 Gateway [14,51] f2040 (builder-on-site, sixth occurrence, self-recovered); DEMOTE twin 1x CyberneticsC, recovered per ledger; all 12 STARTED warped. STUCK_WORKER 220x across 24 workers at ~30f cadence - cyclic, not stalls.

 2026-10-26 macro 062958 (VICTORY 1/0, BUILDER_LOG=1): pipeline healthy - Pylon#1 f952 [10,52], 14 STARTED, 1 CANCEL (Gateway refresh [17,40] f7981, re-STARTED f7991 warped f8112 - cancel-and-replace, same frame+tile as 050409), BREAKDOWN x0; DEMOTE x0; all 14 STARTED warped. STUCK_WORKER 220x across 24 workers at ~30f cadence - cyclic, not stalls.

 2026-10-26 macro 063315 (VICTORY 6/1, BUILDER_LOG=1): pipeline healthy - Pylon#1 f976 [92,113] (bottom spawn), 12 STARTED, 0 CANCELs, BREAKDOWN x1 Pylon [90,107] f5550 (builder-on-site, post-demote linger); DEMOTE twin 2x (Gateway + Pylon), both recovered per ledger; all 12 STARTED warped. STUCK_WORKER 221x across 24 workers at ~30f cadence - cyclic, not stalls.

 2026-10-26 macro 064036 (VICTORY 1/0, BUILDER_LOG=1): pipeline healthy - Pylon#1 f964 [92,113] (bottom spawn), 12 STARTED, 0 CANCELs, BREAKDOWN x1 Gateway [84,108] f4860 (builder-on-site, 244f still, 4f before warp); DEMOTE twin 1x Gateway ([84,108] f4706, re-armed f4864 - slowest re-arm on record); all 12 STARTED warped. STUCK_WORKER 231x across 24 workers at ~30f cadence - cyclic, not stalls.

 2026-10-26 macro 064330 (VICTORY 1/0, BUILDER_LOG=1): pipeline healthy - Pylon#1 f964 [92,113] (bottom spawn), 13 STARTED, 1 CANCEL (Pylon refresh [98,111] f6716, null re-request, re-queued and warped f6808); BREAKDOWN x1 Pylon [90,107] f5190 (travelling builder, self-recovered); DEMOTE x0; 12 warps from 13 STARTED rows (cancelled row replaced). STUCK_WORKER 216x across 24 workers at ~30f cadence - cyclic, not stalls.

 2026-10-26 macro 065027 (VICTORY 4/1, BUILDER_LOG=1): pipeline busy - Pylon#1 f964 [92,113] (bottom spawn), 17 STARTED, 1 CANCEL (Pylon refresh [112,100] f8718 attempt-only, re-queued and warped [112,114] f8850); BREAKDOWN x5 Pylon [112,100] (f7800 x3 + f7830 x2, travelling, unexplored, recovered via re-request); DEMOTE x0; all 17 STARTED warped (cancel hit a never-started order only). STUCK_WORKER 223x across 24 workers at ~30f cadence - cyclic, not stalls.

 2026-10-26 macro 065812 (VICTORY 0/0, BUILDER_LOG=1): pipeline healthy - Pylon#1 f964 [92,113] (bottom spawn), 13 STARTED, 0 CANCELs, BREAKDOWN x1 Pylon [95,106] f7770 (tx/ty archived, travelling builder distToBuild 10.4, warped f7855); DEMOTE x0; all 13 STARTED warped (7 Pylon + 4 Gateway + CyberneticsC + Assimilator). STUCK_WORKER 215x across 24 workers at ~30f cadence - cyclic, not stalls.

 2026-10-26 macro 071321 (Defeat 6/22, 564s game, 22 own deaths, BUILDER_LOG=1): pipeline overwhelmed, not unhealthy - Pylon#1 f976 [92,113] (bottom spawn), 22 STARTED, 7 CANCELs (1 Pylon refresh attempt-only [112,100] + 6 Gateway refresh same-site [92,107]); BREAKDOWN x4 (CyberneticsC [98,111] f5130 post-demote linger + Gateway [92,107] x3 f11490 same-frame triple); DEMOTE twin 2x (Cyber [98,111] f5129 91f + Pylon [112,100] f9586 90f with no STARTED twin - 19th/20th live demotes; Cyber re-armed and warped f5134, Pylon cancelled); 17 warps from 22 STARTED rows (Gateway [92,107]: 5 STARTED + 6 cancels + 1 warp f12240 - churned site, eventually warped; PhotonC STARTED f13206 unwarped at exit); Nexus#2 STARTED f11495, warp placed [51,113] per ledger (archived roster Nexus=2); Forge STARTED f9308 and warped [87,124] f13190 per ledger. STUCK_WORKER 300x across 29 workers at ~30f cadence - cyclic, not stalls. First defeat, first wipe (10 Zealot + 7 Dragoon + 5 Probe dead per twin); twin stream intact (no abnormal exit).

 2026-10-26 macro 072350 (VICTORY 0/1, BUILDER_LOG=1): pipeline busy - Pylon#1 f964 [92,113] (bottom spawn), 13 STARTED, 1 CANCEL (CyberneticsC refresh f4117 [91,111], re-requested oid 24, NEVER warped - second terminal Cyber zombie-row lock: 33x core-single-instance, 17x oid 24 + 16x oid 9, same shape+oids as B22); DEMOTE twin 1x (Gateway [101,110] f5392 stoodStill 90f, cid 52 = STARTED [103,111] f5397 oid 29, warped f5397 - 5f re-arm, 21st live demote, recovered; STARTED twin lagged demote by 5f, tile drift [103,111]->[101,110]); BREAKDOWN twin 1x Pylon [94,101] f7770 (tx/ty archived, travelling builder; site already warped f3363, 4400f earlier - post-warp linger, not a pipeline stall); all other STARTED warped (6 Pylon + 5 Gateway + Nexus); STUCK_WORKER 240x across 27 workers at ~30f cadence - cyclic, not stalls. No Assimilator at all this game (BO #10 never STARTED - first Assimilator-less game).

 2026-10-26 macro 074823 (VICTORY 0/0, BUILDER_LOG=1): pipeline healthy - Pylon#1 f964 [92,113] (bottom spawn), 12 STARTED, 0 CANCELs, 0 DEMOTEs, BREAKDOWN twin 2x (Pylon [112,114] f6480 + Pylon [96,106] f8100, travelling builders, both warped after f6610/f8206 per ledger - self-recovered, no demote needed); all 12 STARTED warped (6 Pylon + 4 Gateway + CyberneticsC f4421 + Assimilator f4637); Nexus queued once (NOT_READY f7847) never started (final Nexus=1). No refresh-cancel on Cyber, no zombie row, no core-single-instance - first clean game after B36's lock (terminal-lock count stays 2: B22, B36).

 2026-10-26 macro 075608 (VICTORY 0/0, BUILDER_LOG=1): pipeline healthy - Pylon#1 f964 [92,113] (bottom spawn), 14 STARTED, 0 CANCELs, 0 DEMOTEs, BREAKDOWN twin 2x (Pylon [94,101] f3210 unexplored-site 2/4 + Pylon [98,111] f6810 travelling 4/4, both warped after f3413/f6862 per ledger - self-recovered, no demote needed); 13 of 14 STARTED warped (Pylon [79,114] STARTED f8550 unwarped at the supply-cap exit - late dynamic, no stall); Nexus never queued (final Nexus=1). No refresh-cancel on Cyber, no zombie row, no core-single-instance - second straight clean game after B36's lock.

 2026-10-26 macro 080452 (VICTORY 11/1, BUILDER_LOG=1): pipeline busy - Pylon#1 f964 [92,113] (bottom spawn), 18 STARTED, 2 CANCELs (Pylon refresh [95,114] f7245 recovered as [95,106] f8982 oid42 - cancel-and-replace with 1737f gap; Pylon refresh [112,100] f8188 attempt-only, abandoned after 3 same-tile breakdowns), 0 DEMOTEs, BREAKDOWN twin 3x same tile [112,100] f7350/7380/7410 (builder #27 on PlayerGuard order, unexplored 0/4 - guard-post stall, new shape, not traveller); all 18 STARTED warped incl. Nexus#2 warp placed f9123 (IN_PROGRESS at exit) and Forge f9431; Pylon [79,114] STARTED f6479 warped f6642 (B38's unwarped [79,114] was a one-off). No refresh-cancel on Cyber, no zombie row, no core-single-instance - third straight clean game after B36's lock.

 2026-10-26 macro 081145 (VICTORY 1/0, BUILDER_LOG=1): pipeline healthy - Pylon#1 f964 [92,113] (bottom spawn), 13 STARTED, 1 CANCEL (Pylon refresh [112,100] f8236 oid40 -> resite [80,122] warped f8435, cancel-and-replace with 199f gap), 0 DEMOTEs, BREAKDOWN twin 1x Pylon [112,100] f7350 stoodStill 85f unexplored 0/4 (cancelled at 85f, below the 90f leg - consistent, no demote); all 13 STARTED warped; GW#2/#3 instant starts (no PREPARING twin or console line, builder-rich) warped sup11/13 anyway; Nexus queued once (NOT_READY f8056) never started (final Nexus=1), no Forge. No refresh-cancel on Cyber, no zombie row, no core-single-instance - fourth straight clean game after B36's lock.

 2026-10-26 macro 081641 (VICTORY 0/0, BUILDER_LOG=1): pipeline clean - Pylon#1 f964 [92,113] (bottom spawn), 14 STARTED, 0 CANCELs, 0 DEMOTEs, 0 BREAKDOWNs (cleanest pipeline game on record); all 14 STARTED warped (8 Pylon + 4 Gateway + CyberneticsC f4210 + Assimilator f5164); GW#2/#3 instant starts again (no PREPARING twin or console line, second straight game - builder-rich (series: B41-B46 verified, B43 retroactively)); Nexus never queued (final Nexus=1), no Forge. No refresh-cancel on Cyber, no zombie row, no core-single-instance - fifth straight clean game after B36's lock.

 2026-10-26 macro 081913 (VICTORY 19/7, BUILDER_LOG=1): pipeline busy - Pylon#1 f976 [92,113] (bottom spawn), 18 STARTED, 4 CANCELs (Gateway siteless f6394 oid31 - sixth siteless-mint game; Pylon refresh [95,114] f8809 oid52 -> oid53 resite [96,115] warped [95,114] f8821; Pylon refresh [112,100] f9732 oid52 attempt-only abandoned; Forge refresh [83,107] f10954 oid71 -> oid72 resite [81,110] warped f11347; oid71 re-armed -> Forge [86,115] warped f12140 uncompleted at exit), DEMOTE twin 1x (Gateway [101,110] f7116 stoodStill 90f cid59, warped f7121 - 22nd live demote, 5f re-arm ties the record), BREAKDOWN twin 2x Nexus [51,113] f10140/f10170 (builder #115 on PlayerGuard 49.2 away, unexplored 11/12 - FIRST Nexus breakdowns; distance gate + attempts, no demote); all 18 STARTED warped incl. Nexus warp placed f10631 (roster Nexus=2) and Forge#1 f11347 (Forge#2 warp placed f12140, IN_PROGRESS at exit, roster Forge=1); early zergling rush killed 6 Probes f3866-4167 (WorkerDefenceRun); GW#2/#3 instant starts (no PREPARING twin for oid2/3 - retro-verified, series third game); STUCK_WORKER 303x across 31 workers at ~30f median cadence - cyclic, not stalls. No refresh-cancel on Cyber, no zombie row, no core-single-instance - sixth straight clean game after B36's lock.

 2026-10-26 macro 082330 (VICTORY 7/3, BUILDER_LOG=1): pipeline busy - Pylon#1 f957 [113,15] (top spawn), 15 STARTED, 0 CANCELs, 0 DEMOTEs, 0 BREAKDOWNs; all non-Cyber STARTED warped (14/15); CyberneticsC STARTED f4902 [109,12] never warped, no CANCEL/DEMOTE/BREAKDOWN/csi rows afterward - silent stall to exit (third terminal-Cyber shape after the two zombie locks, but with no lock signature; same no-demote signature as B39's [112,100]); GW#2/#3 instant starts again (no PREPARING twin, fourth straight game (series B41-B46; B43 verified retroactively)); Nexus queued once (NOT_READY f7980) never started (final Nexus=1); no Assimilator at all this game (BO #10 never STARTED - second Assimilator-less game after B36), no Forge; early zergling rush killed 3 Probes f3900-4100 (WorkerDefenceRun); STUCK_WORKER 261x across 28 workers at ~30f median cadence - cyclic, not stalls. Seventh straight game with no lock signature (asterisk: silent Cyber stall).

 2026-10-26 macro 082738 (VICTORY 1/0, BUILDER_LOG=1): pipeline busy - Pylon#1 f964 [92,113] (bottom spawn), 12 STARTED, 0 CANCELs, DEMOTE twin 1x (Pylon [79,121] f6139 stoodStill 90f cid80, warped f6154 - 23rd live demote, 15f re-arm), BREAKDOWN twin 1x [79,121] f6150 stoodStill 101f (11f AFTER the demote - linger, then warp 4f later); all 12 STARTED warped; GW#2/#3 instant starts (no PREPARING twin, fifth straight twin-verified game (series B41-B46)); Nexus queued once (NOT_READY f8875) never started (final Nexus=1), no Forge; CyberneticsC STARTED f4342 warped f4407 clean, Assimilator warped f4643 clean; STUCK_WORKER 221x across 24 workers at ~30f median cadence - cyclic, not stalls. No refresh-cancel on Cyber, no zombie row, no core-single-instance - eighth straight game with no lock signature.

 2026-10-26 macro 082947 (VICTORY 6/5, BUILDER_LOG=1): pipeline busy - Pylon#1 f976 [92,113] (bottom spawn), 13 STARTED, 1 CANCEL (Pylon refresh [112,100] f9784 oid45 -> oid54 resite [79,114] warped f9958, cancel-and-replace across orders with 14f gap), DEMOTE twin 2x (Pylon [90,107] f6936 stoodStill 95f cid57 warped f7044 - 24th live demote, 108f re-arm; Pylon [97,114] f8266 stoodStill 133f cid58 warped f8271 - 25th live demote, 5f re-arm ties the record), 0 BREAKDOWNs; all 13 STARTED warped; GW#2/#3 instant starts (no PREPARING twin, sixth straight twin-verified game (series B41-B46)); Nexus never queued (final Nexus=1), no Forge; CyberneticsC STARTED f4970 [90,104] warped f5144 clean (earliest Cyber ever, sup 17); early rush killed 3 Probes + 1 scout Zealot f3900-4400; STUCK_WORKER 275x across 27 workers at ~30f median cadence - cyclic, not stalls. No refresh-cancel on Cyber, no zombie row, no core-single-instance - ninth straight game with no lock signature.

 2026-10-26 macro 083202 (VICTORY 1/0, BUILDER_LOG=1): pipeline busy - Pylon#1 f964 [92,113] (bottom spawn), 14 STARTED, 2 CANCELs (Pylon refresh [97,114] f4347 oid8 -> resite [98,115] f4410 same oid, warped [97,114] f4410; Pylon refresh [112,100] f8263 oid39 -> oid50 resite [79,121] f8323 unwarped at exit), 0 DEMOTEs, BREAKDOWN twin 2x (Pylon [89,125] f6780 stoodStill 76f harvesting-builder + Pylon [95,114] f7170 stoodStill 136f, both pre-start attempt stalls with builder 8-10 tiles away - no demote, consistent with the distance leg; recovery via resite+warp); 12 of 14 STARTED warped ([97,114] cancelled-resited, [79,121] exit-cutoff); GW#2/#3 instant starts (no PREPARING twin, seventh straight twin-verified game, series B41-B47); Nexus never queued (final Nexus=1), no Forge; CyberneticsC STARTED f3933 warped f4213 clean, Assimilator warped f4868 clean; STUCK_WORKER 222x across 24 workers at ~30f median cadence - cyclic, not stalls. No refresh-cancel on Cyber, no zombie row, no core-single-instance - tenth straight game with no lock signature.

 2026-10-26 macro 083539 (VICTORY 1/0, BUILDER_LOG=1): pipeline busy - Pylon#1 f957 [113,15] (top spawn), 16 STARTED, 2 CANCELs (Gateway refresh [8,54] f2737 + f2760, warped same tile f2959 - double refresh-cancel), 0 DEMOTEs, BREAKDOWN twin 12x (Gateway [8,54] f2760 86f; Pylon [24,44] x9 f3480-f4080 unexplored 2/4 attempt-phase, longest breakdown-to-warp span on record f3480->f4252; Gateway [14,55] f4050 78f builder-on-site Harvest3 class; Pylon [35,65] f7710 64f travelling; zero demotes - all below legs or beyond reach; recovery via start/resite+warp); all 16 STARTED warped; GW#2/#3 instant starts (no PREPARING twin, eighth straight twin-verified game, series B41-B48); Nexus#2 STARTED f8145 [16,14] cid148 oid54 warped [14,13] f8145 per ledger (roster Nexus=2, fifth completed 2-base); Assimilator queued 7:00 never STARTED (third Assimilator-less game after B36/B44); CyberneticsC never STARTED - blocked at prepare-site-overlap 13x f4776+ (no csi, no zombie - fourth terminal-Cyber shape, refusal not lock); no Forge; console-only Pylon [112,100] cancel (6:50, zero rows anywhere in the 4612-row archive - twin coverage gap, explicitly NOT datumed); siteless Gateway (At 23 #31, Null position) with NO cancel twin either (B43's same shape had one) - twin coverage for the mint shape is inconsistent, flagged, not datumed; STUCK_WORKER 381x across 25 workers at ~30f median cadence - cyclic, not stalls (highest STUCK count yet). No core-single-instance fires anywhere - eleventh straight game with no lock signature.

 2026-10-26 macro 084039 (VICTORY 1/0, BUILDER_LOG=1): pipeline busy - Pylon#1 f952 [10,52] (top spawn), 12 STARTED, 0 CANCELs, DEMOTE twin 1x (Pylon [13,58] f4638 stoodStill 90f cid50, warped f4643 - 26th live demote, 5f re-arm ties the record), BREAKDOWN twin 1x (Gateway [14,51] f2040 stoodStill 165f, builder #6 PlayerGuard ON the site footprint self-blocked - the deterministic opening tile again; no demote, warped same tile f2044 just 4f later - fastest breakdown-to-warp recovery on record); all 12 STARTED warped; GW#2/#3 instant starts (no PREPARING twin, ninth straight twin-verified game, series B41-B49); Nexus never queued (final Nexus=1), no Forge; CyberneticsC STARTED f3902 warped f4345 clean, Assimilator warped f4516 clean; STUCK_WORKER 221x across 25 workers at ~30f median cadence - cyclic, not stalls. No refresh-cancel on Cyber, no zombie row, no core-single-instance - twelfth straight game with no lock signature.

 2026-10-26 macro 084359 (VICTORY 5/2, BUILDER_LOG=1): pipeline busy - Pylon#1 f952 [10,52] (top spawn), 16 STARTED, 2 CANCELs (Gateway [17,47] f9309 took-too-long - FIRST took-too-long cancel on record + Gateway [15,58] f9775 refresh), DEMOTE twin 2x ([17,47] f9309 stoodStill 90f + f9674 stoodStill 133f - 27th/28th live demotes; the order re-armed with no STARTED twin and warped [15,58] f9971 - 297f re-arm), BREAKDOWN twin 3x ([14,51] f2040 162f builder-on-site; [24,44] f3300 222f + f3330 252f unexplored 2/4; +7 untwinned transcript rows - twin fires per construction early only, coverage gap noted); REQUEUE_VETO twin 4x HOLD (first twin sighting; maxAllowedAtOnce=2 vs 5 triggerable); all 16 STARTED warped except cancelled [17,47] (superseded by resite); GW#2/#3 instant starts (no PREPARING twin, eleventh straight twin-verified game, series B41-B50); Nexus#2 STARTED f7963 [16,14] cid148 oid54 warp placed [14,13] f7963 per ledger (roster Nexus=2, sixth completed 2-base); Assimilator#1 warped f5078; Assimilator#2 queued 7:00 never STARTED (first second-Assimilator demand); Nexus saga (queued 5:17, IN_PROGRESS to near-exit, rich-but-idle pump stalls with banked minerals); no Forge; deaths 2 (Dragoon#172 + Zealot#165, hydra brawl 6:02-6:03); STUCK_WORKER 228x across 26 workers at ~30f median cadence - cyclic, not stalls. No refresh-cancel on Cyber, no zombie row, no core-single-instance - eleventh straight game with no lock signature.

 2026-10-26 macro 091041 (VICTORY 1/0, BUILDER_LOG=1): pipeline busy - Pylon#1 f964 [92,113] (bottom spawn), 14 STARTED, 1 CANCEL (Pylon refresh [97,114] f4324 -> resite [98,115] f4341 same order, warped [97,114] f4341 - 17f gap), 0 DEMOTEs, 0 BREAKDOWNs; 13 of 14 STARTED warped (cancelled [97,114] superseded by resite); GW#2/#3 instant starts (no PREPARING twin, eleventh straight twin-verified game, series B41-B51); Nexus never queued (final Nexus=1), no Forge; CyberneticsC STARTED f3961 warped f4204 clean, Assimilator warped f4866 clean; STUCK_WORKER 218x across 23 workers at ~30f median cadence - cyclic, not stalls. No refresh-cancel on Cyber, no zombie row, no core-single-instance - fourteenth straight game with no lock signature.

 2026-10-26 macro 064641 (VICTORY 1/0, BUILDER_LOG=1): pipeline busy - Pylon#1 f958 [113,15] (top spawn), 18 STARTED, 2 CANCELs (Pylon refresh f5290 [117,12] never re-warped - site abandoned; siteless Gateway f10613); BREAKDOWN x2 Forge [112,2] f9090 (two builders same frame - first Forge breakdowns); DEMOTE twin 1x Assimilator, recovered per ledger; 17 warps from 18 STARTED rows (1 cancelled Pylon abandoned + 1 warp without STARTED twin). STUCK_WORKER 415x across 26 workers - cyclic cadence over the long game, not stalls.

2026-10-26 macro 062321: ABNORMAL EXIT, no pipeline datum - JVM gone ~f8661 with no verdict/tail; twin stream lost (32 startup rows: no CANCEL/DEMOTE/REFUSED/STUCK/NexusQ evidence retained - the 3 stream-observed deaths, the Pylon demote 131f, the BOARD_FED row and the breakdowns are explicitly NOT datumed). Ledger-only facts: 22 warps (9 Zealot, 5 Pylon, 3 Gateway, 3 Dragoon, 1 CyberneticsC, 1 Assimilator), last warp Dragoon f8661 [113,18]; Pylon#1 f1154 [113,15] (top spawn).

2026-10-26 macro 064330 (VICTORY 1/0, 340s game, zero own deaths): 0 ADOPTED; 1 CANCEL (Pylon refresh f6716 [98,111] "position still not good after refresh", re-requested NULL twice, then re-queued and warped f6808 per ledger - cancel-and-replace live with a null intermediate); 0 DEMOTEs; BREAKDOWN twin 1x Pylon [90,107] f5190 (travelling builder, distToBuild 11.9, self-recovered and warped); all 12 STARTED rows effectively warped (13th STARTED row was the cancelled [98,111], replaced by the f6808 warp); Pylon#1 f964 [92,113] (bottom spawn); Nexus never queued (final Nexus=1). No refresh-cancel on Cyber, no zombie row, no core-single-instance - seventh straight clean game for the lock story (B22 remains the lone lock).

2026-10-26 macro 065027 (VICTORY 4/1, 379s game, 1 own death): 0 ADOPTED; 1 CANCEL (Pylon refresh f8718 [112,100] - attempt-only, never STARTED, re-requested and warped [112,114] f8850); 0 DEMOTEs; BREAKDOWN twin 5x Pylon [112,100] (f7800 x3 + f7830 x2, travelling builders at unexplored tile per console - the B21 attempt-only shape repeating at the same tile, recovered via re-request); Death archive-verified: Dragoon#178 f9414 under hydra contact (ClusterKite per stream, not datumed); all 17 STARTED warped (the cancel hit a never-started order); Pylon#1 f964 [92,113] (bottom spawn); Nexus#2 STARTED f9794, warp placed [51,113] f9794 per ledger - uncompleted at the supply-cap exit (706f later, final Nexus=1 per roster); Forge STARTED f9301 and warped [83,114] f9449 (second Forge warp). No refresh-cancel on Cyber, no zombie row, no core-single-instance - eighth straight clean game for the lock story.

2026-10-26 macro 065812 (VICTORY 0/0, 365s game, zero own deaths): 0 ADOPTED; 0 CANCELs; 0 DEMOTEs; BREAKDOWN twin 1x Pylon [95,106] f7770 (travelling builder, stoodStill 76f, warped f7855 per ledger - self-recovered, no demote needed); all 13 STARTED warped; Pylon#1 f964 [92,113] (bottom spawn); Nexus never queued (final Nexus=1). No refresh-cancel on Cyber, no zombie row, no core-single-instance - ninth straight clean game for the lock story (B22 remains the lone lock).

2026-10-26 macro 071321 (Defeat 6/22, 564s game, 22 own deaths): 0 ADOPTED; 7 CANCELs (1 Pylon refresh f9587 [112,100] attempt-only + 6 Gateway refresh same-site [92,107] f11500-f12075); DEMOTE_STALLED twin 2x (CyberneticsC [98,111] f5129 stoodStill 91f + Pylon [112,100] f9586 stoodStill 90f with no STARTED twin - 19th/20th live demotes; Cyber re-armed and warped f5134, Pylon cancelled); BREAKDOWN twin 4x (CyberneticsC [98,111] f5130 post-demote linger + Gateway [92,107] x3 f11490 same-frame triple, all travelling builders); Death archive-verified: 22 UNIT_DIED f3900-f13449 (5 Probe early Zerg rush + 10 Zealot + 7 Dragoon hydra brawl f9300+, last Dragoon#199 f13280; ClusterFlee/ClusterBreachRetreat per stream, not datumed); 17 warps from 22 STARTED rows (Gateway [92,107]: 5 STARTED + 6 cancels + 1 warp f12240 - churned site, eventually warped; PhotonC STARTED f13206 unwarped at exit); Pylon#1 f976 [92,113] (bottom spawn); Nexus#2 STARTED f11495, warp placed [51,113] per ledger (archived roster Nexus=2); Forge STARTED f9308 and warped [87,124] f13190 per ledger. No refresh-cancel on Cyber, no zombie row, no core-single-instance - tenth straight clean game for the lock story (B22 remains the lone lock).

2026-10-26 macro 072350 (VICTORY 0/1, 377s game, zero own deaths): 0 ADOPTED; 1 CANCEL (CyberneticsC refresh f4117 [91,111] - re-requested as oid 24, NEVER warped: second terminal Cyber zombie-row lock (B22 was first) - 33x core-single-instance, 17x oid 24 + 16x oid 9 in the same frames, both oids verified in archive twins; consequence identical: no CyberneticsC all game (ledger confirms zero Cyber warps); Pylon#1 f964 [92,113]; DEMOTE_STALLED twin 1x Gateway [101,110] f5392 (cid 52 = STARTED [103,111] f5397 oid 29, warped f5397 - 5f re-arm, 21st live demote, recovered); the lock needs no new code reading: same oids, same cadence (300f pairs), same terminal outcome as B22 - fix stays owner-side (order-row retirement). BREAKDOWN twin 1x Pylon [94,101] f7770 (tx/ty archived, travelling builder gathering, site warped f3363 - post-warp linger, not a pipeline stall); deaths: 0 (UNIT_DIED none - first bloodless game besides the 0/0 draws); Nexus#2 STARTED f6511, warp placed [51,113] f6511 per ledger (archived roster Nexus=2, completed); No Assimilator and no Forge this game (neither STARTED nor warped - first Assimilator-less game; Forge absent too). No clean-game streak to extend - second terminal lock breaks B22's lone-lock record (lock count: B22, B36).

2026-10-26 macro 074823 (VICTORY 0/0, 371s game, zero own deaths): 0 ADOPTED; 0 CANCELs; 0 DEMOTEs; BREAKDOWN twin 2x (Pylon [112,114] f6480 stoodStill 73f + Pylon [96,106] f8100 stoodStill 120f, travelling builders, both warped after per ledger - recovery via warp, no demote); CyberneticsC STARTED f4356 [91,111] warped f4421 clean (no refresh-cancel, no zombie row); Pylon#1 f964 [92,113]; Nexus queued once (NOT_READY f7847) never started (final Nexus=1); STUCK_WORKER 215x across 23 workers at ~30f median cadence - cyclic, not stalls. First clean game after B36's lock.

2026-10-26 macro 075608 (VICTORY 0/0, 366s game, zero own deaths): 0 ADOPTED; 0 CANCELs; 0 DEMOTEs; BREAKDOWN twin 2x (Pylon [94,101] f3210 stoodStill 74f unexplored 2/4 + Pylon [98,111] f6810 stoodStill 77f buildable 4/4, both travelling-or-waiting builders, both warped after per ledger - recovery via warp, no demote); CyberneticsC STARTED f3961 warped f4211 clean (no refresh-cancel, no zombie row); Pylon#1 f964 [92,113]; Nexus never queued (final Nexus=1); Pylon [79,114] STARTED f8550 unwarped at exit (late dynamic). STUCK_WORKER 214x across 22 workers at ~30f median cadence - cyclic, not stalls. Second straight clean game after B36's lock.

2026-10-26 macro 080452 (VICTORY 11/1, 446s game, 1 own death): 0 ADOPTED; 2 CANCELs (Pylon refresh [95,114] f7245 re-request oid42 STARTED f8982 warped f8982 - recovered; Pylon [112,100] f8188 attempt-only, 3 breakdowns then abandoned); 0 DEMOTEs despite 127f stillness ([112,100] attempts every ~30f kept resetting the 150f-since-last-attempt leg - consistent with the timing rule); BREAKDOWN twin 3x [112,100] guard-post stall; deaths 1 (Dragoon#166 f10145, hydra brawl f9800-10150, ClusterKite/Flee per stream; 11 kills per verdict, 10 accounted - Zealot 2 + Dragoon 8); Nexus#2 STARTED f9123 [53,114] cid58 oid62 warp placed [51,113] f9123 per ledger, IN_PROGRESS at exit (roster Nexus=1, uncompleted); Forge STARTED f9372 warped f9431; STUCK_WORKER 232x across 24 workers at ~30f median cadence - cyclic, not stalls. Third straight clean game after B36's lock.

2026-10-26 macro 081145 (VICTORY 1/0, 371s game, zero own deaths): 0 ADOPTED; 1 CANCEL (Pylon refresh [112,100] f8236, resited [80,122] warped f8435 - recovered; fourth game with [112,100] trouble: B33/B35 refresh-cancels, B39 triple-breakdown abandon, B41 breakdown+cancel+resite-recover); 0 DEMOTEs (85f stillness below the leg); BREAKDOWN twin 1x [112,100] guard-post-class unexplored; deaths 0 (1 kill per verdict); STUCK_WORKER 215x across 25 workers at ~30f median cadence - cyclic, not stalls. Fourth straight clean game after B36's lock.

2026-10-26 macro 081641 (VICTORY 0/0, 365s game, zero own deaths): 0 ADOPTED; 0 CANCELs; 0 DEMOTEs; 0 BREAKDOWNs; 14 of 14 STARTED warped (CyberneticsC STARTED f4051 warped f4210 clean, no lock); Pylon#1 f964 [92,113]; Nexus never queued (final Nexus=1), no Forge; STUCK_WORKER 220x across 24 workers at ~30f median cadence - cyclic, not stalls. Fifth straight clean game after B36's lock.

2026-10-26 macro 081913 (VICTORY 19/7, 525s game, 7 own deaths): 0 ADOPTED; 4 CANCELs (Gateway siteless f6394 "Null position" - sixth siteless-mint game after 212818/003012/045503/060844/064641; Pylon [95,114] f8809 + Pylon [112,100] f9732 (same oid52, both attempt-only, second site abandoned); Forge [83,107] f10954, resited [81,110] warped f11347); DEMOTE_STALLED twin 1x Gateway [101,110] f7116 stoodStill 90f (cid59, warped f7121 - 22nd live demote, 5f re-arm ties B36's record); BREAKDOWN twin 2x Nexus [51,113] (first of type, guard-post-class unexplored, no demote - distance gate 49.2 + attempts; recovery via warp 461f later); deaths 7 (6 Probe zergling rush + Dragoon#177 7:57 ClusterFlee vs drones/lings); kills 19 per verdict (13 accounted: Zealot 9 + Probe 1 + Dragoon 3); Nexus#2 STARTED f10631 [53,114] cid65 oid66 warp placed [51,113] f10631 per ledger (roster Nexus=2); Forge STARTED f11147 [81,110] cid77 oid72 warped f11347 per ledger (roster Forge=1; second Forge STARTED f12140 [86,115] cid96 oid71 warped f12140 uncompleted at exit). Sixth straight clean game after B36's lock.

2026-10-26 macro 082330 (VICTORY 7/3, 388s game, 3 own deaths): 0 ADOPTED; 0 CANCELs; 0 DEMOTEs (Cyber 4340f stall without demote - attempts/distance legs, same signature as B39's [112,100]); 0 BREAKDOWNs; 14 of 15 STARTED warped (CyberneticsC STARTED f4902 [109,12] never warped, silent stall, no recovery twin); Pylon#1 f957 [113,15] (top spawn); Nexus queued once (NOT_READY f7980) never started (final Nexus=1); deaths 3 (Probe zergling rush f3900-4100); kills 7 per verdict (3 accounted: Zealot 1 + Probe 2).

2026-10-26 macro 082738 (VICTORY 1/0, 374s game, zero own deaths): 0 ADOPTED; 0 CANCELs; DEMOTE_STALLED twin 1x Pylon [79,121] f6139 stoodStill 90f (cid80, warped f6154 - 23rd live demote, 15f re-arm); BREAKDOWN twin 1x [79,121] f6150 stoodStill 101f (demote-then-breakdown-then-warp inside 15f: f6139 -> f6150 -> f6154); deaths 0, kills 1; Pylon#1 f964 [92,113] (bottom spawn); Nexus queued once (NOT_READY f8875) never started (final Nexus=1).

2026-10-26 macro 082947 (VICTORY 6/5, 423s game, 5 own deaths): 0 ADOPTED; 1 CANCEL (Pylon refresh [112,100] f9784, resited [79,114] warped f9958 - sixth game with [112,100] trouble: B33/B35 cancels, B39 abandon, B41 resite-recover, B43 abandon, B46 resite-recover); DEMOTE_STALLED twin 2x (Pylon [90,107] f6936 95f + Pylon [97,114] f8266 133f, both recovered per ledger - 24th/25th live demotes); 0 BREAKDOWNs; deaths 5 (4 Probe + 1 scout Zealot, rush f3900-4400); kills 6 per verdict (1 accounted: Probe 1); Pylon#1 f976 [92,113] (bottom spawn); Nexus never queued (final Nexus=1).

2026-10-26 macro 083202 (VICTORY 1/0, 355s game, zero own deaths): 0 ADOPTED; 2 CANCELs (Pylon [97,114] f4347 resited [98,115] warped - same oid8, 63f gap; Pylon [112,100] f8263, resited [79,121] unwarped at exit - seventh game with [112,100] trouble); 0 DEMOTEs (76f below the leg + 136f beyond it but builder 8.4 away - distance leg); BREAKDOWN twin 2x (attempt-phase pre-start stalls, recovery via resite+warp); deaths 0, kills 1; Pylon#1 f964 [92,113] (bottom spawn); Nexus never queued (final Nexus=1).

2026-10-26 macro 083539 (VICTORY 1/0, 355s game, zero own deaths): 0 ADOPTED; 2 CANCELs (Gateway [8,54] f2737/f2760 double refresh-cancel, warped same tile f2959); 0 DEMOTEs; BREAKDOWN twin 12x (Gateway [8,54], Pylon [24,44] x9, Gateway [14,55], Pylon [35,65]; zero demotes, all recovered); deaths 0, kills 1 per verdict (unattributed in archive - no kill rows, no UNIT_DIED); Pylon#1 f957 [113,15] (top spawn); Nexus#2 warp placed f8145 (roster Nexus=2); siteless Gateway (At 23 #31) - seventh siteless-mint game after 212818/003012/045503/060844/064641/B43, this time with NO twin at all.

2026-10-26 macro 084039 (VICTORY 1/0, 366s game, zero own deaths): 0 ADOPTED; 0 CANCELs; DEMOTE_STALLED twin 1x Pylon [13,58] f4638 stoodStill 90f (cid50, warped f4643 - 26th live demote); BREAKDOWN twin 1x Gateway [14,51] f2040 stoodStill 165f (builder-on-site self-block, no demote - attempts leg, warped f2044, 4f re-arm); deaths 0, kills 1 per verdict (unattributed in archive); Pylon#1 f952 [10,52] (top spawn); Nexus never queued (final Nexus=1).

2026-10-26 macro 084359 (VICTORY 5/2, 420s game, 2 own deaths): 0 ADOPTED; 2 CANCELs (Gateway [17,47] f9309 took-too-long - FIRST took-too-long cancel on record + Gateway [15,58] f9775 refresh); DEMOTE_STALLED twin 2x ([17,47] f9309 90f + f9674 133f - 27th/28th live demotes; the order re-armed with no STARTED twin and warped [15,58] f9971); BREAKDOWN twin 3x + 8 untwinned transcript rows (coverage gap, counted but not datumed beyond twins); REQUEUE_VETO twin 4x HOLD (first twin sighting - veto blocks 5 triggerable at maxAllowedAtOnce=2); deaths 2 (Dragoon#172 + Zealot#165, hydra brawl 6:02-6:03); kills 5 per verdict (Zealot 1 + Dragoon 2 per kill section + 2 unattributed in archive); Pylon#1 f952 [10,52] (top spawn); Nexus#2 STARTED f7963 [16,14] cid56 oid42 warp placed f7963 (roster Nexus=2).

2026-10-26 macro 064641 (VICTORY 1/0, 476s game, zero own deaths): 0 ADOPTED; 2 CANCELs (Pylon refresh f5290 [117,12] - re-queued, warped? NO: no [117,12] warp in ledger, site abandoned; siteless Gateway f10613 "Null position" - fifth siteless-mint game after 212818/003012/045503/060844); DEMOTE_STALLED twin 1x (Assimilator [117,4] f4657 stoodStill 90f, warped f4676 per ledger - eighteenth live demote, recovered; second Assimilator demote after 061958's); BREAKDOWN twin 2x Forge [112,2] f9090 (two builders same frame - the B22 Pylon-triple class, first Forge breakdowns); NO twin gap: all 8 Gateway warps match 8 STARTED rows by orderId (oid 164: STARTED [106,5] f10612 = warp [104,4] f10612 - the STARTED twin names the reserved tile, the ledger the actual warp tile, drift up to 2 tiles; same class as B36's demote tile drift); Pylon#1 f958 [113,15] (top spawn); Nexus#2 STARTED f9444 and warp placed [103,37] per ledger (console roster Nexus=2); Forge STARTED f9088 and warped [112,2] (first Forge warp on record). No refresh-cancel on Cyber, no zombie row, no core-single-instance - eighth straight clean game for the lock story.

2026-10-26 macro 063610: ABNORMAL EXIT, no pipeline datum - JVM gone ~f6963 with no verdict/tail (last ledger warp Dragoon f6963, last stream event 6:02 Nexus ADDED); twin stream lost (32 startup rows: no CANCEL/DEMOTE/REFUSED/STUCK/NexusQ evidence retained - stream-observed items explicitly NOT datumed); ledger-only facts: 19 warps (6 Zealot, 5 Pylon, 3 Gateway, 3 Dragoon, 1 CyberneticsC, 1 Assimilator); Pylon#1 f1172 [113,15] (top spawn, ledger Pylon#1 sup 8). Second abnormal exit with the identical twin-loss signature (see 062321 datum) - pattern, cause open.

2026-10-26 macro 080801: ABNORMAL EXIT, no pipeline datum - JVM gone ~f8550 with no verdict/tail (last ledger warp Pylon f8541 [116,21], last live-log event 5:53 Gateway PREPARING [108,4]; twin stream wiped - 32-row artifact 4th instance: no CANCEL/DEMOTE/REFUSED/STUCK/NexusQ evidence retained - stream-observed items explicitly NOT datumed); ledger-only facts: 13 building warps (7 Pylon, 4 Gateway, 1 CyberneticsC, 1 Assimilator) + 12 army (7 Zealot, 5 Dragoon); Pylon#1 f1156 [113,15] (top spawn, ledger sup 8); Nexus never queued (no Nexus/Forge warps, died ~f8550). Third abnormal exit with the twin-wipe signature - first with a KNOWN concurrent suite (unit suite overlapped the run): the race mechanism is now observed live, not inferred. The death itself is still unexplained.
2026-10-26 B40 stdout anomaly (open, no behavior change): archived WINE_LOG is startup-only while WINE_DEBUG is full and the live transcript showed the full stream incl. exitGame lines; tests never touch WINE_LOG (verified: only GameSummaryShutdownTest redirects System.out with immediate restore, BotOutputReachesConsoleTest only greps the script) - pipe/tee-level loss, a second mechanism beyond the stream wipe; no stuck client on the box (verified by process table at the time); transcript-vs-archive discrepancy noted - the archive-only rule is vindicated, transcripts are not evidence.
2026-10-26 B40 death-cause hunt (code search, no behavior change): no silent exit path exists - every exitGame caller prints first (message/summary/backstop), AGame.exit(String) callers are startup-only except keyboard Exit, trainNowIfHaveWhatsRequired's exit is unreachable (sole caller passes true); dmesg shows no segfaults in the Oct-23 window and no OOM-kills (62 GB box); no stuck client on the box; no hs_err anywhere. Excluded: bot-code exit, OOM, native crash with dump. Remaining: sudden host/Wine-level death with no JVM involvement. Still open, now narrowed.
2026-10-26 twin-stream-loss ROOT CAUSE (archive + 167-file scan + code, no behavior change): the 32 rows are FOREIGN, not the game's startup - 29 Terran ORDER_NOT_READY/PREPARING (SupplyD/Barracks/Academy/Refinery/ComsatS/Factory/...) + 1 Protoss Gateway STARTED f3 at a random tile ([39,36] in 062321 vs [22,61] in 063610, otherwise byte-identical); healthy macros have ~6 frame0-3 rows, all Protoss, never Terran. DECISIONS agrees: 062321 10 rows frames 2-40 ("At 16 Pylon*/At 10 Forge*" dynamic storm, oids 147+, adopt paths - never in healthy macros, whose rows mirror the BO with matching cids) and 063610 11 rows = same fixtures + own f6802 row interleaved (two concurrent writers). Mechanism, all verified: OnGameEnd.flush -> EventLog.flushTo does removeFile on the first flush OF THAT JVM (truncatedPaths/firstFlushDone are per-process, no cross-JVM guard), so a ~3-frame test/sim instance ending mid-game wipes the live 6-minute stream and leaves its 32 rows; DecisionLogTest/DecisionDemandContextTest/CloseWarpsTwinTest write the REAL production paths with racy backup/restore (delete and restore both wipe live rows). Ledger survives because it is rewritten whole on every warp (self-healing); streams are append-only (delete = permanent loss). Scan: 167 unique logs, zero backward frame jumps, zero double startups, exactly 3 tiny files (201818 UMS + the pair). Fix direction is owner decision (test/write-dir isolation - the tests state they use real paths to prove the production path); never overlap a suite run with a Wine game until then. STILL OPEN, separate issue: why the client JVM itself died at f8661/f6963 with no verdict/tail (no hs_err, no OOM in dmesg) - the stream loss is explained, the death is not.

2026-10-26 macro 062612 (VICTORY 1/0, 350s game, zero own deaths): 0 ADOPTED; 1 CANCEL (Gateway refresh f2668 [8,54], re-sited [4,55] warped f3759 - cancel-and-replace live); DEMOTE_STALLED twin 1x (CyberneticsC [16,47] f4706 stoodStill 90f, warped f4711 per ledger - fourteenth live demote, recovered; second Cyber demote after 002228's); BREAKDOWN twin 1x Gateway [14,51] f2040 builder-on-site (SIXTH occurrence of the deterministic opening tile, self-recovered); all 12 STARTED warped (7 Pylon + 3 Gateway + CyberneticsC f4711 + Assimilator f4967); Pylon#1 f952 [10,52]; Nexus queued once (NOT_READY f8322) never started (final Nexus=1). No refresh-cancel on Cyber, no zombie row, no core-single-instance - third straight clean game for the lock story.

2026-10-26 macro 062958 (VICTORY 1/0, 364s game, zero own deaths): 0 ADOPTED; 1 CANCEL (Gateway refresh f7981 [17,40] "position still not good after refresh", re-STARTED f7991 and warped f8112 per ledger - cancel-and-replace live on the same site; SAME frame+tile as 050409's f7981 [17,40] cancel - deterministic refresh-cancel timing, second occurrence); 0 DEMOTEs; 0 BREAKDOWNs; all 14 STARTED warped (7 Pylon + 5 Gateway + CyberneticsC + Assimilator); Pylon#1 f952 [10,52]; Nexus queued once (NOT_READY f8662) never started (final Nexus=1). Gateway refresh-cancel with clean re-request and no lock - fourth straight clean game for the zombie-row story (B22 remains the lone lock).

2026-10-26 macro 063315 (VICTORY 6/1, 361s game, 1 own death): 0 ADOPTED; 0 CANCELs; DEMOTE_STALLED twin 2x in one game (Gateway [101,110] f5400 stoodStill 93f, warped f5405 - fifteenth live demote; Pylon [90,107] f5549 stoodStill 90f, warped f5554 - sixteenth; both recovered per ledger; first double-demote game); BREAKDOWN twin 1x Pylon [90,107] f5550 (builder on site, PlayerGuard, lastCommand=Build 214f ago - fired one frame AFTER the f5549 demote, builder lingered at the site; warp f5554 closes all three rows); Death archive-verified: Zealot#135 f4717 under Zerg rush (second fight game after 060047; ClusterBreachRetreat per stream, not datumed); all 12 STARTED warped (6 Pylon + 4 Gateway + CyberneticsC + Assimilator); Pylon#1 f976 [92,113] (bottom spawn); Nexus never queued (final Nexus=1). No refresh-cancel, no zombie row, no core-single-instance - fifth straight clean game for the lock story.

2026-10-26 macro 064036 (VICTORY 1/0, 371s game, zero own deaths): 0 ADOPTED; 0 CANCELs; DEMOTE_STALLED twin 1x (Gateway [84,108] f4706 stoodStill 90f per twin - console printed 93f, twin rules - re-armed and warped f4864 per ledger, 158f later - seventeenth live demote, longest re-arm on record vs 5-31f before, no second STARTED row so same construction); BREAKDOWN twin 1x Gateway [84,108] f4860 (tx/ty archived; builder on site, PlayerGuard, 244f still - 4f BEFORE the warp, recovery in flight); all 12 STARTED warped (6 Pylon + 4 Gateway + CyberneticsC f4414 + Assimilator f4629 sup 23 = At 23 ON PLAN); Pylon#1 f964 [92,113] (bottom spawn); Nexus never queued (final Nexus=1). No refresh-cancel, no zombie row, no core-single-instance - sixth straight clean game for the lock story.

2026-10-26 macro 060844 (VICTORY 1/0, BUILDER_LOG=1): pipeline busy - Pylon#1 f957 [113,15] (top spawn), 17 STARTED, 8 CANCELs (7 refresh + 1 siteless Gateway, all archived), BREAKDOWN x6 (Gateway x3 incl. the 291f stall, Pylon [123,23] x3 same-frame triple); DEMOTE x0 (the 291f stall never tripped it - dist gate held, builder never within 3.5); 13 of 17 STARTED warped (5 Pylon + 7 Gateway + Nexus#2; unwarped: CyberneticsC (lock, see below) + Pylon [118,13] + the [117,12] churn (3 STARTED rows, 1 warp)); Nexus#2 warped f6969 (final Nexus=2). STUCK_WORKER 262x across 28 workers at ~30f cadence - cyclic, not stalls.

 2026-10-26 macro 003012 (VICTORY 4/0, BUILDER_LOG=1): pipeline healthy - Pylon#1 f964, 18 STARTED, 2 CANCELs (Pylon refresh f8316; Gateway siteless f8937), BREAKDOWN x2 Pylon self-recovered; DEMOTE_STALLED 1x Forge (twin live, recovered); BO audit only 3 mismatches. STUCK_WORKER 234x across 25 workers at ~30f cadence - cyclic, not stalls.


 2026-10-26 macro 092241 (VICTORY 6/8, BUILDER_LOG=1): pipeline busy - Pylon#1 f976 [92,113] (bottom spawn), 14 STARTED, 1 CANCEL (Pylon refresh [112,100] f10941 took-too-long shape, never re-warped), DEMOTE twin 2x (Gateway#31 f6474 + Pylon f7674, both 90f); BREAKDOWN twin 2x early (Pylon [94,101] f3210 63f + [112,114] f8550 71f, both recovered) + late Gateway cluster f11160-11670 (stoodStill 82->222, exit-cutoff, unrecovered) + Nexus pair f10350; 13 of 14 STARTED warped; GW#2/#3 instant starts (no PREPARING twin); Nexus#2 STARTED f10769 [53,114] cid60 warp placed [51,113] per ledger; Assimilator#1 warped f6837; no Forge; CyberneticsC STARTED f5127 warped f5181 (sup14, earliest Cyber on record); STUCK_WORKER 306x across 32 workers - cyclic, not stalls. No refresh-cancel on Cyber, no zombie row, no core-single-instance.
 2026-10-26 macro 110209 (VICTORY 6/11, BUILDER_LOG=1): pipeline busy - Pylon#1 f976 [92,113] (bottom spawn), 16 STARTED, 2 CANCELs (Cyber took-too-long + Pylon resite); CyberneticsC STARTED f7405 cid54 oid9, 50x core-single-instance refusals, NEVER warped (fifth terminal-Cyber shape (B48 datum names the fourth) - refusal, not lock, per lock-pair test); Assimilator never STARTED (gasless, fourth Assimilator-less after B36/B44/B48); no Dragoons warped; 14 of 16 STARTED warped (Cyber never + Pylon [90,107] superseded by resite); GW#2/#3 instant starts (no PREPARING twin, dynamic oids 33/55 only); Nexus#2 f11948 + Nexus#3 f12250 warped; no Forge; STUCK_WORKER 0x; NexusQ 156. No refresh-cancel on Cyber (cancel was took-too-long), no zombie row beyond the refusal shape, no core-single-instance outside Cyber.
## §6 — #74 breakdown + demote analyses

Collects the demote/abnormal-exit analyses attached to the #84 and #74 chain regions.

 2026-10-23 macro 203305: BUILD_BREAKDOWN once (Assimilator, self-recovered); no alternating-handoff shape.

 2026-10-23 long macro 212818: no MOVE_BUILD rows in this log's schema (handoff shape unobservable here); no alternating-handoff evidence either way.

 2026-10-23 macro 224120: same schema (0 MOVE_BUILD rows); BUILD_BREAKDOWN x2 both self-recovered (Pylon [112,114] warped f6664), no alternating-handoff evidence either way.

 2026-10-23 macro 225319: same schema (0 MOVE_BUILD rows); BUILD_BREAKDOWN x2 both self-recovered (all 14 STARTED warped), no alternating-handoff evidence either way.

 2026-10-23 macro 231335: same schema (0 MOVE_BUILD rows); breakdowns self-recovered, all 12 STARTED warped, no alternating-handoff evidence either way.

 2026-10-23 macro 235430: same schema (0 MOVE_BUILD rows); zero breakdowns, all 13 STARTED warped, no alternating-handoff evidence either way.

 2026-10-26 DRY_RUN macro 002228: same schema (0 MOVE_BUILD rows); zero alternating-handoff evidence either way (2 Pylon breakdowns with travelling builders, both self-recovered without demote).

 2026-10-26 macro 001819: same schema (0 MOVE_BUILD rows); zero breakdowns, all 15 STARTED warped, no alternating-handoff evidence either way.

 2026-10-26 macro 003419: same schema (0 MOVE_BUILD rows); zero alternating-handoff evidence either way (Gateway breakdown, then demote+re-arm - the recovery path firing as designed).

 2026-10-26 macro 045503: same schema (0 MOVE_BUILD rows); no breakdowns at all, all 12 STARTED warped, zero alternating-handoff evidence either way.

 2026-10-26 macro 045940: same schema (0 MOVE_BUILD rows); zero breakdowns, all 13 STARTED warped, zero alternating-handoff evidence either way.

 2026-10-26 macro 050409: same schema (0 MOVE_BUILD rows); zero breakdowns; 14 of 15 STARTED warped (1 refresh-cancelled and replaced on the same site) - the variance remainder did not reproduce again, zero alternating-handoff evidence either way.

 2026-10-26 macro 051600: same schema (0 MOVE_BUILD rows); 1 Gateway breakdown ([14,51] f2070, builder-on-site, self-recovered in 4f) then all 15 STARTED warped - the deterministic opening self-block fired a fourth time, recovery path as designed, zero alternating-handoff evidence either way.

 2026-10-26 macro 055715: same schema (0 MOVE_BUILD rows); 3 breakdowns, all recovered (Gateway [17,44] via demote+re-arm; Pylon [35,65] x2 self-recovered without demote - the unexplored-site shape, warped f8452), all 13 STARTED warped, zero alternating-handoff evidence either way.

 2026-10-26 macro 060047: same schema (0 MOVE_BUILD rows); zero breakdowns, all 15 STARTED warped, zero alternating-handoff evidence either way.

 2026-10-26 macro 060458: same schema (0 MOVE_BUILD rows); 2 Pylon breakdowns, both unexplored sites (second and third sites after 055715's [35,65]: [94,101] recovered, [112,100] attempt-only with no construction) - the unexplored-site shape repeats; 11 of 12 STARTED warped (Gateway cut off by exit), zero alternating-handoff evidence either way.

 2026-10-26 macro 061958: same schema (0 MOVE_BUILD rows); 1 Gateway breakdown ([14,51] f2040 builder-on-site - fifth occurrence of the deterministic opening tile, self-recovered); all 13 STARTED warped, zero alternating-handoff evidence either way.

 2026-10-26 macro 062612: same schema (0 MOVE_BUILD rows); 1 Gateway breakdown ([14,51] f2040 builder-on-site - sixth occurrence, self-recovered in the opening); all 12 STARTED warped, zero alternating-handoff evidence either way.

 2026-10-26 macro 062958: same schema (0 MOVE_BUILD rows); zero breakdowns (the [17,40] refresh-cancel needed none - re-START and warp, no stall); all 14 STARTED warped, zero alternating-handoff evidence either way.

 2026-10-26 macro 063315: same schema (0 MOVE_BUILD rows); 1 Pylon breakdown ([90,107] f5550, one frame after the demote - linger, not variance); all 12 STARTED warped (both demotes re-armed), zero alternating-handoff evidence either way.

 2026-10-26 macro 064036: same schema (0 MOVE_BUILD rows); 1 Gateway breakdown ([84,108] f4860, 4f before the warp - recovery in flight, not variance); all 12 STARTED warped (demote re-armed after 158f), zero alternating-handoff evidence either way.

 2026-10-26 macro 064330: same schema (0 MOVE_BUILD rows); 1 Pylon breakdown ([90,107] f5190 travelling, self-recovered) + 1 Pylon refresh-cancel ([98,111] f6716, re-queued and warped); 12 warps from 13 STARTED rows, zero alternating-handoff evidence either way.

 2026-10-26 macro 065027: same schema (0 MOVE_BUILD rows); 5 Pylon breakdowns ([112,100] f7800 x3 + f7830 x2, travelling at unexplored tile - attempt-only, no construction; the B21 shape at the same tile, recovered via re-request), zero alternating-handoff evidence either way.

 2026-10-26 macro 065812: same schema (0 MOVE_BUILD rows); 1 Pylon breakdown ([95,106] f7770 travelling, recovered via warp - not the unexplored-site shape, builder in transit); all 13 STARTED warped, zero alternating-handoff evidence either way.

 2026-10-26 macro 071321: same schema (0 MOVE_BUILD rows); 4 breakdowns (Cyber post-demote linger + Gateway [92,107] x3 same-frame, travelling builders; the [112,100] attempt-only demote+cancel pair left no breakdown); 17 warps, zero alternating-handoff evidence either way.

 2026-10-26 macro 072350: same schema (0 MOVE_BUILD rows); 1 Pylon breakdown ([94,101] f7770 travelling, post-warp linger - same tile as B21's breakdown but AFTER the warp, not unexplored-site); 12 of 13 STARTED warped (Cyber cancelled, never re-warped); zero alternating-handoff evidence either way.

 2026-10-26 macro 074823: same schema (0 MOVE_BUILD rows); 2 Pylon breakdowns ([112,114] f6480 stoodStill 73f + [96,106] f8100 stoodStill 120f, travelling builders, both warped after per ledger - recovery via warp, no demote); all 12 STARTED warped; zero alternating-handoff evidence either way.

 2026-10-26 macro 075608: same schema (0 MOVE_BUILD rows); 2 Pylon breakdowns ([94,101] f3210 unexplored-site 2/4 + [98,111] f6810 travelling buildable 4/4, both warped after per ledger - recovery via warp, no demote); 13 of 14 STARTED warped (Pylon [79,114] unwarped at exit); zero alternating-handoff evidence either way.

 2026-10-26 macro 080452: same schema (0 MOVE_BUILD rows); 3 Pylon breakdowns same tile ([112,100] f7350/7380/7410, guard-post stall unexplored 0/4, cancelled f8188 not demoted - attempt cadence beat the 150f leg); all 18 STARTED warped (Nexus#2 warp placed, IN_PROGRESS at exit); zero alternating-handoff evidence either way.

 2026-10-26 macro 081145: same schema (0 MOVE_BUILD rows); 1 Pylon breakdown ([112,100] f7350 stoodStill 85f unexplored 0/4, cancelled f8236 below the 90f leg then resited+warped - recovery via resite, no demote); 13 of 13 STARTED warped; zero alternating-handoff evidence either way.

 2026-10-26 macro 081641: same schema (0 MOVE_BUILD rows); zero breakdowns (first zero-breakdown game since 062958's - the [112,100] streak ends: no attempt there at all this game); 14 of 14 STARTED warped; zero alternating-handoff evidence either way.

 2026-10-26 macro 081913: same schema (0 MOVE_BUILD rows); 2 Nexus breakdowns (first of type: [51,113] f10140/f10170 guard-post-class unexplored 11/12, no demote - distance gate 49.2 + attempts; recovery via warp 461f later, Nexus completed per roster); 18 of 18 STARTED warped (Forge#2 warp placed, IN_PROGRESS at exit); zero alternating-handoff evidence either way.

 2026-10-26 macro 082330: same schema (0 MOVE_BUILD rows); zero breakdowns (second straight zero-breakdown game); 14 of 15 STARTED warped (Cyber silent stall, no recovery twin); zero alternating-handoff evidence either way.

 2026-10-26 macro 082738: same schema (0 MOVE_BUILD rows); 1 Pylon breakdown ([79,121] f6150 stoodStill 101f, 11f after the demote twin - linger class, recovery via warp 4f later); 12 of 12 STARTED warped; zero alternating-handoff evidence either way.

 2026-10-26 macro 082947: same schema (0 MOVE_BUILD rows); zero breakdowns (second zero-breakdown game in a row after B42 - the [112,100] site churns via refresh-cancel instead); 13 of 13 STARTED warped; zero alternating-handoff evidence either way.

 2026-10-26 macro 083202: same schema (0 MOVE_BUILD rows); 2 Pylon breakdowns ([89,125] f6780 76f harvesting-builder + [95,114] f7170 136f, both pre-start attempt stalls, builder 8-10 tiles out - no demote per the distance leg; recovery via resite+warp); 12 of 14 STARTED warped ([79,121] exit-cutoff); zero alternating-handoff evidence either way.

 2026-10-26 macro 083539: same schema (0 MOVE_BUILD rows); 12 breakdowns (Gateway [8,54] same-frame-as-cancel; Pylon [24,44] x9 unexplored 2/4 attempt-phase; Gateway [14,55] builder-on-site; Pylon [35,65] travelling; zero demotes; recovery via start/resite+warp); 16 of 16 STARTED warped; zero alternating-handoff evidence either way.

 2026-10-26 macro 084039: same schema (0 MOVE_BUILD rows); 1 Gateway breakdown ([14,51] f2040 stoodStill 165f, builder-on-site self-block class - no demote, warped same tile 4f later); 12 of 12 STARTED warped; zero alternating-handoff evidence either way.

 2026-10-26 demote-timing re-sweep (168 unique logs): 21 DEMOTE_STALLED total, 17x exactly stoodStillFor=90f + 4 overshoots (91/93/127/136f) - stillness leg dominates, overshoots consistent with the 150f-since-last-attempt leg holding fire (B39's 127f no-demote case: attempts every ~30f); total 21 matches the per-game live-demote numbering (21st = B36, nothing since through B42; B43 adds the 22nd - 5f re-arm (ties the record); B45 adds the 23rd - 15f re-arm; B46 adds the 24th (108f re-arm) and 25th (5f re-arm, ties the record); B49 adds the 26th - 5f re-arm (ties the record); B50 adds the 27th (cancelled same frame) and 28th (297f re-arm))
 2026-10-26 demote rule precision (code read, no behavior change): the builder leg is a conjunction - idle (not constructing/moving) + within 3.5 tiles + 90f stillness (ConstructionStatusChanger:336-370, 150f leg checked first at :346); B39's [112,100] case was double-covered (attempts reset the 150f leg AND the builder sat 19.8 tiles away on PlayerGuard, failing the distance gate).
 2026-10-26 demote hole characterization (code read + B44/B39 twins, no behavior change): a builder that is assigned, alive, but perpetually travelling/far falls through BOTH recovery legs - the gone-builder leg needs null/dead/non-worker (:354), the idle leg needs idle + within 3.5 tiles (:364-366), and attempts keep resetting the 150f leg (:346). Result: silent stall with zero twins and no re-arm (B44 Cyber 4340f, B39 [112,100]). Same family as the #150 frozen-warp case; the remedy side (progress-based demote) is already owner-side WO territory - this names the exact gap it must close.

 2026-10-26 macro 064641: same schema (0 MOVE_BUILD rows); 2 Forge breakdowns (first of type, recovery via warp, no demote); 17 warps from 18 STARTED rows (2 cancelled: Pylon site abandoned, Gateway siteless; 1 warp twin-less), zero alternating-handoff evidence either way.

 2026-10-26 macro 061711: same schema (0 MOVE_BUILD rows); 1 Pylon breakdown ([35,65], stranded by the supply-cap exit 5f after START - the 060458-Gateway stranded class, not variance); 12 of 13 STARTED warped, zero alternating-handoff evidence either way.

 2026-10-26 macro 060844: same schema (0 MOVE_BUILD rows); 6 breakdowns, all recovered-or-cancelled (Gateway x3 travelling, Pylon triple same-frame; the 291f stall warped without demote - dist gate, not a variance remainder), 13 of 17 STARTED warped, zero alternating-handoff evidence either way.

 2026-10-26 macro 003012: same schema (0 MOVE_BUILD rows); zero alternating-handoff evidence either way (2 Pylon breakdowns, travelling builders, self-recovered without demote).


 2026-10-26 macro 092241: same schema (0 MOVE_BUILD rows); 2 early Pylon breakdowns recovered ([94,101] f3210, [112,114] f8550) + Nexus pair f10350 + late Gateway exit-cutoff cluster (6 rows f11160-11670, stoodStill 82->222, unrecovered); demote-timing: 29th/30th live demotes, both exactly 90f (30 DEMOTE_STALLED total across 182 retained logs).
## §7 — #92 second-Gateway lag

 2026-10-23 macro 203305: same +1 lag, systematic (Gateway #2 sup 11 vs BO At 10, #3 sup 13 vs At 12, Pylon #5 sup 17, Assimilator sup 25) - no 3-min lateness, warped all.

2026-10-23 long macro 212818: identical +1 systematic lag (#2 sup 11, #3 sup 13, #5 sup 16, #8 sup 23, #11 sup 32) - still lateness-free.

2026-10-23 macro 224120: identical +1 systematic lag (Gateways #2/#3 sup 11/13 vs BO At 10/12, Pylon #5 sup 16, all warped) - lateness not reproduced.

2026-10-23 macro 225319: identical +1 lag again (Gateways #2/#3 sup 11/13, Pylon #5 sup 17, Pylon #11 sup 31 vs At 27, all warped) - lateness not reproduced.

2026-10-23 macro 231335: identical +1 lag again (Gateways #2/#3 sup 11/13, Pylon #5 sup 17, Pylon #11 sup 32 vs At 27, all warped) - lateness not reproduced.

2026-10-23 macro 235430: identical +1 lag again (Gateways #2/#3 sup 11/13, Pylon #5 sup 17, Pylon #11 sup 29 vs At 27, all warped) - lateness not reproduced.

2026-10-26 DRY_RUN macro 002228: identical +1 lag again (Gateways #2/#3 sup 11/13, Pylon #5 sup 17, all warped) - lateness not reproduced.

2026-10-26 macro 001819: identical +1 lag again (Gateways #2/#3 sup 11/13, Pylon #5 sup 17, Pylon #11 sup 29 vs At 27, all warped) - lateness not reproduced.

2026-10-26 macro 003419: identical +1 lag again (Gateways #2/#3 sup 11/13, Pylon #5 sup 17, Pylon #8 sup 23 vs At 21, all warped) - lateness not reproduced.

2026-10-26 macro 045503: Gateways #2/#3 ON PLAN for the first time (sup 10/12 vs At 10/12, ledger-verified) - Pylon #5 sup 16, Pylon #8 sup 20 vs At 21 (early); the systematic +1 lag broke on the top-spawn base. Lateness never reproduced anywhere.

2026-10-26 macro 045940: systematic +1 lag is BACK (Gateways #2/#3 sup 11/13, Pylon #5 sup 16 vs At 15, Pylon #8 sup 23 vs At 21 - the 045503 on-plan game was the one-off, not the turn; bottom spawn this game) - lateness not reproduced.

2026-10-26 macro 050409: systematic +1 lag again (Gateways #2/#3 sup 11/13, Pylon #5 sup 16 vs At 15, Pylon #8 sup 23 vs At 21, all warped) - lateness not reproduced.

2026-10-26 macro 051600: systematic +1 lag again (Gateways #2/#3 sup 11/13, Pylon #5 sup 17 vs At 15, Pylon #8 sup 22 vs At 21, all warped) - lateness not reproduced.

2026-10-26 macro 055715: systematic +1 lag again (Gateways #2/#3 sup 11/13, Pylon #5 sup 16 vs At 15, Pylon #8 sup 22 vs At 21, all warped; BO audit 3 off-plan, the same +1s) - lateness not reproduced.

2026-10-26 macro 060047: systematic +1 lag again (Gateways #2/#3 sup 11/13, Pylon #5 sup 17 vs At 15, Pylon #8 sup 22 vs At 21, all warped; BO audit 3 off-plan, the same +1s) - lateness not reproduced.

2026-10-26 macro 060458: systematic +1 lag again (Gateways #2/#3 sup 11/13, Pylon #5 sup 16 vs At 15, Pylon #8 sup 22 vs At 21; BO audit 3 off-plan, the same +1s) - lateness not reproduced.

2026-10-26 macro 061711: systematic +1 lag again (Gateways #2/#3 sup 11/13, Pylon #5 sup 16 vs At 15, Pylon #8 sup 23 vs At 21 (+2), Pylon #11 sup 29 vs At 27 (+2); BO audit 5 off-plan) - lateness not reproduced.

2026-10-26 macro 061958: systematic +1 lag again (Gateways #2/#3 sup 11/13, Pylon #5 sup 17 vs At 15, Pylon #8 sup 23 vs At 21 (+2), Pylon #11 sup 28 vs At 27 (+1), all warped; console audit printed P5->16/P11->29, ledger says 17/28 - ledger rules, first caught audit-number mismatch) - lateness not reproduced.

2026-10-26 macro 062612: systematic +1 lag again (Gateways #2/#3 sup 11/13, Pylon #5 sup 17 vs At 15, Pylon #8 sup 23 vs At 21 (+2), Pylon #11 sup 27 vs At 27 ON PLAN, all warped; console audit matches the ledger this time - the B24 mismatch stays a single caught instance) - lateness not reproduced.

2026-10-26 macro 062958: systematic +1 lag again (Gateways #2/#3 sup 11/13, Pylon #5 sup 16 vs At 15, Pylon #8 sup 22 vs At 21 (+1), Pylon #11 sup 29 vs At 27 (+2), all warped; console audit lists the same 4 mismatches and nothing false - second straight audit that checks out against the ledger after the single B24 mismatch) - lateness not reproduced.

2026-10-26 macro 063610 (ledger-only, no twin stream): systematic +1 lag (Gateways #2/#3 sup 11/13, Pylon #5 sup 17 vs At 15, Pylon #8 sup 22 vs At 21 (+1), all warped to exit) - and this kills the base-correlated on-plan idea: same [113,15] base as on-plan B25, opposite outcome (B22 at [113,15] also lagged 11/13). Score by base tile: [10,52] 1 on-plan (045503) vs 8 lagged; [113,15] 1 on-plan (B25) vs 2 lagged (B22, B29); [92,113] 0 vs 3. On-plan stays a rare exception (2/16 with verified ledger sups), base-independent as far as twins show.

2026-10-26 macro 063315: systematic +1 lag again (Gateways #2/#3 sup 11/13, Pylon #5 sup 16 vs At 15, Pylon #8 sup 22 vs At 21 (+1), Assimilator sup 26 vs At 23 (+3, first off-plan Assimilator on record), all warped; console audit lists the same 4 - matches the ledger) - lateness not reproduced.

2026-10-26 macro 064036: systematic +1 lag again (Gateways #2/#3 sup 11/13, Pylon #5 sup 16 vs At 15, Pylon #8 sup 22 vs At 21 (+1), Pylon #11 sup 32 vs At 27 (+5, largest P11 lag - B22 had +6 at 33), Assimilator sup 23 = At 23 ON PLAN, all warped; console audit lists the same 4 but prints P11 sup 29 vs ledger 32 - second caught audit-number mismatch after B24) - lateness not reproduced.

2026-10-26 macro 064330: systematic +1 lag again (Gateways #2/#3 sup 11/13, Pylon #5 sup 17 vs At 15, Pylon #8 sup 22 vs At 21 (+1), all warped; console audit 3 off-plan (GW#2, GW#3, P5) - ledger agrees, P8 +1 unflagged like B27/B30) - lateness not reproduced.

2026-10-26 macro 065027: systematic +1 lag again (Gateways #2/#3 sup 11/13, Pylon #5 sup 16 vs At 15, Pylon #8 sup 23 vs At 21 (+2), Pylon #11 sup 32 vs At 27 (+5), Assimilator sup 23 = At 23 ON PLAN, all warped; console audit lists the same 5 including Assimilator - matches the ledger) - lateness not reproduced.

2026-10-26 macro 065812: systematic +1 lag again (Gateways #2/#3 sup 11/13, Pylon #5 sup 17 vs At 15, Pylon #8 sup 22 vs At 21 (+1), Pylon #11 sup 28 vs At 27 (+1), all warped; console audit 3 off-plan (GW#2, GW#3, P5) - ledger agrees on those, P8/P11 +1s unflagged) - lateness not reproduced.

2026-10-26 macro 071321: systematic +1 lag mostly holds, with two early birds (Gateways #2/#3 sup 11/13, Pylon #5 sup 16 vs At 15 (+1), Pylon #8 sup 18 vs At 21 (-3, FIRST early P8 on record), Pylon #11 sup 27 vs At 27 ON PLAN, CyberneticsC sup 19 vs At 22 (-3, early), Assimilator sup 26 vs At 23 (+3, second off-plan Assimilator after B28), all warped; archived console audit lists the same 6 and matches the ledger on every number) - lateness not reproduced.

2026-10-26 macro 072350: systematic +1 lag (Gateways #2/#3 sup 11/13, Pylon #5 sup 16 vs At 15 (+1), Pylon #8 sup 22 vs At 21 (+1), Pylon #11 sup 26 vs At 27 (-1, early), all warped; console audit 3 off-plan (GW#2, GW#3, P5) - ledger agrees; CyberneticsC sup 22 vs At 22 (on plan, never warped - lock, not lag); no Assimilator at all this game (BO #10 never started - first Assimilator-less game) - lateness not reproduced.

2026-10-26 macro 074823: systematic +1 lag (Gateways #2/#3 sup 11/13, Pylon #5 sup 16 vs At 15 (+1), Pylon #8 sup 23 vs At 21 (+2), Pylon #11 sup 29 vs At 27 (+2); console audit 5 off-plan (GW#2, GW#3, P5, P8, P11) - matches the ledger on every number; CyberneticsC sup 22 vs At 22 ON PLAN, warped clean; Assimilator sup 23 vs At 23 ON PLAN, warped) - lateness not reproduced.

2026-10-26 macro 075608: systematic +1 lag (Gateways #2/#3 sup 11/13, Pylon #5 sup 17 vs At 15 (+2), Pylon #8 sup 22 vs At 21 (+1), Pylon #11 sup 28 vs At 27 (+1); console audit 3 off-plan (GW#2, GW#3, P5) - matches the ledger on every number; CyberneticsC sup 22 vs At 22 ON PLAN, warped clean; Assimilator sup 23 vs At 23 ON PLAN, warped) - lateness not reproduced.

2026-10-26 macro 080452: systematic +1 lag (Gateways #2/#3 sup 11/13, Pylon #5 sup 17 vs At 15 (+2), Pylon #8 sup 22 vs At 21 (+1), Pylon #11 sup 28 vs At 27 (+1); console audit 3 off-plan (GW#2, GW#3, P5) - matches the ledger on every number; CyberneticsC sup 22 vs At 22 ON PLAN, warped clean; Assimilator sup 23 vs At 23 ON PLAN, warped) - lateness not reproduced.

2026-10-26 macro 081145: systematic +1 lag (Gateways #2/#3 sup 11/13, Pylon #5 sup 17 vs At 15 (+2), Pylon #8 sup 22 vs At 21 (+1), Pylon #11 sup 29 vs At 27 (+2); console audit 4 off-plan (GW#2, GW#3, P5, P11) - matches the ledger on every number; CyberneticsC sup 22 vs At 22 ON PLAN, warped clean; Assimilator sup 23 vs At 23 ON PLAN, warped) - lateness not reproduced.

2026-10-26 macro 081641: systematic +1 lag (Gateways #2/#3 sup 11/13, Pylon #5 sup 17 vs At 15 (+2), Pylon #8 sup 22 vs At 21 (+1), Pylon #11 sup 28 vs At 27 (+1); console audit 3 off-plan (GW#2, GW#3, P5) - matches the ledger on every number; CyberneticsC sup 22 vs At 22 ON PLAN, warped clean; Assimilator sup 23 vs At 23 ON PLAN, warped) - lateness not reproduced.

2026-10-26 macro 081913: early-and-late mix (Gateways #2/#3 sup 11/13 (+1), Pylon #5 sup 16 vs At 15 (+1), Pylon #8 sup 18 vs At 21 (-3, second early P8 after B35), CyberneticsC sup 18 vs At 22 (-4, early, warped clean - second early Cyber after B35, no lock), Assimilator sup 26 vs At 23 (+3, third off-plan Assimilator after B28/B35), Pylon #11 sup 27 vs At 27 ON PLAN; console audit 6 off-plan - matches the ledger on every number) - lateness not reproduced.

2026-10-26 macro 082330: systematic +1 lag with early P8 (Gateways #2/#3 sup 11/13, Pylon #5 sup 17 vs At 15 (+2), Pylon #8 sup 20 vs At 21 (-1, early - on the audit list); Pylon #11 sup 27 vs At 27 ON PLAN; console audit 4 off-plan (GW#2, GW#3, P5, P8) - matches the ledger; CyberneticsC STARTED f4902 never warped (no audit row - unwarped); Assimilator never STARTED) - lateness not reproduced.

2026-10-26 macro 082738: systematic +1 lag (Gateways #2/#3 sup 11/13, Pylon #5 sup 16 vs At 15 (+1), Pylon #8 sup 23 vs At 21 (+2), Pylon #11 sup 32 vs At 27 (+5, latest Pylon #11 on record); CyberneticsC sup 23 vs At 22 (+1, on plan per audit, warped clean); Assimilator sup 23 vs At 23 ON PLAN, warped clean; console audit 5 off-plan (GW#2, GW#3, P5, P8, P11) - matches the ledger on every number) - lateness not reproduced.

2026-10-26 macro 082947: early-heavy mix (Gateways #2/#3 sup 11/13, Pylon #5 sup 16 vs At 15 (+1), CyberneticsC sup 17 vs At 22 (-5, earliest Cyber ever, warped clean), Pylon #8 sup 17 vs At 21 (-4, earliest P8 ever), Pylon #11 sup 30 vs At 27 (+3); Assimilator sup 23 vs At 23 ON PLAN (breaks the B35/B43 off-plan streak); console audit 6 off-plan (GW#2, GW#3, P5, Cyber, P8, P11) - matches the ledger on every number) - lateness not reproduced.

2026-10-26 macro 083202: systematic +1 lag (Gateways #2/#3 sup 11/13, Pylon #5 sup 16 vs At 15 (+1), Pylon #8 sup 22 vs At 21 (+1), Pylon #11 sup 28 vs At 27 (+1); CyberneticsC sup 22 vs At 22 ON PLAN, warped clean; Assimilator sup 23 vs At 23 ON PLAN, warped clean; console audit 3 off-plan (GW#2, GW#3, P5) - matches the ledger on every number) - lateness not reproduced.

2026-10-26 macro 064641: systematic +1 lag again (Gateways #2/#3 sup 11/13, Pylon #5 sup 17 vs At 15, Pylon #8 sup 23 vs At 21 (+2), Pylon #11 sup 29 vs At 27 (+2), Assimilator sup 23 = At 23 ON PLAN, all warped; console audit 5 off-plan - matches the ledger) - lateness not reproduced.

2026-10-26 macro 083539: systematic +1 lag (Gateways #2/#3 sup 11/13, Pylon #5 sup 16 vs At 15 (+1), Pylon #11 sup 25 vs At 27 (-2, early); CyberneticsC/Assimilator unwarped (no audit rows - gate-blocked/never queued); Pylon #8 sup 21 vs At 21 ON PLAN, warped; console audit 4 off-plan (GW#2, GW#3, P5, P11) - matches the ledger on every number) - lateness not reproduced.

2026-10-26 macro 084039: systematic +1 lag (Gateways #2/#3 sup 11/13, Pylon #5 sup 16 vs At 15 (+1), Pylon #8 sup 23 vs At 21 (+2), Pylon #11 sup 29 vs At 27 (+2); CyberneticsC sup 23 vs At 22 (+1, on plan per audit, warped clean); Assimilator sup 23 vs At 23 ON PLAN, warped clean; console audit 5 off-plan (GW#2, GW#3, P5, P8, P11) - matches the ledger on every number) - lateness not reproduced.

2026-10-26 macro 084359: systematic +1 lag (Gateways #2/#3 sup 11/13, Pylon #5 sup 17 vs At 15 (+2), Pylon #8 sup 22 vs At 21 (+1), Pylon #11 sup 28 vs At 27 (+1); CyberneticsC sup 22 vs At 22 ON PLAN, warped clean; Assimilator sup 23 vs At 23 ON PLAN, warped clean; console audit 3 off-plan (GW#2, GW#3, P5) - matches the ledger on every number) - lateness not reproduced.

2026-10-26 macro 062321 (ledger-only, no twin stream): Gateways #2/#3 ON PLAN (sup 10/12, ledger-verified) - second on-plan game after 045503, and like it a top spawn ([113,15]); Pylon #8 sup 20 vs At 21 (early, like 045503) but Pylon #5 sup 17 (+2, unlike 045503's 16). Spawn-correlated pattern idea (N=2, coincidence noted, no causal claim): on-plan Gateways + early P8 on top spawns, +1 lag everywhere else.

2026-10-26 macro 060844: systematic +1 lag again (Gateways #2/#3 sup 11/13, Pylon #5 sup 16 vs At 15, Pylon #8 sup 23 vs At 21 (+2, like 045940/050409), Pylon #11 sup 33 vs At 27 (+6 - largest lag on record); BO audit 5 off-plan, the same +1s plus the two big Pylon lags) - lateness not reproduced.

2026-10-26 macro 003012: identical +1 lag again (Gateways #2/#3 sup 11/13, Pylon #5 sup 17, all warped; only 3 BO mismatches, Pylon #8/#11 on plan for once) - lateness not reproduced.


2026-10-26 macro 092241: gateway +1 lag holds (GW#2/#3 sup 11/13, P5 sup 16 vs At 15) but P8 sup 21 ON PLAN (rare, 1 prior in chain) and Cyber sup 14 vs At 22 (-8, lowest on record, prior min 17); P11 sup 31 vs At 27 (+4); console audit 5 off-plan - lateness pattern holds for gateways only.
2026-10-26 macro 110209: gateway +1 lag holds (GW#2/#3 sup 11/13, P5 sup 17 vs At 15 (+2)) but P8 sup 19 vs At 21 (-2, early) and P11 sup 25 vs At 27 (-2, early); console audit 5 off-plan.
## §8 — #95 artefact re-checks

 Re-checked 2026-10-22 (later, 52 logs): still zero - the deadlock shape
 absent everywhere again.

 Re-checked 2026-10-23 (64 unique logs, latest symlink deduped): zero
 deadlock shape; single transient frame-3 BO-load CyberneticsC (300 @
 sup 10, the known load shape, not a stall).

 Re-checked 2026-10-23 (94 unique logs): still zero - same single
 known transient, no deadlock shape.

 Re-checked 2026-10-23 (95 unique logs, incl. 224120): still zero - same single known transient, no deadlock shape.

 Re-checked 2026-10-23 (97 unique logs, incl. 225319): still zero - same single known transient, no deadlock shape.

 Re-checked 2026-10-23 (110 unique logs, incl. 231335): still zero - same single known transient, no deadlock shape.

 Re-checked 2026-10-23 (116 unique logs, incl. 235430): still zero - same single known transient, no deadlock shape.

 Re-checked 2026-10-26 (141 unique logs, incl. 001819 + 002228): still zero - same single known transient, no deadlock shape.

 Re-checked 2026-10-26 (142 unique logs, incl. 003012): still zero - same single known transient, no deadlock shape.

 Re-checked 2026-10-26 (143 unique logs, incl. 003419): still zero - same single known transient, no deadlock shape.

 Re-checked 2026-10-26 (144 unique logs, incl. 045503): still zero - same single known transient, no deadlock shape.

 Re-checked 2026-10-26 (147 unique logs, incl. 045940 + 050409 + 051600): still zero - same single known transient, no deadlock shape.

 Re-checked 2026-10-26 (148 unique logs, incl. 055715): still zero - same single known transient, no deadlock shape.

 Re-checked 2026-10-26 (149 unique logs, incl. 060047): still zero - same single known transient, no deadlock shape.

 Re-checked 2026-10-26 (150 unique logs, incl. 060458): still zero - same single known transient, no deadlock shape.

 Re-checked 2026-10-26 (151 unique logs, incl. 060844): still zero - same single known transient, no deadlock shape.

 Re-checked 2026-10-26 (152 unique logs, incl. 061711): still zero - same single known transient, no deadlock shape.

 Re-checked 2026-10-26 (153 unique logs, incl. 061958): still zero - same single known transient, no deadlock shape.

 Re-checked 2026-10-26 (154 unique logs, incl. 062321): still zero - same single known transient, no deadlock shape (062321's 32 retained rows are startup-only).

 Re-checked 2026-10-26 (155 unique logs, incl. 062612): the startup transient reproduced byte-identical in a second game (201818 UMS duel + 062321 macro rows match field-for-field at frame 3: CyberneticsC, supply 10/18, minerals 250, reserved 300) - same harness-startup artifact across backends/maps, still no deadlock shape.

 Re-checked 2026-10-26 (156 unique logs, incl. 062958): still only the two byte-identical frame-3 startup rows - same artifact, no deadlock shape.

 Re-checked 2026-10-26 (157 unique logs, incl. 063315): still only the two byte-identical frame-3 startup rows - same artifact, no deadlock shape.

 Re-checked 2026-10-26 (158 unique logs, incl. 063610): 063610's 32 retained rows DO contain the frame-3 startup row (third byte-identical instance - found in the 064330 sweep) - same artifact, no deadlock shape.

 Re-checked 2026-10-26 (159 unique logs, incl. 064036): still only the two byte-identical frame-3 startup rows - same artifact, no deadlock shape.

 Re-checked 2026-10-26 (160 unique logs, incl. 064330): the frame-3 startup row reproduced a THIRD time byte-identical (201818 UMS + 062321 + 063610 macros, all frame 3, CyberneticsC, supply 10/18, minerals 250, reserved 300) - same harness-startup artifact, no deadlock shape.

 Re-checked 2026-10-26 (161 unique logs, incl. 064641): still only the three byte-identical frame-3 startup rows - same artifact, no deadlock shape.

 Re-checked 2026-10-26 (162 unique logs, incl. 065027): still only the three byte-identical frame-3 startup rows - same artifact, no deadlock shape.

 Re-checked 2026-10-26 (163 unique logs, incl. 065812): still only the three byte-identical frame-3 startup rows - same artifact, no deadlock shape.

 Re-checked 2026-10-26 (164 unique logs, incl. 071321): still only the three byte-identical frame-3 startup rows - same artifact, no deadlock shape.

 Re-checked 2026-10-26 (165 unique logs, incl. 072350): still only the three byte-identical frame-3 startup rows - same artifact, no deadlock shape.

 Re-checked 2026-10-26 (166 unique logs, incl. 074823): still only the three byte-identical frame-3 startup rows - same artifact, no deadlock shape.

 Re-checked 2026-10-26 (167 unique logs, incl. 075608): still only the three byte-identical frame-3 startup rows - same artifact, no deadlock shape.

 Re-checked 2026-10-26 (168 unique logs, incl. 080452): still only the three byte-identical frame-3 startup rows - same artifact, no deadlock shape.

 Re-checked 2026-10-26 (169 unique logs, incl. 080801): fourth instance of the 32-row artifact (Gateway tile [56,23]); 25/32 rows identical across all four files, 7 run-specific (5 frame-0 PREPARING coords + 2 frame-3 Gateway rows, tiles [39,36]/[22,61]/[56,22]/[56,23]) - same test-boot signature, no deadlock shape.

 Re-checked 2026-10-26 (170 unique logs, incl. 081145): B41 clean, still only the four artifact instances - same test-boot shape, no deadlock shape.

 Re-checked 2026-10-26 (171 unique logs, incl. 081641): still only the four artifact instances - same test-boot shape, no deadlock shape.

 Re-checked 2026-10-26 (172 unique logs, incl. 081913): still only the four artifact instances - same test-boot shape, no deadlock shape.

 Re-checked 2026-10-26 (173 unique logs, incl. 082330): still only the four artifact instances - same test-boot shape, no deadlock shape.

 Re-checked 2026-10-26 (174 unique logs, incl. 082738): still only the four artifact instances - same test-boot shape, no deadlock shape.

 Re-checked 2026-10-26 (175 unique logs, incl. 082947): still only the four artifact instances - same test-boot shape, no deadlock shape.

 Re-checked 2026-10-26 (176 unique logs, incl. 083202): still only the four artifact instances - same test-boot shape, no deadlock shape.

 Re-checked 2026-10-26 (177 unique logs, incl. 083539): still only the four artifact instances - same test-boot shape, no deadlock shape.

 Re-checked 2026-10-26 (178 unique logs, incl. 084039): still only the four artifact instances - same test-boot shape, no deadlock shape.

 Re-checked 2026-10-26 (179 unique logs, incl. 084359): still only the four artifact instances - same test-boot shape, no deadlock shape.


 Re-checked 2026-10-26 (182 unique logs, incl. 092241 + foreign 091345): no new artifact shape (both archives complete: verdict + roster + Total time present, full streams, no twin-stream loss).
 Re-checked 2026-10-26 (183 unique logs, incl. 110209): no new artifact shape (B53 archive complete: verdict + roster + Total time present, 4366 rows).
## §9 — #155 BUILD_REFUSED guards

 2026-10-23 macro 203305 (374 s, supply-cap exit): BUILD_REFUSED Pylon 5 + Gateway 3 (min-supply 4, earlier-reserve 2, funding 1, core-priority 1 re-fire); no new gate proven.

 2026-10-23 long macro 212818 (38 warps): re-fire across min-supply, earlier-reserve (incl. 28x PhotonC + 3x Pylon + 4x Nexus + Gateway/CyberneticsC/Assimilator singles), warping-link 8x PhotonC, unstarted-pylon 5x, pylon-cap 2x, core-priority 2x, funding 1x, Stargate prepare-site-overlap 7x. Still zero live fires: live-construction, power-not-funded, core-single-instance, pylon-overstock (stub-pinned only, unchanged).

 2026-10-23 macro 224120 (16 warps): re-fire only - unstarted-pylon 10x, min-supply 6x (Gateway/Pylon/CyberneticsC), core-priority 2x, funding 1x, earlier-reserve 5x (Pylon/CyberneticsC/Assimilator/Nexus/Forge singles); zero live fires unchanged (live-construction, power-not-funded, core-single-instance, pylon-overstock).

 2026-10-23 macro 231335 (12 warps): re-fire only - min-supply 6x, earlier-reserve 4x, funding 1x, core-priority 1x, unstarted-pylon 1x; zero live fires unchanged (same four stub-pinned).

 2026-10-23 macro 235430 (13 warps): re-fire only - min-supply 4x, core-priority 2x, funding 2x, earlier-reserve 1x, unstarted-pylon 1x; zero live fires unchanged (same four stub-pinned).

 2026-10-26 macro 001819 (15 warps): re-fire only - min-supply 4x, funding 2x, earlier-reserve 1x, core-priority 1x, pylon-cap 1x, unstarted-pylon 1x; zero live fires unchanged (same four stub-pinned).

 2026-10-26 DRY_RUN macro 002228 (16 warps): re-fire only - unstarted-pylon 4x, min-supply 4x, earlier-reserve 6x (Pylon/Gateway/CyberneticsC/Nexus singles), pylon-cap 2x, funding 1x, core-priority 1x; zero live fires unchanged (same four stub-pinned).

 2026-10-23 macro 225319 (14 warps): re-fire only - core-priority 4x, min-supply 5x, funding 3x, earlier-reserve 5x, pylon-cap 2x, unstarted-pylon 1x; zero live fires unchanged (same four stub-pinned).

 2026-10-26 macro 003419 (13 warps): re-fire only - min-supply 5x, core-priority 2x, earlier-reserve 2x, funding 2x, unstarted-pylon 1x; zero live fires unchanged (same four stub-pinned).

 2026-10-26 macro 045503 (12 warps): re-fire only - earlier-reserve 3x (all Gateway), unstarted-pylon 3x, min-supply 4x (Gateway x2/Pylon/CyberneticsC singles), core-priority 2x, funding 1x (Gateway); zero live fires unchanged (same four stub-pinned).

 2026-10-26 macro 045940 (13 warps): re-fire only - min-supply 5x (Gateway x2/Pylon x2/CyberneticsC), earlier-reserve 2x (Pylon/CyberneticsC singles), core-priority 2x, unstarted-pylon 2x, funding 1x (Gateway), pylon-cap 1x; zero live fires unchanged (same four stub-pinned).

 2026-10-26 macro 050409 (15 warps): re-fire only - earlier-reserve 4x (all Pylon), min-supply 5x (Gateway x2/Pylon x2/CyberneticsC), core-priority 2x, unstarted-pylon 2x, pylon-cap 2x, funding 1x (Gateway); zero live fires unchanged (same four stub-pinned).

 2026-10-26 macro 051600 (15 warps): re-fire only - min-supply 5x (Gateway x3/Pylon x2), earlier-reserve 4x (Gateway x2/Pylon/Forge singles), funding 1x (Gateway), core-priority 1x, unstarted-pylon 1x; zero live fires unchanged (same four stub-pinned).

 2026-10-26 macro 055715 (13 warps): re-fire only - min-supply 5x (Gateway x2/Pylon x2/CyberneticsC), funding 2x (Gateway/CyberneticsC singles), earlier-reserve 2x (Pylon/Gateway singles), core-priority 1x (Pylon), unstarted-pylon 1x (Pylon); zero live fires unchanged (same four stub-pinned).

 2026-10-26 macro 060047 (15 warps): re-fire only - min-supply 4x (Gateway x2/Pylon x2), earlier-reserve 2x (both Pylon), funding 1x (Gateway), core-priority 1x (Pylon), unstarted-pylon 1x (Pylon); zero live fires unchanged (same four stub-pinned).

 2026-10-26 macro 060458 (12 warps): re-fire only - min-supply 4x (Gateway x2/Pylon x2), funding 2x (Gateway/CyberneticsC singles), earlier-reserve 2x (Pylon/Gateway singles), core-priority 1x (Pylon), pylon-cap 1x (Pylon); zero live fires unchanged (same four stub-pinned).

 2026-10-26 macro 061711 (13 warps): re-fire only - min-supply 5x (Gateway x2/Pylon x2/CyberneticsC), core-priority 2x (Pylon), funding 1x (Gateway), earlier-reserve 1x (Pylon); zero live fires of the stub guards this game (no core-single-instance, no pylon-overstock - Cyber warped cleanly with no cancel, the B22 lock needed its zombie row).

 2026-10-26 macro 061958 (13 warps): re-fire only - min-supply 6x (Gateway x3/Pylon x2/CyberneticsC), core-priority 2x (Pylon), funding 2x (Gateway/CyberneticsC singles), earlier-reserve 1x (Pylon), pylon-cap 1x (Pylon), unstarted-pylon 1x (Pylon); zero live fires of the stub guards (no cancel, no zombie, no lock - second straight game consistent with the gate+cancel-specific story).

 2026-10-26 macro 062958 (14 warps): re-fire only - min-supply 5x (Gateway x2/Pylon x2/CyberneticsC), core-priority 2x (Pylon), pylon-cap 2x (Pylon), funding 1x (Gateway), earlier-reserve 1x (Pylon), unstarted-pylon 1x (Pylon); zero live fires of the stub guards (Gateway refresh-cancel re-requested cleanly, no lock - fourth straight clean game, B22 remains the lone lock).

 2026-10-26 macro 063315 (12 warps): re-fire only - min-supply 4x (Gateway x2/Pylon x2), funding 2x (Gateway/CyberneticsC singles), earlier-reserve 1x (Pylon), core-priority 1x (Pylon), pylon-cap 1x (Pylon), unstarted-pylon 1x (Pylon); zero live fires of the stub guards (no cancel at all, no zombie, no lock - fifth straight clean game).

 2026-10-26 macro 064036 (12 warps): re-fire only - min-supply 5x (Gateway x2/Pylon x2/CyberneticsC), core-priority 2x (Pylon), pylon-cap 2x (Pylon), funding 1x (Gateway), earlier-reserve 1x (Pylon), unstarted-pylon 1x (Pylon); zero live fires of the stub guards (no cancel, no zombie, no lock - sixth straight clean game).

 2026-10-26 macro 064330 (12 warps): re-fire only - min-supply 4x (Gateway x2/Pylon x2), funding 1x (Gateway), earlier-reserve 1x (Pylon), core-priority 1x (Pylon), unstarted-pylon 1x (Pylon); zero live fires of the stub guards (Pylon refresh-cancel re-queued cleanly, no lock - seventh straight clean game).

 2026-10-26 macro 065027 (17 warps): re-fire only - min-supply 7x (Gateway x2/Pylon x2/CyberneticsC x2/Nexus), earlier-reserve 3x (Pylon/CyberneticsC/Assimilator singles), core-priority 2x (Pylon), pylon-cap 2x (Pylon), funding 1x (Gateway), unstarted-pylon 3x (Pylon); zero live fires of the stub guards (Pylon refresh-cancel re-queued cleanly, no lock - eighth straight clean game).

 2026-10-26 macro 065812 (13 warps): re-fire only - min-supply 5x (Gateway x2/Pylon x2/CyberneticsC), core-priority 2x (Pylon), pylon-cap 2x (Pylon), funding 1x (Gateway), earlier-reserve 1x (Pylon), unstarted-pylon 1x (Pylon); zero live fires of the stub guards (no cancel, no zombie, no lock - ninth straight clean game).

 2026-10-26 macro 071321 (17 warps): re-fire only - min-supply 8x (Pylon x3/CyberneticsC x3/Gateway x2), earlier-reserve 5x (Gateway x3/Pylon/CyberneticsC singles), core-priority 2x (Pylon), funding 1x (Gateway), pylon-cap 1x (Pylon), unstarted-pylon 1x (Pylon); zero live fires of the stub guards (Pylon refresh-cancel re-queued cleanly, no lock - tenth straight clean game).

 2026-10-26 macro 072350 (12 warps): re-fire only EXCEPT the lock - core-single-instance 33x (CyberneticsC, terminal: re-request never warped, second occurrence after B22 with near-identical count) + min-supply 4x (Gateway x2/Pylon x2), core-priority 2x (Pylon), funding 2x (Gateway/CyberneticsC singles), earlier-reserve 1x (Pylon), pylon-cap 1x (Pylon), unstarted-pylon 1x (Pylon); the Cyber lock is now a twice-observed terminal shape - still no fix from here (owner territory: order-row retirement).

 2026-10-26 macro 074823 (12 warps): re-fire only - min-supply 6x (Gateway x2/Pylon x2/CyberneticsC x2), core-priority 2x (Pylon), funding 1x (Gateway), earlier-reserve 2x (Pylon/CyberneticsC singles), unstarted-pylon 1x (Pylon); zero live fires of the stub guards (same four stub-pinned).

 2026-10-26 macro 075608 (13 warps): re-fire only - min-supply 4x (Gateway x2/Pylon x2), unstarted-pylon 2x (Pylon), funding 2x (Gateway/CyberneticsC singles), earlier-reserve 1x (Pylon), core-priority 1x (Pylon), pylon-cap 1x (Pylon); zero live fires of the stub guards (same four stub-pinned).

 2026-10-26 macro 080452 (18 warps): re-fire only - unstarted-pylon 14x (Pylon - the [95,114]/[112,100] churn window), earlier-reserve 5x (Pylon x3/Nexus/Gateway singles), pylon-cap 3x (Pylon), min-supply 4x (Gateway x2/Pylon x2), core-priority 2x (Pylon), funding 2x (Gateway/CyberneticsC singles); zero live fires of the stub guards (same four stub-pinned).

 2026-10-26 macro 081145 (13 warps): re-fire only - min-supply 4x (Gateway x2/Pylon x2), funding 2x (Gateway/CyberneticsC singles), earlier-reserve 2x (Pylon/Gateway singles), core-priority 1x (Pylon), pylon-cap 1x (Pylon), unstarted-pylon 1x (Pylon); zero live fires of the stub guards (same four stub-pinned).

 2026-10-26 macro 081641 (14 warps): re-fire only - min-supply 4x (Gateway x2/Pylon x2), funding 2x (Gateway/CyberneticsC singles), earlier-reserve 1x (Pylon), core-priority 1x (Pylon), pylon-cap 1x (Pylon), unstarted-pylon 1x (Pylon); zero live fires of the stub guards (same four stub-pinned).

 2026-10-26 macro 081913 (18 warps): re-fire only - prepare-site-overlap 3x (Forge, first Forge instance - previously Stargate 19x/7x, guard proven) + earlier-reserve 10x (Gateway x4/CyberneticsC x2/Forge x2/Pylon/Nexus singles), min-supply 9x (CyberneticsC x4/Gateway x2/Pylon x3), unstarted-pylon 6x (Pylon), pylon-cap 4x (Pylon), funding 1x (Gateway), pylon-overstock 1x (Pylon, benign), core-priority 1x (Pylon); zero live fires unchanged (same four stub-pinned); zero core-single-instance.

 2026-10-26 macro 082330 (14 warps): re-fire only - earlier-reserve 12x (Gateway x8/Pylon x3/CyberneticsC single - rush economy), pylon-cap 4x (Pylon), unstarted-pylon 4x (Pylon), min-supply 7x (Pylon x3/Gateway x2/CyberneticsC x2), core-priority 3x (Pylon), funding 1x (Gateway); zero core-single-instance.

 2026-10-26 macro 082738 (12 warps): re-fire only - min-supply 6x (Gateway x2/Pylon x2/CyberneticsC x2), core-priority 2x (Pylon), unstarted-pylon 2x (Pylon), funding 1x (Gateway), earlier-reserve 3x (Pylon/CyberneticsC/Gateway singles); zero live fires of the stub guards (same four stub-pinned).

 2026-10-26 macro 082947 (13 warps): re-fire only - min-supply 10x (Pylon x4/CyberneticsC x4/Gateway x2), core-priority 2x (Pylon), pylon-overstock 2x (Pylon, benign), unstarted-pylon 2x (Pylon), funding 1x (Gateway), earlier-reserve 3x (Pylon/CyberneticsC/Gateway singles), pylon-cap 1x (Pylon); zero core-single-instance.

 2026-10-26 macro 083202 (12 warps): re-fire only - min-supply 4x (Gateway x2/Pylon x2), earlier-reserve 3x (Pylon x2/Gateway single), unstarted-pylon 2x (Pylon), funding 1x (Gateway), core-priority 1x (Pylon), pylon-cap 1x (Pylon); zero live fires of the stub guards (same four stub-pinned).

 2026-10-26 macro 083539 (16 warps): re-fire only - earlier-reserve 22x (Gateway x12/Pylon x8/CyberneticsC/Nexus singles - dynamic-heavy top-spawn game), prepare-site-overlap 13x (CyberneticsC - gate-blocked, never STARTED; third live instance after Oct-21 macro 19x + 212818 7x Stargate, first Cyber instance), pylon-cap 10x (Pylon), unstarted-pylon 7x (Pylon), min-supply 5x (Gateway x2/Pylon x2/CyberneticsC), core-priority 3x (Pylon), funding 3x (Gateway); zero live fires of the stub guards (same four stub-pinned); zero core-single-instance.

 2026-10-26 macro 084039 (12 warps): re-fire only - min-supply 6x (Gateway x3/Pylon x2/CyberneticsC), earlier-reserve 5x (Pylon x3/Gateway x2), core-priority 2x (Pylon), unstarted-pylon 2x (Pylon), funding 1x (Gateway); zero live fires of the stub guards (same four stub-pinned).

 2026-10-26 macro 084359 (15 warps): re-fire only - min-supply 5x (Gateway x3/Pylon x2), funding 1x (Gateway), earlier-reserve 1x (Pylon), core-priority 1x (Pylon), unstarted-pylon 1x (Pylon); zero live fires of the stub guards (same four stub-pinned); zero core-single-instance.

 2026-10-26 macro 064641 (17 warps): re-fire only - min-supply 4x (Gateway x2/Pylon x2), core-priority 2x (Pylon), funding 2x (Gateway/CyberneticsC singles), earlier-reserve 3x (Pylon/Gateway/Nexus singles), pylon-cap 1x (Pylon), unstarted-pylon 24x (Pylon - new dominant, long-game Pylon rows re-fire while unstarted; all 5 Pylons warped anyway); zero live fires of the stub guards (Pylon refresh-cancel, no lock - eighth straight clean game).

 2026-10-26 macro 062612 (12 warps): re-fire only - min-supply 5x (Gateway x3/Pylon x2), core-priority 4x (Pylon), funding 3x (all Gateway), earlier-reserve 4x (Gateway x3/Pylon), unstarted-pylon 1x (Pylon); zero live fires of the stub guards (one Gateway refresh-cancel, re-request warped cleanly - third straight game with no lock, the zombie-row story holds).

 2026-10-26 macro 060844 (13 warps): NOT re-fire only anymore - two of the four stub-pinned guards fired live: core-single-instance 34x (CyberneticsC, terminal mutual lock with the zombie row, no warp all game) + pylon-overstock 6x (Pylon, benign - all 5 Pylons warped anyway); other refusals re-fire only - min-supply 5x (Gateway x2/Pylon x2/CyberneticsC), earlier-reserve 2x (Pylon/Gateway singles), pylon-cap 4x, funding 2x (Gateway/CyberneticsC singles), core-priority 1x (Pylon), unstarted-pylon 1x (Pylon). The "zero live fires" standing line ends here.

 2026-10-26 macro 003012 (18 warps): re-fire only - unstarted-pylon 6x, min-supply 4x, earlier-reserve 4x (Pylon/Gateway/Nexus/Forge singles), pylon-cap 2x, funding 2x, core-priority 1x; zero live fires unchanged (same four stub-pinned).


 2026-10-26 macro 092241 (13 warps): re-fire only - unstarted-pylon 8x, pylon-overstock 6x, below-min-supply-and-poor 11x (Pylon 5/CyberneticsC 4/Gateway 2), earlier-reserve 7x (Gateway 3/CyberneticsC 2/Pylon 1/Nexus 1), core-priority 2x, funding 1x; zero live fires, no new guard shapes vs the chain.
 2026-10-26 macro 110209 (14 warps): re-fire only - CyberneticsC core-single-instance 50x + below-min-supply 12x + earlier-reserve 3x, Pylon pylon-overstock 17x, below-min-supply 8x, earlier-reserve 1x, pylon-cap 2x, core-priority 1x, unstarted-pylon 1x, Gateway below-min-supply 2x + funding 1x; zero live fires, no new guard shapes vs the chain.
## §10 — game-state snapshot 2026-10-21

- Wine `1Drag_v_1Zeal.scm` (UMS micro): baseless ATTACK now marches
  from the army center (was: idle to the wall kill); pinch retreat
  (<80 hp + 2+ melee in 2 tiles) yields instead of standing into
  the pocket; tuner mutates all CombatTuning knobs incl. pinch;
  default unknown-matchup BO is Zealot into Goon (was Forge FE).
  Latest duel: 1/0 WIN in 499 frames, 34 stepped-aside vs 9 stuck,
  shot cadence median 33. Pinch A/B on `Drag_v_LingsUpg` (N=5+5,
  2026-10-21): pinch OFF loses ~half (avg 3.2 vs 6.0 over 10 games;
  fixed-defaults arms 2.0 vs 4.0) - yielding to faster lings on
  contact breaks the line; standing trades better. Default stays ON
  (1v1 never triggers it, spread shapes proven in sim only - #159
  closed with the verdict + wired gate, default call parked
  owner-side). Tuner batch:
  10/10 wins (NEXT #152).
- Wine `(3)TauCross` macro vs Zerg (2026-10-21, BUILDER_LOG=1,
  fresh jar): 9.7 game-min Defeat by Hydras. Opening healthy
  (Gateway 1:27, 3x by 4:11, Core 3:00, Dragoon 4:06, 8 Pylons,
  Forge 6:42, FE 6:59, PhotonC 8:45, Robo 8:48); lost 3/23, army
  wiped ~8:45-9:30. BOARD_FED x2 (sustained rows, pumps unjudged);
  zero BUILD_REFUSED; one BUILD_BREAKDOWN open (Gateway [123,26],
  stood still 74f, never warped). #71 shape missed (2 bases).
- OpenBW UMS (`M&M_v_ZealDrag`): PASS 5/2 KILLED/LOST on the rebuilt
  engine - the working OpenBW proof vehicle. `1Drag_v_1Zeal` seats
  the bot in a unit-less human slot (verified slot-pick code);
  whether the enemy Zealot even spawns there is unproven either way
  (mixed evidence: bot got a preplaced Dragoon on 12:59, nothing on
  10-19). 2026-10-20 probe names the stall: main blocked in
  `waitForServerData:90` read <- `Client.connect:181` (dump in run
  `162410`; server PUBLISHED, client "Connected", never
  "Connection successful"). 2026-10-20 handshake fixed both sides
  (their pump + our units guard: 2/2 `Connection successful`);
  the match itself never ticks on UMS (no frames, no MatchStart -
  engine-side). 2026-10-21: SUPERSEDED - with the ticking match the
  whole chain works (run `183729`: HELLO, 3:00 fight, 82k rows).
  Wine stays the duel vehicle until the UMS verdict quality
  (W/L past the 30 s cap) lands.
- OpenBW `TauCross` (`113933` long, FE forced reverted probe #2,
  12493f/8:44, exit 143 wall-cap - inconclusive as verdict, rich as
  data): FE OPENS (Forge 1:25, Core, 45 warps, Dragoons 10) - the
  `112337` "jam at Forge" was the disconnect, not the logic. BOARD
  top `wall-in` once (5:14); sited exact pieces after: PhotonC
  [19,19]/[18,22] (6:08/6:47), Pylon [12,46] (8:11, wall power),
  PhotonC [14,43] (8:42); NO At-0 Gateway plug warped. Geometry
  twin LANDED (WALL_ANCHOR_WARP: warp tile vs anchor row + exact
  planned tile, pinned WallAnchorWarpTwinTest 5/5) - read on the next
  FE game; survival unprovable on
  OpenBW (#94). Twin stream intact to the cap (253 rows past the
  last 720-frame interval - shutdown flush verified live).
  prepare-site-overlap 0 (no overlap case). Watcher quiet (no
  false-positive jstack - the EVENTS watch verified live).
- OpenBW `TauCross` (`112337` long, FE strategy forced as a reverted
  probe - `Forge FE vZ` vs Zerg, 138 s wall, exit 143 wall-cap):
  the run ended by CLIENT DISCONNECT (~1:28 game time:
  `Reconnecting...`, then `Stopped (no game end reached) after 95
  seconds`), not by a proven opening jam. Pylon #1 warped 1:07@8
  [50,106]; Forge (oid=2, minSupply 11) decided once at 1:28
  (`prev=cid41:PREPARING path=adopt`, frame 2120) with zero twin
  rows after - but the twin stream itself stops at frame 1558:
  EventLog buffers 720 frames between flushes and the kill dropped
  the tail (shutdown hook now flushes, so the next capped run keeps
  it). Construction ids burned 1->41 for 1 flushed PREPARING -
  the burn is real (in-memory counter) but the missing rows are
  buffer loss, not proof of silent unlinks; the `prepare-site-overlap`
  twin stands as instrumentation (stub-pinned), undecided live.
  Forge fate unknown; wall-in unreached. The disconnect shape matches
  the known host-death class (contrast #149), not a new item.
- OpenBW `TauCross` (`105921` long, legacy default, 16546f/11:34,
  exit 143 wall-cap - inconclusive as verdict, rich as data): Pylon
  0:52@8 (control holds for #84), Gateway 1:24/1:53, Core 3:02,
  Dragoons 12 from 4:12, Nexus=2, RoboticsF 11:28; BOARD_FED 1
  (sustained-dragoon 5:16, rescue not routine); #71 correctly silent
  (2 bases, ORDER_SAVING 0, end bank 288); hang-watcher jstacked a
  40 s main-thread spin in Cache/Selection at 11:00:03, recovered
  (#153). Retry `110411` same day: same shape (16063f/11:14,
  exit 143 wall-cap, Nexus=2, ORDER_SAVING 0, Pylon 0:52@8,
  BOARD_FED 1, watcher again same signature) - #71 stays lottery
  (the bot expands on its own; no forced 1-base shape, no more
  retries until the shape is forced or luck lands).
- OpenBW `TauCross` (`203655`, PRODUCTION_V2=LIVE with rule+lag+>60,
  21933f/15 min): Dragoons 8, first warps 5:25 - the ASAP rule fires
  live (was 0 in the pre-rule LIVE game). Pylons 7 with one 150f
  pair, both tiles named by CLOSE_WARPS; twin + blocker-construction
  rows fire as designed. CANCEL 691 still dominated by the Nexus loop
  (C14 untouched).
- OpenBW `TauCross` (`194718`, PRODUCTION_V2=LIVE with the PurpleWave
  rule, 21905f/15 min): 5 Pylons, Dragoons 4 (floor not frozen this
  game), suppression engages live (blocker-construction twins,
  coming=2 stops demand once two travel). Residual pairs come from a
  per-frame mint-cancel stream on covered V2-sited tiles (28 fresh
  Pylon plans in 300f, 51 proximity cancels) - doomed sites die
  same-frame so no coming-count sees them (WO-146 siting +
  WO-147 in-flight). Unstarted pile-up: zero instances.
- OpenBW `TauCross` (`191444`, PRODUCTION_V2=LIVE, 23026f/16 min,
  wall-cap 143): tech on time (Forge 1:50, Core 4:12, gas 3:54),
  6 Pylons with no supply block and 97 SUPPLY_BLOCKED samples - but
  the migration is NOT ready: 609 Nexus cancel/re-request cycles
  (expansion siting C14, Nexus=2 never), 0 Dragoons (floor freeze),
  Pylon pairs 89f/72f apart (now attributable: V2_GOAL_SOURCE
  names the emitting list per provider goal, V2_DISPATCH rows every
  issued/refused plan item - both LANDED stub-pinned, read on the next
  live run), 18 warps vs ~40 legacy. Blockers, in order: C14,
  Dragoons, parity (#45 ordering stands).
- OpenBW `TauCross` (`185357`, PLACEMENT=catalogue, 9614f, ended
  environmentally like the clean run above): catalogue answered 6
  (2 Pylon + 4 Gateway), declined 29 named (no-builder 20,
  dedicated-finder 2, legacy-refused 7), 6 REQUESTED vs 1 CANCEL,
  25 warps; 1 residual Can't-find-place (Gateway #86 At 44, known
  late-dynamic class, NEW class: none). WO-145 parity direction
  holds; flip waits on Gateway-arm coverage. 2026-10-20: adapter
  verified wired in both funnels (sited via RequestBuildingNear:132,
  queue via APositionFinder:47, same decline→legacy contract);
  Gateway-arm parity still needs a SiteTwin-read game.
- OpenBW `TauCross` (`185237` DRY_RUN short): V2 plans the opening
  provider 16x at stable (112,8) while legacy warps [113,15] -
  demand parity in the window, tile a few tiles off (known siting
  gap). Late: V2 wants ZERO Pylons past frame 5000 while legacy
  warps 3 - suppression against in-flight providers or a demand
  miss; the LIVE run (WO-144 step 3) decides.
- OpenBW `TauCross` (9149f, 6.4 game-min, engine VICTORY at Killed:1 -
  environmental end, rich datum): 14 BUILD rows, every order exactly
  one row, each naming its cause (`prev=cid:PREPARING path=adopt` vs
  `prev=link-null path=new` + oid join key). Zero same-tile storms,
  4 REQUESTED vs 2 benign orphan-drop cancels, roster Nexus=1
  Probe=26 Pylon=7 Gateway=3 Dragoon=2. The DECISIONS readability bar
  holds: one demand, one row. New doors live-dormant this game
  (standing-cover 0 fires, live-construction 0 fires), no regression
  in the refusal profile.

- Wine `TauCross` LONG (2026-10-20, 10.8 game-min, 7/29 loss to Garm
  Brood, formation Phase A live in the jar): FIGHT 301 / FLEE 82,
  KITE 140 + HIT 409 + SHOT 7871 flowing - the cluster fought, no
  freeze, no crash. Loss driver is production, not combat: Forge
  6:58 (late, no-forge 230 TECH_CHECKs), NO Core all game at bank
  3750 (the single-instance mutual lock, fixed same day - see the
  #155 note below), zero Dragoons, Nexus#3 10:28. Pylon#1 healthy
  (0:47@8, #84 datum); Gateways #2/#3 on time 1:26/1:51 (#92 datum,
  no Wine lateness this game); ORDER_SAVING 0 (2-3 bases, #71 wants
  a 1-base 700 s shape - 649 s reached, just short; FE long game
  the same day reached 707 s but with 3 bases + bank 3750, so
  correctly silent again - the shape, not the duration, is missing;
  legacy long attempt the same day died on attach ("No server proc
  ID", IDE build running 30+ min on the box - killed as
  inconclusive, never a verdict). WINE_LOG /
  WINE_DEBUG / WINE_DECISIONS triple + BUILDINGS.json collected
  (#96 file-outputs half verified; painter labels stay visual).
  Open question: SUPPLY_BLOCKED twin silent through the 90/90
  blocks (twin gap?). ANSWERED 2026-10-21 and fixed the same day:
  the twin lived in the V2 snapshot only, so OFF games (144848: 286
  near-90/90 rows, 162311: 269, both zero twin rows) never emitted it
  while the V2 game in the same window filed 111 rows. Same rows now
  from a shared `SupplyBlockedTwin` (V2 delegates, OFF games sample
  through ProductionOrdersCommander; DRY_RUN/LIVE untouched),
  stub-pinned `SupplyBlockedTwinTest` 2/2. Fires from the next OFF
  game; the sample already distinguishes coming==0 (demand-side) from
  named blockers.

- Wine `TauCross` LONG FE (2026-10-20, 11.7 game-min, 9/42 loss,
  FE-forced + reverted right after): Forge 2:07, PhotonC sited at
  anchor+36px and warped 2:36, Core 4:03 (gate fix holds - ZERO
  core-single-instance refusals all game), Dragoon 5:59 x14,
  Nexus#2 only 6:58, RoboticsF 10:45. Singularity researched 10:07
  (#85: the post-Forge window EXISTS and completes - 64
  bank-700-or-3rd + 216 weapons-l1 + 152 combat-10 TECH rows).
  707 s reached but 3 bases + bank 3750 at death, so ORDER_SAVING
  correctly silent again (#71 still wants the 1-base shape).
  BUILD_REFUSED guards healthy (earlier-reserve 14, min-supply 2,
  unstarted-pylon 1 - #155 per-gate evidence). Loss: zealot line
  wiped ~8:30, dragoons ~10:30, bank hoarded unspent - same thin
  shape, now WITH tech. Per-run archive works (out/wine_runs/+latest).

- OpenBW `TauCross` LONG (16575f, 11.5 game-min, wall-cap exit 143 -
  inconclusive as verdict, rich as data): roster Nexus=2 Probe=44
  Pylon=15 Gateway=7 Dragoon=10 Forge=1 PhotonC=1 RoboticsF=1. 57
  BUILD rows, every legacy-path order exactly one row (new oid+context
  suffix live: adopts name their cid, re-requests name link-null).
  No same-tile REQUESTED+CANCEL storm; new standing-cover refusal 0
  fires (spiral offered no covered tile). live-construction refusals 0
  (guard dormant, no regression). Open doors, named: sited PhotonC
  21x at [115,15], two PREPARING Pylon adopts into cover (#151).

- Wine `TauCross` SHORT (exit at supply 51 via `ForceExitAtSupply`,
  clean end, ~6 min game-s: 26 ledger rows, BUILDER_LOG=1, full new
  code incl. STARTING + power-not-funded): peak free 26. Per-author
  triggers hold (every Pylon ordered at free <= 6), but same-need
  duplicates land together anyway: BO#8 + dynamic #26 same frame
  (At 21), dynamic #67 + #68 130 frames apart (both At 23), each pair
  warping into one supply block. The queue-door spam cap (refuse above
  12 free) is the backstop, not the cure - single author (WO-144) is.
  STARTING twinned live (66 rows, builder travelling to site).
  power-not-funded never fired (Pylon#1 up before gateways needed
  builders). 1 kill, no real fight yet - engage/cluster await contact.

- Wine `TauCross` SHORT, first run (exit at supply 51 via `ForceExitAtSupply`,
  clean end, 364 game-s: Nexus=1 Probe=25 Gateway=3 Dragoon=4 Zealot=7
  Pylon=6 CyberneticsC=1 Assimilator=1, 24 ledger rows, BUILDER_LOG=1):
  supply-cap doctrine HELD that game - peak free 14 all game (was 27),
  4 BO + 2 single dynamic Pylons, zero same-need duplicates,
  SUPPLY_BLOCKED 0 (no starvation). Same-builder adoption live
  (Pylon#1: reserved #22 at pick 0:13, builder 22 at start 0:46).
  Gateways #2/#3 on time (1:20/1:42), one dynamic #24. 0/0 kills.
  Variance vs the second run above is the finding: the cap bounds the
  trigger, not the outcome - same-frame pairs and lag stacking decide
  the peak, which is why the door is a backstop and WO-144 the cure.

- Wine `TauCross` (910 s, vs real Zerg Grendel Brood, 21/51 kills -4175:
  Nexus=2 Probe=36 Dragoon=2 Pylon=9 Gateway=5 RoboticsF=1, 53 ledger
  rows, BUILDER_LOG=1, feasibly the first game on the cid/oid twins +
  supply cap + engage fix): ENGAGEMENT PROVEN - 21 army kills with zero
  static defense (FIGHT 283 + FLEE 300 CLUSTER rows, latch both ways;
  winning proof still pending, -4175). Supply cap partial: per-author
  triggers hold, but same-need duplicates persist (Pylon #50/#51,
  #67/#68, #101/#102; gateway triple at 49) - mid-game peak free 26
  (WO-144). Pylon#1 healthy (builder#5 at pick 0:13 → STARTED 0:46 →
  warp 0:58, #84 datum). Gateways #2/#3 on time 1:24/1:51 (#92 pattern
  not reproduced). Bank peak 689, 2 bases, ORDER_SAVING 0.

- Long `20261018_143050` (155 s wall, 657 game-s, legacy rich on the
  FE-gated tree: Nexus=2 Probe=52 Pylon=13 Gateway=6 Dragoon=18 Forge=1
  PhotonC=5, 67 ledger rows): first long game after the wall-in FE
  gate - healthy, no regression. wall-in in 0/10 BOARD rows, zero
  `At 0 Gateway` wall orders (gate holds for 11 game-minutes, not just
  the short tier). Forge warped 4:48@63, post-Forge bank max 559 < 700
  so research correctly silent (#85 still pending its window).
  Adhoc gate missed on Nexus>=3 only (expansion timing, not a defect).
  `ORDER_SAVING` 0 (2 bases - correctly silent, #71 wants 1).

- Long `20261018_144015` (155 s wall, 677 game-s, legacy rich on
  Python 1.3, first non-TauCross long: Nexus=2 Probe=51 Pylon=13
  Gateway=6 Dragoon=13 Zealot=2 Forge=1 Stargate=1 + Corsair,
  RoboticsF, PhotonC, 65 ledger rows): map-independence holds, FE
  gate holds (wall-in 0/10 BOARD). Forge 3:58@63, post-Forge bank 784
  at 4:42 - the #85 window EXISTS here: Singularity researched 7:53
  (started 6:06, engineRes+staticRes true; UPGRADE_COMPLETED twin
  never fired - twin gap, not engine - WIRED 2026-10-19: the
  observation sat in handle() behind the gate, a researched upgrade
  refuses so handle() never saw the rise; note() now runs in the base
  applies() (Armor wired in its own, Storm is a tech and stays out),
  pinned by UpgradeCompletedBehindGateTest 1/1, single-level and
  max-level rises both fire). Weapons TECH_CHECK still false
  at 772/306 bank (refusing gate unnamed in the twin - the open half
  of #85). 2 `Can't find place` (Gateway #87/#88 8:27, unpowered
  dynamic rows - known class, #146). GAME_END present, cap exit (#149
  no occurrence). `ORDER_SAVING` 0 on 2 bases (#71 wants 1).

- Long `20261017_213248` (79 s wall, 590 game-s, legacy rich: Nexus=2
  Probe=34 Pylon=9 Gateway=9 Zealot=10 Dragoon=10 Assimilator=2 Core=1
  Forge=1): Forge full-twin loop live - BUILD #123 at 8:29
  (supply 58, bank 483) → PREPARING [116,19] → BUILDING_STARTED →
  ledger warp 8:34 → roster Forge=1, zero silent steps. `GAME_END`
  first fire (won=false, 14058, 78/90, bank 453/308). Game ended 9:50
  unnamed (no force/leave/GG; #149 pattern, now WITH state).
  Post-Forge bank max 641 < 700 → research correctly silent (#85
  still pending a post-Forge 700-bank/3rd-base window). `BUILD_REFUSED`
  2740x (Gateway warping-link 762 new live, Pylon cap 1021,
  core-priority 796). `E2E_DECISIONS` clean (339 rows, maxframe ==
  game). 0/0 kills (#94).

- Long `20261017_210732` (46 s wall, 308 game-s, legacy: Nexus=1 Probe=24
  Zealot=4 Pylon=6 Gateway=2 Dragoon=1 Assimilator=1 Core=1): FIRST CLEAN
  `E2E_DECISIONS` (11 rows, maxframe matches the game - the pre-run rm
  fix verified). `BUILD_REFUSED` fired 1087x, all named-correct: Pylon
  poverty 204, pylon-cap 155, core-priority 682 (the Core landed - the
  priority rule works, documented live); Gateway poverty 46. Core BO#9
  waited 14 NOT_READY then warped. Gateway [49,107] engine-refused x2 +
  null-position x5 (loud, not a silent pile-up). Game ended 5:08 by
  unnamed cause (no force/leave/GG line; exit-path jstack fired post-game
  per the O3 pattern) - second occurrence of the `131407` shape, filed
  as #149. TECH_CHECK 126x all false (no Forge - #85 still pending).
  0/0 kills (#94).

- Long V2 LIVE `20261017_202456` (139 s wall, 1055 game-s, Protoss:
  Zealot=7 Probe=25 Forge=1 Pylon=6 Core=1 Assimilator=1 Nexus=1
  Gateway=2): exit 143 is the wall cap. 18 warps (11 buildings + 7
  Zealots, zero Dragoons). Tech coverage closed vs `131515`: Forge
  2:59@15, Assimilator 4:08@27 on the exact geyser tile (V2 gas order
  live fire), Core 4:35@31 - the frame-2 `Not powered x523/523`
  decline resolved once Pylons lit space. No Dragoon transition and no
  second Nexus in 17.6 min - remaining thinness drivers. Legacy pumps
  provably out (1 GAS_FIX, no DRAGOON/BOARD/SINGULARITY rows);
  `BUILD_REFUSED` 0 (V2 delegates builders past the twinned legacy
  door). NOTE: this run's `E2E_DECISIONS` is stale pre-fix accumulation
  (identical 12223 rows) - read V2 from EVENTS/ledger/roster only.
- Long `20261017_200123` (138 s wall, 944 game-s, 1 base: Nexus=1 Probe=22
  Pylon=7 Gateway=4 Dragoon=3 Zealot=6, single Core, NO Forge): exit 143
  is the wall cap (game healthy at the cut). 23 ledger rows (unit rows
  log train issues, not completions - the 4th Dragoon was still
  training at the cut, hence 3 standing). TECH_CHECK 602 rows all false with bank peak 8883
  (no Forge - the gate is correctly silent at any bank); ORDER_SAVING 0
  (1 base but bank >> 550 - correctly silent); scout idle 0, proximity
  twins flowing. 0/0 kills (enemy never spawns, #94).
- Long `20261017_091624` (883 s, 55-unit cluster, no enemy spawned):
  Phase-3 movement proof - DEFEND→ATTACK at 8 army, anchors main →
  natural → enemy corner [9,45], group holds there; CLUSTER twin
  (posture/tactic/size/anchor) verified flowing. Fighting unproven:
  host spawned no enemy in 4 straight games (environmental).
  Unexplored-start fallback landed (else ATTACK sat home 18 min).

- Long `20261017_080114` (58 s wall, FULL 1209 game-s, rich: Nexus=7
  Probe=56 Pylon=27 Gateway=9 Dragoon=24, cap-inconclusive): Phase-1
  proof - opening script-clean (Pylon 0:51@8, Gateway 1:25@10, Gateway
  1:53@12, Zealots, Core 3:02@23, ONE of each), exactly ONE
  CyberneticsC in 20 min (no duplicate Core/Tech - the outstanding cap
  holds live), reserved trajectory healthy (0 early, single-row 150
  late - the 0:07 250-ghost never reappeared with flow restored),
  DECISIONS.log 720 rows (26 BUILD + 9 ARMY from BUILD_ORDER, rest
  dynamic with reasons).

- Long `20261016_163357` (FIRST PvT, 15 s wall, defeat 5:13,
  Probe=22 Zealot=3 Dragoon=1): Terran rushed us dead early - no
  baseline exists for this matchup (rush twins are Zerg-scripted),
  one game is not a defect per §3a, recorded as the datum to beat.
- Long `20261016_151714` (141 s wall, 628 s game, rich: Probe=37
  Dragoon=13 Nexus=2, cap-inconclusive): lottery blank - no Core
  cancel (#88 went to `131201` instead), no `ORDER_SAVING` (#71),
  `SINGULARITY_APPLIES` x8 climbing to 348/372 then silence with no
  completion marker anywhere (#72: completion unobservable, hence
  the CHECK twin).
- LIVE `20261016_131407` (9 s wall, 247 game-s, roster Pylon=2
  Gateway=1 Zealot=3 Forge=1, 0 exceptions): INCONCLUSIVE, not evidence
  - the bot exited at 4:07 for no logged reason with no enemy spawned
  (`enemy: ` empty). Retry in flight; if it repeats, the early-exit
  path (not V2) is the hunt.
- Last recorded `--long` pass: 210002, 912 s, exit 0 (Nexus=6 Dragoon=7
  Forge=1, first ever; gas 270+; Singularity correctly idle at one
  Dragoon). Log not retained.
- Feeder: first `BOARD_FED` observed `20261015_143903` (supply-pylon at
  a 39/41 block); 0 in all other retained logs.
  `ORDER_SAVING_FOR_EXPANSION` 0 everywhere (string present in
  `IsReadyToProduceOrder:76`, never fires - `20261015_150026` long run
  included). Raw counts, no diagnosis.
- Long `20261015_185046` (141 s wall, 575 s game, 0 kills, rich
  economy: Nexus=2 Probe=31 Pylon=13 Gateway=5 Zealot=15 Core Dragoon=6
  Forge; shutdown-hook summary worked): `SINGULARITY_APPLIES` firing,
  completion unproven (#72); 2 Pylon refresh-cancels, no Core cancel
  (#88), no `ORDER_SAVING` (#71); hang-watcher fired once on
  busy-compute (57 s CPU, not deadlock).
- Wine first Pylon still unbuilt in both instances (see #84); the
  convertible-waiters fix plus walking-builder shield are in the jar,
  final proof is the owner's re-run.
