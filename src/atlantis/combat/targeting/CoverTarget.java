package atlantis.combat.targeting;

import atlantis.combat.formation.CoverWithdraw;
import atlantis.combat.formation.FormationTwin;
import atlantis.units.AUnit;

/**
 * Covering fire (ladder rule (6)): a healthy unit with nothing urgent of
 * its own covers the pursuer chewing a withdrawing ally instead of its
 * nearest. Split out of the ladder body as a named rule, ordered
 * visibly in {@link TargetLadder}: one behavior, one gate. The usual
 * answer is a ling, which focus fire never picks - that is why this
 * rule exists below it instead of inside it.
 */
public final class CoverTarget {

    private CoverTarget() {
    }

    public static AUnit pick(AUnit unit) {
        if (unit == null) return null;

        try {
            CoverWithdraw.Cover cover = CoverWithdraw.coverTarget(unit);
            if (cover == null) return null;

            FormationTwin.covering(unit, cover.pursuer, cover.escaper);

            return cover.pursuer;
        } catch (Exception | Error ignored) {
            return null;
        }
    }
}
