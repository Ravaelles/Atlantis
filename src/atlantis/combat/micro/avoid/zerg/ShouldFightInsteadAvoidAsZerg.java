package atlantis.combat.micro.avoid.zerg;

import atlantis.units.AUnit;
import atlantis.util.Enemy;
import atlantis.util.cache.Cache;

public class ShouldFightInsteadAvoidAsZerg {
    private static Cache<Boolean> cache = new Cache<>();

    public static boolean shouldFight(AUnit unit) {
        return cache.get(
            "shouldFight",
            1,
            () -> {
                if (!unit.isZerg()) return false;
                if (unit.combatEvalRelative() <= 0.7) return false;
                if (unit.isWounded() && unit.lastStartedRunningLessThanAgo(20)) return false;

                if (asHydra(unit)) return true;
                if (asZergling(unit)) return true;
                if (protectOurSunken(unit)) return true;

                return false;
            }
        );
    }

    private static boolean protectOurSunken(AUnit unit) {
        if (unit.hasCooldown()) return false;

        AUnit ourSunken = unit.friendsNear().sunkens().nearestTo(unit);
        if (ourSunken != null && ourSunken.meleeEnemiesNearCount() >= 2) {
            unit.setTooltip("SaveSunken");
            return true;
        }

        return false;
    }

    private static boolean asHydra(AUnit unit) {
        if (!unit.isHydralisk() || unit.hasCooldown()) return false;

        if (unit.isHealthy()) {
            unit.setTooltip("Keke");
            return true;
        }

        if (unit.hp() > 30 && unit.meleeEnemiesNearCount(1.9) <= 1 && unit.enemiesNear().ranged().empty()) {
            unit.setTooltip("Hehe");
            return true;
        }

        return false;
    }

    private static boolean asZergling(AUnit unit) {
        if (!unit.isZergling()) return false;

        int meleeEnemiesVeryNear = unit.meleeEnemiesNearCount(1.2);

        if (meleeEnemiesVeryNear == 1 && unit.hp() <= (Enemy.protoss() ? 17 : 7)) {
            unit.setTooltip("KamikazeA");
            return true;
        }

        if (meleeEnemiesVeryNear >= 2) {
            unit.setTooltip("KamikazeB");
            return true;
        }

        return false;
    }
}
