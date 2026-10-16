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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeEnemies;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

/**
 * A refresh pass cancels only dead sites, never transient traffic.
 *
 * <p><b>Measured (Wine):</b> a miner walking over the Core site cancelled
 * it 7 times in 10 seconds (orders #126-#132, bank 230-308 against the 200
 * price). Every cancel minted a duplicate 200-mineral reservation through
 * the Top-priority backdoor until the bank froze near 600 with the
 * building affordable. Units move - only a standing building makes
 * refresh hopeless. Null sites keep the old path (fresh placement heals);
 * true stalls still die by overdue.</p>
 */
public class RefreshCancelNeedsBuildingTest extends WorldStubForTests {

    private static final int SX = 86;
    private static final int SY = 118;

    private boolean fired = false;

    @Override
    public Race initRace() {
        return Race.Protoss;
    }

    @AfterEach
    public void clean() {
        ConstructionRequests.constructions.clear();
        fired = false;
    }

    private void vetoPosture() {
        ConstructionRequests.constructions.clear();
        Queue.get().allOrders().list().clear();
        Queue.get().clearCache();
        currentMinerals = 300;
        currentGas = 100;
    }

    private static int coreOrdersInQueue() {
        int total = 0;
        for (ProductionOrder order : Queue.get().allOrders().list()) {
            if (order.unitType() != null
                && order.unitType().equals(AUnitType.Protoss_Cybernetics_Core)) {
                total++;
            }
        }
        return total;
    }

    @Test
    public void walkingUnitDoesNotCancel() {
        FakeUnit miner = fake(AUnitType.Protoss_Probe, SX, SY);

        world(30, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 7, 44),
            miner
        ), fakeEnemies(), () -> {
            // 23 is the occupancy-tick frame (everyNthGameFrame(23)).
            if (atlantis.game.A.now() != 23 || fired) return;
            fired = true;
            vetoPosture();

            APosition site = APosition.create(SX, SY);
            Construction waiting = new Construction(AUnitType.Protoss_Cybernetics_Core);
            waiting.setPositionToBuild(site);
            ConstructionRequests.addConstruction(waiting);

            RefreshConstructionPosition.finalRefreshBeforeIssuingOrderFailed(
                miner, waiting, AUnitType.Protoss_Cybernetics_Core, site, null);

            assertTrue(
                ConstructionRequests.constructions.contains(waiting),
                "a miner crossing the site is traffic, not a verdict - "
                    + "the builder issues when the tile is free");
            assertTrue(
                coreOrdersInQueue() == 0,
                "no cancel, no duplicate reservation");
        });

        assertTrue(fired, "the occupancy-tick frame was reached - else this test proves nothing");
    }

    @Test
    public void standingBuildingStillCancels() {
        world(30, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 7, 44),
            fake(AUnitType.Protoss_Pylon, SX, SY),
            fake(AUnitType.Protoss_Probe, 80, 118)
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 23 || fired) return;
            fired = true;
            vetoPosture();

            APosition site = APosition.create(SX, SY);
            Construction doomed = new Construction(AUnitType.Protoss_Pylon);
            doomed.setPositionToBuild(site);
            ConstructionRequests.addConstruction(doomed);

            RefreshConstructionPosition.finalRefreshBeforeIssuingOrderFailed(
                null, doomed, AUnitType.Protoss_Pylon, site, null);

            assertFalse(
                ConstructionRequests.constructions.contains(doomed),
                "a standing building on the site is hopeless - the old path stands");

            ProductionOrder reRequested = null;
            for (ProductionOrder order : Queue.get().allOrders().list()) {
                if (order.unitType() != null
                    && order.unitType().equals(AUnitType.Protoss_Pylon)) {
                    reRequested = order;
                }
            }
            assertTrue(
                reRequested != null && reRequested.aroundPosition() == null,
                "the re-request re-picks elsewhere - same dead tile spun 10x");
        });

        assertTrue(fired, "the frame was reached - else this test proves nothing");
    }

    @Test
    public void nullSiteKeepsTheOldPath() {
        world(30, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 7, 44),
            fake(AUnitType.Protoss_Probe, 80, 118)
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 3 || fired) return;
            fired = true;
            vetoPosture();

            Construction siteless = new Construction(AUnitType.Protoss_Cybernetics_Core);
            ConstructionRequests.addConstruction(siteless);

            RefreshConstructionPosition.finalRefreshBeforeIssuingOrderFailed(
                null, siteless, AUnitType.Protoss_Cybernetics_Core, null, null);

            assertFalse(
                ConstructionRequests.constructions.contains(siteless),
                "no site at all still cancels - fresh placement is the only heal");
        });

        assertTrue(fired, "the frame was reached - else this test proves nothing");
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
