package tests.e2e;

import atlantis.game.A;
import atlantis.map.position.APosition;
import atlantis.production.constructions.Construction;
import atlantis.production.constructions.ConstructionOrderStatus;
import atlantis.production.constructions.ConstructionRequests;
import atlantis.production.orders.production.queue.Queue;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import atlantis.units.workers.WorkerManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A builder mining-while-waiting on a power-dead site must be re-sited,
 * not parked (live TauCross: Gateway on an unpowered tile, builder
 * mining at 200 minerals, BuilderMiningWhileWaiting/BuilderManager
 * alternating forever, nothing ever warps). The forecast only counts
 * money, not power, and no other refresh path runs while the builder
 * mine-waits (travel is skipped, overdue is supply-gated, the final
 * refresh needs standstill) - so the mine branch itself re-sites.
 *
 * <p>Shape under test: an UNCURED unpowered Gateway site (no finished
 * Pylon covers it, none warping toward it) with a poor bank, so the
 * builder genuinely mine-waits instead of walking. Deliberately no
 * curse: a cursed tile already resites through the refusal branch -
 * this pins the gap the curse leaves behind when its TTL expires
 * (live: same tile re-served after expiry, another 80 s burn).
 * The funnel serves random tiles under test (the testing seam), so
 * the pin asserts movement, not destination.</p>
 */
public class BuilderMinesOnDeadSiteResitesTest extends WorldStubForTests {

    private Construction construction;

    @Override
    protected FakeUnit[] generateOur() {
        return null;
    }

    @Override
    protected FakeUnit[] generateEnemies() {
        return null;
    }

    @AfterEach
    public void clean() {
        ConstructionRequests.constructions.clear();
        if (Queue.get() != null) {
            Queue.get().allOrders().list().clear();
            Queue.get().clearCache();
        }
    }

    @Test
    public void miningWaiterOnPowerDeadSiteIsResited() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 20, 20);
        FakeUnit probe = fake(AUnitType.Protoss_Probe, 30, 30);
        FakeUnit miner = fake(AUnitType.Protoss_Probe, 31, 31);

        world(400, units(nexus, probe, miner), fakeEnemies(), () -> {
            if (A.now() == 5) {
                currentMinerals = 100;

                construction = new Construction(AUnitType.Protoss_Gateway);
                construction.setProductionOrder(
                    new ProductionOrder(AUnitType.Protoss_Gateway, 10));
                construction.setBuilder(probe);
                construction.setPositionToBuild(APosition.create(40, 40));
                construction.setStatus(ConstructionOrderStatus.NOT_STARTED);
                ConstructionRequests.addConstruction(construction);

                return;
            }

            if (A.now() < 100) return;

            AUnit builder = construction.builder();
            if (builder == null || !builder.isAlive()) return;

            // Drive the REAL worker manager chain, exactly as the game does.
            new WorkerManager(builder).forceHandle();
        });

        assertTrue(construction != null && construction.buildPosition() != null,
            "the row must survive the window (resite, not cancel)");
        APosition site = construction.buildPosition();
        assertFalse(site.tx() == 40 && site.ty() == 40,
            "a mining waiter on a power-dead site must be re-sited - "
                + "the builder kept mining for [" + site + "]"
                + " instead of warping anywhere");
    }
}
