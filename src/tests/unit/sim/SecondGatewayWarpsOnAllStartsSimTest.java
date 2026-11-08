package tests.unit.sim;

import atlantis.config.env.Env;
import atlantis.config.env.ProductionV2Mode;
import atlantis.production.demand.BuildOrderGoals;
import atlantis.production.orders.build.ABuildOrder;
import atlantis.production.orders.build.CurrentBuildOrder;
import atlantis.units.AUnitType;
import bwapi.Race;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeUnit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The second Gateway warps on every TauCross start (owner 2026-11-08):
 * an unpowered slot with no free covering Pylon parks the Gateway
 * demand while supply Pylons mint around it (measured live: Gamma
 * droughts with 700+ banked, then a support-Pylon detour ahead of
 * the BO). The full opening order in a live game is a wallet outcome
 * as much as a placement one, so this pins the placeability half:
 * G2 must be warpable everywhere, not the exact warp interleaving
 * (a strict P,G,G,P pin would force a scheduling doctrine the sim's
 * coarse bank cannot adjudicate - that call belongs to live games).
 *
 * <p>Same vehicle as the first-Gateway pin: the real opening on the
 * live TauCross snapshot, one run per start.</p>
 */
@Tag("placement")
public class SecondGatewayWarpsOnAllStartsSimTest extends WorldStubForTests {

    private static final int FRAME_CAP = 30000;

    @Override
    public Race initRace() {
        return Race.Protoss;
    }

    @AfterEach
    public void clean() {
        atlantis.production.constructions.ConstructionRequests.constructions.clear();
        atlantis.placement.core.ReservedSlots.clearForTests();
        if (atlantis.production.orders.production.queue.Queue.get() != null) {
            atlantis.production.orders.production.queue.Queue.get().allOrders().list().clear();
            atlantis.production.orders.production.queue.Queue.get().clearCache();
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
    public void secondGatewayWarpsOnEveryStart() throws Exception {
        ProductionV2Mode previous = Env.productionV2();
        Env.setProductionV2ForTest(ProductionV2Mode.LIVE);
        Env.setPlacementCatalogueForTest(true);
        try {
            for (int base = 0; base <= 2; base++) {
                runOpeningOnStart(base);
            }
        }
        finally {
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
            atlantis.production.orders.build.ABuildOrder previousStrategyBuildOrder =
                strategy.buildOrder();
            ArrayList<atlantis.production.orders.production.queue.order.ProductionOrder> orders =
                new ArrayList<>(Arrays.asList(
                    new atlantis.production.orders.production.queue.order.ProductionOrder(
                        AUnitType.Protoss_Pylon, 8),
                    new atlantis.production.orders.production.queue.order.ProductionOrder(
                        AUnitType.Protoss_Gateway, 10),
                    new atlantis.production.orders.production.queue.order.ProductionOrder(
                        AUnitType.Protoss_Gateway, 12),
                    new atlantis.production.orders.production.queue.order.ProductionOrder(
                        AUnitType.Protoss_Pylon, 15)));
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
                List<String> buildings = new ArrayList<>();
                while (driver.frame() < FRAME_CAP && countGateways(buildings) < 2) {
                    driver.step();
                    buildings.clear();
                    for (SimDriver.WarpRecord record : driver.warpLog()) {
                        if (record.kind.equals("building")) {
                            buildings.add(record.type);
                        }
                    }
                }
                assertTrue(countGateways(buildings) >= 2,
                    "start " + baseIndex + ": the second Gateway warps by f" + FRAME_CAP
                        + " - got " + buildings);
            }
            finally {
                CurrentBuildOrder.set(previousOrder);
                setStrategyBuildOrder(strategy, previousStrategyBuildOrder);
            }
            driver.close();
        }
        catch (Exception | Error e) {
            try {
                driver.close();
            }
            catch (Exception | Error ignored) {
            }
            throw e;
        }
    }

    private static int countGateways(List<String> buildings) {
        int n = 0;

        for (String type : buildings) {
            if (type.equals("Gateway")) n++;
        }

        return n;
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
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
