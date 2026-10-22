package atlantis.battle.targeting;

import atlantis.battle.DuelMemory;
import atlantis.battle.cluster.ClusterBreach;
import atlantis.battle.formation.CoverWithdraw;
import atlantis.battle.formation.FocusFire;
import atlantis.battle.formation.FormationTwin;
import atlantis.units.AUnit;
import atlantis.units.select.Selection;

/**
 * The target chain, stickiest first: committed, shared, covering, squad,
 * nearest, breach. Extracted from BattleCommander; the fight-or-hold
 * decision reads it as one call.
 */
public final class AcquireTarget {

    private AcquireTarget() {
    }

    public static AUnit acquire(AUnit unit) {
        if (unit == null || !unit.isAlive()) return null;
        // Shoot-now priority (owner 2026-10-26): a valid target inside
        // our own weapon range beats every sharing rule - walking past
        // it to a far squadmate's kill donates hits and wastes the
        // cooldown. Among in-range targets the weakest still draws the
        // fire (focus, no walking involved); the current target wins
        // ties so committed fire does not churn.
        AUnit inRange = focusInRangeTarget(unit);
        if (inRange != null) return inRange;
        // Sticky first: a committed unit finishes its own target (never
        // re-pick every frame - the restart costs the shot). Uncommitted
        // units join the shared focus target (weakest in reach of the
        // most guns); the squad target and the nearest enemy stay as
        // fallbacks, in that order.
        AUnit target = StickyTarget.currentTarget(unit);
        if (target == null) {
            target = FocusFire.sharedTarget(unit, 9);
        }
        if (target == null) {
            CoverWithdraw.Cover cover = CoverWithdraw.coverTarget(unit);
            if (cover != null) {
                target = cover.pursuer;
                FormationTwin.covering(unit, cover.pursuer, cover.escaper);
            }
        }
        if (target == null) {
            target = ASquadTargeting.useSquadTargetIfPossible(unit);
        }
        if (target == null) {
            target = ASquadTargeting.nearestEngageableEnemy(unit, 9);
        }
        if (target == null) {
            target = ClusterBreach.breachedBaseTarget(unit);
        }
        if (target != null && target.isVisibleUnitOnMap() && target.position() != null) {
            // Sight is memory: the ghost march closes on this even when
            // the engagement below declines the chase.
            DuelMemory.snapshotContact(unit, target.position());
        }
        return target;
    }

    /** Weakest alive valid enemy inside our own weapon range, or null. Public as a seam. */
    public static AUnit focusInRangeTarget(AUnit unit) {
        try {
            AUnit current = keepableCurrentTarget(unit);
            if (current != null) return current;

            Selection enemies = unit.enemiesNear();
            if (enemies == null) return null;

            return new WeakestInRange(unit).pickFrom(enemies);
        } catch (Exception ignored) {
            return null;
        }
    }

    /** The committed target holds while alive, valid and inside our weapon range - no churn. */
    private static AUnit keepableCurrentTarget(AUnit unit) {
        AUnit target = unit.target();
        if (target == null || !target.isAlive()) return null;

        if (!ASquadTargeting.isValidTarget(unit, target)) return null;

        if (!unit.hasWeaponRangeToAttack(target, 0)) return null;

        return target;
    }

    /**
     * One pass over nearby enemies remembering the weakest valid one
     * inside weapon range (hp first, distance breaks ties). The mutable
     * best-tracking lives here so the scan above reads as a sentence.
     */
    private static final class WeakestInRange {
        private final AUnit unit;
        private AUnit best;
        private int bestHp = Integer.MAX_VALUE;
        private double bestDist = Double.MAX_VALUE;

        private WeakestInRange(AUnit unit) {
            this.unit = unit;
        }

        private AUnit pickFrom(Selection enemies) {
            for (AUnit enemy : enemies.list()) consider(enemy);
            return best;
        }

        private void consider(AUnit enemy) {
            if (enemy == null || !enemy.isAlive()) return;

            if (!ASquadTargeting.isValidTarget(unit, enemy)) return;

            if (!unit.hasWeaponRangeToAttack(enemy, 0)) return;

            int hp = enemy.hp() + enemy.shields();
            double dist = unit.distTo(enemy);
            if (hp < bestHp || (hp == bestHp && dist < bestDist)) take(enemy, hp, dist);
        }

        private void take(AUnit enemy, int hp, double dist) {
            best = enemy;
            bestHp = hp;
            bestDist = dist;
        }
    }
}
