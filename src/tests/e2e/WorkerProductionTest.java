package tests.e2e;

import atlantis.game.A;
import atlantis.production.v2.ProductionPlan;
import atlantis.production.v2.engine.GameStateSnapshot;
import atlantis.production.v2.engine.ProductionEngine;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import bwapi.Race;
import org.junit.jupiter.api.Test;
import tests.acceptance.AbstractTestWithWorld;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The M6 acceptance criterion for the production redesign: the economy must
 * function. The bot has to produce and keep producing workers up to saturation,
 * and the plan must actually issue them.
 *
 * <p>
 * A plan that is computed every frame and never produces a worker is a broken
 * economy no matter how correct the scheduling math is - which is exactly the
 * failure mode the redesign exists to remove (the old queue could linger in
 * "in progress" and starve production while looking busy). So this test asserts
 * the <em>outcome</em>: a worker appears in the plan, and the engine keeps
 * asking for workers as long as the base is not saturated.
 * </p>
 *
 * <p>
 * Why this is not "trivial": the income projection reads the number of workers
 * actively mining. If that number is zero - a fresh bot whose probes have not
 * reached the patches yet - the whole timeline has no income and every goal is
 * unaffordable, so the plan silently comes back empty. That is a real,
 * plausible regression with no exception and no log, and this test is the one
 * thing that catches it.
 * </p>
 */
public class WorkerProductionTest extends AbstractTestWithWorld {

    @Override
    public Race initRace() {
        return Race.Protoss;
    }

    @Test
    public void freshBasePlansWorkers() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 10);
        FakeUnit[] probes = {
                fake(AUnitType.Protoss_Probe, 8.3),
                fake(AUnitType.Protoss_Probe, 9.1),
                fake(AUnitType.Protoss_Probe, 10.6),
                fake(AUnitType.Protoss_Probe, 11.4),
        };
        FakeUnit pylon = fake(AUnitType.Protoss_Pylon, 12);

        FakeUnit[] ours = units(nexus, probes[0], probes[1], probes[2], probes[3], pylon);

        // A normal opening economy: plenty of minerals, supply to spare.
        options.set("supplyUsed", 20);
        options.set("supplyTotal", 40);

        world(60, ours, new FakeUnit[0], () -> {
            currentMinerals = 500;
            currentGas = 0;

            if (A.now() != 30)
                return;

            ProductionEngine engine = new ProductionEngine();
            ProductionPlan plan = engine.updateFrame();

            assertFalse(plan.isEmpty(), "a base with 4 probes, 500 minerals and free supply must plan something -"
                    + " an empty plan means the economy is stalled");

            boolean plansAWorker = plan.items().stream()
                    .anyMatch(i -> i.item().id().equals("Probe"));
            assertTrue(plansAWorker,
                    "the base is far from saturated (4 workers of 25), so a Probe must be in the plan;"
                            + " plan was: " + plan);

            assertTrue(Select.ourWorkers().count() >= 4, "the probes must still be selectable");
        });
    }

    @Test
    public void aBaseNeverPlansASecondNexus() {
        // The regression that made the opening unplayable: a Probe's engine
        // prerequisite is the Nexus, so without an "already have it" check the
        // scheduler planned a 400-mineral Nexus before it would schedule a
        // 50-mineral Probe. The symptom was the first Pylon waiting for 400
        // minerals, as if the bot were saving for a base (owner report,
        // 2026-10-07). The plan must contain the Probe and no extra Nexus.
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 10);
        FakeUnit[] probes = {
                fake(AUnitType.Protoss_Probe, 8.3),
                fake(AUnitType.Protoss_Probe, 9.1),
                fake(AUnitType.Protoss_Probe, 10.6),
        };
        FakeUnit[] ours = units(nexus, probes[0], probes[1], probes[2]);

        world(60, ours, new FakeUnit[0], () -> {
            currentMinerals = 150;

            if (A.now() != 30) return;

            ProductionPlan plan = new ProductionEngine().updateFrame();

            long nexuses = plan.items().stream()
                    .filter(i -> i.item().id().equals("Nexus"))
                    .count();
            assertEquals(0, nexuses,
                    "the base already has a Nexus - planning another blocks the opening"
                            + " and is the bug this test pins; plan was: " + plan);
        });
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

    @Test
    public void saturatedBaseStopsPlanningWorkers() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 10);
        FakeUnit[] probes = new FakeUnit[30];
        for (int i = 0; i < 30; i++) {
            probes[i] = fake(AUnitType.Protoss_Probe, 8.0 + (i % 6));
        }
        FakeUnit pylon = fake(AUnitType.Protoss_Pylon, 12);

        FakeUnit[] ours = new FakeUnit[probes.length + 2];
        ours[0] = nexus;
        ours[1] = pylon;
        System.arraycopy(probes, 0, ours, 2, probes.length);

        options.set("supplyUsed", 30);
        options.set("supplyTotal", 60);

        world(60, ours, new FakeUnit[0], () -> {
            currentMinerals = 800;

            if (A.now() != 30)
                return;

            ProductionEngine engine = new ProductionEngine();
            ProductionPlan plan = engine.updateFrame();

            boolean plansAWorker = plan.items().stream()
                    .anyMatch(i -> i.item().id().equals("Probe"));
            assertFalse(plansAWorker,
                    "30 workers on one base is past saturation - more workers must not be planned;"
                            + " plan was: " + plan);        });
    }
}
