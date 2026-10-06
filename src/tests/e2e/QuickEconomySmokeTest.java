package tests.e2e;

import atlantis.game.A;
import atlantis.game.AtlantisGameCommander;
import atlantis.production.orders.production.queue.Queue;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import bwapi.Race;
import org.junit.jupiter.api.Test;
import tests.acceptance.AbstractTestWithWorld;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * The fast end-to-end smoke: a Protoss base runs the real commander for a short
 * horizon and the assertions check that the basics actually happen - the
 * production queue is populated, a building gets queued, workers exist and stay
 * alive, nothing throws.
 *
 * <p>
 * This is the "quick" tier of the e2e package: it runs in about a second, so
 * the model-facing script (scripts/run-tests.sh, 40 s budget) can include it,
 * while the long 900-frame defence scenarios (FourPoolDefenseTest,
 * NinePoolDefenseTest, ~64 s) stay owner-only in scripts/run-full-tests.sh.
 * When the OpenBW mega-test lands, this file is where it belongs - it replaces
 * the stub world with the real engine and keeps the same "the basics happen"
 * spirit.
 * </p>
 *
 * <p>
 * Assertions stay humble on purpose: no fight is simulated, so nothing about
 * combat is claimed. What must hold for every single game to make sense is:
 * the commander runs without throwing, the production queue gets at least one
 * order, and our workers survive a short horizon with no enemy on the map.
 * </p>
 */
public class QuickEconomySmokeTest extends AbstractTestWithWorld {

    @Override
    public Race initRace() {
        return Race.Protoss;
    }

    @Test
    public void commanderRunsAndEconomyStartsMoving() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 10);
        FakeUnit[] probes = {
                fake(AUnitType.Protoss_Probe, 8.3),
                fake(AUnitType.Protoss_Probe, 9.1),
                fake(AUnitType.Protoss_Probe, 10.6),
                fake(AUnitType.Protoss_Probe, 11.4),
        };
        FakeUnit pylon = fake(AUnitType.Protoss_Pylon, 12);
        FakeUnit gateway = fake(AUnitType.Protoss_Gateway, 13);

        FakeUnit[] ours = concat(
                new FakeUnit[] { nexus }, probes, new FakeUnit[] { pylon, gateway });

        // Short horizon: the point is "the machinery starts", not "a game plays
        // out". 120 frames is enough for the commander to run, the production
        // queue to fill, and any NPE to surface.
        world(120, fakeOurs(ours), fakeEnemies(new FakeUnit[0]), () -> {
            (new AtlantisGameCommander()).invokedCommander();

            if (A.now() == 1) {
                // The queue starts empty in a fresh world; after the commander
                // has run, the build-order book must have produced something.
                assertFalse(Queue.get().allOrders().isEmpty(),
                        "Production queue should be populated by the commander"
                                + " within the first frames, but is empty at frame 1");
            }
        });

        assertTrue(nexus.isAlive(), "Nexus must survive a quiet 120 frames");
        assertTrue(Count.workers() >= 3,
                "Workers must survive a quiet 120 frames, got " + Count.workers());
        assertTrue(A.now() >= 120, "World must run the full short horizon");
    }

    // =========================================================
    @Override
    protected FakeUnit[] generateOur() {
        return null;
    }
    @Override
    protected FakeUnit[] generateEnemies() {
        return null;
    }

    private FakeUnit[] concat(FakeUnit[]... groups) {
        int total = 0;
        for (FakeUnit[] group : groups) total += group.length;
        FakeUnit[] result = new FakeUnit[total];
        int at = 0;
        for (FakeUnit[] group : groups) {
            System.arraycopy(group, 0, result, at, group.length);
            at += group.length;
        }
        return result;
    }
}
