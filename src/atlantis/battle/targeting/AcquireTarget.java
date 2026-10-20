package atlantis.battle.targeting;

import atlantis.battle.cluster.ClusterBreach;
import atlantis.battle.formation.CoverWithdraw;
import atlantis.battle.formation.FocusFire;
import atlantis.battle.formation.FormationTwin;
import atlantis.units.AUnit;

/**
 * The target chain, stickiest first: committed, shared, covering, squad,
 * nearest, breach. Extracted from BattleCommander; the fight-or-hold
 * decision reads it as one call.
 */
public final class AcquireTarget {

    private AcquireTarget() {
    }

    public static AUnit acquire(AUnit unit) {
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
        return target;
    }
}
