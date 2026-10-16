package atlantis.protoss.reaver.reaver_with_shuttle;

import atlantis.units.AUnit;

public class IsNotSafeToUnloadReaverHere {
    public static boolean check(AUnit unit, AUnit reaver) {
        if (preventFromUnloadingNextToMelee(unit, reaver)) return true;
        if (preventFromUnloadingNextToRanged(unit, reaver)) return true;

        return false;
    }

    private static boolean preventFromUnloadingNextToRanged(AUnit unit, AUnit reaver) {
        double minSafety = 2;
        if (reaver.hp() <= 70) minSafety = 5.7;
        else if (reaver.hp() <= 120) minSafety = 3.9;

        int minEnemies = reaver.hp() <= 80 ? 1 : 2;
        return unit.enemiesNear().ranged().canAttack(reaver, minSafety).countInRadius(minSafety, unit) >= minEnemies;
    }

    private static boolean preventFromUnloadingNextToMelee(AUnit unit, AUnit reaver) {
        double minDist = 2;
        if (reaver.hp() <= 70) minDist = 5.7;
        else if (reaver.hp() <= 120) minDist = 3.9;

        return unit.enemiesNear().melee().countInRadius(minDist, unit) > 0;
    }
}
