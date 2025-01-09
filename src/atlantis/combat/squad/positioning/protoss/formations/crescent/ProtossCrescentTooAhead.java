package atlantis.combat.squad.positioning.protoss.formations.crescent;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;

public class ProtossCrescentTooAhead extends Manager {
    public ProtossCrescentTooAhead(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (DragoonCrescent.dontApply(unit)) return false;

//        System.out.println(unit + " delta: " + deltaDist
//            + " / dist:" + ProtossCrescent.distToConventionalPoint + " " +
//            "/ pref:" + ProtossCrescent.preferredDistToConventionalPoint);

        return ProtossCrescent.deltaDist > ProtossCrescent.DELTA_MARGIN;
    }

    @Override
    protected Manager handle() {
//        if (takeSmallStepBack() && smallStepBack()) return usedManager(this, "SmallStepBack");

        double moveDistance = ProtossCrescent.deltaDist >= 2 ? 0.8 : 0.2;

        if (unit.moveAwayFrom(
            ProtossCrescent.conventionalPoint, moveDistance, Actions.MOVE_FORMATION, "CrescentTooAhead"
        )) {
//            System.out.println("Move away");
            return usedManager(this, "CrescentTooAhead");
        }

        if (unit.moveToSafety(Actions.MOVE_FORMATION, "CrescentTooAhead")) {
//            System.out.println("Move to safety");
            return usedManager(this, "CrescentTooAhead");
        }

        System.out.println("---- COULD NOT - TOO AHEAD, move:" + moveDistance + " / delta:" + ProtossCrescent.deltaDist);
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
