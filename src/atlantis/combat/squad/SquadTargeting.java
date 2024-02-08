package atlantis.combat.squad;

import atlantis.units.AUnit;

public class SquadTargeting {
    /**
     * Last target attacked by any member of this squad.
     */
    private AUnit lastTarget = null;

    public void justAttacked(AUnit target) {
        lastTarget = target;
    }

    public AUnit lastTarget() {
        return lastTarget;
    }

    public AUnit lastTargetIfAlive() {
        if (lastTarget == null || !lastTarget.isAlive()) return null;

        return lastTarget;
    }

    public void forceTarget(AUnit target) {
        lastTarget = target;
    }
}
