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

public class GameSummary {
    public static void print(boolean winner) {
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
    private static String buildingsLine() {
        // The race comes from AtlantisRaceConfig, which is the single source of truth for
        // "which race are we" (an earlier two-sources-of-truth bug read it back from the game
        // and disabled a whole race's code - see _AI/CHALLENGES/BuildAndLogging.md).
        StringBuilder line = new StringBuilder(
            "### " + atlantis.config.AtlantisRaceConfig.MY_RACE.name() + ": "
        );

        for (AUnitType type : CompletedUnitsHistory.typesByFirstCompletion()) {
            int alive = Select.ourWithUnfinished(type).count();
            int lost = UnitsArchive.ourLost(type);

            // `AUnitType.name()` already strips the race prefix, which is why this report can
            // be written once rather than per race.
            line.append(type.name()).append("=").append(alive);

            // `(1 lost)` only when something was actually lost, and only when that is not
            // already the whole story: a unit with nothing destroyed reads as a plain count
            // rather than as `Zealot=3 (0 lost)`, which is noise.
            //
            // The loss count is NOT `built - alive`. A unit can be lost and rebuilt, and the
            // difference of the two totals would then report the rebuild as if nothing had
            // been killed - `UnitsArchive` counts destruction events, which is the number a
            // player would quote.
            if (lost > 0) {
                line.append(" (").append(lost).append(" lost)");
            }

            line.append(" ");
        }

        // Kept from the old line so a run that produced nothing still says so explicitly
        // rather than printing an empty list that reads like a formatting bug.
        if (CompletedUnitsHistory.typesByFirstCompletion().isEmpty()) {
            line.append("(nothing completed) ");
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
