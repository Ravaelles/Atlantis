package tests.unit;

import atlantis.production.v2.ProductionItem;
import atlantis.production.v2.ProductionPlan;
import atlantis.production.v2.UnitProducible;
import atlantis.production.v2.execution.DispatchResult;
import atlantis.production.v2.execution.GameOrderDirector;
import atlantis.production.v2.execution.ProductionDispatcher;
import atlantis.units.AUnitType;
import bwapi.Race;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.AbstractTestWithWorld;
import tests.fakes.FakeUnit;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * A V2 train that hits the already-training dedup issues no command, so it
 * is not "issued" - the dispatcher's own contract ({@code DispatchResult}:
 * "whether a command left", pinned by {@code ProductionDispatcherTest} and
 * by the {@code OrderDirector} javadoc) says so.
 *
 * <p>
 * Measured need: a LIVE game logged 20 V2 train orders for +7 net Probes.
 * Every frame the worker goal re-plans while the Nexus trains, the director
 * hit this dedup and logged "trained" - so the count measured dispatches,
 * not engine accepts, and the worker gap read bigger than it is.
 * </p>
 */
@Tag("production")
public class AlreadyTrainingIsNotIssuedTest extends AbstractTestWithWorld {

    @Override
    protected FakeUnit[] generateOur() {
        return null;
    }

    @Override
    protected FakeUnit[] generateEnemies() {
        return null;
    }

    @Override
    public Race initRace() {
        return Race.Protoss;
    }

    @Test
    public void dedupHitReportsNotIssued() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 20, 20);
        nexus.setTrainingQueue(Collections.singletonList(AUnitType.Protoss_Probe));

        world(5, fakeOurs(
            nexus,
            fake(AUnitType.Protoss_Probe, 21, 21)
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 3) return;

            ProductionItem item = new ProductionItem(
                UnitProducible.of(AUnitType.Protoss_Probe), 0, false, null, nexus.id());
            ProductionPlan plan = new ProductionPlan();
            plan.add(item);

            List<DispatchResult> results = new ProductionDispatcher(new GameOrderDirector())
                .dispatch(plan, 3, 10);

            assertEquals(1, results.size());
            assertFalse(results.get(0).issued(),
                "the Nexus trained nothing this frame - the Probe was already first in its queue");
            assertEquals("no free facility", results.get(0).detail());
        });
    }
}
