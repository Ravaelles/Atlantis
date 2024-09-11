package atlantis.combat.squad;

import atlantis.units.AUnit;

public class SquadTargeting {
    /**
     * Last target attacked by any member of this squad.
     */
    private AUnit lastTarget = null;

    public void setLastTarget(AUnit target) {
//        if (!target.equals(lastTarget)) System.err.println("-- lastTarget = " + target.typeWithUnitId());
        lastTarget = target;
    }

    public AUnit lastTarget() {
        return lastTarget;
    }

    public AUnit lastTargetIfAlive() {
        if (lastTarget == null || !lastTarget.isAlive()) return null;

        return lastTarget;
    }

//    public void forceTarget(AUnit target) {
//        lastTarget = target;
//    }
}
