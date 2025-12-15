package atlantis.game.util;

import atlantis.Atlantis;
import atlantis.debug.profiler.LongFrames;
import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.information.enemy.UnitsArchive;
import atlantis.units.AUnit;
import atlantis.units.select.Select;

public class GameSummary {
    public static void print(boolean winner) {
        String result = "#####################################\n";
        result += "############ " + (winner ? "VICTORY!" : "Defeat...") + " ###############\n";
        result += "############ Lost: " + Atlantis.LOST + " ################\n";
        result += "########## Killed: " + Atlantis.KILLED + " ################\n";
        result += "#####################################\n";

        LongFrames.printSummary();

        A.println(result);

        if (Atlantis.game() == null) {
            return;
        }

        int resourceBalance = AGame.killsLossesResourceBalance();
        int totalS = AGame.timeSeconds();
        A.println(
            "\n### Total time: " + totalS + " seconds. ###" +
                (totalS >= 90 ? "\n\n" : "\n### Total frames: " + A.now() + " frames. ###\n\n") +
                "### Units killed/lost:    " + Atlantis.KILLED + "/" + Atlantis.LOST + " ###\n" +
                "### Resource killed/lost: " + (resourceBalance > 0 ? "+" + resourceBalance : resourceBalance) +
                " ###\n"
        );

        if (A.isUms()) {
            paintCombatStatistics();

            UnitsArchive.paintLostUnits();
            UnitsArchive.paintKilledUnits();
        }
        UnitsArchive.paintKillLossResources();
    }

    private static void paintCombatStatistics() {
        AUnit unit = Select.ourCombatUnits().havingWeapon().first();
        if (unit == null) return;

//        A.println("### " + unit.type() + " Wound: " + unit.woundHp() + "hp ###");
//        int totalHits = unit.totalHitCount();
        int totalHits = Select.ourCombatUnits().totalHits();

        A.println("### Total hits: " + totalHits + " ###\n");
    }
}
