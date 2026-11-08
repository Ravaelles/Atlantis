package tests.unit;

import atlantis.battle.cluster.Alpha;
import atlantis.combat.cluster.ClusterTactic;
import atlantis.combat.cluster.FightOrFlight;
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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeEnemies;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

/**
 * No-flee holds at the chokes (owner 2026-11-09): 2+ Dragoons with the
 * ball within 7 tiles of the main choke stand and fight, and 11+
 * combat units within 8 tiles of the natural choke stand and fight.
 * Both gates live with the other FIGHT bans, ahead of the
 * weak-early-Zerg veto - a skirmish veto must not overrule a
 * committed choke defense.
 */
@Tag("combat")
public class ChokeStandNoFleeTest extends WorldStubForTests {

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

    private static FakeUnit[] dragoons(int count, int tx, int ty) {
        List<FakeUnit> units = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            units.add(fake(AUnitType.Protoss_Dragoon, tx, ty));
        }

        return units.toArray(new FakeUnit[0]);
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
    public void twoDragoonsAtMainChokeStand() {
        FakeUnit main = fake(AUnitType.Protoss_Nexus, 20, 20);
        FakeUnit[] ball = dragoons(2, 45, 20);

        try (MockedStatic<Chokes> ignored = mockChokes(50, 20, 64, 25)) {
            world(3, fakeOurs(main, ball[0], ball[1]), fakeEnemies(), () -> {
                if (atlantis.game.A.now() != 2) return;
                addToAlpha(ball);

                assertTrue(ClusterTactic.dragoonChokeStand(),
                    "2 dragoons 5 tiles from the main choke: only FIGHT");
            });
        }
    }

    @Test
    public void twoDragoonsAtSevenStand() {
        FakeUnit main = fake(AUnitType.Protoss_Nexus, 20, 20);
        FakeUnit[] ball = dragoons(2, 43, 20);

        try (MockedStatic<Chokes> ignored = mockChokes(50, 20, 64, 25)) {
            world(3, fakeOurs(main, ball[0], ball[1]), fakeEnemies(), () -> {
                if (atlantis.game.A.now() != 2) return;
                addToAlpha(ball);

                assertTrue(ClusterTactic.dragoonChokeStand(),
                    "exactly 7 tiles still holds: the rule reads <=");
            });
        }
    }

    @Test
    public void oneDragoonKeepsRetreat() {
        FakeUnit main = fake(AUnitType.Protoss_Nexus, 20, 20);
        FakeUnit[] ball = dragoons(1, 45, 20);

        try (MockedStatic<Chokes> ignored = mockChokes(50, 20, 64, 25)) {
            world(3, fakeOurs(main, ball[0]), fakeEnemies(), () -> {
                if (atlantis.game.A.now() != 2) return;
                addToAlpha(ball);

                assertFalse(ClusterTactic.dragoonChokeStand(),
                    "a lone dragoon is not a choke stand");
            });
        }
    }

    @Test
    public void dragoonsFarFromChokeKeepRetreat() {
        FakeUnit main = fake(AUnitType.Protoss_Nexus, 20, 20);
        FakeUnit[] ball = dragoons(2, 42, 20);

        try (MockedStatic<Chokes> ignored = mockChokes(50, 20, 64, 25)) {
            world(3, fakeOurs(main, ball[0], ball[1]), fakeEnemies(), () -> {
                if (atlantis.game.A.now() != 2) return;
                addToAlpha(ball);

                assertFalse(ClusterTactic.dragoonChokeStand(),
                    "8 tiles out: the distance gate stays shut");
            });
        }
    }

    @Test
    public void elevenAtNaturalChokeStand() {
        FakeUnit main = fake(AUnitType.Protoss_Nexus, 20, 20);
        FakeUnit[] ball = zealots(11, 60, 25);

        try (MockedStatic<Chokes> ignored = mockChokes(50, 20, 64, 25)) {
            world(3, fakeOurs(concat(main, ball)), fakeEnemies(), () -> {
                if (atlantis.game.A.now() != 2) return;
                addToAlpha(ball);

                assertTrue(ClusterTactic.naturalChokeStand(),
                    "11 units 4 tiles from the natural choke: only FIGHT");
            });
        }
    }

    @Test
    public void tenAtNaturalChokeKeepRetreat() {
        FakeUnit main = fake(AUnitType.Protoss_Nexus, 20, 20);
        FakeUnit[] ball = zealots(10, 60, 25);

        try (MockedStatic<Chokes> ignored = mockChokes(50, 20, 64, 25)) {
            world(3, fakeOurs(concat(main, ball)), fakeEnemies(), () -> {
                if (atlantis.game.A.now() != 2) return;
                addToAlpha(ball);

                assertFalse(ClusterTactic.naturalChokeStand(),
                    "10 is not over 10: the headcount gate stays shut");
            });
        }
    }

    @Test
    public void naturalChokeAtEightKeepsRetreat() {
        FakeUnit main = fake(AUnitType.Protoss_Nexus, 20, 20);
        FakeUnit[] ball = zealots(11, 56, 25);

        try (MockedStatic<Chokes> ignored = mockChokes(50, 20, 64, 25)) {
            world(3, fakeOurs(concat(main, ball)), fakeEnemies(), () -> {
                if (atlantis.game.A.now() != 2) return;
                addToAlpha(ball);

                assertFalse(ClusterTactic.naturalChokeStand(),
                    "exactly 8 tiles: the rule reads strictly less than 8");
            });
        }
    }

    @Test
    public void dragoonChokeStandDecidesFight() {
        FakeUnit main = fake(AUnitType.Protoss_Nexus, 20, 20);
        FakeUnit[] goons = dragoons(2, 45, 20);
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 45, 20);
        FakeUnit[] foes = lings(30, 55, 28);
        List<FakeUnit> ours = new ArrayList<>();
        ours.add(main);
        for (FakeUnit g : goons) ours.add(g);
        ours.add(zealot);

        try (MockedStatic<Chokes> ignored = mockChokes(50, 20, 64, 25)) {
            world(3, fakeOurs(ours.toArray(new FakeUnit[0])), fakeEnemies(foes), () -> {
                if (atlantis.game.A.now() != 2) return;
                discover(foes);
                addToAlpha(goons);
                Alpha.get().addUnit(zealot);

                assertEquals(FightOrFlight.Tactic.FIGHT, ClusterTactic.decide(),
                    "ranged ball on the main choke holds it, however lopsided");
            });
        }
    }

    @Test
    public void awayFromChokeTheSameBoardFlees() {
        FakeUnit main = fake(AUnitType.Protoss_Nexus, 20, 20);
        FakeUnit[] goons = dragoons(2, 30, 20);
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 30, 20);
        FakeUnit[] foes = lings(30, 40, 28);
        List<FakeUnit> ours = new ArrayList<>();
        ours.add(main);
        for (FakeUnit g : goons) ours.add(g);
        ours.add(zealot);

        try (MockedStatic<Chokes> ignored = mockChokes(50, 20, 64, 25)) {
            world(3, fakeOurs(ours.toArray(new FakeUnit[0])), fakeEnemies(foes), () -> {
                if (atlantis.game.A.now() != 2) return;
                discover(foes);
                addToAlpha(goons);
                Alpha.get().addUnit(zealot);

                assertFalse(ClusterTactic.dragoonChokeStand(),
                    "fixture check: 20 tiles out the gate stays shut");
                assertEquals(FightOrFlight.Tactic.FLEE, ClusterTactic.decide(),
                    "the same losing ball off the choke still retreats");
            });
        }
    }

    @Test
    public void naturalChokeStandDecidesFight() {
        FakeUnit main = fake(AUnitType.Protoss_Nexus, 20, 20);
        FakeUnit[] ball = zealots(11, 60, 25);
        FakeUnit[] foes = lings(30, 70, 33);
        List<FakeUnit> ours = new ArrayList<>();
        ours.add(main);
        for (FakeUnit z : ball) ours.add(z);

        try (MockedStatic<Chokes> ignored = mockChokes(50, 20, 64, 25)) {
            world(3, fakeOurs(ours.toArray(new FakeUnit[0])), fakeEnemies(foes), () -> {
                if (atlantis.game.A.now() != 2) return;
                discover(foes);
                addToAlpha(ball);

                assertTrue(ClusterTactic.weakEarlyVsZergWithoutDragoons(),
                    "fixture check: the veto would fire here on its own");
                assertEquals(FightOrFlight.Tactic.FIGHT, ClusterTactic.decide(),
                    "a committed force at the natural choke holds it");
            });
        }
    }

    @Test
    public void awayFromNaturalChokeTheSameBoardFlees() {        FakeUnit main = fake(AUnitType.Protoss_Nexus, 20, 20);
        FakeUnit[] ball = zealots(11, 40, 25);
        FakeUnit[] foes = lings(30, 50, 33);
        List<FakeUnit> ours = new ArrayList<>();
        ours.add(main);
        for (FakeUnit z : ball) ours.add(z);

        try (MockedStatic<Chokes> ignored = mockChokes(50, 20, 64, 25)) {
            world(3, fakeOurs(ours.toArray(new FakeUnit[0])), fakeEnemies(foes), () -> {
                if (atlantis.game.A.now() != 2) return;
                discover(foes);
                addToAlpha(ball);

                assertFalse(ClusterTactic.naturalChokeStand(),
                    "fixture check: 24 tiles out the gate stays shut");
                assertEquals(FightOrFlight.Tactic.FLEE, ClusterTactic.decide(),
                    "the same force off the choke still retreats");
            });
        }
    }

    @Test
    public void twoBasesNearMainHold() {
        FakeUnit main = fake(AUnitType.Protoss_Nexus, 20, 20);
        FakeUnit natural = fake(AUnitType.Protoss_Nexus, 38, 20);
        FakeUnit[] ball = zealots(3, 30, 20);

        try (MockedStatic<Chokes> ignored = mockChokes(50, 20, 64, 25)) {
            world(3, fakeOurs(concat(main, concat(natural, ball))), fakeEnemies(), () -> {
                if (atlantis.game.A.now() != 2) return;
                addToAlpha(ball);

                assertTrue(ClusterTactic.twoBaseHoldNearMain(),
                    "two bases, ball 10 ground from the main: no retreat");
            });
        }
    }

    @Test
    public void oneBaseKeepsRetreat() {
        FakeUnit main = fake(AUnitType.Protoss_Nexus, 20, 20);
        FakeUnit[] ball = zealots(3, 30, 20);

        try (MockedStatic<Chokes> ignored = mockChokes(50, 20, 64, 25)) {
            world(3, fakeOurs(concat(main, ball)), fakeEnemies(), () -> {
                if (atlantis.game.A.now() != 2) return;
                addToAlpha(ball);

                assertFalse(ClusterTactic.twoBaseHoldNearMain(),
                    "one base: the gate stays shut");
            });
        }
    }

    @Test
    public void twoBasesFarFromMainKeepRetreat() {
        FakeUnit main = fake(AUnitType.Protoss_Nexus, 20, 20);
        FakeUnit natural = fake(AUnitType.Protoss_Nexus, 38, 20);
        FakeUnit[] ball = zealots(3, 70, 20);

        try (MockedStatic<Chokes> ignored = mockChokes(50, 20, 64, 25)) {
            world(3, fakeOurs(concat(main, concat(natural, ball))), fakeEnemies(), () -> {
                if (atlantis.game.A.now() != 2) return;
                addToAlpha(ball);

                assertFalse(ClusterTactic.twoBaseHoldNearMain(),
                    "50 ground out: the distance gate stays shut");
            });
        }
    }

    @Test
    public void twoBasesNearMainDecideFight() {
        FakeUnit main = fake(AUnitType.Protoss_Nexus, 20, 20);
        FakeUnit natural = fake(AUnitType.Protoss_Nexus, 38, 20);
        FakeUnit[] ball = zealots(3, 30, 20);
        FakeUnit[] foes = lings(30, 40, 28);
        List<FakeUnit> ours = new ArrayList<>();
        ours.add(main);
        ours.add(natural);
        for (FakeUnit z : ball) ours.add(z);

        try (MockedStatic<Chokes> ignored = mockChokes(50, 20, 64, 25)) {
            world(3, fakeOurs(ours.toArray(new FakeUnit[0])), fakeEnemies(foes), () -> {
                if (atlantis.game.A.now() != 2) return;
                discover(foes);
                addToAlpha(ball);

                assertTrue(ClusterTactic.weakEarlyVsZergWithoutDragoons(),
                    "fixture check: the veto would fire here on its own");
                assertEquals(FightOrFlight.Tactic.FIGHT, ClusterTactic.decide(),
                    "two bases with the ball home hold instead of fleeing");
            });
        }
    }

    @Test
    public void twoBasesFarFromMainDecideFlee() {
        FakeUnit main = fake(AUnitType.Protoss_Nexus, 20, 20);
        FakeUnit natural = fake(AUnitType.Protoss_Nexus, 38, 20);
        FakeUnit[] ball = zealots(3, 70, 20);
        FakeUnit[] foes = lings(30, 80, 28);
        List<FakeUnit> ours = new ArrayList<>();
        ours.add(main);
        ours.add(natural);
        for (FakeUnit z : ball) ours.add(z);

        try (MockedStatic<Chokes> ignored = mockChokes(50, 20, 64, 25)) {
            world(3, fakeOurs(ours.toArray(new FakeUnit[0])), fakeEnemies(foes), () -> {
                if (atlantis.game.A.now() != 2) return;
                discover(foes);
                addToAlpha(ball);

                assertFalse(ClusterTactic.twoBaseHoldNearMain(),
                    "fixture check: 50 ground out the gate stays shut");
                assertEquals(FightOrFlight.Tactic.FLEE, ClusterTactic.decide(),
                    "the same ball far from home still retreats");
            });
        }
    }

    private static FakeUnit[] concat(FakeUnit first, FakeUnit[] rest) {
        List<FakeUnit> out = new ArrayList<>();
        out.add(first);
        for (FakeUnit u : rest) out.add(u);

        return out.toArray(new FakeUnit[0]);
    }
}
