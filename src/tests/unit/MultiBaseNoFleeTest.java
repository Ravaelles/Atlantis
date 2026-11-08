package tests.unit;

import atlantis.battle.cluster.Alpha;
import atlantis.combat.cluster.ClusterTactic;
import atlantis.combat.cluster.FightOrFlight;
import atlantis.information.enemy.EnemyUnitsUpdater;
import atlantis.information.generic.ArmyStrength;
import atlantis.map.base.Bases;
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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeEnemies;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

/**
 * No-flee holds past the second base (owner 2026-11-08): with three
 * bases the ball never trades the natural for a retreat home, and
 * 15+ combat units within 20 ground tiles of the natural at 90%+
 * relative strength stand and fight. Both gates live in the tactic
 * chain next to the home-ground hold, ahead of the weak-early-Zerg
 * veto - a skirmish veto must not overrule a committed defense.
 */
@Tag("combat")
public class MultiBaseNoFleeTest extends WorldStubForTests {

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
        Bases.clearForTests();
        ArmyStrength.clearCache();
    }

    private static MockedStatic<Chokes> mockChokeAt(int tx, int ty) {
        AChoke choke = Mockito.mock(AChoke.class);
        Mockito.when(choke.center()).thenReturn(APosition.create(tx, ty));
        MockedStatic<Chokes> chokes = Mockito.mockStatic(Chokes.class);
        chokes.when(Chokes::mainChoke).thenReturn(choke);

        return chokes;
    }

    private static FakeUnit[] zealots(int count, int tx, int ty) {
        List<FakeUnit> zealots = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            zealots.add(fake(AUnitType.Protoss_Zealot, tx + (i % 4), ty + (i / 4)));
        }

        return zealots.toArray(new FakeUnit[0]);
    }

    private static FakeUnit[] lings(int count, int tx, int ty) {
        List<FakeUnit> lings = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            FakeUnit ling = fake(AUnitType.Zerg_Zergling, tx + (i % 8), ty + (i / 8));
            ling.setEnemy();
            lings.add(ling);
        }

        return lings.toArray(new FakeUnit[0]);
    }

    private static void discover(FakeUnit[] enemies) {
        for (FakeUnit enemy : enemies) {
            EnemyUnitsUpdater.weDiscoveredEnemyUnit(enemy);
        }
    }

    private static void addToAlpha(FakeUnit[] ours) {
        for (FakeUnit unit : ours) {
            if (unit.isCombatUnit()) {
                Alpha.get().addUnit(unit);
            }
        }
    }

    @Test
    public void threeBasesNearerMainHold() {
        FakeUnit main = fake(AUnitType.Protoss_Nexus, 20, 20);
        FakeUnit natural = fake(AUnitType.Protoss_Nexus, 38, 20);
        FakeUnit third = fake(AUnitType.Protoss_Nexus, 20, 60);
        FakeUnit[] ball = zealots(2, 22, 20);

        try (MockedStatic<Chokes> ignored = mockChokeAt(50, 20)) {
            world(3, fakeOurs(main, natural, third, ball[0], ball[1]), fakeEnemies(), () -> {
                if (atlantis.game.A.now() != 2) return;
                addToAlpha(ball);

                assertTrue(ClusterTactic.multiBaseHoldAgainstRetreat(),
                    "three bases, ball 2.5 from main vs 15.5 from natural: no retreat home");
            });
        }
    }

    @Test
    public void twoBasesKeepRetreat() {
        FakeUnit main = fake(AUnitType.Protoss_Nexus, 20, 20);
        FakeUnit natural = fake(AUnitType.Protoss_Nexus, 38, 20);
        FakeUnit[] ball = zealots(2, 22, 20);

        try (MockedStatic<Chokes> ignored = mockChokeAt(50, 20)) {
            world(3, fakeOurs(main, natural, ball[0], ball[1]), fakeEnemies(), () -> {
                if (atlantis.game.A.now() != 2) return;
                addToAlpha(ball);

                assertFalse(ClusterTactic.multiBaseHoldAgainstRetreat(),
                    "two bases: the gate stays shut");
            });
        }
    }

    @Test
    public void nearerNaturalKeepsRetreat() {
        FakeUnit main = fake(AUnitType.Protoss_Nexus, 20, 20);
        FakeUnit natural = fake(AUnitType.Protoss_Nexus, 38, 20);
        FakeUnit third = fake(AUnitType.Protoss_Nexus, 20, 60);
        FakeUnit[] ball = zealots(2, 36, 20);

        try (MockedStatic<Chokes> ignored = mockChokeAt(50, 20)) {
            world(3, fakeOurs(main, natural, third, ball[0], ball[1]), fakeEnemies(), () -> {
                if (atlantis.game.A.now() != 2) return;
                addToAlpha(ball);

                assertFalse(ClusterTactic.multiBaseHoldAgainstRetreat(),
                    "ball nearer the natural (2 vs 16): retreat stays open");
            });
        }
    }

    @Test
    public void equalDistanceKeepsRetreat() {
        FakeUnit main = fake(AUnitType.Protoss_Nexus, 20, 20);
        FakeUnit natural = fake(AUnitType.Protoss_Nexus, 38, 20);
        FakeUnit third = fake(AUnitType.Protoss_Nexus, 20, 60);
        FakeUnit[] ball = zealots(2, 29, 20);

        try (MockedStatic<Chokes> ignored = mockChokeAt(50, 20)) {
            world(3, fakeOurs(main, natural, third, ball[0], ball[1]), fakeEnemies(), () -> {
                if (atlantis.game.A.now() != 2) return;
                addToAlpha(ball);

                assertFalse(ClusterTactic.multiBaseHoldAgainstRetreat(),
                    "tied ground: the ban needs strictly nearer");
            });
        }
    }

    @Test
    public void noNaturalKeepsRetreat() {
        FakeUnit main = fake(AUnitType.Protoss_Nexus, 20, 20);
        FakeUnit second = fake(AUnitType.Protoss_Nexus, 38, 20);
        FakeUnit third = fake(AUnitType.Protoss_Nexus, 20, 60);
        FakeUnit[] ball = zealots(2, 22, 20);

        try (MockedStatic<Chokes> chokes = Mockito.mockStatic(Chokes.class)) {
            chokes.when(Chokes::mainChoke).thenReturn(null);
            world(3, fakeOurs(main, second, third, ball[0], ball[1]), fakeEnemies(), () -> {
                if (atlantis.game.A.now() != 2) return;
                addToAlpha(ball);

                assertFalse(ClusterTactic.multiBaseHoldAgainstRetreat(),
                    "no resolvable natural: no gate");
            });
        }
    }

    @Test
    public void strongEnoughNearNaturalStands() {
        FakeUnit main = fake(AUnitType.Protoss_Nexus, 20, 20);
        FakeUnit natural = fake(AUnitType.Protoss_Nexus, 38, 20);
        FakeUnit[] ball = zealots(16, 36, 22);
        FakeUnit[] foes = lings(62, 44, 20);
        List<FakeUnit> ours = new ArrayList<>();
        ours.add(main);
        ours.add(natural);
        for (FakeUnit z : ball) ours.add(z);

        try (MockedStatic<Chokes> ignored = mockChokeAt(50, 20)) {
            world(3, fakeOurs(ours.toArray(new FakeUnit[0])), fakeEnemies(foes), () -> {
                if (atlantis.game.A.now() != 2) return;
                discover(foes);
                addToAlpha(ball);

                int strength = atlantis.information.generic.Army.strength();
                assertTrue(strength >= 90 && strength < 200,
                    "fixture check: 16 zealots vs 62 lings must read 90-199, got " + strength);
                assertTrue(ClusterTactic.naturalStandWithStrength(),
                    "16 units at ~90%+ within 20 of the natural: only FIGHT");
            });
        }
    }

    @Test
    public void fifteenUnitsKeepRetreat() {
        FakeUnit main = fake(AUnitType.Protoss_Nexus, 20, 20);
        FakeUnit natural = fake(AUnitType.Protoss_Nexus, 38, 20);
        FakeUnit[] ball = zealots(15, 36, 22);
        FakeUnit[] foes = lings(62, 44, 20);
        List<FakeUnit> ours = new ArrayList<>();
        ours.add(main);
        ours.add(natural);
        for (FakeUnit z : ball) ours.add(z);

        try (MockedStatic<Chokes> ignored = mockChokeAt(50, 20)) {
            world(3, fakeOurs(ours.toArray(new FakeUnit[0])), fakeEnemies(foes), () -> {
                if (atlantis.game.A.now() != 2) return;
                discover(foes);
                addToAlpha(ball);

                assertFalse(ClusterTactic.naturalStandWithStrength(),
                    "15 is not over 15: the headcount gate stays shut");
            });
        }
    }

    @Test
    public void weakArmyNearNaturalKeepsRetreat() {
        FakeUnit main = fake(AUnitType.Protoss_Nexus, 20, 20);
        FakeUnit natural = fake(AUnitType.Protoss_Nexus, 38, 20);
        FakeUnit[] ball = zealots(16, 36, 22);
        FakeUnit[] foes = lings(68, 44, 20);
        List<FakeUnit> ours = new ArrayList<>();
        ours.add(main);
        ours.add(natural);
        for (FakeUnit z : ball) ours.add(z);

        try (MockedStatic<Chokes> ignored = mockChokeAt(50, 20)) {
            world(3, fakeOurs(ours.toArray(new FakeUnit[0])), fakeEnemies(foes), () -> {
                if (atlantis.game.A.now() != 2) return;
                discover(foes);
                addToAlpha(ball);

                int strength = atlantis.information.generic.Army.strength();
                assertTrue(strength < 90,
                    "fixture check: 16 zealots vs 68 lings must read under 90, got " + strength);
                assertFalse(ClusterTactic.naturalStandWithStrength(),
                    "under 90%: the strength gate stays shut");
            });
        }
    }

    @Test
    public void farFromNaturalKeepsRetreat() {
        FakeUnit main = fake(AUnitType.Protoss_Nexus, 20, 20);
        FakeUnit natural = fake(AUnitType.Protoss_Nexus, 38, 20);
        FakeUnit[] ball = zealots(16, 70, 20);
        FakeUnit[] foes = lings(62, 76, 20);
        List<FakeUnit> ours = new ArrayList<>();
        ours.add(main);
        ours.add(natural);
        for (FakeUnit z : ball) ours.add(z);

        try (MockedStatic<Chokes> ignored = mockChokeAt(50, 20)) {
            world(3, fakeOurs(ours.toArray(new FakeUnit[0])), fakeEnemies(foes), () -> {
                if (atlantis.game.A.now() != 2) return;
                discover(foes);
                addToAlpha(ball);

                assertFalse(ClusterTactic.naturalStandWithStrength(),
                    "32 tiles out: the distance gate stays shut");
            });
        }
    }

    @Test
    public void naturalStandBeatsWeakEarlyVeto() {
        // Order pin: the same board that makes weakEarlyVsZergWithoutDragoons
        // fire (Zerg, early, no dragoons, under 155) must still answer FIGHT
        // when the natural-stand shape holds - the committed defense
        // outranks the skirmish veto. Foes stay clear of every base so no
        // earlier gate (breach, blocking, home hold) can answer first: the
        // only FIGHT on this board comes from the new gate.
        FakeUnit main = fake(AUnitType.Protoss_Nexus, 20, 20);
        FakeUnit natural = fake(AUnitType.Protoss_Nexus, 38, 20);
        FakeUnit third = fake(AUnitType.Protoss_Nexus, 20, 60);
        FakeUnit[] ball = zealots(16, 36, 22);
        FakeUnit[] foes = lings(62, 90, 80);
        List<FakeUnit> ours = new ArrayList<>();
        ours.add(main);
        ours.add(natural);
        ours.add(third);
        for (FakeUnit z : ball) ours.add(z);

        try (MockedStatic<Chokes> ignored = mockChokeAt(50, 20)) {
            world(3, fakeOurs(ours.toArray(new FakeUnit[0])), fakeEnemies(foes), () -> {
                if (atlantis.game.A.now() != 2) return;
                discover(foes);
                addToAlpha(ball);

                assertTrue(ClusterTactic.weakEarlyVsZergWithoutDragoons(),
                    "fixture check: the veto would fire here on its own");
                assertEquals(FightOrFlight.Tactic.FIGHT, ClusterTactic.decide(),
                    "committed natural defense outranks the early skirmish veto");
            });
        }
    }
}
