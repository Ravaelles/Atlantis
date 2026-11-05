package tests.unit;

import atlantis.combat.scout.ScoutSeparateFromCloseEnemies;
import atlantis.game.A;
import atlantis.map.position.APosition;
import atlantis.units.AUnitType;
import bwapi.Race;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A healthy scout vs one pacing enemy worker must not flip-flop every
 * frame (measured live: HoldCourse/Separate alternating every 1-7f,
 * the scout vibrating in its own base on a potential building site).
 * The evade latches briefly once engaged and releases only outside a
 * wider exit band.
 */
@Tag("combat")
public class ScoutEvadeHysteresisTest extends WorldStubForTests {

    @Override
    public Race initRace() {
        return Race.Protoss;
    }

    @Test
    public void pacedWorkerDoesNotFlapTheEvade() {
        FakeUnit scout = fake(AUnitType.Protoss_Probe, 30, 20);
        FakeUnit worker = fake(AUnitType.Protoss_Probe, 32, 20);
        worker.setEnemy();
        ScoutSeparateFromCloseEnemies.clearEvadeLatchForTests();

        world(40, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 7, 44),
            scout
        ), fakeEnemies(worker), () -> {
            int now = A.now();
            if (now < 2 || now > 32) return;
            if (now == 2) resetFlips();

            // Pace across the margin (enter ~4 tiles escorted by the
            // worker's own range): even frames inside it, odd frames in
            // the exit band (outside enter, inside exit+2). Without the
            // latch applies() flaps every frame; latched it holds until
            // the worker truly leaves.
            if (now % 2 == 0) worker.position = APosition.create(32, 20);
            else worker.position = APosition.create(35, 20);

            ScoutSeparateFromCloseEnemies separate = new ScoutSeparateFromCloseEnemies(scout);
            transitions(separate.applies());
        });

        assertTrue(flips() <= 3,
            "a pacing worker must not flap the evade every frame, flips=" + flips());
    }

    @Test
    public void departedWorkerReleasesTheEvade() {
        FakeUnit scout = fake(AUnitType.Protoss_Probe, 30, 20);
        FakeUnit worker = fake(AUnitType.Protoss_Probe, 32, 20);
        worker.setEnemy();
        ScoutSeparateFromCloseEnemies.clearEvadeLatchForTests();

        world(40, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 7, 44),
            scout
        ), fakeEnemies(worker), () -> {
            int now = A.now();
            if (now < 2 || now > 32) return;
            if (now == 2) resetFlips();

            if (now < 10) worker.position = APosition.create(32, 20);
            else worker.position = APosition.create(45, 20);

            ScoutSeparateFromCloseEnemies separate = new ScoutSeparateFromCloseEnemies(scout);
            transitions(separate.applies());
        });

        assertTrue(flips() <= 3, "flips=" + flips());
        assertTrue(lastSeen(), "released once the worker truly left");
    }

    // ---- flip counting across the frame callbacks ----

    private static Boolean last = null;
    private static int flipCount = 0;
    private static boolean lastSeenReleased = false;

    private static void resetFlips() {
        last = null;
        flipCount = 0;
        lastSeenReleased = false;
    }

    private static void transitions(boolean applies) {
        if (last != null && last != applies) flipCount++;
        last = applies;
        if (!applies) lastSeenReleased = true;
    }

    private static int flips() {
        return flipCount;
    }

    private static boolean lastSeen() {
        return lastSeenReleased;
    }
}
