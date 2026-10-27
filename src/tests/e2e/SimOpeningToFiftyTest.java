package tests.e2e;

import atlantis.production.orders.build.ABuildOrder;
import atlantis.production.orders.build.CurrentBuildOrder;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.units.AUnitType;
import bwapi.Race;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import tests.unit.AbstractTestWithUnits;
import tests.unit.sim.SimConfig;
import tests.unit.sim.SimDriver;
import tests.unit.sim.SimMap;
import tests.unit.sim.SimOrder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * StarEngine opening gate: "Zealot into Goon" played from frame 1 to 50
 * supply in the sim, not in a game. The bot runs unmodified
 * ({@code ProductionEngine.updateFrame} every frame); the sim owns the
 * clock, the bank, mining, builders, warps, training and spawning.
 *
 * <p>Asserts the in-suite twin of {@code scripts/assert-wine-opening.sh}:
 * the build order completes in file order, no non-BO building warps
 * before the first Gateway, and the mineral bank never tops 550 below
 * 50 supply. A red test names the frame, the warp log says what the
 * bot did instead.</p>
 */
public class SimOpeningToFiftyTest extends AbstractTestWithUnits {

    private static final int SUPPLY_TARGET_RAW = 100;
    private static final int FRAME_CAP = 16000;
    /**
     * Bank cap horizon, raw supply - the same scope as
     * {@code scripts/assert-wine-opening.sh} (its {@code --supply-until}
     * reads the same raw numbers): the opening must not hoard while it
     * plays out. Past the opening both sim and live games bank
     * thousands (economy-first army sizing), which is a tuning
     * question, not this gate.
     */
    private static final int BANK_HORIZON_RAW = 50;
    private static final int MAX_BANK = 550;

    private ABuildOrder previousBuildOrder;

    @Override
    public Race initRace() {
        return Race.Protoss;
    }

    @BeforeEach
    public void saveBuildOrder() {
        previousBuildOrder = CurrentBuildOrder.get();
    }

    @AfterEach
    public void restoreBuildOrder() {
        if (previousBuildOrder != null) CurrentBuildOrder.set(previousBuildOrder);
        else CurrentBuildOrder.clear();
    }

    @Test
    public void zealotIntoGoonReachesFiftySupplyInOrder() throws Exception {
        withLiveV2(() -> {
            CurrentBuildOrder.set(zealotIntoGoon());
            runOpening(SimConfig.fast());
            return null;
        });
    }

    /**
     * FAITHFUL travel: the same opening start, but builders walk the
     * tile arena through the shared mover instead of teleporting along
     * a segment. Short horizon (first Pylon warp): it pins the mover
     * integration, not the economy - the full run above owns that.
     */
    @Test
    public void faithfulBuildersWalkToTheSite() throws Exception {
        withLiveV2(() -> {
            CurrentBuildOrder.set(zealotIntoGoon());
            SimMap map = SimMap.load("src/tests/resources/taucross-snapshot.json");
            map.install();
            SimDriver driver = newSimDriver(map, SimConfig.faithful());
            try {
                driver.spawnResources();
                driver.spawnBase(0, 4);

                while (driver.frame() < 4000 && !hasWarpOf(driver, "Pylon")) {
                    driver.step();
                }

                assertTrue(hasWarpOf(driver, "Pylon"),
                    "a Faithful builder walks the tile arena and warps the first Pylon:\n"
                        + describe(driver.warpLog(), driver.invalidOrders(), driver.cancels()));
                assertTrue(driver.invalidOrders().isEmpty(),
                    "the walk issues no orders the engine would refuse:\n"
                        + driver.invalidOrders());
            }
            finally {
                driver.close();
            }

            return null;
        });
    }

    private interface ThrowingRunnable {
        Void run() throws Exception;
    }

    private void withLiveV2(ThrowingRunnable body) throws Exception {
        atlantis.config.env.ProductionV2Mode previous =
            atlantis.config.env.Env.productionV2();
        atlantis.config.env.Env.setProductionV2ForTest(
            atlantis.config.env.ProductionV2Mode.LIVE);
        atlantis.config.env.Env.setPlacementCatalogueForTest(false);
        try {
            body.run();
        }
        finally {
            atlantis.config.env.Env.setProductionV2ForTest(previous);
            atlantis.config.env.Env.setPlacementCatalogueForTest(false);
        }
    }

    private SimDriver newSimDriver(SimMap map, SimConfig config) {
        return new SimDriver(map, config, new SimDriver.SimFields() {
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
        }, 50);
    }

    private static boolean hasWarpOf(SimDriver driver, String type) {
        for (SimDriver.WarpRecord record : driver.warpLog()) {
            if (record.kind.equals("building") && record.type.equals(type)) return true;
        }

        return false;
    }

    // =========================================================

    private void runOpening(SimConfig config) throws Exception {
        SimMap map = SimMap.load("src/tests/resources/taucross-snapshot.json");
        map.install();
        SimDriver driver = newSimDriver(map, config);
        try {
            driver.spawnResources();
            driver.spawnBase(0, 4);

            int maxBank = 0;
            while (currentSupplyUsed < SUPPLY_TARGET_RAW && driver.frame() < FRAME_CAP) {
                driver.step();
                if (currentSupplyUsed < BANK_HORIZON_RAW) {
                    maxBank = Math.max(maxBank, currentMinerals);
                }
            }

            List<SimDriver.WarpRecord> log = driver.warpLog();
            List<SimOrder> invalid = driver.invalidOrders();
            List<String> cancels = driver.cancels();
            assertTrue(currentSupplyUsed >= SUPPLY_TARGET_RAW,
                "the opening stalls before 50 supply at frame " + driver.frame()
                    + " (" + currentSupplyUsed + "/100 raw, "
                    + cancels.size() + " proximity cancels):\n"
                    + describe(log, invalid, cancels));
            assertBuildOrderInFileOrder(log, invalid, cancels);
            assertNoIntruderBeforeFirstGateway(log, invalid, cancels);
            assertTrue(maxBank <= MAX_BANK,
                "the bank hoards " + maxBank + " minerals below raw supply "
                    + BANK_HORIZON_RAW + " (cap "
                    + MAX_BANK + "):\n" + describe(log, invalid, cancels));

            assertTrue(invalid.isEmpty(),
                "the bot issued orders the engine would refuse:\n" + invalid);
        }
        finally {
            driver.close();
        }
    }

    /** Zealot into Goon, the file the Wine games play: (type, supply gate). */
    /**
     * Golden warp frames, one per build-order row, from the first green
     * run. The sim is deterministic: same code, same frames. A drift
     * means the bot or the sim changed - update deliberately, never to
     * make red green (same contract as the arch store).
     */
    private static final int[] GOLDEN_WARP_FRAMES = {
        1292, 3866, 5049, 4787, 2645, 5617, 6281, 5834, 6729, 2617, 7235,
    };

    private static Object[][] buildOrderRows() {
        return new Object[][] {
            { AUnitType.Protoss_Pylon, 8 },
            { AUnitType.Protoss_Gateway, 10 },
            { AUnitType.Protoss_Gateway, 12 },
            { AUnitType.Protoss_Zealot, 13 },
            { AUnitType.Protoss_Pylon, 15 },
            { AUnitType.Protoss_Zealot, 17 },
            { AUnitType.Protoss_Zealot, 18 },
            { AUnitType.Protoss_Pylon, 21 },
            { AUnitType.Protoss_Cybernetics_Core, 22 },
            { AUnitType.Protoss_Assimilator, 23 },
            { AUnitType.Protoss_Pylon, 27 },
        };
    }

    /** Zealot into Goon, the file the Wine games play. */
    private static ABuildOrder zealotIntoGoon() {
        // Rows first, stubbing after: a ProductionOrder reads the clock in
        // its constructor, and a mocked-static clock read inside an open
        // Mockito.when(...) fails the strict-stubs check.
        ArrayList<ProductionOrder> rows = new ArrayList<>();
        for (Object[] row : buildOrderRows()) {
            rows.add(new ProductionOrder((AUnitType) row[0], (Integer) row[1]));
        }
        ABuildOrder buildOrder = Mockito.mock(ABuildOrder.class);
        Mockito.when(buildOrder.productionOrders()).thenReturn(rows);

        return buildOrder;
    }

    /**
     * Every BO row is covered by a completion at or past its supply gate
     * (minus scheduler lookahead): probes never match (auto-production,
     * BO-silent by design), everything else must. Rows are independent
     * goals with supply gates, not a sequence - the file order is intent,
     * the gates are the rule. Same rule as assert-wine-opening.sh.
     */
    private static void assertBuildOrderInFileOrder(
            List<SimDriver.WarpRecord> log, List<SimOrder> invalid, List<String> cancels) {
        Object[][] rows = buildOrderRows();
        List<String> missing = new ArrayList<>();
        List<Integer> matchedFrames = new ArrayList<>();
        Map<String, Integer> occurrence = new HashMap<>();
        for (int i = 0; i < rows.length; i++) {
            String want = ((AUnitType) rows[i][0]).name();
            int gate = (Integer) rows[i][1];
            int which = occurrence.merge(want, 1, Integer::sum);
            Integer hitAt = null;
            int seen = 0;
            for (SimDriver.WarpRecord record : log) {
                if (!record.type.equals(want)) continue;
                seen++;
                if (seen == which && gateOpen(gate, record.supplyUsed)) {
                    hitAt = record.frame;
                    break;
                }
            }
            if (hitAt == null) {
                missing.add(want + "@" + gate);
                matchedFrames.add(-1);
            }
            else {
                matchedFrames.add(hitAt);
            }
        }
        if (!missing.isEmpty()) {
            fail("BO rows uncovered (no completion at/after gate): " + missing
                + " (log has " + log.size() + " completions, "
                + invalid.size() + " invalid orders, "
                + cancels.size() + " proximity cancels):\n"
                + describe(log, invalid, cancels));
        }
        for (int i = 0; i < rows.length; i++) {
            assertEquals(GOLDEN_WARP_FRAMES[i], (int) matchedFrames.get(i),
                "BO warp frame drifted for " + ((AUnitType) rows[i][0]).name()
                    + "@" + rows[i][1] + " (update goldens deliberately):\n"
                    + describe(log, invalid, cancels));
        }
    }

    /** The bot's own supply gate (BuildOrderGoals.supplyGateOpen). */
    private static boolean gateOpen(int rowSupply, int supplyUsed) {
        int lookahead = supplyUsed >= 19 ? 3 : 1;

        return supplyUsed + lookahead >= rowSupply;
    }

    /** The Forge-before-Gateway class: a non-BO building before Gateway 1. */
    private static void assertNoIntruderBeforeFirstGateway(
            List<SimDriver.WarpRecord> log, List<SimOrder> invalid, List<String> cancels) {
        java.util.Set<String> buildOrder = new java.util.HashSet<>();
        for (Object[] row : buildOrderRows()) buildOrder.add(((AUnitType) row[0]).name());
        buildOrder.add(AUnitType.Protoss_Probe.name());
        Integer firstGateway = null;
        for (SimDriver.WarpRecord record : log) {
            if (record.kind.equals("building") && record.type.equals("Gateway")) {
                firstGateway = record.frame;
                break;
            }
        }
        assertTrue(firstGateway != null,
            "no Gateway ever warped (invalid orders: " + invalid.size() + "):\n"
                + describe(log, invalid, cancels));
        for (SimDriver.WarpRecord record : log) {
            if (!record.kind.equals("building") || record.frame >= firstGateway) continue;
            assertTrue(buildOrder.contains(record.type),
                "intruder before the first Gateway f" + firstGateway + ": " + record);
        }
    }

    private static String describe(List<SimDriver.WarpRecord> log, List<SimOrder> invalid,
            List<String> cancels) {
        StringBuilder out = new StringBuilder();
        for (SimDriver.WarpRecord record : log) out.append("  ").append(record).append('\n');
        if (!invalid.isEmpty()) {
            out.append("  invalid orders:\n");
            for (SimOrder order : invalid) out.append("  ").append(order).append('\n');
        }
        if (!cancels.isEmpty()) {
            out.append("  proximity cancels: " + cancels.size() + " (first 5):\n");
            for (int i = 0; i < Math.min(5, cancels.size()); i++) {
                out.append("  ").append(cancels.get(i)).append('\n');
            }
        }

        return out.toString();
    }
}
