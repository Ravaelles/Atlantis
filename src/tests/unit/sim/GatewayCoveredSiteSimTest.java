package tests.unit.sim;

import atlantis.config.env.Env;
import atlantis.config.env.ProductionV2Mode;
import atlantis.map.position.APosition;
import atlantis.placement.engine.CataloguePlacementPlanner;
import atlantis.production.constructions.Construction;
import atlantis.production.constructions.ConstructionRequests;
import atlantis.production.constructions.position.CatalogueSiteAdapter;
import atlantis.production.orders.production.queue.Queue;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.units.AUnitType;
import atlantis.util.log.EventLog;
import bwapi.Race;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Live defect (Wine 20261106_175548, TauCross base 1): the second Gateway
 * never warps - buildings.json shows one Gateway all game. The log shows
 * the shape: 1511x {@code CATALOGUE_SITE declined worker-covered slot}
 * while a builder waits, then an 80 s cancel, re-request, repeat.
 *
 * <p>The layout's Gateway slot is powered (completed Pylon) and free, but
 * every base tile sits within 12 tiles of 4+ mining workers, so the
 * worker-cover decline fires forever and the demand starves. The cover
 * rule predicts "no builder could be assigned here" - but the demand
 * holds a committed builder (V2-direct assigns via the free pool even
 * on covered tiles), so the prediction is wrong and the wait is
 * permanent.</p>
 *
 * <p>StarEngine shape: the real slot path ({@link CatalogueSiteAdapter})
 * over the live TauCross snapshot, a saturated base (cover needs 4+
 * workers within 12 tiles), a completed Pylon and a committed builder.
 * Red while the adapter declines the powered slot on cover (the hunt
 * then serves a far tile - or nothing live); green when the committed
 * builder's demand is served its slot.</p>
 */
@Tag("placement")
public class GatewayCoveredSiteSimTest extends WorldStubForTests {

    @Override
    public Race initRace() {
        return Race.Protoss;
    }

    @AfterEach
    public void clean() {
        ConstructionRequests.constructions.clear();
        atlantis.placement.core.ReservedSlots.clearForTests();
        if (Queue.get() != null) {
            Queue.get().allOrders().list().clear();
            Queue.get().clearCache();
        }
        EventLog.clearForTests();
    }

    private final class Numbers implements SimDriver.SimFields {
        int minerals;
        int gas;
        int supplyUsed;

        @Override
        public void setMinerals(int minerals) {
            this.minerals = minerals;
        }

        @Override
        public void setGas(int gas) {
            this.gas = gas;
        }

        @Override
        public void setSupply(int used, int total) {
            this.supplyUsed = used;
        }
    }

    @Test
    public void coveredButPoweredGatewaySlotIsServedToACommittedBuilder() throws Exception {
        ProductionV2Mode previous = Env.productionV2();
        Env.setProductionV2ForTest(ProductionV2Mode.LIVE);
        Env.setPlacementCatalogueForTest(true);
        try {
            SimMap map = SimMap.load("out/map-snapshots/sscai_(3)TauCross.scx.json");
            map.install();
            Numbers numbers = new Numbers();
            SimDriver driver = new SimDriver(
                map, SimConfig.fast().withStride(60), numbers, 50);
            try {
                driver.spawnResources();
                // Live-like saturation: the starving game mined two bases
                // with ~30 workers (cover needs 4+ within 12 tiles).
                driver.spawnBase(1, 24);
                // The live natural (Nexus f6984, mining by f8289 when the
                // 1511 declines start): its line covers the south too.
                for (int i = 0; i < 8; i++) {
                    driver.units().spawn(
                        AUnitType.Protoss_Probe, 100 + (i % 4), 33 + (i / 4));
                }

                // The live Pylon, completed (wall anchor f7545 in the game).
                FakeUnit pylon = driver.units().spawn(AUnitType.Protoss_Pylon, 107, 16);
                assertTrue(pylon.isCompleted(), "sim Pylon spawns completed");

                // The committed builder the demand holds while starving.
                FakeUnit builder = driver.units().spawn(AUnitType.Protoss_Probe, 112, 14);

                // A plain Gateway demand (no exact tile): the funnel hunt,
                // like the live second-Gateway row.
                Construction construction = new Construction(AUnitType.Protoss_Gateway);
                ProductionOrder order = new ProductionOrder(AUnitType.Protoss_Gateway, 12);
                construction.setProductionOrder(order);
                construction.setBuilder(builder);
                // The live layout slot: powered by the completed Pylon,
                // free, but inside the miners' cover (the 1511x decline).
                // Seeded, not hunted: the hunt has its own pins, this one
                // owns the slot path that starved the live game.
                atlantis.placement.core.ReservedSlots.setSlotsForTests(
                    java.util.Collections.singletonList(
                        new atlantis.placement.core.ReservedSlots.Slot(
                            "G", AUnitType.Protoss_Gateway,
                            APosition.create(113, 17))));

                APosition site = CatalogueSiteAdapter.siteFor(
                    new CataloguePlacementPlanner(), builder,
                    AUnitType.Protoss_Gateway, construction);

                atlantis.placement.core.ReservedSlots.clearForTests();

                assertNotNull(site,
                    "a powered Gateway slot with a committed builder must be served - "
                        + "the live game declined it 1511x on worker-cover and built "
                        + "one Gateway all game. Twins: " + EventLog.toJsonLines());
                assertEquals(113, site.tx(),
                    "the layout slot serves, not a hunt tile: " + site
                        + " Twins: " + EventLog.toJsonLines());
                assertEquals(17, site.ty(),
                    "the layout slot serves, not a hunt tile: " + site
                        + " Twins: " + EventLog.toJsonLines());
                assertTrue(
                    atlantis.map.position.PsiField.covers(
                        pylon.tx(), pylon.ty(), site.tx(), site.ty(),
                        AUnitType.Protoss_Gateway.getTilesWidth(),
                        AUnitType.Protoss_Gateway.getTilesHeights()),
                    "the served site must read powered on the completed Pylon "
                        + "(engine truth, like buildings.json): served " + site);
            } finally {
                driver.close();
            }
        } finally {
            Env.setProductionV2ForTest(previous);
            Env.setPlacementCatalogueForTest(true);
        }
    }
}
