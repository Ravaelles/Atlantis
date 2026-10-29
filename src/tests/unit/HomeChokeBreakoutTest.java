package tests.unit;

import atlantis.combat.strategy.BattleStrategies;
import atlantis.combat.strategy.BattleStrategy;
import atlantis.information.enemy.EnemyUnitsUpdater;
import atlantis.information.generic.Army;
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

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeEnemies;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

/**
 * Home-choke breakout (owner 2026-10-29): at 150%+ strength with fewer
 * than 2 bases the line never holds its own main or natural choke -
 * CHOKE_HOLD and COLLAPSE would park a finished army at home instead
 * of marching out. Geometry mirrors ChokeStandoffTest (choke 28,18,
 * line 11 tiles south, pressers inside the contest ring): without the
 * guard the decider holds, with it the line takes the open field.
 */
@Tag("combat")
public class HomeChokeBreakoutTest extends WorldStubForTests {

    private static FakeUnit dragoon(double tx, double ty) {
        return fake(AUnitType.Protoss_Dragoon, tx, ty);
    }

    private static FakeUnit ling(double tx, double ty) {
        FakeUnit ling = fake(AUnitType.Zerg_Zergling, tx, ty);
        ling.setEnemy();

        return ling;
    }

    private static FakeUnit[] pressers() {
        return new FakeUnit[]{ ling(30, 22), ling(31, 22), ling(30, 23), ling(31, 23) };
    }

    private static FakeUnit[] line() {
        return new FakeUnit[]{
            dragoon(27, 29), dragoon(28, 29), dragoon(29, 29),
            dragoon(27, 30), dragoon(28, 30), dragoon(29, 30)
        };
    }

    private static FakeUnit[] plus(FakeUnit first, FakeUnit[] rest) {
        FakeUnit[] out = new FakeUnit[rest.length + 1];
        out[0] = first;
        System.arraycopy(rest, 0, out, 1, rest.length);

        return out;
    }

    private static void discover(FakeUnit... foes) {
        for (FakeUnit foe : foes) EnemyUnitsUpdater.weDiscoveredEnemyUnit(foe);
    }

    @Test
    public void strongOneBaseArmyBreaksOutThroughMainChoke() {
        FakeChoke home = new FakeChoke(APosition.create(28, 18), 6);
        FakeUnit[] ours = line();
        FakeUnit[] foes = pressers();

        try (MockedStatic<Chokes> chokes = Mockito.mockStatic(Chokes.class)) {
            chokes.when(Chokes::mainChoke).thenReturn(home);
            chokes.when(() -> Chokes.nearestChoke(Mockito.any(), Mockito.anyString())).thenReturn(home);
            chokes.when(() -> Chokes.nearestChoke(Mockito.any())).thenReturn(home);

            world(5, fakeOurs(plus(fake(AUnitType.Protoss_Nexus, 20, 35), ours)), fakeEnemies(foes), () -> {
                if (atlantis.game.A.now() != 2) return;
                discover(foes);

                assertTrue(Army.strength() >= BattleStrategies.BREAKOUT_STRENGTH,
                    "fixture: 6 dragoons vs 4 lings must read 150+, got " + Army.strength());
                assertSame(BattleStrategy.OPEN_FIELD, BattleStrategies.pick(ours[1], null),
                    "one base at 150%+: no choke strategy at our own main choke, march out");
            });
        }
    }

    @Test
    public void secondBaseKeepsTheHold() {
        FakeChoke home = new FakeChoke(APosition.create(28, 18), 6);
        FakeUnit[] ours = line();
        FakeUnit[] foes = pressers();

        try (MockedStatic<Chokes> chokes = Mockito.mockStatic(Chokes.class)) {
            chokes.when(Chokes::mainChoke).thenReturn(home);
            chokes.when(() -> Chokes.nearestChoke(Mockito.any(), Mockito.anyString())).thenReturn(home);
            chokes.when(() -> Chokes.nearestChoke(Mockito.any())).thenReturn(home);

            world(5, plus(fake(AUnitType.Protoss_Nexus, 20, 35),
                plus(fake(AUnitType.Protoss_Nexus, 10, 10), ours)), fakeEnemies(foes), () -> {
                if (atlantis.game.A.now() != 2) return;
                discover(foes);

                assertSame(BattleStrategy.CHOKE_HOLD, BattleStrategies.pick(ours[1], null),
                    "two bases: the choke strategies own the home choke as before");
            });
        }
    }

    @Test
    public void weakArmyStillHoldsTheHomeChoke() {
        FakeChoke home = new FakeChoke(APosition.create(28, 18), 6);
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 28, 29);
        FakeUnit[] foes = pressers();

        try (MockedStatic<Chokes> chokes = Mockito.mockStatic(Chokes.class)) {
            chokes.when(Chokes::mainChoke).thenReturn(home);
            chokes.when(() -> Chokes.nearestChoke(Mockito.any(), Mockito.anyString())).thenReturn(home);
            chokes.when(() -> Chokes.nearestChoke(Mockito.any())).thenReturn(home);

            world(5, fakeOurs(plus(fake(AUnitType.Protoss_Nexus, 20, 35), new FakeUnit[]{ zealot })), fakeEnemies(foes), () -> {
                if (atlantis.game.A.now() != 2) return;
                discover(foes);

                assertTrue(Army.strength() < BattleStrategies.BREAKOUT_STRENGTH,
                    "fixture: 1 zealot vs 4 lings must read under 150, got " + Army.strength());
                assertSame(BattleStrategy.CHOKE_HOLD, BattleStrategies.pick(zealot, null),
                    "under 150%: the hold stands even at our own choke");
            });
        }
    }

    @Test
    public void foreignChokeStillHoldsAtFullStrength() {
        // (53,74) is neither the stub main (13,25) nor the stub natural
        // (28,18): same shape as the breakout fixture, different choke.
        FakeUnit[] ours = new FakeUnit[]{
            dragoon(52, 85), dragoon(53, 85), dragoon(54, 85),
            dragoon(52, 86), dragoon(53, 86), dragoon(54, 86)
        };
        FakeUnit[] foes = new FakeUnit[]{
            ling(55, 77), ling(56, 77), ling(55, 78), ling(56, 78)
        };

        world(5, fakeOurs(plus(fake(AUnitType.Protoss_Nexus, 20, 35), ours)), fakeEnemies(foes), () -> {
            if (atlantis.game.A.now() != 2) return;
            discover(foes);

            assertTrue(Army.strength() >= BattleStrategies.BREAKOUT_STRENGTH,
                "fixture: 6 dragoons vs 4 lings must read 150+, got " + Army.strength());
            assertSame(BattleStrategy.CHOKE_HOLD, BattleStrategies.pick(ours[1], null),
                "not our choke: the hold stands even strong with one base");
        });
    }

    @Test
    public void strongOneBaseArmyBreaksOutThroughNaturalChoke() {
        FakeChoke home = new FakeChoke(APosition.create(28, 18), 6);
        FakeUnit[] ours = line();
        FakeUnit[] foes = pressers();

        try (MockedStatic<Chokes> chokes = Mockito.mockStatic(Chokes.class)) {
            chokes.when(Chokes::natural).thenReturn(home);
            chokes.when(() -> Chokes.nearestChoke(Mockito.any(), Mockito.anyString())).thenReturn(home);
            chokes.when(() -> Chokes.nearestChoke(Mockito.any())).thenReturn(home);

            world(5, fakeOurs(plus(fake(AUnitType.Protoss_Nexus, 20, 35), ours)), fakeEnemies(foes), () -> {
                if (atlantis.game.A.now() != 2) return;
                discover(foes);

                assertSame(BattleStrategy.OPEN_FIELD, BattleStrategies.pick(ours[1], null),
                    "one base at 150%+: no choke strategy at our own natural choke either");
            });
        }
    }
}
