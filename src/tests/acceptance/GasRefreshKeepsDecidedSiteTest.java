package tests.acceptance;

import atlantis.map.position.APosition;
import atlantis.production.constructions.Construction;
import atlantis.production.constructions.ConstructionRequests;
import atlantis.production.constructions.builders.RefreshConstructionPosition;
import atlantis.production.orders.production.queue.Queue;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.units.AUnitType;
import bwapi.Race;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A refresh pass must never cancel + re-request a refinery site that is
 * already decided.
 *
 * <p><b>Measured (Wine log, 3:44):</b> the Assimilator warped at 3:30 on its
 * geyser, but a stale duplicate construction kept driving. On the 23-frame
 * occupancy tick the site read "not good" - the standing Assimilator itself
 * overlaps its own footprint - refresh ran (a no-op for gas by design),
 * the site still read "not good", and the construction was cancelled and
 * re-requested at the same tile. By then the geyser was consumed, so the
 * re-request found {@code geysersIn6=0 / nearest=none}, the gas fix
 * returned null, and the order died ({@code Can't find place ... (Reset)}).
 * A same-tile re-request after a no-op refresh can never heal: with the
 * geyser present it rebuilds the identical site (churn), with it consumed
 * it dies on null.</p>
 *
 * <p>Null sites keep the old path: fresh placement there can genuinely help,
 * so only decided (non-null) gas sites skip the refresh-cancel pair.</p>
 */
public class GasRefreshKeepsDecidedSiteTest extends WorldStubForTests {

    private static final int GX = 86;
    private static final int GY = 118;

    private boolean fired = false;

    private static int assimilatorOrdersInQueue() {
        int total = 0;
        for (ProductionOrder order : Queue.get().allOrders().list()) {
            if (order.unitType() != null
                && order.unitType().equals(AUnitType.Protoss_Assimilator)) {
                total++;
            }
        }
        return total;
    }

    @Override
    public Race initRace() {
        return Race.Protoss;
    }

    @AfterEach
    public void clean() {
        ConstructionRequests.constructions.clear();
        fired = false;
    }

    @Test
    public void warpedRefinerySiteIsNeitherCancelledNorReRequested() {
        FakeUnit probe = fake(AUnitType.Protoss_Probe, 80, 118);

        world(30, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 7, 44),
            fake(AUnitType.Protoss_Assimilator, GX, GY),
            probe
        ), fakeEnemies(), () -> {
            // 23 is the occupancy-tick frame (everyNthGameFrame(23)).
            if (atlantis.game.A.now() != 23 || fired) return;

            fired = true;
            ConstructionRequests.constructions.clear();
            Queue.get().allOrders().list().clear();
            Queue.get().clearCache();

            currentMinerals = 300;
            currentGas = 200;

            APosition site = APosition.create(GX, GY);
            Construction stale = new Construction(AUnitType.Protoss_Assimilator);
            stale.setPositionToBuild(site);
            ConstructionRequests.addConstruction(stale);

            RefreshConstructionPosition.finalRefreshBeforeIssuingOrderFailed(
                probe, stale, AUnitType.Protoss_Assimilator, site, null);

            assertTrue(
                ConstructionRequests.constructions.contains(stale),
                "a decided gas site survives the refresh pass - the standing "
                    + "refinery overlaps its own footprint, which is not a "
                    + "reason to cancel anything");
            assertEquals(
                0, assimilatorOrdersInQueue(),
                "no same-tile re-request: after warp the geyser is consumed, "
                    + "so it could only die on null");
        });

        assertTrue(fired, "the occupancy-tick frame was reached - else this test proves nothing");
    }

    @Override
    protected FakeUnit[] generateOur() {
        return null;
    }

    @Override
    protected FakeUnit[] generateEnemies() {
        return null;
    }
}
