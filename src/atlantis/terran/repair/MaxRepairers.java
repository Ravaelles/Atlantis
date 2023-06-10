package atlantis.terran.repair;

import atlantis.game.AGame;
import atlantis.units.AUnit;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import atlantis.util.Enemy;

public class MaxRepairers {

    public static final int MAX_REPAIRERS_AT_ONCE = 8;

    // =========================================================

    public static boolean tooManyRepairers(AUnit unit) {
        return ARepairAssignments.countRepairersForUnit(unit) >= RepairerAssigner.optimalRepairersFor(unit);
    }

    public static boolean usingMoreRepairersThanAllowed() {
        return ARepairAssignments.countTotalRepairers() > MAX_REPAIRERS_AT_ONCE;
    }

    protected static int optimalRepairersForBunker(AUnit bunker) {
        Selection potentialEnemies = Select.enemy().combatUnits().inRadius(18, bunker);

        if (potentialEnemies.empty()) {
            return 0;
        }

        int enemiesVeryNear = potentialEnemies.inRadius(10, bunker).count();
        int enemiesQuiteFar = potentialEnemies.count() - enemiesVeryNear;
        double optimalNumber;

        if (Enemy.protoss()) {
            optimalNumber = enemiesVeryNear + enemiesQuiteFar * 0.2;
        } else if (Enemy.terran()) {
            optimalNumber = enemiesVeryNear * 0.38 + enemiesQuiteFar * 0.1;
        } else {
            optimalNumber = enemiesVeryNear * 0.4 + enemiesQuiteFar * 0.15;
        }

        if (bunker.hp() < 250) {
            optimalNumber += 2;
        }

        return Math.min(6, (int) Math.ceil(optimalNumber));
    }
}
