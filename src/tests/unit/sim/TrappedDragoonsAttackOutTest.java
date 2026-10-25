package tests.unit.sim;

import atlantis.battle.BattleCommander;
import atlantis.battle.cluster.FightOrFlight;
import atlantis.map.position.APosition;
import atlantis.units.AUnitType;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.fakes.FakeUnit;
import tests.unit.AbstractTestWithUnits;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeEnemy;

/**
 * NEXT #197: dragoons boxed in the base die surrounded with no room to
 * kite, while the same force marching out meets the pack on open ground
 * and kites it. The pocket is a base interior (walls west/north, mineral
 * line south, mouth facing the incoming pack); the anchor is the only
 * difference between the arms, everything else identical.
 */
@Tag("combat")
public class TrappedDragoonsAttackOutTest extends AbstractTestWithUnits {

    private static final int TILE = 32;

    private static class Outcome {
        int dragoonsDead;
        int zealotsDead;
    }

    private Outcome runScenario(boolean marchOut) {
        SimArena arena = new SimArena(70 * TILE, 40 * TILE);
        // Base interior: east wall, north wall, mineral line south, mouth west.
        arena.block(54 * TILE, 8 * TILE, 3 * TILE, 5 * TILE);
        arena.block(44 * TILE, 7 * TILE, 15 * TILE, 1 * TILE);
        arena.block(44 * TILE, 12 * TILE, 15 * TILE, 1 * TILE);

        List<FakeUnit> dragoons = new ArrayList<>();
        for (double[] spot : new double[][] { { 49, 10 }, { 50, 10 }, { 51, 10 }, { 50, 11 } }) {
            FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, spot[0], spot[1]);
            dragoon.hp = 120;
            dragoon.shields = 80;
            dragoon.cooldown = 0;
            dragoons.add(dragoon);
        }
        List<FakeUnit> zealots = new ArrayList<>();
        for (double[] spot : new double[][] {
            { 30, 9 }, { 31, 9 }, { 32, 9 }, { 33, 9 },
            { 30, 10 }, { 31, 10 }, { 32, 10 }, { 33, 10 } }) {
            FakeUnit zealot = fakeEnemy(AUnitType.Protoss_Zealot, spot[0], spot[1]);
            zealots.add(zealot);
        }

        APosition anchor = marchOut ? APosition.create(25, 12) : APosition.create(50, 10);
        FakeUnit leader = dragoons.get(0);

        SimWorld world = new SimWorld(
            dragoons.toArray(new FakeUnit[0]), zealots.toArray(new FakeUnit[0]), arena);
        try {
            BattleCommander.clearForTests();
            atlantis.util.log.EventLog.clearForTests();

            for (int i = 0; i < 700; i++) {
                world.tick(() -> {
                    for (FakeUnit dragoon : dragoons) {
                        if (dragoon.hp <= 0) continue;
                        BattleCommander.act(
                            dragoon, FightOrFlight.Tactic.FIGHT, anchor, leader);
                    }
                    ZombiePolicies.chaseNearest(zealots, new ArrayList<>(dragoons));
                });
            }

            Outcome outcome = new Outcome();
            for (FakeUnit dragoon : dragoons) if (dragoon.hp <= 0) outcome.dragoonsDead++;
            for (FakeUnit zealot : zealots) if (zealot.hp <= 0) outcome.zealotsDead++;
            System.err.println("TRAPPED marchOut=" + marchOut
                + " dragoonsDead=" + outcome.dragoonsDead
                + " zealotsDead=" + outcome.zealotsDead);
            return outcome;
        }
        finally {
            world.close();
            BattleCommander.clearForTests();
            atlantis.util.log.EventLog.clearForTests();
        }
    }

    @Test
    public void attackOutBeatsHoldingThePocket() {
        Outcome hold = runScenario(false);
        Outcome out = runScenario(true);

        assertTrue(out.dragoonsDead < hold.dragoonsDead,
            "leaving the pocket must save dragoons: out=" + out.dragoonsDead
                + " dead, hold=" + hold.dragoonsDead + " dead");
        assertTrue(hold.dragoonsDead >= 2,
            "the pocket is actually lethal: hold=" + hold.dragoonsDead + " dead");
        assertTrue(out.zealotsDead >= 2,
            "attack means engage on open ground, not flee: out killed " + out.zealotsDead);
    }
}
