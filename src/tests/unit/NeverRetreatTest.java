package tests.unit;

import atlantis.battle.cluster.Alpha;
import atlantis.combat.cluster.ClusterTactic;
import atlantis.combat.cluster.FightOrFlight;
import atlantis.combat.retreating.FleeReason;
import atlantis.combat.retreating.RetreatFromLostFight;
import atlantis.information.enemy.EnemyUnitsUpdater;
import atlantis.map.choke.AChoke;
import atlantis.map.choke.Chokes;
import atlantis.map.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import bwapi.Race;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeUnit;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeEnemies;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

/**
 * Standardized no-retreat (owner 2026-11-09): with two or more bases
 * and over ten combat units standing, no retreat leg runs - not the
 * cluster FLEE, not the per-unit organized walk home. Whatever the
 * evaluator says, the army fights where it stands.
 */
@Tag("combat")
public class NeverRetreatTest extends WorldStubForTests {

    @Override
    public Race initRace() {
        return Race.Protoss;
    }

    @Override
    public Race initEnemyRace() {
        return Race.Zerg;
    }

    @AfterEach
    public void clean() {
        for (AUnit unit : Alpha.get().selection().list().toArray(new AUnit[0])) {
            Alpha.get().removeUnit(unit);
        }
        ClusterTactic.reset();
    }

    private static MockedStatic<Chokes> mockChokes(int mainTx, int mainTy, int natTx, int natTy) {
        AChoke main = Mockito.mock(AChoke.class);
        Mockito.when(main.center()).thenReturn(APosition.create(mainTx, mainTy));
        AChoke natural = Mockito.mock(AChoke.class);
        Mockito.when(natural.center()).thenReturn(APosition.create(natTx, natTy));
        MockedStatic<Chokes> chokes = Mockito.mockStatic(Chokes.class);
        chokes.when(Chokes::mainChoke).thenReturn(main);
        chokes.when(() -> Chokes.natural()).thenReturn(natural);

        return chokes;
    }

    private static FakeUnit[] zealots(int count, int tx, int ty) {
        List<FakeUnit> units = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            units.add(fake(AUnitType.Protoss_Zealot, tx, ty));
        }

        return units.toArray(new FakeUnit[0]);
    }

    private static FakeUnit[] lings(int count, int tx, int ty) {
        List<FakeUnit> units = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            FakeUnit ling = fake(AUnitType.Zerg_Zergling, tx, ty);
            ling.setEnemy();
            units.add(ling);
        }

        return units.toArray(new FakeUnit[0]);
    }

    @Test
    public void twoBasesAndElevenHold() {
        FakeUnit main = fake(AUnitType.Protoss_Nexus, 20, 20);
        FakeUnit natural = fake(AUnitType.Protoss_Nexus, 38, 20);
        FakeUnit[] ball = zealots(11, 30, 20);

        try (MockedStatic<Chokes> ignored = mockChokes(50, 20, 64, 25)) {
            world(3, fakeOurs(concat(main, natural, ball)), fakeEnemies(), () -> {
                if (atlantis.game.A.now() != 2) return;

                assertTrue(ClusterTactic.neverRetreats(),
                    "two bases, 11 combat units: no retreat leg runs");
            });
        }
    }

    @Test
    public void tenUnitsKeepRetreat() {
        FakeUnit main = fake(AUnitType.Protoss_Nexus, 20, 20);
        FakeUnit natural = fake(AUnitType.Protoss_Nexus, 38, 20);
        FakeUnit[] ball = zealots(10, 30, 20);

        try (MockedStatic<Chokes> ignored = mockChokes(50, 20, 64, 25)) {
            world(3, fakeOurs(concat(main, natural, ball)), fakeEnemies(), () -> {
                if (atlantis.game.A.now() != 2) return;

                assertFalse(ClusterTactic.neverRetreats(),
                    "10 is not over 10: the headcount gate stays shut");
            });
        }
    }

    @Test
    public void oneBaseKeepsRetreat() {
        FakeUnit main = fake(AUnitType.Protoss_Nexus, 20, 20);
        FakeUnit[] ball = zealots(11, 30, 20);

        try (MockedStatic<Chokes> ignored = mockChokes(50, 20, 64, 25)) {
            world(3, fakeOurs(concat(main, ball)), fakeEnemies(), () -> {
                if (atlantis.game.A.now() != 2) return;

                assertFalse(ClusterTactic.neverRetreats(),
                    "one base: the base gate stays shut");
            });
        }
    }

    @Test
    public void neverRetreatBeatsWeakEarlyVeto() {
        FakeUnit main = fake(AUnitType.Protoss_Nexus, 20, 20);
        FakeUnit natural = fake(AUnitType.Protoss_Nexus, 38, 20);
        FakeUnit[] ball = zealots(11, 70, 20);
        FakeUnit[] foes = lings(30, 80, 28);
        List<FakeUnit> ours = new ArrayList<>();
        ours.add(main);
        ours.add(natural);
        for (FakeUnit z : ball) ours.add(z);

        try (MockedStatic<Chokes> ignored = mockChokes(50, 20, 100, 20)) {
            world(3, fakeOurs(ours.toArray(new FakeUnit[0])), fakeEnemies(foes), () -> {
                if (atlantis.game.A.now() != 2) return;
                for (FakeUnit foe : foes) EnemyUnitsUpdater.weDiscoveredEnemyUnit(foe);
                for (FakeUnit z : ball) Alpha.get().addUnit(z);

                assertTrue(ClusterTactic.weakEarlyVsZergWithoutDragoons(),
                    "fixture check: the veto would fire here on its own");
                assertTrue(ClusterTactic.neverRetreats(),
                    "fixture check: far from every other gate, only the ban holds");
                assertEquals(FightOrFlight.Tactic.FIGHT, ClusterTactic.decide(),
                    "a real army with two bases fights where it stands");
            });
        }
    }

    @Test
    public void perUnitRetreatStaysOnFleeTactic() {
        FakeUnit main = fake(AUnitType.Protoss_Nexus, 20, 20);
        FakeUnit natural = fake(AUnitType.Protoss_Nexus, 38, 20);
        FakeUnit[] ball = zealots(11, 30, 20);
        FakeUnit[] foes = lings(30, 40, 28);
        List<FakeUnit> ours = new ArrayList<>();
        ours.add(main);
        ours.add(natural);
        for (FakeUnit z : ball) ours.add(z);

        world(3, fakeOurs(ours.toArray(new FakeUnit[0])), fakeEnemies(foes), () -> {
            if (atlantis.game.A.now() != 2) return;
            for (FakeUnit foe : foes) EnemyUnitsUpdater.weDiscoveredEnemyUnit(foe);

            RetreatFromLostFight.Verdict verdict = RetreatFromLostFight.decide(
                ball[0], FightOrFlight.Tactic.FLEE, null, null);

            assertEquals(FleeReason.STAY_NEVER_RETREAT, verdict.reason,
                "even on a FLEE tactic the standardized ban holds the line");
        });
    }

    @Test
    public void tenUnitsKeepPerUnitRetreat() {
        FakeUnit main = fake(AUnitType.Protoss_Nexus, 20, 20);
        FakeUnit natural = fake(AUnitType.Protoss_Nexus, 38, 20);
        FakeUnit[] ball = zealots(10, 30, 20);
        FakeUnit[] foes = lings(30, 40, 28);
        List<FakeUnit> ours = new ArrayList<>();
        ours.add(main);
        ours.add(natural);
        for (FakeUnit z : ball) ours.add(z);

        world(3, fakeOurs(ours.toArray(new FakeUnit[0])), fakeEnemies(foes), () -> {
            if (atlantis.game.A.now() != 2) return;
            for (FakeUnit foe : foes) EnemyUnitsUpdater.weDiscoveredEnemyUnit(foe);

            RetreatFromLostFight.Verdict verdict = RetreatFromLostFight.decide(
                ball[0], FightOrFlight.Tactic.FLEE, null, null);

            assertNotEquals(FleeReason.STAY_NEVER_RETREAT, verdict.reason,
                "10 units: the ban stays shut, normal flee logic answers");
        });
    }

    private static FakeUnit[] concat(FakeUnit first, FakeUnit[] rest) {
        List<FakeUnit> out = new ArrayList<>();
        out.add(first);
        for (FakeUnit u : rest) out.add(u);

        return out.toArray(new FakeUnit[0]);
    }

    private static FakeUnit[] concat(FakeUnit first, FakeUnit second, FakeUnit[] rest) {
        List<FakeUnit> out = new ArrayList<>();
        out.add(first);
        out.add(second);
        for (FakeUnit u : rest) out.add(u);

        return out.toArray(new FakeUnit[0]);
    }
}
