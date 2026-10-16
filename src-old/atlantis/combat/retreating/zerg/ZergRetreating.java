package atlantis.combat.retreating.zerg;

import atlantis.combat.retreating.RetreatManager;
import atlantis.decisions.Decision;
import atlantis.units.AUnit;
import atlantis.units.select.Selection;
import atlantis.game.player.Enemy;

public class ZergRetreating {
    public static Decision decision(AUnit unit) {
        Selection enemies = enemies(unit);

        if (shouldSmallScaleRetreat(unit, enemies)) {
            RetreatManager.GLOBAL_RETREAT_COUNTER++;
            return Decision.TRUE;
        }

        if ("Retreat".equals(unit.tooltip())) {
            unit.removeTooltip();
        }

        return Decision.FALSE;
    }

    // =========================================================

    private static boolean shouldSmallScaleRetreat(AUnit unit, Selection enemies) {
        int ourStrength = unit.friendsNear()
            .inRadius(5, unit)
            .count();

        double enemiesMultiplier = enemiesMultiplier();
        double enemiesStrength = enemies.inRadius(5, unit).count();

        return ourStrength < enemiesStrength * enemiesMultiplier;
    }

    private static double enemiesMultiplier() {
        if (Enemy.zerg()) return 1;
        if (Enemy.terran()) return 0.8;
        if (Enemy.protoss()) return 2.8;
        return 1;
    }

    private static Selection enemies(AUnit unit) {
        return unit.enemiesNear()
            .ranged()
            .canAttack(unit, 6);
    }
}
