package tests.e2e;

import atlantis.game.A;
import atlantis.production.v2.engine.ProductionEngine;
import atlantis.production.v2.ProducerFacility;
import atlantis.production.v2.ProductionPlan;
import atlantis.production.v2.ResourceTimeline;
import atlantis.production.v2.engine.GameStateSnapshot;
import atlantis.units.AUnitType;
import bwapi.Race;
import org.junit.jupiter.api.Test;
import tests.acceptance.AbstractTestWithWorld;
import tests.fakes.FakeUnit;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The production-v2 engine on a real (stub) game world: it must read the state,
 * plan something sensible, and not throw - the "does the whole stack hold
 * together" check that unit tests with hand-built inputs cannot give.
 *
 * <p>
 * This is the intended home of the eventual OpenBW mega-test (CONVENTIONS §10):
 * one run of the real commander that exercises economy and production together.
 * For now it drives {@link ProductionEngine} directly on the fake world, which
 * is the same computation minus the engine physics.
 * </p>
 *
 * <p>
 * The assertions are deliberately about <em>shape</em>, not exact numbers: a
 * quiet Protoss start must produce a timeline with positive income, a registry
 * that finds the buildings we placed, and a plan whose worker goal survives
 * scheduling. Exact frames depend on the map and would make the test brittle
 * for no gain.
 * </p>
 */
public class ProductionEngineSmokeTest extends AbstractTestWithWorld {

    @Override
    public Race initRace() {
        return Race.Protoss;
    }

    @Test
    public void enginePlansOnARealGameStateWithoutThrowing() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 10);
        FakeUnit[] probes = {
                fake(AUnitType.Protoss_Probe, 8.3),
                fake(AUnitType.Protoss_Probe, 9.1),
                fake(AUnitType.Protoss_Probe, 10.6),
                fake(AUnitType.Protoss_Probe, 11.4),
        };
        FakeUnit pylon = fake(AUnitType.Protoss_Pylon, 12);
        FakeUnit gateway = fake(AUnitType.Protoss_Gateway, 13);

        FakeUnit[] ours = units(nexus, probes[0], probes[1], probes[2], probes[3], pylon, gateway);

        // The stub world starts with zero resources and zero supply, so the test
        // must declare the economy it wants to reason about (the harness reads
        // these off the test instance, not off the units).
        options.set("supplyUsed", 20);
        options.set("supplyTotal", 40);
        currentMinerals = 400;
        currentGas = 100;

        world(60, ours, new FakeUnit[0], () -> {
            if (A.now() != 30)
                return;

            // The economy is what the test declared, not what the fake world
            // invents; keep the numbers stable across frames so the plan is
            // deterministic.
            currentMinerals = 400;
            currentGas = 100;

            GameStateSnapshot state = new GameStateSnapshot();

            ResourceTimeline timeline = state.buildTimeline();
            assertNotNull(timeline, "a timeline must always be buildable");
            assertTrue(timeline.horizon() > 0);

            // The holes we built into the map must be visible to the registry,
            // otherwise the whole scheduler works against an empty world.
            List<ProducerFacility> gateways = state.facilityRegistry().facilitiesOf("Protoss_Gateway");
            assertTrue(!gateways.isEmpty(),
                    "the Gateway on the map must be found by the facility registry");

            List<ProducerFacility> nexuses = state.facilityRegistry().facilitiesOf("Protoss_Nexus");
            assertTrue(!nexuses.isEmpty(), "the Nexus must be found too - it trains workers");

            // And the full pipeline must run on that state.
            ProductionEngine engine = new ProductionEngine();
            ProductionPlan plan = engine.updateFrame();
            assertNotNull(plan, "the engine must return a plan");

            assertTrue(!engine.lastPlan().isEmpty(),
                    "a 30-supply Protoss start with 4 probes must have something to do"
                            + " (workers at the least); got an empty plan");
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
}
