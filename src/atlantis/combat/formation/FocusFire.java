package atlantis.combat.formation;

import atlantis.combat.targeting.ASquadTargeting;
import atlantis.units.AUnit;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;

/**
 * Shared focus target (formation Phase A): the weakest enemy in reach of
 * the most guns, sticky until dead. Uncommitted units (no current
 * target) join it instead of each acquiring their own nearest enemy -
 * the scatter that lets a wounded Zealot drag one Dragoon off while the
 * rest chew a healthy one. Committed units finish theirs first
 * (sticky-first ordering in the caller, never here).
 *
 * <p>Validity reuses the squad target gate
 * ({@code ASquadTargeting.isValidTarget}: buildings, lings, workers,
 * wounded, dead men and unhittable types stay excluded), so focus can
 * never aim at a {@code DeadMan} - the in-flight damage is already
 * spoken for. Single-enemy situations answer the same unit the nearest
 * rule would, so duels stay byte-identical.</p>
 */
public final class FocusFire {

    private FocusFire() {
    }

    /**
     * The weakest valid enemy within reach, or null when none
     * qualifies (the caller falls back to the squad target, then the
     * nearest enemy, unchanged).
     */
    public static AUnit sharedTarget(AUnit unit, double maxDist) {
        if (unit == null || !unit.isAlive()) return null;

        Selection enemies = unit.enemiesNear();
        if (enemies == null) return null;

        AUnit best = null;
        int bestHp = Integer.MAX_VALUE;
        int bestGuns = -1;
        for (AUnit enemy : enemies.list()) {
            if (enemy == null || !enemy.isAlive()) continue;
            if (unit.distTo(enemy) > maxDist) continue;
            if (!unit.hasWeaponToAttackThisUnit(enemy)) continue;
            if (!ASquadTargeting.isValidTarget(unit, enemy)) continue;
            int hp = enemy.hp() + enemy.shields();
            int guns = coveringGuns(enemy, maxDist);
            if (hp < bestHp || (hp == bestHp && guns > bestGuns)) {
                best = enemy;
                bestHp = hp;
                bestGuns = guns;
            }
        }
        return best;
    }

    /**
     * How many of our combat units could engage this enemy (same
     * envelope): the tiebreak that piles guns onto one target instead
     * of spreading them across equals.
     */
    static int coveringGuns(AUnit enemy, double maxDist) {
        Selection ours = Select.ourCombatUnits();
        if (ours == null) return 0;

        int guns = 0;
        for (AUnit u : ours.list()) {
            if (u == null || !u.isAlive()) continue;
            if (!u.hasWeaponToAttackThisUnit(enemy)) continue;
            if (u.distTo(enemy) <= maxDist) guns++;
        }
        return guns;
    }
}
