package atlantis.game.util;

import atlantis.Atlantis;
import atlantis.debug.profiler.LongFrames;
import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.information.enemy.UnitsArchive;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import atlantis.util.AConsole;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameSummary {
    private static boolean printed = false;

    /**
     * Last resort final message: the JVM is going down without a game end
     * (wall-timeout SIGTERM, owner kill) and {@code OnGameEnd} never ran,
     * so no summary would exist at all. Prints the same ### stat lines
     * with a "Stopped" header instead of a verdict - a timeout is not a
     * defeat. Never throws: a hook that throws poisons shutdown.
     */
    public static void printOnShutdown() {
        if (printed) return;

        printed = true;
        try {
            // Twins buffer up to 720 frames between flushes; a kill without
            // a game end used to drop the whole tail (measured 2026-10-19:
            // FE run `112337` decided at frame 2120 with zero twin rows
            // after 1558 - the rows sat in the buffer when SIGTERM landed).
            // The hook runs on SIGTERM, so flush here: a capped run keeps
            // the frames that explain why it was capped.
            atlantis.util.log.EventLog.flush();
            if (Atlantis.game() == null) return;

            int totalS = AGame.timeSeconds();
            int resourceBalance = AGame.killsLossesResourceBalance();
            AConsole.println(
                "\n### Stopped (no game end reached) after " + totalS + " seconds. ###"
                    + "\n### Units killed/lost:    " + Atlantis.KILLED + "/" + Atlantis.LOST + " ###\n" +
                    "### Resource killed/lost: " + (resourceBalance > 0 ? "+" + resourceBalance : resourceBalance) +
                    " ###\n" +
                    buildingsLine());
            BuildOrderAudit.printAudit();
        } catch (Throwable ignored) {
        }
    }

    public static void print(boolean winner) {
        printed = true;
        String result = "#####################################\n";
        result += "############ " + (winner ? "VICTORY!" : "Defeat...") + " ###############\n";
        result += "############ Lost: " + Atlantis.LOST + " ################\n";
        result += "########## Killed: " + Atlantis.KILLED + " ################\n";
        result += "#####################################\n";

        LongFrames.printSummary();

        AConsole.println(result);

        if (Atlantis.game() == null) {
            return;
        }

        int resourceBalance = AGame.killsLossesResourceBalance();
        int totalS = AGame.timeSeconds();
        AConsole.println(
            "\n### Total time: " + totalS + " seconds. ###" +
                (totalS >= 90 ? "\n\n" : "\n### Total frames: " + A.now() + " frames. ###\n\n") +
                "### Units killed/lost:    " + Atlantis.KILLED + "/" + Atlantis.LOST + " ###\n" +
                "### Resource killed/lost: " + (resourceBalance > 0 ? "+" + resourceBalance : resourceBalance) +
                " ###\n" +
                buildingsLine()
        );

        if (A.isUms()) {
            paintCombatStatistics();

            UnitsArchive.paintLostUnits();
            UnitsArchive.paintKilledUnits();
        }
        UnitsArchive.paintKillLossResources();
        BuildOrderAudit.printAudit();
    }

    /**
     * What we built, as `Type=remaining/built` - and only the types we actually built.
     *
     * <p>
     * The line used to be two hardcoded counts of what stands on the board
     * ({@code Pylons=.. Gateways=..}), which threw away the more useful half. Now each entry
     * is `remaining/built`, so the gap between them is what was destroyed: a game ending
     * `Pylon=3/4 Gateway=2/8` built four Pylons and lost one, built eight Gateways and lost
     * six - and it is immediately obvious that the army was doing something, which the old
     * line could not say at all.
     * </p>
     *
     * <p>
     * <b>The order is the game's order.</b> Types appear in the sequence their FIRST unit
     * finished ({@code CompletedUnitsHistory.typesByFirstCompletion}), so the line reads
     * like the build progression did - the opening first, tech later - instead of following
     * a hand-written list in this class that would need editing whenever the bot's opening
     * changes.
     * </p>
     */
    /**
     * What stands on the board, as `Type=completed` - only completed units,
     * only the types present right now.
     *
     * <p>
     * <b>Completed only (owner's ruling 2026-10-13, supersedes the 2026-10-12
     * unfinished-included rule):</b> a Gateway ordered 15 s before exit read
     * as a built Gateway, and E2E gates read this line - training must not
     * pass a built-gate.
     * </p>
     *
     * <p>
     * <b>The live board is the only source (owner's ruling, 2026-10-12): "no history for
     * counting - it is NOT and will never be the truth".</b> The former implementation read
     * `CompletedUnitsHistory` (onUnitComplete events) for the built total and it LIED on
     * OpenBW in both directions: the engine's `rearmUnitDiscovery()` re-delivers
     * UnitComplete for units that predate the bot's attach (duplicates), and units
     * completing while detached are delivered late or never (gaps). Measured in one run:
     * history said 15 buildings / the board held 9; for the same Gateway type one engine
     * reported alive=3 hist=2. `Select.ourWithUnfinishedOfType` reads
     * `playerUs().getUnits()` directly at print time, which is what the replay shows.
     * </p>
     *
     * <p>
     * <b>The order is the engine's own unit-list order</b> - deterministic per game, close
     * enough to build progression, and it needs no extra bookkeeping. (The previous order
     * came from UnitComplete events, which OpenBW distorts - see above.)
     * </p>
     */
    private static String buildingsLine() {
        // The race comes from AtlantisRaceConfig, which is the single source of truth for
        // "which race are we" (an earlier two-sources-of-truth bug read it back from the game
        // and disabled a whole race's code - see _AI/CHALLENGES/BuildAndLogging.md).
        StringBuilder line = new StringBuilder(
            "### " + atlantis.config.AtlantisRaceConfig.MY_RACE.name() + ": "
        );

        // Types present on the board, in the order the engine's own unit list presents them
        // (deterministic per game). A "first created frame" field would be nicer but does
        // not exist on AUnit and adding one just for the report is scope creep.
        List<AUnitType> types = new ArrayList<>();
        Map<AUnitType, Integer> firstSeenOrder = new HashMap<>();
        int order = 0;
        for (AUnit unit : Select.our().list()) {
            if (!firstSeenOrder.containsKey(unit.type())) {
                // The board list is order-stable enough for our purpose: the first time a
                // type is met in `playerUs().getUnits()` is deterministic per game (the same
                // list the engine returns). A `createdFrame` field would be nicer but does
                // not exist on AUnit and adding one just for the report is scope creep.
                firstSeenOrder.put(unit.type(), order++);
                types.add(unit.type());
            }
        }
        types.sort(Comparator.comparingInt(firstSeenOrder::get));

        Map<AUnitType, Integer> completed = new HashMap<>();
        for (AUnit unit : Select.our().list()) {
            completed.merge(unit.type(), 1, Integer::sum);
        }

        for (AUnitType type : types) {
            // `AUnitType.name()` already strips the race prefix, which is why this report can
            // be written once rather than per race.
            line.append(type.name()).append("=").append(completed.getOrDefault(type, 0)).append(" ");
        }

        // Kept from the old line so a run that produced nothing still says so explicitly
        // rather than printing an empty list that reads like a formatting bug.
        if (types.isEmpty()) {
            line.append("(nothing on board) ");
        }

        return line.append("###\n").toString();
    }

    private static void paintCombatStatistics() {
        AUnit unit = Select.ourCombatUnits().havingWeapon().first();
        if (unit == null) return;

//        AConsole.println("### " + unit.type() + " Wound: " + unit.woundHp() + "hp ###");
//        int totalHits = unit.totalHitCount();
        int totalHits = Select.ourCombatUnits().totalHits();

        AConsole.println("### Total hits: " + totalHits + " ###\n");
    }
}
