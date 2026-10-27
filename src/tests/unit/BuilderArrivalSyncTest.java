package tests.unit;

import atlantis.production.constructions.Construction;
import atlantis.production.constructions.ConstructionOrderStatus;
import atlantis.production.constructions.ConstructionRequests;
import atlantis.production.constructions.builders.TravelToConstruct;
import atlantis.production.orders.production.queue.Queue;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.actions.Actions;
import bwapi.Race;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.AbstractTestWithWorld;
import tests.fakes.FakeOrderSink;
import tests.fakes.FakeUnit;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The builder walks so it arrives as the money lands (owner): with the
 * travel time known (engine topSpeed, pinned below) and the income
 * modeled, dispatch fires when the affordable frame meets the arrival
 * frame - and the wait, when there is one, happens AT the site.
 *
 * <p>Synthetic world, real gates: 9 miners (0.405 minerals/frame), a
 * 20-tile walk, 100-mineral Pylon. The walk takes ~144f, the money
 * ~247f, so the builder leaves around frame 79 and arrives with the
 * bank 1-16 short of the price - never 50 early, never after a full
 * bank sat idle. Asserted on outcomes (orders, positions, bank), not
 * on internals.</p>
 */
@Tag("production")
public class BuilderArrivalSyncTest extends AbstractTestWithWorld {

    // (geometry inline: 20-tile walk, ~144f at 4px/f)
    private static final int PRICE = 100;
    private static final double INCOME_PER_FRAME = 9 * 0.045;

    private FakeUnit builder;
    private double bankFloat;
    private ProductionOrder linkedOrder;
    private Construction linkedConstruction;
    private int arrivalBank;

    private static List<FakeOrderSink.Record> ordersOf(FakeUnit builder) {
        return ((FakeOrderSink) builder.orderSink()).orders();
    }

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

    @BeforeEach
    public void hermetic() {
        bankFloat = 0;
        linkedOrder = null;
        linkedConstruction = null;
        arrivalBank = -1;
        if (Queue.get() != null) {
            Queue.get().allOrders().list().clear();
            Queue.get().clearCache();
        }
        ConstructionRequests.constructions.clear();
    }

    @AfterEach
    public void clean() {
        if (Queue.get() != null) {
            Queue.get().allOrders().list().clear();
            Queue.get().clearCache();
        }
        ConstructionRequests.constructions.clear();
    }

    @Test
    public void builderArrivesAsMoneyLands() {
        currentSupplyUsed = 20;
        currentSupplyTotal = 25;

        FakeUnit[] miners = new FakeUnit[9];
        for (int i = 0; i < miners.length; i++) {
            miners[i] = fake(AUnitType.Protoss_Probe, 21 + i, 24);
            miners[i].setAction(Actions.GATHER_MINERALS);
        }
        builder = fake(AUnitType.Protoss_Probe, 20, 24);
        FakeUnit[] ours = new FakeUnit[11];
        ours[0] = fake(AUnitType.Protoss_Nexus, 20, 20);
        System.arraycopy(miners, 0, ours, 1, miners.length);
        ours[10] = builder;

        final int[] dispatchedAt = new int[]{-1};
        final int[] arrivedAt = new int[]{-1};
        final double[] speedTiles = new double[]{-1};

        world(400, fakeOurs(ours), fakeEnemies(), () -> {
            int now = atlantis.game.A.now();
            if (now == 1) {
                Queue.get().allOrders().list().clear();
                Queue.get().clearCache();

                return;
            }

            bankFloat += INCOME_PER_FRAME;
            currentMinerals = (int) bankFloat;

            ProductionOrder order = linkedOrder;
            if (order == null) {
                order = new ProductionOrder(AUnitType.Protoss_Pylon, 16);
                Queue.get().addNew(0, order);
                linkedOrder = order;
            }
            if (linkedConstruction == null) {
                linkedConstruction = new Construction(AUnitType.Protoss_Pylon);
                linkedConstruction.setProductionOrder(linkedOrder);
                linkedOrder.setConstruction(linkedConstruction);
                linkedConstruction.setPositionToBuild(
                    atlantis.map.position.APosition.create(40, 24));
                linkedConstruction.setBuilder(builder);
                linkedConstruction.setStatus(ConstructionOrderStatus.NOT_STARTED);
                ConstructionRequests.addConstruction(linkedConstruction);
            }
            Construction construction = linkedConstruction;

            if (speedTiles[0] < 0) speedTiles[0] = builder.maxSpeed() / 32.0;
            // Observation ends at arrival: past it the issue path runs
            // (stub engine refuses, live game builds) - the sync this
            // test pins is dispatch-to-arrival, not the warp itself.
            if (arrivedAt[0] < 0) {
                new ExposedTravel(builder).run(construction);
            }

            if (dispatchedAt[0] < 0 && !ordersOf(builder).isEmpty()) {
                dispatchedAt[0] = now;
            }
            if (arrivedAt[0] < 0 && dispatchedAt[0] >= 0
                && builder.distTo(construction.buildPosition()) <= 2.0) {
                arrivedAt[0] = now;
                arrivalBank = currentMinerals;
            }
        });

        assertEquals(4.92, builder.maxSpeed(), 0.001,
            "engine topSpeed the sync depends on (vendored jar, not memory)");
        assertTrue(dispatchedAt[0] > 2,
            "no instant dispatch at bank 0: the walk waits for the forecast");
        assertTrue(dispatchedAt[0] < arrivedAt[0],
            "dispatch precedes arrival: the builder walks, not the bank");
        assertTrue(arrivedAt[0] > 0, "the builder reached the site");

        int shortfall = PRICE - arrivalBank;
        assertTrue(shortfall >= 1 && shortfall <= 16,
            "arrival bank " + arrivalBank + " of " + PRICE
                + " (short " + shortfall + "): walks in as money lands, "
                + "never 50 early: speed=" + speedTiles[0]);
        assertTrue(
            builder.distTo(atlantis.map.position.APosition.create(40, 24)) <= 2.0,
            "waited AT the site, not midway");
    }

    private static final class ExposedTravel extends TravelToConstruct {
        ExposedTravel(AUnit unit) {
            super(unit);
        }

        boolean run(Construction construction) {
            return travelWhenReady(construction);
        }
    }
}
