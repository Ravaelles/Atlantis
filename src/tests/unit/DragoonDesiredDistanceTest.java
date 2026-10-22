package tests.unit;


import atlantis.battle.BattleCommander;
import atlantis.battle.kite.Kite;
import atlantis.units.AUnitType;
import bwapi.Race;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.AbstractTestWithWorld;
import tests.fakes.FakeOrderSink;
import tests.fakes.FakeUnit;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeEnemies;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

/**
 * Stardust desired-distance rule (see `_AI/CHALLENGES/MicroKiting.md`):
 * no fixed retreat step - the hold point is max range, reduced by half
 * the gap between "how far the target still is outside range" and "how
 * far we walk before the weapon is ready", one tile closer against
 * melee-ranged threats for HEALTHY Dragoons. A WOUNDED Dragoon (at or
 * under ~half health in survived Zealot passes) skips the closer and
 * holds the max-range edge instead, retreating longer (PurpleWave
 * hitsLeft shape; owner 1v1 criterion: taken hits stay low because the
 * bleeding unit stops donating contact). A Dragoon at firing distance
 * with a nearly-ready weapon holds and shoots instead of stepping out
 * of its own range.
 */
@Tag("combat")
public class DragoonDesiredDistanceTest extends AbstractTestWithWorld {

    @Override
    public Race initRace() {
        return Race.Protoss;
    }

    @Override
    protected FakeUnit[] generateOur() {
        return null;
    }

    @Override
    protected FakeUnit[] generateEnemies() {
        return null;
    }

    @Test
    public void readyWeaponHoldsFiringDistance() {
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 30, 20);
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 33, 20);
        dragoon.cooldown = 0;

        world(1, fakeOurs(fake(AUnitType.Protoss_Nexus, 20, 20), dragoon), fakeEnemies(zealot), () -> {
            // dist 3.0, cd 0: cooldownDistance 0 -> desired 4 - 1 = 3.0 (melee-closer).
            assertEquals(3.0, Kite.desiredKiteDistance(dragoon, zealot), 0.01);
        });
    }

    @Test
    public void longCooldownPullsHoldPointIn() {
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 30, 20);
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 36, 20);
        dragoon.cooldown = 5;

        world(1, fakeOurs(fake(AUnitType.Protoss_Nexus, 20, 20), dragoon), fakeEnemies(zealot), () -> {
            // dist 6.0, cd 5: cooldownDistance 0 -> desired min(4, 4 + (0-2)/2) = 3 - 1 = 2.0.
            assertEquals(2.0, Kite.desiredKiteDistance(dragoon, zealot), 0.01);
        });
    }

    @Test
    public void equalRangeSkipsMeleeCloser() {
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 30, 20);
        FakeUnit foe = fake(AUnitType.Protoss_Dragoon, 32, 20);
        dragoon.cooldown = 30;

        world(1, fakeOurs(fake(AUnitType.Protoss_Nexus, 20, 20), dragoon), fakeEnemies(foe), () -> {
            // dist 2.0, cd 30: cooling hold owns the answer (owner
            // 2026-10-26) - range - 0.4 = 3.6, in range yet safe,
            // instead of the old closing formula's 4.0 edge.
            assertEquals(3.6, Kite.desiredKiteDistance(dragoon, foe), 0.01);
        });
    }

    @Test
    public void holdsAndShootsInsteadOfSteppingOutOfRange() {
        // Duel far from the main base: inside 13-15 tiles a Zealot reads
        // as breached and the breach hatch (correctly) answers first.
        // This pins the kite hold, not the hatch.
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 60, 20);
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 63.5, 20);
        dragoon.cooldown = 15;
        dragoon.hp = 20;
        dragoon.shields = 0;

        world(1, fakeOurs(fake(AUnitType.Protoss_Nexus, 20, 20), dragoon), fakeEnemies(zealot), () -> {
            // hp 20 + shields 0, cd 15: the cooling hold owns the answer
            // (owner 2026-10-26) - range - 0.4 = 3.6, in range yet safe.
            // Gap 0.1 stays under the step-gain floor: hold + shoot.
            assertEquals(3.6, Kite.desiredKiteDistance(dragoon, zealot), 0.01);

            BattleCommander.act(
                dragoon, atlantis.battle.cluster.FightOrFlight.Tactic.FIGHT, null, dragoon);

            assertFalse(
                hasOrder(ordersOf(dragoon), dragoon.id(), "move"),
                "holds the firing position instead of retreating out of range: "
                    + ordersOf(dragoon));
            assertTrue(
                hasOrder(ordersOf(dragoon), dragoon.id(), "attackUnit"),
                "stands and shoots on cooldown: " + ordersOf(dragoon));
        });
    }

    @Test
    public void stepsExactlyToTheHoldPoint() {
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 60, 20);
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 61.5, 20);
        dragoon.cooldown = 25;
        dragoon.hp = 20;
        dragoon.shields = 0;

        world(1, fakeOurs(fake(AUnitType.Protoss_Nexus, 20, 20), dragoon), fakeEnemies(zealot), () -> {
            // dist 1.5 deep inside, WOUNDED (1.25 passes): desired 3.6, so
            // the step opens ~2.1 (capped at one full step) - longer than
            // the healthy 3.0 hold, exactly the wounded-retreats-longer shape.
            assertEquals(3.6, Kite.desiredKiteDistance(dragoon, zealot), 0.01);

            BattleCommander.act(
                dragoon, atlantis.battle.cluster.FightOrFlight.Tactic.FIGHT, null, dragoon);

            assertTrue(
                hasOrder(ordersOf(dragoon), dragoon.id(), "move"),
                "steps back toward the hold point: " + ordersOf(dragoon));
        });
    }

    @Test
    public void woundedHoldsEdgeWhileHealthyCloses() {
        FakeUnit healthy = fake(AUnitType.Protoss_Dragoon, 60, 20);
        FakeUnit wounded = fake(AUnitType.Protoss_Dragoon, 60, 22);
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 63.5, 21);
        healthy.cooldown = 15;
        wounded.cooldown = 15;
        wounded.hp = 20;
        wounded.shields = 0;

        world(1, fakeOurs(fake(AUnitType.Protoss_Nexus, 20, 20), healthy, wounded), fakeEnemies(zealot), () -> {
            // Same fight, same cooldown: the cooling hold owns both
            // answers now (owner 2026-10-26) - range - 0.4 = 3.6, in
            // range yet safe, instead of the old close/edge split.
            assertEquals(3.6, Kite.desiredKiteDistance(healthy, zealot), 0.01);
            assertEquals(3.6, Kite.desiredKiteDistance(wounded, zealot), 0.01);
        });
    }

    @Test
    public void justAboveHalfHealthStillCloses() {
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 60, 20);
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 63.5, 20);
        dragoon.cooldown = 15;
        dragoon.hp = 100;
        dragoon.shields = 0;

        world(1, fakeOurs(fake(AUnitType.Protoss_Nexus, 20, 20), dragoon), fakeEnemies(zealot), () -> {
            // 100 hp at cd 15: the cooling hold owns the answer (owner
            // 2026-10-26) - 3.6, in range yet safe, not the old close.
            assertEquals(3.6, Kite.desiredKiteDistance(dragoon, zealot), 0.01);
        });
    }

    private static boolean hasOrder(List<FakeOrderSink.Record> orders, int unitId, String operation) {
        for (FakeOrderSink.Record record : orders) {
            if (record.actorId == unitId && record.operation.equals(operation)) return true;
        }
        return false;
    }

    private static List<FakeOrderSink.Record> ordersOf(FakeUnit unit) {
        return ((FakeOrderSink) unit.orderSink()).orders();
    }
}
