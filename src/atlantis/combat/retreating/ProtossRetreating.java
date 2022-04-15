package atlantis.combat.retreating;

import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Selection;
import atlantis.util.Enemy;

public class ProtossRetreating {

    public static boolean shouldSmallScaleRetreat(AUnit unit, Selection enemies) {
        if (unit.isRanged()) {
            return false;
        }

        if (unit.isReaver() && unit.cooldownRemaining() <= 4) {
            return false;
        }

        double radius = 1.2;

        // =========================================================

        AUnit enemy = enemies.nearestTo(unit);
        int enemiesNear = enemies.inRadius(2, unit).count();
        int ourCount;
        if (enemy != null) {
            ourCount = enemy.enemiesNear().inRadius(radius, unit).count();
        } else {
            ourCount = unit.friendsNear().inRadius(0.6, unit).count();
        }

        if (ourCount == enemiesNear && unit.isZealot() && unit.hpMoreThan(36) && unit.isMissionDefend()) {
            unit.setTooltip("Homeland!", true);
            unit.addLog("Homeland!");
            return false;
        }

        Selection friendsNear = unit.friendsNear().inRadius(1, unit);

        if (friendsNear.atLeast(3)) {
            return false;
        }

        if (ourCount <= enemiesNear && unit.friendsNear().inRadius(5, unit).atLeast(2)) {
//        Selection enemiesAroundEnemy = enemy.friendsNear().inRadius(radius, unit);
//        if (oursAroundEnemy.count() > enemiesAroundEnemy.count()) {
            if (
                friendsNear.atMost(1) && unit.enemiesNear().inRadius(7, unit).onlyMelee()
            ) {
                unit.setTooltip("RetreatingB", false);
                unit.addLog("RetreatingB");
                return true;
            }
        }

        if (Enemy.protoss() && applyZealotVsZealotFix(unit, enemies)) {
            unit.setTooltip("RetreatingZ", false);
            unit.addLog("RetreatingZ");
            return true;
        }

        // =========================================================

        Selection friends = unit.friendsNear().inRadius(radius, unit);
        Selection veryCloseEnemies = enemies.inRadius(radius, unit);

        if (veryCloseEnemies.totalHp() > friends.totalHp()) {
            unit.setTooltip("RetreatingA", false);
            unit.addLog("RetreatingA");
            return true;
        }

        // =========================================================

        return false;
    }

    // =========================================================

    private static boolean applyZealotVsZealotFix(AUnit unit, Selection enemies) {
        if (unit.friendsNear().ofType(AUnitType.Protoss_Photon_Cannon).inRadius(3.8, unit).notEmpty()) {
            return false;
        }

        int ourZealots = unit.friendsNear().ofType(AUnitType.Protoss_Zealot).inRadius(1.4, unit).count();
        int enemyZealots = enemies.ofType(AUnitType.Protoss_Zealot).inRadius(1.4, unit).count();

        if (ourZealots < enemyZealots) {
            return true;
        }

//        if (ourZealots < enemyZealots) {
//            return true;
//        }

        return false;
    }

}
