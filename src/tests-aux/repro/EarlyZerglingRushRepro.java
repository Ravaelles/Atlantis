package tests.aux.repro;

import atlantis.battle.cluster.Alpha;
import atlantis.combat.BattleCommander;
import atlantis.units.AUnitType;
import atlantis.units.workers.defence.fight.WorkerDefenceFight;
import atlantis.map.position.HasPosition;
import atlantis.util.log.EventLog;
import tests.fakes.FakeUnit;
import tests.unit.sim.SimArena;
import tests.unit.sim.SimWorld;
import tests.unit.sim.ZombiePolicies;

import java.util.ArrayList;
import java.util.List;

/**
 * TEMPORARY repro harness (owner order 2026-11-04): NOT part of any test
 * suite - plain main() under src/testsaux, never selected by
 * run-tests.sh (it only selects tests.unit.* and tests.architecture).
 * Delete once the pursuit bug is pinned by a real test.
 *
 * Shape under test (live report 2026-11-04): Zerglings raid the mineral
 * line, workers fight them at home (allowed), the lings retreat toward
 * mid-map - and workers PURSUE past the 10-tile leash instead of
 * holding (WorkerDefenceFightCombatUnits fired at f4728-5409 while the
 * fight had moved out). The documented rule: a raider beyond 10 tiles
 * of OUR main is not ours to chase; past 12 a worker turns back
 * unconditionally (WorkerStrayedFromBase).
 *
 * Scenario: Nexus + probes at the west edge, lings attack, then retreat
 * east on a trigger. Oracle: from the first frame where every living
 * ling is past 12 tiles from the main, no probe may hold an attack
 * order on a ling past 10 tiles - that is the documented rule,
 * evaluated with the production predicates themselves.
 *
 * Exit codes: 0 = NO CHASE (rule holds), 1 = CHASE (bug reproduced,
 * violations listed), 2 = INCONCLUSIVE (no base contact / no defenders
 * left to chase - harness did not produce the shape, not a pass).
 */
public final class EarlyZerglingRushRepro {

    private static final int TILE = 32;

    private static final double MAIN_X = 15;
    private static final double MAIN_Y = 30;

    private static final double RETREAT_X = 85;
    private static final double RETREAT_Y = 30;

    private static final int RETREAT_FRAME = 280;
    private static final int END_FRAME = 2000;

    private static FakeUnit unit(AUnitType type, double tx, double ty, int hp) {
        FakeUnit u = new FakeUnit(type, tx, ty);
        u.hp = hp;
        return u;
    }

    public static void main(String[] args) throws Exception {
        // Minimal static world the worker chain needs: enemy race (the
        // sim never attaches a real BWAPI game, so AGame.enemy() is null)
        // and a plausible supply readout for the stop-fighting branch.
        // SimWorld.tick advances A time itself; nothing else is stubbed.
        org.mockito.MockedStatic<atlantis.game.race.EnemyRace> enemyRace =
            org.mockito.Mockito.mockStatic(atlantis.game.race.EnemyRace.class);
        enemyRace.when(atlantis.game.race.EnemyRace::isEnemyZerg).thenReturn(true);
        enemyRace.when(atlantis.game.race.EnemyRace::isEnemyProtoss).thenReturn(false);
        enemyRace.when(atlantis.game.race.EnemyRace::isEnemyTerran).thenReturn(false);
        org.mockito.MockedStatic<atlantis.game.AGame> aGame =
            org.mockito.Mockito.mockStatic(atlantis.game.AGame.class);
        aGame.when(atlantis.game.AGame::supplyUsed).thenReturn(16);
        aGame.when(atlantis.game.AGame::supplyTotal).thenReturn(25);
        // Eval path reads chokes/bases through static getters (BWEM-backed
        // live). The unit-test world stubs them; the repro does the same
        // minimum so the fight gate reaches the leash instead of dying.
        org.mockito.MockedStatic<atlantis.map.choke.AllChokes> chokes =
            org.mockito.Mockito.mockStatic(atlantis.map.choke.AllChokes.class);
        chokes.when(atlantis.map.choke.AllChokes::get)
            .thenReturn(tests.fakes.FakeChokes.get());
        org.mockito.MockedStatic<atlantis.map.base.AllBaseLocations> bases =
            org.mockito.Mockito.mockStatic(atlantis.map.base.AllBaseLocations.class);
        bases.when(atlantis.map.base.AllBaseLocations::get)
            .thenReturn(tests.fakes.FakeBaseLocations.get());
        try {
            runScenario();
        }
        finally {
            bases.close();
            chokes.close();
            aGame.close();
            enemyRace.close();
        }
    }

    private static void runScenario() throws Exception {
        // Open ground: the sim arena has no map behind it, so the run-position
        // finder would reach the engine and die. Everything walkable/visible.
        tests.fakes.TileMapSource.everythingFine().install();
        SimArena arena = new SimArena(100 * TILE, 60 * TILE);

        List<FakeUnit> ours = new ArrayList<>();
        ours.add(unit(AUnitType.Protoss_Nexus, MAIN_X, MAIN_Y, 1500));
        double[][] mineralSpots = {{12, 28}, {13, 28}, {14, 28}, {16, 28}, {17, 28}, {15, 32}, {13, 32}, {16, 32}};
        for (double[] s : mineralSpots) {
            ours.add(unit(AUnitType.Protoss_Probe, s[0], s[1], 40));
        }

        List<FakeUnit> foes = new ArrayList<>();
        foes.add(unit(AUnitType.Zerg_Zergling, 60, 28, 35).setEnemy());
        foes.add(unit(AUnitType.Zerg_Zergling, 61, 32, 35).setEnemy());
        foes.add(unit(AUnitType.Zerg_Zergling, 60, 32, 35).setEnemy());
        foes.add(unit(AUnitType.Zerg_Zergling, 61, 28, 35).setEnemy());

        doRun(arena, ours, foes);
    }

    private static void doRun(SimArena arena, List<FakeUnit> ours, List<FakeUnit> foes) {
        FakeUnit[] oursArr = ours.toArray(new FakeUnit[0]);
        FakeUnit[] foesArr = foes.toArray(new FakeUnit[0]);
        SimWorld world = new SimWorld(oursArr, foesArr, arena);
        List<String> violations = new ArrayList<>();
        List<String> timeline = new ArrayList<>();
        boolean[] retreated = {false};
        boolean[] contact = {false};
        int[] retreatFrame = {-1};
        int[] attackersAtRetreat = {-1};

        try {
            BattleCommander.clearForTests();
            EventLog.clearForTests();
            Alpha.gather();

            for (int f = 0; f < END_FRAME; f++) {
                final boolean retreatNow = retreated[0] || f >= RETREAT_FRAME;
                if (retreatNow && !retreated[0]) {
                    retreated[0] = true;
                    retreatFrame[0] = f;
                    int attackers = 0;
                    for (FakeUnit u : oursArr) {
                        if (u.hp > 0 && u.type().equals(AUnitType.Protoss_Probe)
                            && "AttackUnit".equals(u.lastCommand)
                            && u.target instanceof FakeUnit
                            && ((FakeUnit) u.target).hp > 0) attackers++;
                    }
                    attackersAtRetreat[0] = attackers;
                    timeline.add("f" + f + ": lings retreat to mid-map (attackers=" + attackers + ")");
                }
                final boolean retreat = retreated[0];

                world.tick(() -> {
                    // Script the raiders: attack the line, then walk home.
                    if (retreat) {
                        for (FakeUnit z : foesArr) {
                            if (z.hp <= 0) continue;
                            z.lastCommand = "Move";
                            z.target = null;
                            z.targetPosition = new atlantis.map.position.APosition(
                                (int) RETREAT_X, (int) RETREAT_Y);
                        }
                    }
                    else {
                        ZombiePolicies.chaseNearest(foes, ours);
                    }

                    // Drive the fight subtree directly (not the whole worker
                    // chain): the chase verdict lives in FightCombatUnits +
                    // StopFighting, and the sibling run path needs a real
                    // map for run positions, which this arena does not have.
                    for (FakeUnit u : oursArr) {
                        if (u.hp <= 0) continue;
                        if (!u.type().equals(AUnitType.Protoss_Probe)) continue;
                        new WorkerDefenceFight(u).forceHandle();
                    }

                    observe(world, oursArr, foesArr, retreat, contact, violations, timeline);
                });

                if (f % 100 == 0 || (retreat && f < retreatFrame[0] + 150 && f % 10 == 0)) {
                    timeline.add(snapshot(f, oursArr, foesArr));
                }

                if (retreat && foes.stream().noneMatch(z -> z.hp > 0)) break;
            }
        }
        finally {
            world.close();
            tests.fakes.TileMapSource.uninstall();
            BattleCommander.clearForTests();
            EventLog.clearForTests();
        }

        report(contact[0], retreated[0], retreatFrame[0], attackersAtRetreat[0], violations, timeline, oursArr);
    }

    private static void observe(
        SimWorld world, FakeUnit[] ours, FakeUnit[] foes,
        boolean retreat, boolean[] contact, List<String> violations, List<String> timeline
    ) {
        for (FakeUnit z : foes) {
            if (z.hp > 0 && distToMain(z) < 12) contact[0] = true;
        }
        if (!retreat) return;

        boolean allLingsFar = true;
        for (FakeUnit z : foes) {
            if (z.hp > 0 && distToMain(z) <= 12) allLingsFar = false;
        }
        if (!allLingsFar) return;

        for (FakeUnit p : ours) {
            if (p.hp <= 0 || !p.type().equals(AUnitType.Protoss_Probe)) continue;
            Object target = p.target;
            if (!(target instanceof FakeUnit)) continue;
            FakeUnit t = (FakeUnit) target;
            if (!t.isEnemy() || t.hp <= 0) continue;
            double targetDist = distToMain(t);
            boolean attacking = "AttackUnit".equals(p.lastCommand);
            boolean advancing = "Move".equals(p.lastCommand);
            if (targetDist > 10 && (attacking || advancing)) {
                violations.add("f" + world.frame() + " probe#" + p.id()
                    + " chases ling#" + t.id + " (" + p.lastCommand + ")"
                    + " targetDistToMain=" + String.format("%.1f", targetDist)
                    + " probeDistToMain=" + String.format("%.1f", distToMain(p)));
            }
        }
        double maxChase = 0;
        for (FakeUnit p : ours) {
            if (p.hp > 0 && p.type().equals(AUnitType.Protoss_Probe)) {
                maxChase = Math.max(maxChase, distToMain(p));
            }
        }
        if (maxChase > 12) {
            timeline.add("f" + world.frame() + ": probe out at " + String.format("%.1f", maxChase));
        }
    }

    private static double distToMain(HasPosition p) {
        return atlantis.units.select.Select.main().distTo(p);
    }

    private static String snapshot(int f, FakeUnit[] ours, FakeUnit[] foes) {
        int probes = 0;
        double maxProbe = 0;
        StringBuilder cmds = new StringBuilder();
        for (FakeUnit u : ours) {
            if (u.hp > 0 && u.type().equals(AUnitType.Protoss_Probe)) {
                probes++;
                maxProbe = Math.max(maxProbe, distToMain(u));
                if (distToMain(u) > 5) {
                    String t = "-";
                    if (u.target instanceof FakeUnit) {
                        FakeUnit ft = (FakeUnit) u.target;
                        t = "t#" + ft.id + "@" + String.format("%.0f", distToMain(ft)) + (ft.hp <= 0 ? "(dead)" : "");
                    }
                    cmds.append(" p#").append(u.id).append(":").append(u.lastCommand).append(t);
                }
            }
        }
        StringBuilder sb = new StringBuilder("f" + f + ": probes=" + probes
            + " maxProbeDist=" + String.format("%.1f", maxProbe) + " lings=[");
        for (FakeUnit z : foes) {
            if (z.hp > 0) sb.append(String.format("%.0f", distToMain(z))).append(",");
        }
        sb.append("]");
        if (cmds.length() > 0) sb.append(" cmds:{").append(cmds).append(" }");
        return sb.toString();
    }

    private static void report(
        boolean contact, boolean retreated, int retreatFrame, int attackersAtRetreat,
        List<String> violations, List<String> timeline, FakeUnit[] ours
    ) {
        System.out.println("=== EarlyZerglingRushRepro ===");
        for (String line : timeline) System.out.println("  " + line);
        long alive = java.util.Arrays.stream(ours)
            .filter(u -> u.hp > 0 && u.type().equals(AUnitType.Protoss_Probe)).count();
        System.out.println("  probes alive at end: " + alive);
        System.out.println("  attackers at retreat: " + attackersAtRetreat);
        if (!contact) {
            System.out.println("RESULT: INCONCLUSIVE - lings never reached the base, no fight shape");
            System.exit(2);
        }
        if (!retreated) {
            System.out.println("RESULT: INCONCLUSIVE - retreat never triggered");
            System.exit(2);
        }
        if (attackersAtRetreat == 0) {
            System.out.println("RESULT: INCONCLUSIVE - nobody mid-attack at retreat, no pursuit shape");
            System.exit(2);
        }
        if (alive == 0) {
            System.out.println("RESULT: INCONCLUSIVE - no defenders left to chase");
            System.exit(2);
        }
        if (!violations.isEmpty()) {
            System.out.println("RESULT: CHASE - " + violations.size() + " leash violation(s):");
            for (String v : violations) System.out.println("  " + v);
            System.exit(1);
        }
        System.out.println("RESULT: NO CHASE - the 10-tile leash holds");
        System.exit(0);
    }
}
