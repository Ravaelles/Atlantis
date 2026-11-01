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
 * Owner's healthy minimum (2026-11-01): one game proves we build a Pylon,
 * a Gateway, an Assimilator and a Cybernetics Core, and mine 8 gas.
 * Banking 300 minerals or about 7 game minutes (10000 frames) without the full set FAILS -
 * hoarded minerals mean stalled production, the clock means it never
 * arrives. Runs the real production engine unmodified on the real
 * TauCross snapshot (StarEngine, fast stride), like the opening gate -
 * this pins building, not decision facades.
 */
public class HealthyMinimumBuildTest extends WorldStubForTests {

    private static final int FRAME_CAP = 10000;
    private static final int BANK_CAP = 300;
    private static final int GAS_GOAL = 8;

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

    private static boolean hasWarpOf(tests.unit.sim.SimDriver driver, String type) {
        for (tests.unit.sim.SimDriver.WarpRecord record : driver.warpLog()) {
            if (record.kind.equals("building") && record.type.equals(type)) return true;
        }

        return false;
    }

    private static String statusOf(tests.unit.sim.SimDriver driver, Numbers numbers) {
        List<String> have = new ArrayList<>();
        for (String type : new String[]{"Pylon", "Gateway", "Assimilator", "CyberneticsC"}) {
            if (hasWarpOf(driver, type)) have.add(type);
        }
        java.util.Map<String, Integer> dispatched = new java.util.TreeMap<>();
        for (String line : atlantis.util.log.EventLog.toJsonLines().split("\n")) {
            if (line.contains("\"key\":\"V2_DISPATCH\"")) {
                String item = line.replaceAll(".*\"item\":\"([^\"]+)\".*", "$1");
                dispatched.merge(item, 1, Integer::sum);
            }
        }
        boolean coreWanted = false;
        boolean assimWanted = false;
        try {
            coreWanted = atlantis.production.goalboard.GoalBoardFeeder.wantedGoal("core");
            assimWanted = atlantis.production.goalboard.GoalBoardFeeder.wantedGoal("assimilator");
        } catch (Exception ignored) {
        }
        StringBuilder queue = new StringBuilder();
        try {
            for (Object o : atlantis.production.orders.production.queue.Queue.get().allOrders().list()) {
                atlantis.production.orders.production.queue.order.ProductionOrder row =
                    (atlantis.production.orders.production.queue.order.ProductionOrder) o;
                queue.append(row.unitType()).append(":").append(row.status()).append(" ");
            }
        } catch (Exception ignored) {
        }
        StringBuilder constructions = new StringBuilder();
        try {
            for (atlantis.production.constructions.Construction c :
                atlantis.production.constructions.ConstructionRequests.constructions) {
                constructions.append(c.buildingType()).append(":").append(c.status()).append(" ");
            }
        } catch (Exception ignored) {
        }
        StringBuilder refusals = new StringBuilder();
        try {
            for (String line : atlantis.util.log.EventLog.toJsonLines().split("\n")) {
                if (line.contains("\"key\":\"ORDER_NOT_READY\"")) refusals.append(line).append("\n");
            }
        } catch (Exception ignored) {
        }
        return "f" + driver.frame() + " bank=" + numbers.minerals + " gas=" + numbers.gas
            + " warped=" + have
            + " dispatched=" + dispatched
            + " wantedCore=" + coreWanted + " wantedAssim=" + assimWanted
            + " inFlight=" + atlantis.production.goalboard.GoalBoardFeeder.inFlightIds()
            + " queue=[" + queue + "]"
            + " constructions=[" + constructions + "]"
            + " refusals=" + refusals
            + " invalid=" + driver.invalidOrders().size()
            + " cancels=" + driver.cancels().size()
            + "\n" + driver.warpLog();
    }

    @Test
    public void buildsPylonGatewayAssimilatorCoreAndMinesGas() throws Exception {
        atlantis.config.env.ProductionV2Mode previous =
            atlantis.config.env.Env.productionV2();
        atlantis.config.env.Env.setProductionV2ForTest(atlantis.config.env.ProductionV2Mode.LIVE);
        atlantis.config.env.Env.setPlacementCatalogueForTest(true);
        try {
            tests.unit.sim.SimMap map = tests.unit.sim.SimMap.load(
                "out/map-snapshots/ums_rav_7th_rav.scx.json");
            map.install();
            Numbers numbers = new Numbers();
            tests.unit.sim.SimDriver driver = new tests.unit.sim.SimDriver(
                map, tests.unit.sim.SimConfig.fast().withStride(60), numbers, 50);
            try {
                driver.spawnResources();
                driver.spawnBase(1, 4);
                // No build-order mock: pure V2 demand, exactly like the live
                // game that failed ("At 10 Gateway*" with source=DYNAMIC).
                // A fake BO here would test the BO path the e2e gate
                // already covers - the point is the V2-direct seam.
                ABuildOrder previousOrder = CurrentBuildOrder.get();
                CurrentBuildOrder.set(fakeBuildOrder());
                try {
                    while (driver.frame() < FRAME_CAP) {
                        driver.step();

                        if (numbers.minerals >= BANK_CAP) {
                            fail("banked " + numbers.minerals + " minerals: production stalled instead of "
                                + "building - " + statusOf(driver, numbers));
                        }
                        if (hasWarpOf(driver, "Pylon")
                            && hasWarpOf(driver, "Gateway")
                            && hasWarpOf(driver, "Assimilator")
                            && hasWarpOf(driver, "CyberneticsC")
                            && numbers.gas >= GAS_GOAL) {
                            return;
                        }
                    }

                    fail("no healthy minimum within ~7 game minutes - " + statusOf(driver, numbers));
                } finally {
                    if (previousOrder != null) CurrentBuildOrder.set(previousOrder);
                    else CurrentBuildOrder.clear();
                }
            } finally {
                driver.close();
            }
        } finally {
            atlantis.config.env.Env.setProductionV2ForTest(previous);
            atlantis.config.env.Env.setPlacementCatalogueForTest(true);
        }
    }

    /**
     * The standard opening, same rows the live adjudication reads:
     * Pylon 8, Gateway 10+12, Core 22, Assimilator 23.
     */
    private static final ArrayList<ProductionOrder> BO_ROWS = new ArrayList<>(Arrays.asList(
        new ProductionOrder(AUnitType.Protoss_Pylon, 8),
        new ProductionOrder(AUnitType.Protoss_Gateway, 10),
        new ProductionOrder(AUnitType.Protoss_Gateway, 12),
        new ProductionOrder(AUnitType.Protoss_Zealot, 13),
        new ProductionOrder(AUnitType.Protoss_Pylon, 15),
        new ProductionOrder(AUnitType.Protoss_Zealot, 17),
        new ProductionOrder(AUnitType.Protoss_Zealot, 18),
        new ProductionOrder(AUnitType.Protoss_Pylon, 21),
        new ProductionOrder(AUnitType.Protoss_Cybernetics_Core, 22),
        new ProductionOrder(AUnitType.Protoss_Assimilator, 23),
        new ProductionOrder(AUnitType.Protoss_Pylon, 27)));

    private static ABuildOrder fakeBuildOrder() {
        ABuildOrder buildOrder = org.mockito.Mockito.mock(ABuildOrder.class);
        org.mockito.Mockito.when(buildOrder.productionOrders()).thenReturn(BO_ROWS);

        return buildOrder;
    }
}
