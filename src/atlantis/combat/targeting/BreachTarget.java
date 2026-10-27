package atlantis.combat.targeting;

import atlantis.combat.cluster.ClusterBreach;
import atlantis.units.AUnit;

/**
 * Breached-base answer (ladder rule (9), last): an enemy inside the base
 * radius outranks everything the ladder declined - the line answers the
 * breach instead of bunching on the anchor. Split out as a named rule,
 * ordered visibly in {@link TargetLadder}: one behavior, one gate, the
 * last resort by design.
 */
public final class BreachTarget {

    private BreachTarget() {
    }

    public static AUnit pick(AUnit unit) {
        if (unit == null) return null;

        try {
            return ClusterBreach.breachedBaseTarget(unit);
        } catch (Exception | Error ignored) {
            return null;
        }
    }
}
