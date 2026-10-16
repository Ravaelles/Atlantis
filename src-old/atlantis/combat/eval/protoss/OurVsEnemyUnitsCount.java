package atlantis.combat.eval.protoss;

import atlantis.units.AUnit;

public class OurVsEnemyUnitsCount {
    public static boolean moreOurThanEnemies(AUnit unit, AUnit enemy) {
        return countOurs(enemy) > countEnemies(unit);
    }

    public static double oursToEnemiesRatio(AUnit unit, AUnit enemy) {
        return (double) countOurs(enemy) / countEnemies(unit);
    }

    private static int countEnemies(AUnit unit) {
        return 1 + unit.enemiesNear().combatUnits().canAttack(unit, 8).count();
    }

    private static int countOurs(AUnit enemy) {
        return enemy.enemiesNear().combatUnits().canAttack(enemy, 8).count();
    }
}
