package atlantis.combat.micro.avoid.terran;

import atlantis.units.AUnit;
import atlantis.util.Enemy;

public class MarineCanAttackNearEnemy {
    public static boolean allowedForThisUnit(AUnit unit) {
        if (unit.cooldown() >= 2) return false;

        return (
                unit.meleeEnemiesNearCount(2.2 + unit.woundPercent() / 55.0) <= 0
            || healthyAndHasMedic(unit)
        );
    }

    private static boolean healthyAndHasMedic(AUnit unit) {
        int meleeEnemiesNearCount = unit.meleeEnemiesNearCount(2.1);

        if (Enemy.protoss()) {
            if (meleeEnemiesNearCount >= 3) return unit.isHealthy() && unit.lastStartedAttackMoreThanAgo(60);
            if (meleeEnemiesNearCount >= 2) return unit.isHealthy();
        }

        return unit.hp() >= 34 && unit.hasMedicInRange();
    }
}
