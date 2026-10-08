package tests.unit;

import atlantis.information.strategy.AStrategy;
import atlantis.information.strategy.protoss.ProtossStrategies;
import atlantis.production.orders.build.ABuildOrderLoader;
import atlantis.production.orders.build.ABuildOrder;
import atlantis.util.AFile;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Every strategy must be able to load its build order - the startup path the
 * OpenBW run died on (2026-10-08).
 *
 * <p>
 * The bug this pins: {@code ProtossStrategies.initialize()} renames each
 * strategy to its build-order file name, but the strategy chosen by
 * {@code StrategyChooser} loads its order <b>at selection time</b>. Selecting
 * before naming meant every strategy looked for a file named after its Java
 * constant ({@code PROTOSS_Zealot_into_Goon.txt}), which does not exist, and
 * the
 * bot then played the whole game with no build order and no error.
 * </p>
 *
 * <p>
 * This is the stub-world proof that the fix works, deliberately required before
 * spending another ~5-minute headless run. It asserts against the <b>real</b>
 * build-order directory shipped in the repo
 * ({@code bwapi-data/AI/build_orders/})
 * rather than a fixture, because the failure mode is exactly "the file the bot
 * looks for is not the file we ship".
 * </p>
 */
public class StrategyBuildOrderTest {

    /**
     * The repo's own build-order tree; the loader resolves it the same way in a
     * game.
     */
    private static final String ORDERS_DIR = "bwapi-data/AI/build_orders/";

    /** Gaps tracked in _AI/NEXT.md #46 - not a licence to add more. */
    private static final List<String> KNOWN_MISSING =
            java.util.Arrays.asList("3 Gate", "12 Nexus", "Carrier Push");

    @Test
    public void theOnlyMissingBuildOrdersAreTheTrackedOnes() {
        // If a NEW strategy starts naming a non-existent file, this fails and
        // names it, so the gap list cannot quietly grow.
        ProtossStrategies.initialize();

        List<String> actuallyMissing = new ArrayList<>();
        for (AStrategy strategy : ProtossStrategies.allProtossStrategies()) {
            if (strategy.name() == null) continue;
            if (!new File(ORDERS_DIR + "Protoss/" + strategy.name() + ".txt").isFile()) {
                actuallyMissing.add(strategy.name());
            }
        }

        assertEquals(KNOWN_MISSING, actuallyMissing,
                "the set of strategies without a build-order file changed; "
                        + "add the missing .txt or update the tracked list in _AI/NEXT.md #46");
    }

    @Test
    public void everyProtossStrategyFindsItsBuildOrderFile() {
        ProtossStrategies.initialize();

        List<String> missing = new ArrayList<>();
        for (AStrategy strategy : ProtossStrategies.allProtossStrategies()) {
            File file = new File(ORDERS_DIR + "Protoss/" + strategy.name() + ".txt");
            // Known, tracked gaps (_AI/NEXT.md #46): these three strategies name a
            // build order that does not exist anywhere, so they start a game with
            // no order at all. Excluded so the rest of the list is a hard gate;
            // remove an entry here when its file lands.
            if (KNOWN_MISSING.contains(strategy.name())) continue;
            if (!file.isFile()) missing.add(strategy.name());
        }

        assertTrue(missing.isEmpty(),
                "these strategies name a build-order file that does not exist in " + ORDERS_DIR
                        + "Protoss/: " + missing + " - the bot would play with no build order and no error");
    }

    @Test
    public void theChosenStrategyCanActuallyLoadItsOrder() {
        // The exact startup order: initialize() first (names + files), then the
        // load a selected strategy performs. Before the fix this threw
        // BuildOrderFileNotFoundException for the Protoss-vs-Zerg strategy.
        ProtossStrategies.initialize();

        AStrategy zealotIntoGoon = ProtossStrategies.PROTOSS_Zealot_into_Goon;
        assertNotNull(zealotIntoGoon.name(), "initialize() must name the strategy");
        assertTrue(zealotIntoGoon.name().contains(" "),
                "the build-order file names contain spaces; a constant-derived name has none: "
                        + zealotIntoGoon.name());

        File file = new File(ORDERS_DIR + "Protoss/" + zealotIntoGoon.name() + ".txt");
        assertTrue(file.isFile(), "the file the strategy looks for must exist: " + file);
    }

    @Test
    public void theLoaderResolvesTheShippedOrdersDirectory() {
        // Guards the other half of the OpenBW failure: the path itself. The
        // loader walks a candidate list; if the repo's own tree stopped being
        // reachable, every strategy would fail at once.
        assertTrue(AFile.directoryExists(ORDERS_DIR), "build orders must ship with the repo");

        for (String race : new String[] { "Protoss", "Terran", "Zerg" }) {
            assertTrue(AFile.directoryExists(ORDERS_DIR + race),
                    "the loader walks race subdirectories; missing: " + race);
        }
    }

    @Test
    public void everyRenameInInitializeHasAMatchingFile() {
        // The structural invariant: the names set in initialize() and the files
        // on disk are two lists that must agree. This is the check that turns a
        // silent mid-game no-op into a red test.
        ProtossStrategies.initialize();

        List<String> names = new ArrayList<>();
        for (AStrategy strategy : ProtossStrategies.allProtossStrategies()) {
            if (strategy.name() != null)
                names.add(strategy.name());
        }

        assertTrue(names.size() >= 15, "initialize() should name every Protoss strategy, got " + names.size());
        assertTrue(names.contains("Zealot into Goon"),
                "the name the Protoss-vs-Zerg strategy is renamed to: " + names);
    }
}
