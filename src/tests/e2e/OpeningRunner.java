package tests.e2e;

import atlantis.production.orders.build.ABuildOrder;
import atlantis.production.orders.build.CurrentBuildOrder;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.units.AUnitType;
import org.mockito.Mockito;
import tests.unit.sim.SimConfig;
import tests.unit.sim.SimDriver;
import tests.unit.sim.SimMap;
import tests.unit.sim.SimOrder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The technical half of the sim opening gate: harness wiring, the step
 * loop and the warp-log plumbing. {@code SimOpeningToFiftyTest} keeps the
 * business half only (the rows, the goldens, the caps), readable as the
 * contract it is.
 *
 * <p>
 * The one seam that cannot move in here: the bank and supply numbers live
 * in the test's harness fields ({@code A.minerals()} and
 * {@code A.supplyUsed()} read them), so the sim pushes every frame through
 * {@link BankAndSupply} and the test owns the fields.
 * </p>
 */
final class OpeningRunner {

    private OpeningRunner() {
    }

    static Row at(AUnitType type, int gate) {
        return new Row(type, gate);
    }

    /**
     * Plays the rows from frame 1 until the supply target, with live V2.
     * Owns the build-order mock (restored afterwards) and the driver.
     */
    static OpeningResult playToSupply(
        List<Row> rows, SimConfig config, BankAndSupply numbers,
        int supplyTarget, int bankHorizon, int frameCap) throws Exception {
        ABuildOrder previous = CurrentBuildOrder.get();
        CurrentBuildOrder.set(fakeBuildOrder(rows));
        try {
            return play(config, numbers, supplyTarget, bankHorizon, frameCap);
        } finally {
            if (previous != null) CurrentBuildOrder.set(previous);
            else CurrentBuildOrder.clear();
        }
    }

    /**
     * The faithful half: builders walk the tile arena to the first Pylon.
     */
    static void assertFaithfulBuilderWalksToFirstPylon(BankAndSupply numbers) throws Exception {
        atlantis.config.env.ProductionV2Mode previous = atlantis.config.env.Env.productionV2();
        atlantis.config.env.Env.setProductionV2ForTest(atlantis.config.env.ProductionV2Mode.LIVE);
        atlantis.config.env.Env.setPlacementCatalogueForTest(false);
        try {
            SimMap map = SimMap.load("src/tests/resources/taucross-snapshot.json");
            map.install();
            SimDriver driver = newSimDriver(map, SimConfig.faithful(), numbers);
            try {
                driver.spawnResources();
                driver.spawnBase(0, 4);

                while (driver.frame() < 4000 && !hasWarpOf(driver, "Pylon")) {
                    driver.step();
                }

                assertTrue(hasWarpOf(driver, "Pylon"),
                    "a Faithful builder walks the tile arena and warps the first Pylon:\n"
                        + describe(driver.warpLog(), driver.invalidOrders(), driver.cancels(), null));
                assertTrue(driver.invalidOrders().isEmpty(),
                    "the walk issues no orders the engine would refuse:\n"
                        + driver.invalidOrders());
            } finally {
                driver.close();
            }
        } finally {
            atlantis.config.env.Env.setProductionV2ForTest(previous);
            atlantis.config.env.Env.setPlacementCatalogueForTest(false);
        }
    }

    private static OpeningResult play(
        SimConfig config, BankAndSupply numbers, int supplyTarget, int bankHorizon, int frameCap)
        throws Exception {
        atlantis.config.env.ProductionV2Mode previous = atlantis.config.env.Env.productionV2();
        atlantis.config.env.Env.setProductionV2ForTest(atlantis.config.env.ProductionV2Mode.LIVE);
        atlantis.config.env.Env.setPlacementCatalogueForTest(false);
        try {
            SimMap map = SimMap.load("src/tests/resources/taucross-snapshot.json");
            map.install();
            SimDriver driver = newSimDriver(map, config, numbers);
            try {
                driver.spawnResources();
                driver.spawnBase(0, 4);

                int maxBank = 0;
                while (numbers.supplyUsed() < supplyTarget && driver.frame() < frameCap) {
                    driver.step();
                    if (numbers.supplyUsed() < bankHorizon) {
                        maxBank = Math.max(maxBank, numbers.minerals());
                    }
                }

                return new OpeningResult(
                    numbers.supplyUsed(), driver.frame(), driver.warpLog(),
                    driver.invalidOrders(), driver.cancels(), maxBank);
            } finally {
                driver.close();
            }
        } finally {
            atlantis.config.env.Env.setProductionV2ForTest(previous);
            atlantis.config.env.Env.setPlacementCatalogueForTest(false);
        }
    }

    private static SimDriver newSimDriver(SimMap map, SimConfig config, BankAndSupply numbers) {
        return new SimDriver(map, config, new SimDriver.SimFields() {
            @Override
            public void setMinerals(int minerals) {
                numbers.setMinerals(minerals);
            }

            @Override
            public void setGas(int gas) {
                numbers.setGas(gas);
            }

            @Override
            public void setSupply(int used, int total) {
                numbers.setSupply(used, total);
            }
        }, 50);
    }

    /**
     * The fake build order under test, defined row by row.
     */
    private static ABuildOrder fakeBuildOrder(List<Row> rows) {
        // Rows first, stubbing after: a ProductionOrder reads the clock in
        // its constructor, and a mocked-static clock read inside an open
        // Mockito.when(...) fails the strict-stubs check.
        ArrayList<ProductionOrder> orders = new ArrayList<>();
        for (Row row : rows) {
            orders.add(new ProductionOrder(row.type, row.gate));
        }
        ABuildOrder buildOrder = Mockito.mock(ABuildOrder.class);
        Mockito.when(buildOrder.productionOrders()).thenReturn(orders);

        return buildOrder;
    }

    private static boolean hasWarpOf(SimDriver driver, String type) {
        for (SimDriver.WarpRecord record : driver.warpLog()) {
            if (record.kind.equals("building") && record.type.equals(type)) return true;
        }

        return false;
    }

    private static List<RowDivergence> divergenceTable(List<Row> rows, List<SimDriver.WarpRecord> log) {
        Map<String, Integer> occurrence = new HashMap<>();
        List<RowDivergence> table = new ArrayList<>();
        for (Row row : rows) {
            int which = occurrence.merge(row.type.name(), 1, Integer::sum);
            int seen = 0;
            Integer frame = null;
            Integer supply = null;
            for (SimDriver.WarpRecord record : log) {
                if (!record.type.equals(row.type.name())) continue;
                seen++;
                if (seen == which) {
                    frame = record.frame;
                    supply = record.supplyUsed;
                    break;
                }
            }
            // Halved like AGame (BWAPI raw is doubled); the gate file
            // speaks real supply, so own-cost must too (Zealot 2, Probe 1).
            table.add(new RowDivergence(row.type.name(), row.gate, which, frame, supply,
                row.type.ut().supplyRequired() / 2));
        }

        return table;
    }

    /**
     * The bot's own supply gate (BuildOrderGoals.supplyGateOpen): exact.
     */
    private static boolean gateOpen(int rowSupply, int supplyUsed) {
        return supplyUsed >= rowSupply;
    }

    /**
     * Duplicate-construction guard (live games duplicate Pylons and
     * Gateways): during the opening every building warp must answer a
     * row. Occurrence matching alone would absorb a duplicate into the
     * next row's slot, so the counts are asserted too. Units are excluded
     * - the dynamic army goal legitimately trains beyond its rows.
     */
    private static void assertNoExtraConstructions(
            List<Row> rows, List<RowDivergence> table, List<SimDriver.WarpRecord> log,
            List<SimOrder> invalid, List<String> cancels) {
        int openingEnd = 0;
        for (RowDivergence row : table) openingEnd = Math.max(openingEnd, row.warpFrame);
        Map<String, Integer> expected = new HashMap<>();
        for (Row row : rows) {
            if (!row.type.isABuilding()) continue;
            expected.merge(row.type.name(), 1, Integer::sum);
        }
        List<String> extras = new ArrayList<>();
        for (Map.Entry<String, Integer> need : expected.entrySet()) {
            int actual = 0;
            for (SimDriver.WarpRecord record : log) {
                if (!record.kind.equals("building")) continue;
                if (record.frame > openingEnd) continue;
                if (record.type.equals(need.getKey())) actual++;
            }
            if (actual > need.getValue()) {
                extras.add(need.getKey() + ": " + actual + " warps for " + need.getValue() + " rows");
            }
        }
        for (SimDriver.WarpRecord record : log) {
            if (!record.kind.equals("building") || record.frame > openingEnd) continue;
            if (!expected.containsKey(record.type)) {
                extras.add(record.type + " f" + record.frame + ": no row wants it");
            }
        }
        if (!extras.isEmpty()) {
            fail("extra constructions during the opening:\n  " + String.join("\n  ", extras) + "\n"
                + describe(log, invalid, cancels, table));
        }
    }

    private static String describe(List<SimDriver.WarpRecord> log, List<SimOrder> invalid,
                                   List<String> cancels, List<RowDivergence> table) {
        StringBuilder out = new StringBuilder();
        for (SimDriver.WarpRecord record : log) out.append("  ").append(record).append('\n');
        if (table != null) {
            out.append("  divergence (actual - ownCost - gate):\n");
            for (RowDivergence row : table) {
                out.append("  ").append(row.describe()).append('\n');
            }
        }
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

    /**
     * The number feeds the sim pushes into the harness fields every frame.
     */
    interface BankAndSupply {
        void setMinerals(int minerals);

        void setGas(int gas);

        void setSupply(int used, int total);

        int minerals();

        int supplyUsed();
    }

    /**
     * One build-order row: which type, at which supply gate.
     */
    static final class Row {
        final AUnitType type;
        final int gate;

        Row(AUnitType type, int gate) {
            this.type = type;
            this.gate = gate;
        }
    }

    /**
     * Everything one playthrough observed. Assertions read like the contract.
     */
    static final class OpeningResult {
        private final int supplyReached;
        private final int frames;
        private final List<SimDriver.WarpRecord> log;
        private final List<SimOrder> invalid;
        private final List<String> cancels;
        private final int maxBank;

        OpeningResult(
            int supplyReached, int frames, List<SimDriver.WarpRecord> log,
            List<SimOrder> invalid, List<String> cancels, int maxBank) {
            this.supplyReached = supplyReached;
            this.frames = frames;
            this.log = log;
            this.invalid = invalid;
            this.cancels = cancels;
            this.maxBank = maxBank;
        }

        List<SimDriver.WarpRecord> warpLog() {
            return log;
        }

        List<SimOrder> invalidOrders() {
            return invalid;
        }

        /**
         * The opening plays through instead of stalling halfway.
         */
        void assertReachedSupply(int target) {
            assertTrue(supplyReached >= target,
                "the opening stalls before " + target + " supply at frame " + frames
                    + " (" + supplyReached + "/" + target + " real, "
                    + cancels.size() + " proximity cancels):\n"
                    + describe(log, invalid, cancels, null));
        }

        /**
         * Every row warps in file order, matched by occurrence (k-th
         * completion of a type answers the k-th row of that type), at or
         * past its own gate, on its golden frame. Per-row lateness prints
         * as the tuning watch (report, never fail: warp lateness is bank
         * and travel physics, not a defect).
         */
        void assertWarpsMatch(List<Row> rows, int[] goldenFrames) {
            List<RowDivergence> table = divergenceTable(rows, log);
            List<String> missing = new ArrayList<>();
            for (RowDivergence row : table) {
                if (row.warpFrame == null) missing.add(row.type + "@" + row.gate);
            }
            if (!missing.isEmpty()) {
                fail("BO rows uncovered (no completion at all): " + missing
                    + " (log has " + log.size() + " completions, "
                    + invalid.size() + " invalid orders, "
                    + cancels.size() + " proximity cancels):\n"
                    + describe(log, invalid, cancels, table));
            }
            for (int i = 0; i < table.size(); i++) {
                RowDivergence row = table.get(i);
                assertTrue(
                    gateOpen(row.gate, row.supplyUsed),
                    "BO row started before its own gate (exact, no lookahead): " + row.describe()
                        + ":\n" + describe(log, invalid, cancels, table));
                assertEquals(
                    goldenFrames[i], (int) row.warpFrame,
                    "BO warp frame drifted for " + row.type
                        + "@" + row.gate + " (update goldens deliberately):\n"
                        + describe(log, invalid, cancels, table));
            }
            List<String> late = new ArrayList<>();
            for (RowDivergence row : table) {
                if (row.divergence() >= 2) late.add(row.describe());
            }
            if (!late.isEmpty()) {
                System.out.println("SimOpening: late rows (delta>=2, tuning watch):\n  "
                    + String.join("\n  ", late));
            }
            assertNoExtraConstructions(rows, table, log, invalid, cancels);
        }

        /**
         * The Forge-before-Gateway class: no non-BO building before Gateway 1.
         */
        void assertNoIntruderBeforeFirstGateway(List<Row> rows) {
            java.util.Set<String> buildOrder = new java.util.HashSet<>();
            for (Row row : rows) buildOrder.add(row.type.name());
            buildOrder.add(AUnitType.Protoss_Probe.name());
            Integer firstGateway = null;
            for (SimDriver.WarpRecord record : log) {
                if (record.kind.equals("building") && record.type.equals("Gateway")) {
                    firstGateway = record.frame;
                    break;
                }
            }
            assertNotNull(firstGateway, "no Gateway ever warped (invalid orders: " + invalid.size() + "):\n"
                + describe(log, invalid, cancels, null));
            for (SimDriver.WarpRecord record : log) {
                if (!record.kind.equals("building") || record.frame >= firstGateway) continue;
                assertTrue(buildOrder.contains(record.type),
                    "intruder before the first Gateway f" + firstGateway + ": " + record);
            }
        }

        void assertBankBelow(int cap) {
            assertTrue(maxBank <= cap,
                "the bank hoards " + maxBank + " minerals below the opening horizon (cap "
                    + cap + "):\n" + describe(log, invalid, cancels, null));
        }

        void assertNoInvalidOrders() {
            assertTrue(invalid.isEmpty(),
                "the bot issued orders the engine would refuse:\n" + invalid);
        }
    }

    /**
     * One row joined to its completion, with the divergence math.
     */
    private static final class RowDivergence {
        final String type;
        final int gate;
        final int occurrence;
        final Integer warpFrame;
        final Integer supplyUsed;
        final int ownCost;

        RowDivergence(String type, int gate, int occurrence, Integer warpFrame,
                      Integer supplyUsed, int ownCost) {
            this.type = type;
            this.gate = gate;
            this.occurrence = occurrence;
            this.warpFrame = warpFrame;
            this.supplyUsed = supplyUsed;
            this.ownCost = ownCost;
        }

        /**
         * Supply at warp, minus what the warp itself costs, minus the gate.
         */
        int divergence() {
            return supplyUsed - ownCost - gate;
        }

        String describe() {
            return type + "@" + gate + " (#" + occurrence + ") warp f" + warpFrame
                + " supply " + supplyUsed + " diff " + (divergence() >= 0 ? "+" : "")
                + divergence();
        }
    }
}
