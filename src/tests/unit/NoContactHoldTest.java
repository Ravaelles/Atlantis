package tests.unit;

import atlantis.combat.ArmyContact;
import atlantis.combat.Regroup;
import atlantis.combat.cluster.FightOrFlight;
import atlantis.combat.strategy.BattleStrategy;
import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.map.choke.AChoke;
import atlantis.map.choke.Chokes;
import atlantis.map.position.APosition;
import atlantis.units.AUnitType;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeChoke;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;

/**
 * No-contact hold (owner 2026-10-26, live game vs Protoss on 7th_rav):
 * with no enemy to engage and the line defending home, the march holds
 * halfway between the main base and the main choke instead of walking to
 * a far anchor - the far march uncovered the base while raiders ran by.
 * An attack anchor still marches out, and FLEE keeps its own rally.
 */
@Tag("combat")
public class NoContactHoldTest extends WorldStubForTests {

    @Test
    public void blindLineHoldsHalfwayBetweenMainAndChoke() {
        aGame.when(AGame::isUms).thenReturn(false);

        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 20, 20);
        FakeUnit a = fake(AUnitType.Protoss_Dragoon, 28, 22);

        AChoke choke = new FakeChoke(APosition.create(60, 20), 5);
        try (MockedStatic<Chokes> chokes = Mockito.mockStatic(Chokes.class)) {
            chokes.when(Chokes::mainChoke).thenReturn(choke);

            world(5, fakeOurs(nexus, a), fakeEnemies(), () -> {
                if (A.now() != 2) return;

                ArmyContact.clearForTests();
                APosition anchor = APosition.create(26, 20);
                Regroup.march(a, FightOrFlight.Tactic.FIGHT, anchor, a, BattleStrategy.OPEN_FIELD);

                String journal = a.commandHistory().toString();
                assertTrue(journal.contains("ClusterStandoff"),
                    "a blind home line holds instead of marching out: " + journal);

                APosition expected = APosition.create(20, 20).translatePercentTowards(50, choke.center());
                APosition dest = a.targetPosition();
                assertNotNull(dest, "the hold names a destination");
                assertTrue(dest.distTo(expected) < 2,
                    "the hold stands halfway main->choke: dest " + dest + " vs " + expected);
            });
        }
    }

    @Test
    public void attackAnchorStillMarchesOut() {
        aGame.when(AGame::isUms).thenReturn(false);

        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 20, 20);
        FakeUnit a = fake(AUnitType.Protoss_Dragoon, 28, 22);

        AChoke choke = new FakeChoke(APosition.create(60, 20), 5);
        try (MockedStatic<Chokes> chokes = Mockito.mockStatic(Chokes.class)) {
            chokes.when(Chokes::mainChoke).thenReturn(choke);

            world(5, fakeOurs(nexus, a), fakeEnemies(), () -> {
                if (A.now() != 2) return;

                ArmyContact.clearForTests();
                APosition farAnchor = APosition.create(90, 90);
                Regroup.march(a, FightOrFlight.Tactic.FIGHT, farAnchor, a, BattleStrategy.OPEN_FIELD);

                String journal = a.commandHistory().toString();
                assertTrue(!journal.contains("ClusterStandoff"),
                    "an attack anchor must march out, never hold home: " + journal);
            });
        }
    }

    @Test
    public void fleeKeepsItsOwnRally() {
        aGame.when(AGame::isUms).thenReturn(false);

        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 20, 20);
        FakeUnit a = fake(AUnitType.Protoss_Dragoon, 28, 22);

        AChoke choke = new FakeChoke(APosition.create(60, 20), 5);
        try (MockedStatic<Chokes> chokes = Mockito.mockStatic(Chokes.class)) {
            chokes.when(Chokes::mainChoke).thenReturn(choke);

            world(5, fakeOurs(nexus, a), fakeEnemies(), () -> {
                if (A.now() != 2) return;

                ArmyContact.clearForTests();
                APosition anchor = APosition.create(26, 20);
                Regroup.march(a, FightOrFlight.Tactic.FLEE, anchor, a, BattleStrategy.OPEN_FIELD);

                String journal = a.commandHistory().toString();
                assertTrue(!journal.contains("ClusterStandoff"),
                    "FLEE owns the rally, the hold must not steal it: " + journal);
            });
        }
    }

    @Test
    public void smallArmyHoldsMainVsZergEarly() {
        enemyRaceInWorld = bwapi.Race.Zerg;

        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 20, 20);
        FakeUnit a = fake(AUnitType.Protoss_Dragoon, 28, 22);
        FakeUnit b = fake(AUnitType.Protoss_Dragoon, 29, 22);
        FakeUnit c = fake(AUnitType.Protoss_Dragoon, 30, 22);

        AChoke choke = new FakeChoke(APosition.create(60, 20), 5);
        try (MockedStatic<Chokes> chokes = Mockito.mockStatic(Chokes.class)) {
            chokes.when(Chokes::mainChoke).thenReturn(choke);

            world(5, fakeOurs(nexus, a, b, c), fakeEnemies(), () -> {
                if (A.now() != 2) return;

                APosition anchor = APosition.create(26, 20);
                APosition hold = atlantis.combat.cluster.ClusterRally.noContactHoldPoint(anchor);
                assertNotNull(hold, "a home line holds somewhere");
                assertTrue(hold.distTo(nexus) <= 6,
                    "3 units vs Zerg hold the main for worker help, not the halfway standoff: " + hold);
            });
        }
    }

    @Test
    public void bigArmyStillHoldsHalfwayVsZerg() {
        enemyRaceInWorld = bwapi.Race.Zerg;

        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 20, 20);
        FakeUnit[] army = new FakeUnit[6];
        for (int i = 0; i < 6; i++) army[i] = fake(AUnitType.Protoss_Dragoon, 28 + i, 22);

        AChoke choke = new FakeChoke(APosition.create(60, 20), 5);
        try (MockedStatic<Chokes> chokes = Mockito.mockStatic(Chokes.class)) {
            chokes.when(Chokes::mainChoke).thenReturn(choke);

            world(5, fakeOurs(concat(nexus, army)), fakeEnemies(), () -> {
                if (A.now() != 2) return;

                APosition anchor = APosition.create(26, 20);
                APosition hold = atlantis.combat.cluster.ClusterRally.noContactHoldPoint(anchor);
                assertNotNull(hold, "a home line holds somewhere");
                assertTrue(hold.distTo(nexus) > 10,
                    "6 units hold the halfway standoff as before: " + hold);
            });
        }
    }

    @Test
    public void smallArmyProtossEnemyUnchanged() {
        enemyRaceInWorld = bwapi.Race.Protoss;

        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 20, 20);
        FakeUnit a = fake(AUnitType.Protoss_Dragoon, 28, 22);
        FakeUnit b = fake(AUnitType.Protoss_Dragoon, 29, 22);
        FakeUnit c = fake(AUnitType.Protoss_Dragoon, 30, 22);

        AChoke choke = new FakeChoke(APosition.create(60, 20), 5);
        try (MockedStatic<Chokes> chokes = Mockito.mockStatic(Chokes.class)) {
            chokes.when(Chokes::mainChoke).thenReturn(choke);

            world(5, fakeOurs(nexus, a, b, c), fakeEnemies(), () -> {
                if (A.now() != 2) return;

                APosition anchor = APosition.create(26, 20);
                APosition hold = atlantis.combat.cluster.ClusterRally.noContactHoldPoint(anchor);
                assertNotNull(hold, "a home line holds somewhere");
                assertTrue(hold.distTo(nexus) > 10,
                    "vs Protoss the small line keeps the standoff: " + hold);
            });
        }
    }

    /**
     * First-Dragoon gate (owner order 2026-10-26): on a dragoon build vs
     * Zerg the Zealot phase holds at the main even with six battle units
     * - the rule that used to let the line out at five units is exactly
     * what uncovered the base before the first Dragoon existed.
     */
    @Test
    public void dragoonBuildHoldsTheMainUntilItsFirstDragoon() {
        enemyRaceInWorld = bwapi.Race.Zerg;

        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 20, 20);
        FakeUnit[] zealots = new FakeUnit[6];
        for (int i = 0; i < 6; i++) zealots[i] = fake(AUnitType.Protoss_Zealot, 28 + i, 22);

        AChoke choke = new FakeChoke(APosition.create(60, 20), 5);
        try (MockedStatic<Chokes> chokes = Mockito.mockStatic(Chokes.class)) {
            chokes.when(Chokes::mainChoke).thenReturn(choke);

            world(5, fakeOurs(concat(nexus, zealots)), fakeEnemies(), () -> {
                if (A.now() != 2) return;

                atlantis.combat.cluster.FirstDragoonGate.setBuildPlansDragoons(true);
                APosition anchor = APosition.create(26, 20);
                APosition hold = atlantis.combat.cluster.ClusterRally.noContactHoldPoint(anchor);
                assertNotNull(hold, "a home line holds somewhere");
                assertTrue(hold.distTo(nexus) <= 6,
                    "a dragoon build with no Dragoon holds the main: " + hold);
            });
        }
    }

    /**
     * The other half of the owner's order: the gate must OPEN. The same
     * six Zealots stage out to the halfway standoff once a Dragoon is on
     * the field - holding a finished dragoon build at home forever would
     * be the same defect with the sign flipped.
     */
    @Test
    public void aFinishedDragoonOpensTheGateForTheSameArmy() {
        enemyRaceInWorld = bwapi.Race.Zerg;

        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 20, 20);
        FakeUnit[] army = new FakeUnit[7];
        for (int i = 0; i < 6; i++) army[i] = fake(AUnitType.Protoss_Zealot, 28 + i, 22);
        army[6] = fake(AUnitType.Protoss_Dragoon, 34, 22);

        AChoke choke = new FakeChoke(APosition.create(60, 20), 5);
        try (MockedStatic<Chokes> chokes = Mockito.mockStatic(Chokes.class)) {
            chokes.when(Chokes::mainChoke).thenReturn(choke);

            world(5, fakeOurs(concat(nexus, army)), fakeEnemies(), () -> {
                if (A.now() != 2) return;

                atlantis.combat.cluster.FirstDragoonGate.setBuildPlansDragoons(true);
                APosition anchor = APosition.create(26, 20);
                APosition hold = atlantis.combat.cluster.ClusterRally.noContactHoldPoint(anchor);
                assertNotNull(hold, "a home line holds somewhere");
                assertTrue(hold.distTo(nexus) > 10,
                    "one Dragoon opens the gate: dest " + hold);
            });
        }
    }

    /**
     * Control for the gate flag: six Zealots on a build that never
     * trains Dragoons stage out exactly as they did before the order -
     * the gate is the flag, not the army size or the matchup.
     */
    @Test
    public void aZealotBuildWithSixUnitsKeepsStagingOut() {
        enemyRaceInWorld = bwapi.Race.Zerg;

        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 20, 20);
        FakeUnit[] zealots = new FakeUnit[6];
        for (int i = 0; i < 6; i++) zealots[i] = fake(AUnitType.Protoss_Zealot, 28 + i, 22);

        AChoke choke = new FakeChoke(APosition.create(60, 20), 5);
        try (MockedStatic<Chokes> chokes = Mockito.mockStatic(Chokes.class)) {
            chokes.when(Chokes::mainChoke).thenReturn(choke);

            world(5, fakeOurs(concat(nexus, zealots)), fakeEnemies(), () -> {
                if (A.now() != 2) return;

                atlantis.combat.cluster.FirstDragoonGate.setBuildPlansDragoons(false);
                APosition anchor = APosition.create(26, 20);
                APosition hold = atlantis.combat.cluster.ClusterRally.noContactHoldPoint(anchor);
                assertNotNull(hold, "a home line holds somewhere");
                assertTrue(hold.distTo(nexus) > 10,
                    "an ungated build keeps today's halfway standoff: " + hold);
            });
        }
    }

    @org.junit.jupiter.api.AfterEach
    public void forgetTheBuildFlag() {
        atlantis.combat.cluster.FirstDragoonGate.clearForTests();
    }

    private static FakeUnit[] concat(FakeUnit first, FakeUnit[] rest) {
        FakeUnit[] all = new FakeUnit[rest.length + 1];
        all[0] = first;
        System.arraycopy(rest, 0, all, 1, rest.length);

        return all;
    }
}
