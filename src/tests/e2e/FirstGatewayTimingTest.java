package tests.e2e;

import atlantis.game.A;
import atlantis.production.constructions.Construction;
import atlantis.production.constructions.ConstructionRequests;
import atlantis.production.orders.production.queue.Queue;
import atlantis.production.orders.production.queue.order.OrderStatus;
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
 * Owner 2026-10-15: the first Gateway starts too late (Wine: BO row
 * re-queued at 4:39 sup35, first accept 1:09 vs 1:37 on the OpenBW control).
 * Two pins on the legacy path, both about the observed game, not internals:
 *
 * <p>1. The construction object exists EARLY: picked site, waiting, while
 * still ~100 minerals short of the 150 price (at most 50 banked) and at
 * most two supplies below the row (supply 8 for "At 10"). Waiting for
 * affordability before even picking a site is the delay this kills.</p>
 *
 * <p>2. The build is ISSUED on time: the first {@code build} order for the
 * Gateway lands at exactly the build-order supply (10, never drifted past)
 * with at most 158 banked (150 price + 8 headroom for frame granularity).
 * Sitting on affordability while supply runs away is the Wine shape.</p>
 *
 * <p>"Issued" stands in for warp: the walk-and-warp half is engine physics,
 * proven on OpenBW; what the stub pins is that the bot STARTS promptly.
 * The bank timeline accrues like early mining (+1 per 3 frames); supply
 * holds 10 across the affordability crossing so a dawdling bot misses its
 * window and fails on supply, not on an arbitrary frame count.</p>
 */
public class FirstGatewayTimingTest extends AbstractTestWithWorld {

    private Integer seenSupply = null;
    private Integer seenBank = null;
    private Integer adoptedSupply = null;
    private Integer adoptedBank = null;
    private Integer adoptedFrame = null;
    private boolean teleported = false;
    private Integer issuedSupply = null;
    private Integer issuedBank = null;
    private Integer assignedSupply = null;

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
    public void firstGatewayObjectEarlyAndIssueOnTime() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 10);
        FakeUnit pylon = fake(AUnitType.Protoss_Pylon, 12);
        FakeUnit probe1 = fake(AUnitType.Protoss_Probe, 8.3);
        FakeUnit probe2 = fake(AUnitType.Protoss_Probe, 9.1);
        FakeUnit probe3 = fake(AUnitType.Protoss_Probe, 10.6);
        FakeUnit probe4 = fake(AUnitType.Protoss_Probe, 11.4);

        world(600, units(nexus, pylon, probe1, probe2, probe3, probe4), new FakeUnit[0], () -> {
            int frame = A.now();

            if (frame == 1) {
                currentSupplyUsed = 8;
                currentSupplyTotal = 17;
                Queue.get().allOrders().list().clear();
                Queue.get().clearCache();
                ConstructionRequests.constructions.clear();
                Queue.get().addNew(0, new ProductionOrder(AUnitType.Protoss_Gateway, 10));
                return;
            }

            // Opening timeline: supply climbs to 10 and holds it across the
            // affordability crossing (~150 at frame 330); then advances, so a
            // bot that dawdles issues at 11 and fails on supply.
            if (frame <= 100) currentSupplyUsed = 8;
            else if (frame <= 200) currentSupplyUsed = 9;
            else if (frame <= 450) currentSupplyUsed = 10;
            else currentSupplyUsed = 11;
            currentMinerals = Math.min(250, 40 + frame / 3);

            Queue.get().refresh();

            ProductionOrder gateway = Queue.get().allOrders().list().stream()
                .filter(o -> o.isUnitOrBuilding()
                    && AUnitType.Protoss_Gateway.equals(o.unitType()))
                .findFirst().orElse(null);
            if (gateway == null) return;

            if (seenSupply == null) {
                for (Construction c : ConstructionRequests.notFinishedOfType(AUnitType.Protoss_Gateway)) {
                    seenSupply = A.supplyUsed();
                    seenBank = A.minerals();
                    break;
                }
            }

            if (gateway.isStatus(OrderStatus.READY_TO_PRODUCE)) {
                // The production guard (NEXT #57) makes re-requests while
                // adopted-alive a no-op, so this drives the real path: the
                // first call adopts, later calls wait. (An earlier revision
                // kept adoption-once in the driver because the stub never
                // warps and each call added another construction.)
                ProduceBuilding.produceBuilding(AUnitType.Protoss_Gateway, gateway);
            }

            for (Construction c : ConstructionRequests.notFinishedOfType(AUnitType.Protoss_Gateway)) {
                if (adoptedSupply == null && c.builder() != null) {
                    adoptedSupply = A.supplyUsed();
                    adoptedBank = A.minerals();
                    adoptedFrame = frame;
                }
                if (c.builder() != null) {
                    new WorkerManager((AUnit) c.builder()).forceHandle();
                    // Materialise arrival: the stub records move orders but
                    // never walks (FakeOrderSink.move sets no target), so a
                    // dispatched builder would never reach its site. Arrival
                    // is engine physics - the dispatch decision above already
                    // ran - same philosophy as materialising ordered units in
                    // ProtossDragoonProductionTest. Five frames after adoption
                    // so a missing dispatch still shows in the history first.
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

        assertNotNull(seenSupply, "the Gateway construction object must be created");
        assertTrue(
            seenSupply <= 8,
            "object created at supply " + seenSupply + " with " + seenBank
                + " minerals - the site must be picked at least two supplies early (8 for At 10)");
        assertTrue(
            seenBank < 150,
            "object created with " + seenBank + " banked - picking must not wait for affordability");

        assertNotNull(adoptedSupply, "the Gateway must be adopted (builder assigned)");
        assertTrue(
            adoptedSupply <= 10,
            "adoption at supply " + adoptedSupply + " - the builder is claimed at or before "
                + "BO supply (early staging is fine; the exact pin is on issue below)");
        assertTrue(
            adoptedBank <= 158,
            "adoption with " + adoptedBank + " banked - at most 150 + 8 headroom");

        assertNotNull(issuedSupply, () -> {
            StringBuilder dump = new StringBuilder("the Gateway build order must be issued; ");
            for (Construction c : ConstructionRequests.notFinishedOfType(AUnitType.Protoss_Gateway)) {
                dump.append("status=").append(c.status())
                    .append(" builder=").append(c.builder() == null ? "null" : "#" + c.builder().id())
                    .append(" site=").append(c.buildPosition())
                    .append(" dist=").append(c.builder() == null ? "-"
                        : c.builder().distTo(c.buildPosition()));
                if (c.builder() != null) dump.append(" mgr=").append(c.builder().managerLogs());
            }
            return dump.toString();
        });
        assertEquals(
            10, (int) issuedSupply,
            "issued at supply " + issuedSupply + " - must not drift past the BO row");
        assertTrue(
            issuedBank <= 158,
            "issued with " + issuedBank + " banked - at most 150 + 8 headroom");
        assertEquals(
            1, ConstructionRequests.notFinishedOfType(AUnitType.Protoss_Gateway).size(),
            "exactly one Gateway construction - re-requests while adopted-alive"
                + " are a no-op (NEXT #57), never duplicates");
    }

    private boolean buildIssued() {
        for (AUnit worker : Select.ourWorkers().list()) {
            if (!(worker.orderSink() instanceof FakeOrderSink)) continue;
            for (FakeOrderSink.Record record : ((FakeOrderSink) worker.orderSink()).orders()) {
                if (record.operation.equals("build") && record.detail.contains("Gateway")) return true;
            }
        }
        return false;
    }

    @Test
    public void poorEarlyRowWaitsRichEarlyRowBuilds() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 10);
        FakeUnit pylon = fake(AUnitType.Protoss_Pylon, 12);
        FakeUnit probe1 = fake(AUnitType.Protoss_Probe, 8.3);
        FakeUnit probe2 = fake(AUnitType.Protoss_Probe, 9.1);

        // Owner 2026-10-16 revokes the old one-early rule: below minSupply
        // only price+50 builds. Poor waits (even at the same supply the old
        // test watched), rich builds.
        for (int bank : new int[]{100, 500}) {
            assignedSupply = null;
            final int minerals = bank;
            world(400, units(nexus, pylon, probe1, probe2), new FakeUnit[0], () -> {
                int frame = A.now();

                if (frame == 1) {
                    currentSupplyUsed = 12;
                    currentSupplyTotal = 41;
                    currentMinerals = minerals;
                    Queue.get().allOrders().list().clear();
                    Queue.get().clearCache();
                    ConstructionRequests.constructions.clear();
                    Queue.get().addNew(0, new ProductionOrder(AUnitType.Protoss_Gateway, 28));
                    return;
                }

                currentSupplyUsed = frame <= 200 ? 12 : 27;
                currentMinerals = minerals;

                Queue.get().refresh();

                ProductionOrder row = Queue.get().allOrders().list().stream()
                    .filter(o -> o.isUnitOrBuilding()
                        && AUnitType.Protoss_Gateway.equals(o.unitType()))
                    .findFirst().orElse(null);
                if (row == null) return;

                // Direct call, not only-when-READY: the gate under test must
                // refuse no matter which path tries early (the live row #143
                // started at supply 12 for minSupply 28 through this door).
                ProduceBuilding.produceBuilding(AUnitType.Protoss_Gateway, row);

                for (Construction c : ConstructionRequests.notFinishedOfType(AUnitType.Protoss_Gateway)) {
                    if (c.builder() != null) {
                        if (assignedSupply == null) {
                            assignedSupply = A.supplyUsed();
                            new WorkerManager((AUnit) c.builder()).forceHandle();
                        }
                    }
                }
            });

            if (bank < 200) {
                assertTrue(
                    assignedSupply == null,
                    "poor early row (bank " + bank + "): no builder claimed below minSupply");
            }
            else {
                assertNotNull(assignedSupply, "rich early row must start");
                assertTrue(
                    assignedSupply <= 12,
                    "rich early row builds at once, supply " + assignedSupply);
            }
        }
    }
}
