package atlantis.combat.focus;

import atlantis.map.position.HasPosition;

/**
 * Where the army goes and why: the whole attack decision in one value.
 * Immutable; the executor (cluster anchor, formation) reads the target,
 * the twin and the tests read the reason.
 */
public final class AttackDecision {

    private final HasPosition target;
    private final AttackReason reason;

    public AttackDecision(HasPosition target, AttackReason reason) {
        this.target = target;
        this.reason = reason;
    }

    /**
     * The attack ground, or null on a baseless map with nothing known -
     * the same null path the old chain had.
     */
    public HasPosition target() {
        return target;
    }

    public AttackReason reason() {
        return reason;
    }

    @Override
    public String toString() {
        return "AttackDecision{" + reason + " -> " + target + "}";
    }
}
