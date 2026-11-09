package atlantis.game.util;

import atlantis.Atlantis;
import atlantis.debug.profiler.LongFrames;
import atlantis.debug.profiler.RealTime;
import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.information.enemy.UnitsArchive;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import atlantis.util.AConsole;
import atlantis.util.log.EventLog;
import atlantis.util.log.LogKey;
import atlantis.util.log.LogLevel;
import atlantis.util.log.PostGameDigest;

import java.util.*;

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
            AConsole.println(LogLevel.LOW,
                "\n### Stopped (no game end reached) after " + totalS + " seconds. ###"
                    + "\n### Units killed/lost:    " + Atlantis.KILLED + "/" + Atlantis.LOST + " ###\n" +
                    "### Resource killed/lost: " + (resourceBalance > 0 ? "+" + resourceBalance : resourceBalance) +
                    " ###\n" +
                    buildingsLine());
            BuildOrderAudit.printAudit();
            printPostGameDigest();
        } catch (Throwable ignored) {
        }
    }

    public static void print(boolean winner) {
        printed = true;
        int realSeconds = (int) RealTime.gameLengthInRealSeconds();
        int frame = gameFrameOrMinusOne();
        String dir = workingDirectory();

        LongFrames.printSummary();

        AConsole.println(LogLevel.LOW, formatMatchOver(
            winner, Atlantis.KILLED, Atlantis.LOST, realSeconds, frame, dir));
        rowMatchSummary(
            winner, Atlantis.KILLED, Atlantis.LOST, realSeconds, frame, dir,
            Math.max(0, frame), clockOrUnknown());

        if (Atlantis.game() == null) {
            return;
        }

        int resourceBalance = AGame.killsLossesResourceBalance();
        int totalS = AGame.timeSeconds();
        AConsole.println(LogLevel.LOW,
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
        printPostGameDigest();
    }

    /**
     * End-of-match block (owner order 2026-11-09): the last thing the
     * console and the structured log show, in one fixed shape. Pure:
     * every input rides the signature, so the shape pins without a game.
     */
    public static String formatMatchOver(
        boolean won, int killed, int lost, int realSeconds, int gameFrame, String dir
    ) {
        StringBuilder block = new StringBuilder("#####################################\n");
        block.append("### Match over: ").append(won ? "VICTORY" : "DEFEAT").append("\n");
        block.append("### Killed: ").append(killed).append(" / Lost: ").append(lost).append("\n");
        block.append("### Real time: ").append(formatMinSec(realSeconds))
            .append(" (").append(realSeconds).append("s)\n");
        if (gameFrame >= 0) {
            block.append("### Game time: ").append(formatMinSec(gameFrame / 24))
                .append(" (frame ").append(gameFrame).append(")\n");
        }
        else {
            block.append("### Game time: ? (frame ?)\n");
        }
        block.append("### Dir: ").append(dir == null ? "?" : dir).append("\n");
        block.append("#####################################");

        return block.toString();
    }

    /**
     * Seconds as XmYs (180 -&gt; 3m0s). Non-negative by construction.
     */
    public static String formatMinSec(int totalSeconds) {
        int safe = Math.max(0, totalSeconds);

        return (safe / 60) + "m" + (safe % 60) + "s";
    }

    /**
     * The process working directory. Assumption (owner-visible): the
     * harness launches the bot with cwd inside the per-game directory
     * (the scbw GAME_xxx layout), so this names the game folder. If a
     * vehicle ever shows otherwise, plumb the dir explicitly instead.
     */
    static String workingDirectory() {
        try {
            String dir = System.getProperty("user.dir");

            return dir == null ? "?" : dir;
        } catch (Throwable ignored) {
            return "?";
        }
    }

    /**
     * The structured-log half of the block (the console half is the
     * println above): same fields as an EventLog row, so the log file
     * carries what the eye reads. All explicit - no game reads, the
     * caller gathers. Never throws.
     */
    public static void rowMatchSummary(
        boolean won, int killed, int lost, int realSeconds, int gameFrame,
        String dir, int frame, String clock
    ) {
        try {
            EventLog.key(LogKey.MATCH_SUMMARY)
                .param("result", won ? "VICTORY" : "DEFEAT")
                .param("killed", killed)
                .param("lost", lost)
                .param("realSeconds", realSeconds)
                .param("gameFrame", gameFrame)
                .param("dir", dir)
                .log(frame, clock);
        } catch (Throwable ignored) {
        }
    }

    private static int gameFrameOrMinusOne() {
        try {
            return A.now();
        } catch (Throwable ignored) {
            return -1;
        }
    }

    private static String clockOrUnknown() {
        try {
            return A.minSec();
        } catch (Throwable ignored) {
            return "?:??";
        }
    }

    /**
     * The concise end-of-game artifact the agent reads after every game
     * (owner order 2026-11-04): grouped twin/console counts with capped
     * samples plus the construction verdict (warps/off-plan/same-tile
     * dups), as one small JSON file and one bounded console section.
     * Runs on both exit paths ({@code OnGameEnd} with the whole exit
     * window, and the SIGTERM hook as backup) - a file written only by
     * the loser of a crash never exists, the periodic twin/ledger files
     * stay the forensic record for those games. Never throws.
     */
    private static void printPostGameDigest() {
        try {
            int warps = BuildingLedger.buildingWarps();
            int offPlan = BuildOrderAudit.mismatchLines().size();
            int sameTileDups = sameTileDuplicates();
            String construction = "{\"warps\":" + warps
                + ",\"offPlan\":" + offPlan
                + ",\"sameTileDups\":" + sameTileDups + "}";
            PostGameDigest.writeTo(PostGameDigest.OUTPUT_PATH, construction);
            PostGameDigest.printSummary(warps, offPlan, sameTileDups);
        } catch (Throwable ignored) {
        }
    }

    /**
     * Same-tile warp repeats: the double-mint twin (NEXT #238), counted
     * over the ledger's in-memory rows so no file is re-read. Delegates
     * to the ledger so cancelled-then-rebuilt rows never read as dups.
     */
    private static int sameTileDuplicates() {
        try {
            return BuildingLedger.sameTileDuplicates();
        } catch (Exception ignored) {
            return 0;
        }
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
