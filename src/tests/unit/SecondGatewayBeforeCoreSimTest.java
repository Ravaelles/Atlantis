package tests.unit;

import atlantis.battle.cluster.Alpha;
import atlantis.combat.BattleCommander;
import atlantis.production.orders.build.ABuildOrder;
import atlantis.production.orders.build.CurrentBuildOrder;
import atlantis.production.orders.production.queue.Queue;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.units.AUnitType;
import bwapi.Race;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import tests.acceptance.WorldStubForTests;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Live defect, three games (GAME_1077E111, GAME_91A7AAC1, owner-watched
 * 20:28:06): the build order's second Gateway ("At 12") warps at supply
 * ~25, after a dynamic Cybernetics Core and Assimilator that the order
 * lists at 23/26. The order says two Gates first; V2 dynamic tech goals
 * fire on the affordable-now bank once one Gateway stands and jump it.
 *
 * <p>Runs the real production engine unmodified on the TauCross snapshot
 * (StarEngine, fast stride), like the healthy-minimum gate. Red while
 * the Core beats the second Gateway; the fix must make the matured
 * build-order row win here. Timeout/stall guards mirror
 * {@link HealthyMinimumBuildTest} so a stuck run fails loud, not mute.
 */
public class SecondGatewayBeforeCoreSimTest extends WorldStubForTests {

    private static final int FRAME_CAP = 12000;

    @Override
    public Race initRace() {
        return Race.Protoss;
    }

    @AfterEach
    public void clean() {
        atlantis.production.constructions.ConstructionRequests.constructions.clear();
        if (Queue.get() != null) {
            Queue.get().allOrders().list().clear();
            Queue.get().clearCache();
        }
        BattleCommander.clearForTests();
        Alpha.get().clear();
        atlantis.util.log.PostGameDigest.clearForTests();
        atlantis.util.log.EventLog.clearForTests();
        atlantis.util.AConsole.clearCappedForTests();
    }

    private final class Numbers implements tests.unit.sim.SimDriver.SimFields {
        int minerals;
        int gas;
        int supplyUsed;

        @Override
        public void setMinerals(int minerals) {
            this.minerals = minerals;
            currentMinerals = minerals;
        }

        @Override
        public void setGas(int gas) {
            this.gas = gas;
            currentGas = gas;
        }

        @Override
        public void setSupply(int used, int total) {
            this.supplyUsed = used;
            currentSupplyUsed = used;
            currentSupplyTotal = total;
        }
    }

    @Test
    public void secondGatewayWarpsBeforeDynamicCore() throws Exception {
        runSecondGatewayRace(false);
    }

    /**
     * Live pressure variant: the real vZ opening also trains Zealots from
     * supply 12, draining the bank the Gateway and the dynamic Core race
     * for. Red while the live At12-to-25 slip reproduces here (Core beats
     * the second Gateway); green pins the fix.
     */
    @Test
    public void secondGatewayWarpsBeforeDynamicCoreUnderZealotPressure() throws Exception {
        runSecondGatewayRace(true);
    }

    private void runSecondGatewayRace(boolean withZealots) throws Exception {
        atlantis.config.env.ProductionV2Mode previous =
            atlantis.config.env.Env.productionV2();
        atlantis.config.env.Env.setProductionV2ForTest(atlantis.config.env.ProductionV2Mode.LIVE);
        atlantis.config.env.Env.setPlacementCatalogueForTest(true);
        try {
            tests.unit.sim.SimMap map = tests.unit.sim.SimMap.load(
                "out/map-snapshots/sscai_(3)TauCross.scx.json");
            map.install();
            Numbers numbers = new Numbers();
            tests.unit.sim.SimDriver driver = new tests.unit.sim.SimDriver(
                map, tests.unit.sim.SimConfig.fast().withStride(60), numbers, 50);
            try {
                driver.spawnResources();
                driver.spawnBase(1, 4);
                ABuildOrder previousOrder = CurrentBuildOrder.get();
                atlantis.information.strategy.AStrategy strategy =
                    atlantis.information.strategy.Strategy.get();
                atlantis.production.orders.build.ABuildOrder previousStrategyOrder =
                    strategy.buildOrder();
                // The live vZ shape: two Gates (10, 12) before Core (23)
                // and Assimilator (26). Single-row each, so a duplicate
                // warp cannot hide in an "at least one" assertion. The
                // pressure variant adds the live Zealot rows: the bank
                // the Gateway and the dynamic Core race for is drained
                // by unit production, like in the three slipped games.
                ArrayList<ProductionOrder> orders = new ArrayList<>(Arrays.asList(
                    new ProductionOrder(AUnitType.Protoss_Pylon, 8),
                    new ProductionOrder(AUnitType.Protoss_Gateway, 10),
                    new ProductionOrder(AUnitType.Protoss_Gateway, 12),
                    new ProductionOrder(AUnitType.Protoss_Cybernetics_Core, 23),
                    new ProductionOrder(AUnitType.Protoss_Assimilator, 26)));
                if (withZealots) {
                    orders.add(new ProductionOrder(AUnitType.Protoss_Zealot, 12));
                    orders.add(new ProductionOrder(AUnitType.Protoss_Zealot, 12));
                    orders.add(new ProductionOrder(AUnitType.Protoss_Zealot, 14));
                    orders.add(new ProductionOrder(AUnitType.Protoss_Zealot, 16));
                    orders.add(new ProductionOrder(AUnitType.Protoss_Pylon, 15));
                }
                ABuildOrder buildOrder = org.mockito.Mockito.mock(ABuildOrder.class);
                org.mockito.Mockito.when(buildOrder.productionOrders()).thenReturn(orders);
                CurrentBuildOrder.set(buildOrder);
                setStrategyBuildOrder(strategy, buildOrder);
                atlantis.production.orders.production.queue.QueueInitializer.initializeProductionQueue();
                try {
                    Integer secondGatewayFrame = null;
                    Integer coreFrame = null;
                    while (driver.frame() < FRAME_CAP) {
                        driver.step();
                        if (secondGatewayFrame == null && countWarpsOf(driver, "Gateway") >= 2) {
                            secondGatewayFrame = driver.frame();
                        }
                        if (coreFrame == null && hasWarpOf(driver, "CyberneticsC")) {
                            coreFrame = driver.frame();
                        }
                        if (secondGatewayFrame != null && coreFrame != null) break;
                    }
                    assertTrue(secondGatewayFrame != null,
                        "second Gateway never warped by f" + FRAME_CAP + " - " + countsOf(driver));
                    assertTrue(coreFrame != null,
                        "Core never warped by f" + FRAME_CAP + " - " + countsOf(driver));
                    assertTrue(secondGatewayFrame < coreFrame,
                        "BO second Gateway (At 12) must warp before dynamic Core - "
                            + "Gateway#2 f" + secondGatewayFrame + ", Core f" + coreFrame
                            + " - " + countsOf(driver));
                } finally {
                    CurrentBuildOrder.set(previousOrder);
                    setStrategyBuildOrder(strategy, previousStrategyOrder);
                }
            } finally {
                atlantis.config.env.Env.setProductionV2ForTest(previous);
            }
        } catch (Exception e) {
            throw e;
        }
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
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("test seam broke: AStrategy.buildOrder renamed", e);
        }
    }

    private static boolean hasWarpOf(tests.unit.sim.SimDriver driver, String type) {
        for (tests.unit.sim.SimDriver.WarpRecord record : driver.warpLog()) {
            if (record.kind.equals("building") && record.type.equals(type)) return true;
        }

        return false;
    }

    private static int countWarpsOf(tests.unit.sim.SimDriver driver, String type) {
        int count = 0;
        for (tests.unit.sim.SimDriver.WarpRecord record : driver.warpLog()) {
            if (record.kind.equals("building") && record.type.equals(type)) count++;
        }

        return count;
    }

    private static String countsOf(tests.unit.sim.SimDriver driver) {
        List<String> warps = new ArrayList<>();
        for (tests.unit.sim.SimDriver.WarpRecord record : driver.warpLog()) {
            if (record.kind.equals("building")) warps.add(record.type + "@f" + record.frame);
        }

        return warps.toString();
    }
}
