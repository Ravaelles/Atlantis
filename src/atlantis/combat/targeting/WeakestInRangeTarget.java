package atlantis.combat.targeting;

import atlantis.units.AUnit;
import atlantis.units.select.Selection;

/**
 * The weakest damageable enemy inside our own weapon range (hp first,
 * distance breaks ties). Focus, not walking: the pick never leaves
 * range, so committed fire does not donate hits or waste the cooldown.
 * Split out of {@code AcquireTarget} as ladder rule (3): one behavior,
 * one gate, ordered visibly in {@link TargetLadder}.
 */
public final class WeakestInRangeTarget {

    private WeakestInRangeTarget() {
    }

    public static AUnit pick(AUnit unit) {
        if (unit == null) return null;

        try {
            Selection enemies = unit.enemiesNear();
            if (enemies == null) return null;

            AUnit best = null;
            int bestHp = Integer.MAX_VALUE;
            double bestDist = Double.MAX_VALUE;
            for (AUnit enemy : enemies.list()) {
                if (enemy == null || !enemy.isAlive()) continue;
                if (!ASquadTargeting.isValidTarget(unit, enemy)) continue;
                if (!unit.hasWeaponRangeToAttack(enemy, 0)) continue;

                int hp = enemy.hp() + enemy.shields();
                double dist = unit.distTo(enemy);
                if (hp < bestHp || (hp == bestHp && dist < bestDist)) {
                    bestHp = hp;
                    bestDist = dist;
                    best = enemy;
                }
            }

            return best;
        } catch (Exception | Error ignored) {
            return null;
        }
    }
}
