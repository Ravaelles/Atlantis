package atlantis.battle.targeting;

import atlantis.units.AUnit;

/**
 * A committed unit finishes its own target instead of re-picking every
 * frame (the restart costs the shot). Holds while the target is alive,
 * valid and inside our own weapon range - no churn. Split out of
 * {@code AcquireTarget} as ladder rule (2): one behavior, one gate,
 * ordered visibly in {@link TargetLadder}.
 */
public final class KeepCurrentTarget {

    private KeepCurrentTarget() {
    }

    public static AUnit pick(AUnit unit) {
        if (unit == null) return null;

        try {
            AUnit target = unit.target();
            if (target == null || !target.isAlive()) return null;
            if (!ASquadTargeting.isValidTarget(unit, target)) return null;
            if (!unit.hasWeaponRangeToAttack(target, 0)) return null;

            return target;
        } catch (Exception | Error ignored) {
            return null;
        }
    }
}
