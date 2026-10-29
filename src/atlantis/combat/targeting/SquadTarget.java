package atlantis.combat.targeting;

import atlantis.units.AUnit;

/**
 * Squad-shared target (ladder rule (7)): the squad's last living target
 * while it stays valid and in reach, unless the neighbourhood is
 * crowded (6+ friendly combat units pick their own focus). Split out
 * of {@code AcquireTarget} as a named rule, ordered visibly in
 * {@link TargetLadder}: one behavior, one gate.
 *
 * <p>Note (NEXT #184 coordination review): as ordered this rule is
 * nearly shadowed by rule 3 - any valid in-range squad target is also
 * a weakest-in-range candidate, so rule 3 usually owns the pick. Kept
 * byte-identical on extraction; removal waits on its own sim
 * no-regression proof, never on this move.</p>
 */
public final class SquadTarget {

    private SquadTarget() {
    }

    public static AUnit pick(AUnit unit) {
        if (unit == null || unit.squad() == null) return null;

        try {
            // Crowded neighbourhood picks its own focus (moved verbatim
            // from the legacy squad-target helper, NEXT #184): the shared
            // validity predicate below stays shared with rules 1-3.
            if (unit.friendsNear().combatUnits().inRadius(6, unit).atLeast(6)) return null;

            AUnit enemy = unit.squad().targeting().lastTargetIfAlive();

            if (
                ASquadTargeting.isValidTarget(unit, enemy)
                    && unit.distTo(enemy) <= 9
                    && unit.hasWeaponRangeToAttack(enemy, 0)
            ) {
                return enemy;
            }

            return null;
        } catch (Exception | Error ignored) {
            return null;
        }
    }
}
