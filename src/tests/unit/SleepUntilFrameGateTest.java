package tests.unit;

import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import atlantis.units.special.SleepUntilFrameGate;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.AbstractTestWithWorld;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The order-latency gate (owner recommendation, 2026-10-12): after ANY order is issued the
 * unit must sleep until `now + remainingLatency + 2`, and no manager may touch it before
 * that frame.
 *
 * <p>
 * The scenario this pins (measured on OpenBW, NEXT.md #61): a Gather re-issued at
 * {@code lastCommandIssuedAgo=1} was refused by the engine while the same re-issue at
 * {@code >= 3} was accepted - the engine registers a command only after
 * {@code getRemainingLatencyFrames()}. The gate sits at the very top of the Manager chain
 * ({@code Manager.invokeFromParent}), so it protects workers AND combat units with one rule.
 * </p>
 *
 * <p>
 * The stub world has no engine, so the gate's safe fallback of 3 latency frames applies:
 * an order issued at frame F holds the unit through F+4 and releases it on F+5.
 * </p>
 */
@Tag("production")
public class SleepUntilFrameGateTest extends AbstractTestWithWorld {

    private static final int FRAME = 8;
    private static final int HOLD = 5; // latency 3 + margin 2

    private AUnit probe;
    private AUnit zealot;

    @Override
    protected FakeUnit[] generateOur() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 10);
        FakeUnit probeFake = fake(AUnitType.Protoss_Probe, 8.3);
        FakeUnit zealotFake = fake(AUnitType.Protoss_Zealot, 12);

        return new FakeUnit[]{ nexus, probeFake, zealotFake };
    }

    @Override
    protected FakeUnit[] generateEnemies() {
        return new FakeUnit[0];
    }

    private FakeUnit[] worldUnits() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 10);
        FakeUnit probeFake = fake(AUnitType.Protoss_Probe, 8.3);
        FakeUnit zealotFake = fake(AUnitType.Protoss_Zealot, 12);

        return new FakeUnit[]{ nexus, probeFake, zealotFake };
    }

    private void captureUnits() {
        SleepUntilFrameGate.clear();
        probe = Select.ourWorkers().first();
        zealot = Select.ourCombatUnits().first();
        assertNotNull(probe, "stub world must contain a Probe");
        assertNotNull(zealot, "stub world must contain a Zealot");
    }

    @Test
    public void unitIsNotAsleepBeforeAnyOrder() {
        world(FRAME, fakeOurs(worldUnits()), fakeEnemies(new FakeUnit[0]), () -> {
            if (A.now() == 1) captureUnits();

            useFakeTime(2);
            assertFalse(SleepUntilFrameGate.mustSleep(probe));
            assertFalse(SleepUntilFrameGate.mustSleep(zealot));
        });
    }

    @Test
    public void afterOrderUnitSleepsThroughTheLatencyWindow() {
        world(FRAME + HOLD + 2, fakeOurs(worldUnits()), fakeEnemies(new FakeUnit[0]), () -> {
            if (A.now() == 1) captureUnits();

            // The order was issued on FRAME. Frames FRAME..FRAME+HOLD-1: asleep.
            if (A.now() == FRAME) {
                useFakeTime(A.now());
                SleepUntilFrameGate.holdAfterOrder(probe);
                assertTrue(SleepUntilFrameGate.mustSleep(probe), "asleep on the issuing frame");
            }
            if (A.now() == FRAME + 2) {
                useFakeTime(A.now());
                assertTrue(SleepUntilFrameGate.mustSleep(probe),
                    "still asleep mid-window (frame " + A.now() + ")");
            }

            // The window ends on FRAME+HOLD: released.
            if (A.now() == FRAME + HOLD) {
                useFakeTime(A.now());
                assertFalse(SleepUntilFrameGate.mustSleep(probe),
                    "after the latency window the unit must be released");
            }
        });
    }

    @Test
    public void holdAppliesToCombatUnitsToo() {
        world(FRAME + HOLD + 2, fakeOurs(worldUnits()), fakeEnemies(new FakeUnit[0]), () -> {
            if (A.now() == 1) captureUnits();

            if (A.now() == FRAME) {
                SleepUntilFrameGate.holdAfterOrder(zealot);
            }
            if (A.now() == FRAME + 2) {
                useFakeTime(A.now());
                assertTrue(SleepUntilFrameGate.mustSleep(zealot),
                    "a combat unit with an attack in flight sleeps like a worker does");
            }
            if (A.now() == FRAME + HOLD) {
                useFakeTime(A.now());
                assertFalse(SleepUntilFrameGate.mustSleep(zealot));
            }
        });
    }

    @Test
    public void holdsArePerUnit() {
        world(FRAME + HOLD + 2, fakeOurs(worldUnits()), fakeEnemies(new FakeUnit[0]), () -> {
            if (A.now() == 1) captureUnits();

            if (A.now() == FRAME) {
                SleepUntilFrameGate.holdAfterOrder(probe);
            }
            if (A.now() == FRAME + 1) {
                useFakeTime(A.now());
                assertTrue(SleepUntilFrameGate.mustSleep(probe));
                assertFalse(SleepUntilFrameGate.mustSleep(zealot),
                    "only the unit that was ordered sleeps - the rest of the army works");
            }
        });
    }

    @Test
    public void repeatedHoldsReArmTheWindow() {
        world(FRAME + HOLD + 4, fakeOurs(worldUnits()), fakeEnemies(new FakeUnit[0]), () -> {
            if (A.now() == 1) captureUnits();

            // First hold on FRAME (expiry FRAME+HOLD). A re-issue on FRAME+2 re-arms the
            // hold from that frame (expiry FRAME+2+HOLD), so on FRAME+HOLD the unit is
            // STILL held - the original expiry alone would have released it.
            if (A.now() == FRAME) SleepUntilFrameGate.holdAfterOrder(probe);
            if (A.now() == FRAME + 2) SleepUntilFrameGate.holdAfterOrder(probe);

            if (A.now() == FRAME + HOLD) {
                useFakeTime(A.now());
                assertTrue(SleepUntilFrameGate.mustSleep(probe),
                    "a re-issued order re-arms the window from the new frame");
            }
            if (A.now() == FRAME + 2 + HOLD) {
                useFakeTime(A.now());
                assertFalse(SleepUntilFrameGate.mustSleep(probe));
            }
        });
    }

    @Test
    public void stubWorldReportsFallbackLatency() {
        world(FRAME, fakeOurs(worldUnits()), fakeEnemies(new FakeUnit[0]), () -> {
            assertEquals(3, SleepUntilFrameGate.latencyFrames(),
                "no engine in the stub world - the documented fallback of 3 applies, "
                    + "and the kite math (NEXT #154) depends on exactly this value here");
        });
    }
}
