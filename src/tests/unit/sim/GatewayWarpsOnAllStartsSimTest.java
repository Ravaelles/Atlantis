package tests.unit.sim;

import atlantis.config.env.Env;
import atlantis.config.env.ProductionV2Mode;
import atlantis.production.demand.BuildOrderGoals;
import atlantis.production.orders.build.ABuildOrder;
import atlantis.production.orders.build.CurrentBuildOrder;
import atlantis.production.orders.production.queue.Queue;
import atlantis.units.AUnitType;
import bwapi.Race;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.WorldStubForTests;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Live defect (Wine 20261106_211104, TauCross base 0): the first Gateway
 * row is minted on layout slot [12,54], which no standing Pylon powers -
 * it burns 80 s of builder spins ("took too long"), cancels, re-requests
 * the same tile, and buildings.json shows the first Gateway an age late.
 * The slot-planner answered the tile 151x while Pylon [15,49] stood
 * complete nearby.
 *
 * <p>StarEngine shape: the real opening (V2-direct, catalogue planner)
 * on the live TauCross snapshot, one run per start. Red while a Gateway
 * demand warps nowhere by the frame cap on any start.</p>
 */
@Tag("placement")
public class GatewayWarpsOnAllStartsSimTest extends WorldStubForTests {

    private static final int FRAME_CAP = 15000;

    @Override
    public Race initRace() {
        return Race.Protoss;
    }

    @AfterEach
    public void clean() {
        atlantis.production.constructions.ConstructionRequests.constructions.clear();
        atlantis.placement.core.ReservedSlots.clearForTests();
        if (Queue.get() != null) {
            Queue.get().allOrders().list().clear();
            Queue.get().clearCache();
        }
        atlantis.combat.BattleCommander.clearForTests();
        atlantis.battle.cluster.Alpha.get().clear();
        atlantis.util.log.PostGameDigest.clearForTests();
        atlantis.util.log.EventLog.clearForTests();
        atlantis.util.AConsole.clearCappedForTests();
    }

    private final class Numbers implements SimDriver.SimFields {
        @Override
        public void setMinerals(int minerals) {
            currentMinerals = minerals;
        }

        @Override
        public void setGas(int gas) {
            currentGas = gas;
        }

        @Override
        public void setSupply(int used, int total) {
            currentSupplyUsed = used;
            currentSupplyTotal = total;
        }
    }

    @Test
    public void firstGatewayWarpsOnEveryStart() throws Exception {
        ProductionV2Mode previous = Env.productionV2();
        Env.setProductionV2ForTest(ProductionV2Mode.LIVE);
        Env.setPlacementCatalogueForTest(true);
        try {
            for (int base = 0; base <= 2; base++) {
                runOpeningOnStart(base);
            }
        } finally {
            Env.setProductionV2ForTest(previous);
            Env.setPlacementCatalogueForTest(true);
        }
    }

    private void runOpeningOnStart(int baseIndex) throws Exception {
        SimMap map = SimMap.load("out/map-snapshots/sscai_(3)TauCross.scx.json");
        map.install();
        Numbers numbers = new Numbers();
        SimDriver driver = new SimDriver(map, SimConfig.fast().withStride(60), numbers, 50);
        try {
            driver.spawnResources();
            driver.spawnBase(baseIndex, 9);
            ABuildOrder previousOrder = CurrentBuildOrder.get();
            atlantis.information.strategy.AStrategy strategy =
                atlantis.information.strategy.Strategy.get();
            atlantis.production.orders.build.ABuildOrder previousStrategyOrder =
                strategy.buildOrder();
            ArrayList<atlantis.production.orders.production.queue.order.ProductionOrder> orders =
                new ArrayList<>(Arrays.asList(
                    new atlantis.production.orders.production.queue.order.ProductionOrder(
                        AUnitType.Protoss_Pylon, 8),
                    new atlantis.production.orders.production.queue.order.ProductionOrder(
                        AUnitType.Protoss_Gateway, 10),
                    new atlantis.production.orders.production.queue.order.ProductionOrder(
                        AUnitType.Protoss_Gateway, 12)));
            for (atlantis.production.orders.production.queue.order.ProductionOrder row : orders) {
                row.markAsNotDynamic();
            }
            ABuildOrder buildOrder = org.mockito.Mockito.mock(ABuildOrder.class);
            org.mockito.Mockito.when(buildOrder.productionOrders()).thenReturn(
                new ArrayList<>(orders));
            CurrentBuildOrder.set(buildOrder);
            setStrategyBuildOrder(strategy, buildOrder);
            atlantis.production.orders.production.queue.QueueInitializer.initializeProductionQueue();
            try {
                Integer gatewayFrame = null;
                String gatewayTile = "-";
                while (driver.frame() < FRAME_CAP) {
                    driver.step();
                    for (SimDriver.WarpRecord record : driver.warpLog()) {
                        if (record.kind.equals("building") && record.type.equals("Gateway")) {
                            gatewayFrame = record.frame;
                            gatewayTile = record.tx + "," + record.ty;
                            break;
                        }
                    }
                    if (gatewayFrame != null) break;
                }
                assertTrue(gatewayFrame != null,
                    "start " + baseIndex + ": first Gateway never warped by f" + FRAME_CAP
                        + " - warps: " + warpsOf(driver));
            } finally {
                CurrentBuildOrder.set(previousOrder);
                setStrategyBuildOrder(strategy, previousStrategyOrder);
            }
            driver.close();
        } catch (Exception | Error e) {
            try {
                driver.close();
            } catch (Exception | Error ignored) {
            }
            throw e;
        }
    }

    private static String warpsOf(SimDriver driver) {
        List<String> out = new ArrayList<>();
        for (SimDriver.WarpRecord record : driver.warpLog()) out.add(record.toString());
        return out.toString();
    }

    private static void setStrategyBuildOrder(
        atlantis.information.strategy.AStrategy strategy,
        atlantis.production.orders.build.ABuildOrder order
    ) {
        try {
            java.lang.reflect.Field field =
                atlantis.information.strategy.AStrategy.class.getDeclaredField("buildOrder");
            field.setAccessible(true);
            field.set(strategy, order);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
