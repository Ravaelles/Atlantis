package tests.e2e;

import atlantis.game.A;
import atlantis.production.constructions.Construction;
import atlantis.production.constructions.ConstructionRequests;
import atlantis.production.orders.production.queue.Queue;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.production.requests.produce.ProduceBuilding;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import atlantis.units.workers.WorkerManager;
import bwapi.Race;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import tests.acceptance.AbstractTestWithWorld;
import tests.fakes.FakeOrderSink;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Owner 2026-10-16: the first Pylon warped only once the bank already held
 * ~130 minerals against its 100 price - time wasted after affordability.
 * The bot should arrive just in time (wait briefly at the site) rather than
 * arrive with a stockpile. Three pins on the legacy path:
 *
 * <p>1. The site is picked EARLY: the PREPARING object exists by supply 6
 * (two below the At-8 row) while still ~30 short of the price. Picking must
 * not wait for affordability.</p>
 *
 * <p>2. The builder is claimed at or before supply 8 (early staging is
 * fine).</p>
 *
 * <p>3. The build is ISSUED at exactly supply 8 with at most 108 banked
 * (100 price + 8 headroom). Issuing at 130 is the reported shape.</p>
 *
 * <p>"Issued" stands in for warp: the walk itself is engine physics, proven
 * on OpenBW (first Pylon warped at bank 4-6 in the retained ledgers); what
 * the stub pins is that dispatch starts early enough that arrival can
 * coincide with affordability. Bank accrues like early mining (+1 per 3
 * frames); supply holds 8 across the affordability crossing so a dawdling
 * bot issues at 9 and fails on supply.</p>
 */
public class FirstPylonTimingTest extends AbstractTestWithWorld {

    private Integer seenSupply = null;
    private Integer seenBank = null;
    private Integer adoptedSupply = null;
    private Integer adoptedBank = null;
    private Integer adoptedFrame = null;
    private boolean teleported = false;
    private Integer issuedSupply = null;
    private Integer issuedBank = null;

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

    @AfterEach
    public void clean() {
        ConstructionRequests.constructions.clear();
    }

    @Test
    public void firstPylonObjectEarlyAndIssueOnTime() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 10);
        FakeUnit probe1 = fake(AUnitType.Protoss_Probe, 8.3);
        FakeUnit probe2 = fake(AUnitType.Protoss_Probe, 9.1);
        FakeUnit probe3 = fake(AUnitType.Protoss_Probe, 10.6);

        world(600, units(nexus, probe1, probe2, probe3), new FakeUnit[0], () -> {
            int frame = A.now();

            if (frame == 1) {
                currentSupplyUsed = 6;
                currentSupplyTotal = 9;
                Queue.get().allOrders().list().clear();
                Queue.get().clearCache();
                ConstructionRequests.constructions.clear();
                Queue.get().addNew(0, new ProductionOrder(AUnitType.Protoss_Pylon, 8));

                return;
            }

            // Opening timeline: supply climbs to 8 and holds it across the
            // affordability crossing (~100 at frame 280); then advances, so a
            // bot that dawdles issues at 9 and fails on supply. Income is
            // deliberately slow enough that affordability lands inside the
            // supply-8 window: issuing at 7 would mean building before the
            // money exists, issuing past 8 means sitting on a stockpile.
            if (frame <= 100) currentSupplyUsed = 6;
            else if (frame <= 200) currentSupplyUsed = 7;
            else if (frame <= 450) currentSupplyUsed = 8;
            else currentSupplyUsed = 9;
            currentMinerals = Math.min(250, 30 + frame / 4);

            Queue.get().refresh();

            ProductionOrder pylon = Queue.get().allOrders().list().stream()
                .filter(o -> o.isUnitOrBuilding()
                    && AUnitType.Protoss_Pylon.equals(o.unitType()))
                .findFirst().orElse(null);
            if (pylon == null) return;

            if (seenSupply == null) {
                for (Construction c : ConstructionRequests.notFinishedOfType(AUnitType.Protoss_Pylon)) {
                    seenSupply = A.supplyUsed();
                    seenBank = A.minerals();
                    break;
                }
            }

            // Called unconditionally, like ProductionOrderHandler does - it
            // performs no READY check itself; the supply gate and the
            // adopted-alive guard live inside produceBuilding. This drives
            // the real early-adoption path (builder claimed while still
            // poor), not just the affordability edge.
            ProduceBuilding.produceBuilding(AUnitType.Protoss_Pylon, pylon);

            for (Construction c : ConstructionRequests.notFinishedOfType(AUnitType.Protoss_Pylon)) {
                if (adoptedSupply == null && c.builder() != null) {
                    adoptedSupply = A.supplyUsed();
                    adoptedBank = A.minerals();
                    adoptedFrame = frame;
                }
                if (c.builder() != null) {
                    new WorkerManager((AUnit) c.builder()).forceHandle();
                    // Materialise arrival: the stub records move orders but
                    // never walks, so a dispatched builder would never reach
                    // its site. Five frames after adoption so a missing
                    // dispatch still shows in the history first.
                    if (!teleported && adoptedFrame != null && frame >= adoptedFrame + 5
                        && c.buildPosition() != null) {
                        ((FakeUnit) c.builder()).position = c.buildPosition();
                        teleported = true;
                    }
                }
            }

            if (issuedSupply == null && buildIssued()) {
                issuedSupply = A.supplyUsed();
                issuedBank = A.minerals();
                setShouldQuitGameLoopNow(true);
            }
        });

        assertNotNull(seenSupply, "the Pylon construction object must be created");
        assertTrue(
            seenSupply <= 6,
            "object created at supply " + seenSupply + " with " + seenBank
                + " minerals - the site must be picked at least two supplies early (6 for At 8)");
        assertTrue(
            seenBank < 100,
            "object created with " + seenBank + " banked - picking must not wait for affordability");

        assertNotNull(adoptedSupply, "the Pylon must be adopted (builder assigned)");
        assertTrue(
            adoptedSupply <= 8,
            "adoption at supply " + adoptedSupply + " - the builder is claimed at or before "
                + "BO supply (early staging is fine; the exact pin is on issue below)");

        assertNotNull(issuedSupply, "the Pylon build order must be issued");
        assertEquals(
            8, (int) issuedSupply,
            "issued at supply " + issuedSupply + " - must not drift past the BO row");
        assertTrue(
            issuedBank <= 108,
            "issued with " + issuedBank + " banked - at most 100 + 8 headroom, "
                + "never a 130 stockpile");
    }

    private boolean buildIssued() {
        for (AUnit worker : Select.ourWorkers().list()) {
            if (!(worker.orderSink() instanceof FakeOrderSink)) continue;
            for (FakeOrderSink.Record record : ((FakeOrderSink) worker.orderSink()).orders()) {
                if (record.operation.equals("build") && record.detail.contains("Pylon")) return true;
            }
        }
        return false;
    }
}
