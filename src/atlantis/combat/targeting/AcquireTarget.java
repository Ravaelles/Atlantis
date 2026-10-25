package atlantis.combat.targeting;

import atlantis.battle.DuelMemory;
import atlantis.units.AUnit;

/**
 * The fight-or-hold decision reads targeting as one call. Selection
 * itself lives in {@link TargetLadder}: an explicit ordered behavior
 * list (chewer, keep-current, weakest-in-range, sticky, shared, cover,
 * squad, nearest, breach). This class owns only the entry contract
 * and the sight-memory side effect.
 */
public final class AcquireTarget {

    private AcquireTarget() {
    }

    public static AUnit acquire(AUnit unit) {
        if (unit == null || !unit.isAlive()) return null;
        // The ordered behavior list owns target selection now (owner
        // direction): chewer, keep-current, weakest-in-range, sticky,
        // shared, cover, squad, nearest, breach - see TargetLadder for
        // the order and the re-ordering contract.
        AUnit target = TargetLadder.pick(unit);
        if (target != null && target.isVisibleUnitOnMap() && target.position() != null) {
            // Sight is memory: the ghost march closes on this even when
            // the engagement below declines the chase.
            DuelMemory.snapshotContact(unit, target.position());
        }
        return target;
    }
}
