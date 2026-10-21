package tests.unit;

import atlantis.battle.strategy.BattleStrategies;
import atlantis.battle.strategy.BattleStrategy;
import atlantis.battle.strategy.ChokeStandoff;
import atlantis.battle.strategy.DuelKite;
import atlantis.battle.strategy.SquadScout;
import atlantis.map.position.APosition;
import atlantis.units.AUnitType;
import bwapi.Race;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeEnemies;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

/**
 * Duel kiting (NEXT #169): while the weapon cools, a Dragoon steps off
 * every non-fleeing melee threat to range minus 0.4 - unless the shot is
 * imminent or the hold already stands. Baseless duels only; based play
 * keeps the margin rule untouched.
 */
@Tag("combat")
public class DuelKiteTest extends WorldStubForTests {

    @Override
    public Race initRace() {
        return Race.Protoss;
    }

    @Test
    public void coolingDragoonYieldsToMeleeInsideHold() {
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 30, 20);
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 32, 20);
        zealot.setEnemy();
        zealot.setAngle(Math.PI);
        dragoon.cooldown = 20;

        world(1, fakeOurs(dragoon), fakeEnemies(zealot), () -> {
            assertTrue(DuelKite.shouldYield(dragoon, zealot),
                "cooling weapon + melee inside 3.6 + facing us: yield ground, the shot cannot fire now anyway");
            assertEquals(3.6, DuelKite.holdDistance(dragoon), 0.01, "hold is range minus the 0.4 step");
        });
    }

    @Test
    public void imminentShotDoesNotYield() {
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 30, 20);
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 32, 20);
        zealot.setEnemy();
        zealot.setAngle(Math.PI);
        dragoon.cooldown = 0;

        world(1, fakeOurs(dragoon), fakeEnemies(zealot), () -> {
            assertFalse(DuelKite.shouldYield(dragoon, zealot),
                "a ready weapon shoots - never trade DPS for ground");
        });
    }

    @Test
    public void gateEdgeFlipsYield() {
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 30, 20);
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 32, 20);
        zealot.setEnemy();
        zealot.setAngle(Math.PI);

        world(1, fakeOurs(dragoon), fakeEnemies(zealot), () -> {
            // Stub latency fallback is 3, so the gate sits at 5: 6 still
            // cools (yield), 5 already shoots (hold). The exact edge,
            // not just the far sides.
            dragoon.cooldown = 6;
            assertTrue(DuelKite.shouldYield(dragoon, zealot),
                "cooldown 6 > latency 3 + 2: the shot cannot fire, yield");
            dragoon.cooldown = 5;
            assertFalse(DuelKite.shouldYield(dragoon, zealot),
                "cooldown 5 within the window: hold and shoot");
        });
    }

    @Test
    public void fleeingThreatIsChasedNotYielded() {
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 30, 20);
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 32, 20);
        zealot.setEnemy();
        zealot.setAngle(0);
        zealot.lastCommand = "Move";
        dragoon.cooldown = 20;

        world(1, fakeOurs(dragoon), fakeEnemies(zealot), () -> {
            assertFalse(DuelKite.shouldYield(dragoon, zealot),
                "threat shows its back and runs: chase, don't yield ground to a runner");
        });
    }

    @Test
    public void beyondHoldDistanceNoYield() {
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 30, 20);
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 34, 20);
        zealot.setEnemy();
        zealot.setAngle(Math.PI);
        dragoon.cooldown = 20;

        world(1, fakeOurs(dragoon), fakeEnemies(zealot), () -> {
            assertFalse(DuelKite.shouldYield(dragoon, zealot),
                "at 4 tiles the 3.6 hold already stands - nothing to yield");
        });
    }

    @Test
    public void nonMeleeNoYield() {
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 30, 20);
        FakeUnit foe = fake(AUnitType.Protoss_Dragoon, 32, 20);
        foe.setEnemy();
        dragoon.cooldown = 20;

        world(1, fakeOurs(dragoon), fakeEnemies(foe), () -> {
            assertFalse(DuelKite.shouldYield(dragoon, foe),
                "ranged threats keep the margin rule - the standoff is melee-only");
        });
    }

    @Test
    public void basedMapsDontDuelKite() {
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 30, 20);
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 32, 20);
        zealot.setEnemy();
        zealot.setAngle(Math.PI);
        dragoon.cooldown = 20;

        world(1, fakeOurs(fake(AUnitType.Protoss_Nexus, 20, 20), dragoon), fakeEnemies(zealot), () -> {
            assertFalse(DuelKite.shouldYield(dragoon, zealot),
                "based play keeps the margin rule untouched - duels only");
        });
    }

    @Test
    public void deciderPicksDuelOnBaselessSkirmish() {
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 30, 20);
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 32, 20);
        zealot.setEnemy();

        world(1, fakeOurs(dragoon), fakeEnemies(zealot), () -> {
            assertTrue(BattleStrategies.pick(dragoon, null) == BattleStrategy.DUEL_KITE,
                "baseless 1v1 is a duel");
        });
    }

    @Test
    public void deciderHoldsChokeWithoutSuperiority() {
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 30, 20);
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 32, 20);
        zealot.setEnemy();

        world(1, fakeOurs(fake(AUnitType.Protoss_Nexus, 20, 20), dragoon), fakeEnemies(zealot), () -> {
            assertTrue(BattleStrategies.pick(dragoon, null) == BattleStrategy.CHOKE_HOLD,
                "based 1v1 at a contested choke without superiority: hold the standoff, don't push");
        });
    }

    @Test
    public void yieldStepPicksScoredDestination() {
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 30, 20);
        FakeUnit ling = fake(AUnitType.Zerg_Zergling, 32, 20);
        ling.setEnemy();
        dragoon.cooldown = 20;

        world(1, fakeOurs(dragoon), fakeEnemies(ling), () -> {
            APosition dest = DuelKite.yieldStep(dragoon, ling,
                (x, y) -> true, java.util.Collections.emptyList(), java.util.Collections.emptyList());
            assertNotNull(dest, "open field always steps somewhere");
            assertTrue(dest.distTo(ling) > dragoon.distTo(ling),
                "the step opens ground, not closes it: " + dest);
        });
    }

    @Test
    public void yieldStepIsNullWhenBoxed() {
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 30, 20);
        FakeUnit ling = fake(AUnitType.Zerg_Zergling, 32, 20);
        ling.setEnemy();
        dragoon.cooldown = 20;

        world(1, fakeOurs(dragoon), fakeEnemies(ling), () -> {
            APosition dest = atlantis.battle.escape.EscapeDestination.pick(dragoon, ling, 2,
                (x, y) -> false, java.util.Collections.emptyList(), java.util.Collections.emptyList());
            assertNull(dest, "no walkable step: the caller falls back, not into a wall");
        });
    }

    @Test
    public void deciderFallsThroughToOpenField() {
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 60, 60);

        world(1, fakeOurs(fake(AUnitType.Protoss_Nexus, 20, 20), dragoon), fakeEnemies(), () -> {
            assertTrue(BattleStrategies.pick(dragoon, null) == BattleStrategy.OPEN_FIELD,
                "no enemy anywhere near a choke: the standing fight");
        });
    }

    @Test
    public void chokeHoldPointStandsTenTilesOffCenter() {
        APosition center = APosition.create(50, 50);
        APosition ours = APosition.create(50, 70);

        APosition hold = ChokeStandoff.holdPoint(center, ours);

        assertTrue(Math.abs(hold.distTo(center) - ChokeStandoff.STANDOFF_TILES) < 0.6,
            "standoff is ten tiles off the choke: " + hold.distTo(center));
        assertTrue(hold.distTo(ours) < center.distTo(ours),
            "on our side of the center");
    }

    @Test
    public void mayAdvanceNeedsCrushingSuperiority() {
        assertTrue(ChokeStandoff.mayAdvance(4, 2), "4 vs 2 pushes");
        assertTrue(ChokeStandoff.mayAdvance(2, 0), "empty choke pushes");
        assertFalse(ChokeStandoff.mayAdvance(2, 2), "even fight at a choke holds");
        assertFalse(ChokeStandoff.mayAdvance(1, 2), "outnumbered at a choke holds");
    }

    @Test
    public void baitPickIsLowestId() {
        FakeUnit a = fake(AUnitType.Protoss_Dragoon, 30, 20);
        FakeUnit b = fake(AUnitType.Protoss_Dragoon, 31, 21);
        FakeUnit c = fake(AUnitType.Protoss_Dragoon, 29, 19);

        world(1, fakeOurs(a, b, c), fakeEnemies(), () -> {
            assertTrue(SquadScout.pickBait(atlantis.units.select.Select.ourCombatUnits()) == a,
                "the whole line must agree on one bait: the lowest id");
        });
    }
}
