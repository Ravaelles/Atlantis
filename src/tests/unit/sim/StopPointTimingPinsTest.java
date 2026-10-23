package tests.unit.sim;

import atlantis.units.AUnitType;
import atlantis.units.special.SleepUntilFrameGate;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeEnemies;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

/**
 * NEXT #186: the stop-point rule consumes three timing numbers - order
 * latency, attack windup, braking distance. Each is pinned here against
 * the model that will feed the formula, none taken from memory:
 * latency is the stub fallback the duel gate already depends on,
 * windup is SimWorld's documented model seam (not engine data), braking
 * is the mover's measured decay. The future formula reads these pins,
 * not remembered constants.
 */
@Tag("combat")
public class StopPointTimingPinsTest extends WorldStubForTests {

    @Test
    public void stubLatencyFallbackIsThreeFrames() {
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 30, 20);

        world(1, fakeOurs(dragoon), fakeEnemies(), () -> {
            assertEquals(3, SleepUntilFrameGate.latencyFrames(),
                "the stub answers 0 remaining frames; the fallback is the documented floor "
                    + "(the duel yield gate hard-depends on it)");
        });
    }

    @Test
    public void dragoonDamageLandsOnTheSimWindupFrame() {
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 10, 10);
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 12, 10);
        zealot.setEnemy();
        dragoon.cooldown = 0;
        dragoon.lastCommand = "AttackUnit";
        dragoon.target = zealot;

        SimArena arena = new SimArena(20 * 32, 12 * 32);
        SimWorld world = new SimWorld(new FakeUnit[]{dragoon}, new FakeUnit[]{zealot}, arena);
        try {
            int before = zealot.hp + zealot.shields;
            int dropTick = -1;
            for (int tick = 0; tick < 20; tick++) {
                world.tick(() -> {
                });
                if (zealot.hp + zealot.shields < before) {
                    dropTick = tick;
                    break;
                }
            }
            // Measured 8: WINDUP keys ("Protoss_Dragoon") match
            // type().fullName() (prefixes intact, unlike name() which
            // strips them). A 2026-10-26 fix corrected a
            // getOrDefault(name(), 6) lookup under which every type fell
            // through to default 6 and the map entries were dead.
            assertEquals(8, dropTick,
                "dragoon damage lands 8 ticks after the attack starts (sim WINDUP model seam, "
                    + "PurpleWave-derived - not engine data; a changed value fails loudly here)");
            assertEquals(dragoon.damageAgainst(zealot), before - (zealot.hp + zealot.shields),
                "one windup cycle deals exactly one damage packet");
        }
        finally {
            world.close();
        }
    }

    @Test
    public void fullSpeedMoverStopsInsideHalfATile() {
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 10, 10);
        dragoon.targetPosition = atlantis.map.position.APosition.create(100, 10);
        dragoon.lastCommand = "Move";

        SimArena arena = new SimArena(200 * 32, 12 * 32);
        SimWorld world = new SimWorld(new FakeUnit[]{dragoon}, new FakeUnit[]{}, arena);
        try {
            double pxPerTile = 32.0;
            for (int i = 0; i < 40; i++) world.tick(() -> {
            });
            double[] before = world.precisePosition(dragoon);
            world.tick(() -> {
            });
            double[] after = world.precisePosition(dragoon);
            double cruiseV = after[0] - before[0];
            assertEquals(5.0, cruiseV, 0.3,
                "cruising mover covers top speed per tick (engine 5.0 px/f, pinned behaviorally)");

            dragoon.lastCommand = "None";
            double stopX = world.precisePosition(dragoon)[0];
            int stopTick = -1;
            double lastMoved = 0;
            for (int tick = 0; tick < 40; tick++) {
                world.tick(() -> {
                });
                double moved = world.precisePosition(dragoon)[0] - stopX;
                if (tick > 0 && Math.abs(moved - lastMoved) < 0.05) {
                    stopTick = tick;
                    break;
                }
                lastMoved = moved;
            }
            assertTrue(stopTick >= 0, "the mover comes to a full stop after orders cut");
            assertTrue(stopTick < 20, "braking takes a handful of frames, not seconds: " + stopTick);
            double brakingPx = world.precisePosition(dragoon)[0] - stopX;
            System.out.println("BRAKING dragoon v0=" + cruiseV + "px/f stops in "
                + brakingPx + "px over " + stopTick + " ticks");
            assertTrue(brakingPx < 0.5 * pxPerTile,
                "braking distance fits inside half a tile - the stop-point rule can place "
                    + "a stop within the standoff band: " + brakingPx + "px");
            assertTrue(brakingPx > 0,
                "braking covers ground - a stop ordered at full speed is not in place");
        }
        finally {
            world.close();
        }
    }
}
