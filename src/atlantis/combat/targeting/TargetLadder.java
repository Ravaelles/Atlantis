package atlantis.combat.targeting;

import java.util.Arrays;
import java.util.List;

import atlantis.combat.cluster.ClusterBreach;
import atlantis.combat.formation.CoverWithdraw;
import atlantis.combat.formation.FocusFire;
import atlantis.combat.formation.FormationTwin;
import atlantis.units.AUnit;

/**
 * Targeting as an explicit ordered behavior list (owner direction):
 * each rule is a small class with one gate ({@code pick} returns a
 * target or declines with null), and the ORDER lives here, once -
 * visible, re-orderable, testable. No nested ifs hiding the sequence,
 * no machinery besides the order itself.
 *
 * <p>Order (chewer first, breach last): a melee unit with its shot
 * ready hits the chewer instead of walking past it; a committed unit
 * finishes its target (no restart churn); the weakest valid enemy in
 * range draws focus fire; then squad sharing, covering, squad-last
 * and nearest-engageable fallbacks; a breached base answers last.
 * Reordering any two adjacent rules is a behavior change: the
 * order-sensitivity pins fail until the test names the intended
 * change.</p>
 *
 * <p>This ladder is the learner's future action space (NEXT #185):
 * hand-written gates are what the gating network will replace, one
 * rule at a time, behind the same seam.</p>
 */
public final class TargetLadder {

    private TargetLadder() {
    }

    /** The rules in execution order. Read it as the behavior. */
    public static List<String> order() {
        return Arrays.asList(
            "chewer",
            "keepCurrent",
            "weakestInRange",
            "sticky",
            "shared",
            "cover",
            "squad",
            "nearest",
            "breach"
        );
    }

    /**
     * First rule that names a target wins. Pure selection - the twin
     * row for the pick lives with the caller ({@code AcquireTarget}).
     */
    public static AUnit pick(AUnit unit) {
        if (unit == null || !unit.isAlive()) return null;

        AUnit target = MeleeChewerTarget.pick(unit);
        if (target != null) return target;

        target = KeepCurrentTarget.pick(unit);
        if (target != null) return target;

        target = WeakestInRangeTarget.pick(unit);
        if (target != null) return target;

        target = StickyTarget.currentTarget(unit);
        if (target != null) return target;

        target = FocusFire.sharedTarget(unit, 9);
        if (target != null) return target;

        CoverWithdraw.Cover cover = CoverWithdraw.coverTarget(unit);
        if (cover != null) {
            FormationTwin.covering(unit, cover.pursuer, cover.escaper);

            return cover.pursuer;
        }

        target = ASquadTargeting.useSquadTargetIfPossible(unit);
        if (target != null) return target;

        target = ASquadTargeting.nearestEngageableEnemy(unit, 9);
        if (target != null) return target;

        return ClusterBreach.breachedBaseTarget(unit);
    }
}
