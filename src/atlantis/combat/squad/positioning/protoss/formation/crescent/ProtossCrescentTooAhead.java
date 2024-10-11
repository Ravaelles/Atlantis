package atlantis.combat.squad.positioning.protoss.formation.crescent;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;

public class ProtossCrescentTooAhead extends Manager {
    private double deltaDist;

    public ProtossCrescentTooAhead(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (DragoonCrescent.dontApply(unit)) return false;

        deltaDist = ProtossCrescent.preferredDistToConventionalPoint - ProtossCrescent.distToConventionalPoint;

        return deltaDist > 0.2;
    }

    @Override
    protected Manager handle() {
//        if (takeSmallStepBack() && smallStepBack()) return usedManager(this, "SmallStepBack");

        if (unit.moveToSafety(Actions.MOVE_FORMATION, "CrescentTooAhead")) {
            return usedManager(this, "CrescentTooAhead");
        }

        return null;
    }

//    // === Small step ===========================================
//
//    private boolean smallStepBack() {
//        return unit.moveAwayFrom(ProtossCrescent.conventionalPoint, 1.5);
//    }
//
//    private boolean takeSmallStepBack() {
//        return deltaDist <= 0.5;
//    }
//
//    // === Big step ===========================================
}
