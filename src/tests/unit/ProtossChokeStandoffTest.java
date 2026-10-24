package tests.unit;

import atlantis.battle.BattleCommander;
import atlantis.battle.cluster.Alpha;
import atlantis.battle.cluster.ClusterBreach;
import atlantis.battle.cluster.ClusterRally;
import atlantis.battle.cluster.FightOrFlight;
import atlantis.battle.retreating.RetreatFromLostFight;
import atlantis.game.AGame;
import atlantis.game.player.Enemy;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnitType;
import bwapi.Race;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeEnemies;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

/**
 * Owner doctrine (vs Protoss): with 4+ combat units the army never
 * retreats into the base - it holds the main choke as usual, even
 * when outmatched. A retreat into the mineral line piles the army
 * onto itself and stops the fight (tested loss vs Protoss: the line
 * stood at the choke, then fell back home and went silent).
 */
@Tag("combat")
public class ProtossChokeStandoffTest extends WorldStubForTests {

    @Test
    public void fourVsProtossRallyAtChokeNotHome() {
        enemyRaceInWorld = Race.Protoss;
        aGame.when(AGame::isUms).thenReturn(false);

        FakeUnit main = fake(AUnitType.Protoss_Nexus, 20, 20);
        FakeUnit a = fake(AUnitType.Protoss_Zealot, 60, 60);
        FakeUnit b = fake(AUnitType.Protoss_Zealot, 61, 60);
        FakeUnit c = fake(AUnitType.Protoss_Zealot, 60, 61);
        FakeUnit d = fake(AUnitType.Protoss_Zealot, 61, 61);
        FakeUnit foe = fake(AUnitType.Protoss_Dragoon, 90, 90);
        foe.setEnemy();

        world(1, fakeOurs(main, a, b, c, d), fakeEnemies(foe), () -> {
            Alpha.get().addUnit(a);
            Alpha.get().addUnit(b);
            Alpha.get().addUnit(c);
            Alpha.get().addUnit(d);
            try {
                assertTrue(Enemy.protoss(), "test runs vs Protoss: " + Enemy.protoss());

                HasPosition standoff = ClusterRally.chokeStandoffPoint();
                assertNotNull(standoff, "the stub world has a main choke to hold");

                boolean fled = RetreatFromLostFight.retreat(
                    a, FightOrFlight.Tactic.FLEE, null, a);
                assertTrue(fled, "a FLEE still issues a retreat");

                HasPosition dest = a.targetPosition();
                assertNotNull(dest, "the retreat names a destination");
                assertTrue(dest.distTo(standoff) < 1,
                    "the rally stands off at the choke, not inside the base: dest "
                        + dest + " vs standoff " + standoff);
                assertTrue(standoff.distTo(ClusterBreach.breachRallyPoint()) >= 1,
                    "standoff and breach-rally are different answers: " + standoff);
            }
            finally {
                Alpha.get().removeUnit(a);
                Alpha.get().removeUnit(b);
                Alpha.get().removeUnit(c);
                Alpha.get().removeUnit(d);
            }
        });
    }
}
