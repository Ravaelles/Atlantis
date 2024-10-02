package atlantis.combat.advance.focus;

import atlantis.combat.advance.leader.AdvanceAsAlphaLeader;
import atlantis.combat.missions.MissionManager;
import atlantis.combat.missions.Missions;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;

public abstract class MoveToFocusPoint extends MissionManager {
    protected static final double MARGIN = 0.4;

    protected DistFromFocus distFromFocus;
    protected double optimalDist;
    protected double unitToFocus;
    protected double unitToFromSide;
    protected double focusToFromSide;
    protected HasPosition fromSide;

    // =========================================================

    public MoveToFocusPoint(AUnit unit) {
        super(unit);

        if (focusPoint != null) {
            fromSide = focusPoint.fromSide();
            optimalDist = optimalDist(focusPoint);
            unitToFocus = unit.distTo(focusPoint);
            unitToFromSide = focusPoint.fromSide() == null ? -1 : unit.distTo(focusPoint.fromSide());
            focusToFromSide = focusPoint.fromSide() == null ? -1 : focusPoint.distTo(focusPoint.fromSide());
        }
    }

    @Override
    public boolean applies() {
        return focusPoint != null && focusPoint.isValid();
    }

    // =========================================================

    /**
     * Optimal distance to focus point or -1 if not defined.
     */
    public abstract double optimalDist(AFocusPoint focusPoint);

    // =========================================================

    public DistFromFocus evaluateDistToFocusPointComparingToLeader() {
        unitToFocus = unit.distTo(focusPoint);
//        distFromFocus =

        int threshold = Missions.isGlobalMissionAttack() ? 9 : 6;

        if (unitToFocus <= threshold) return DistFromFocus.TOO_CLOSE;

        double ALLOWED_MARGIN = 0.7;
        double diff = unitToFocus - AdvanceAsAlphaLeader.lastLeaderDistToTargetChoke();

        if (diff > ALLOWED_MARGIN) {
            distFromFocus = DistFromFocus.TOO_FAR;
        }
        else if (diff < -ALLOWED_MARGIN) {
            distFromFocus = DistFromFocus.TOO_CLOSE;
        }
        else {
            distFromFocus = DistFromFocus.OPTIMAL;
        }

        return distFromFocus;
    }

//    protected DistFromFocus evaluateDistFromFocusPoint() {
//        if (!focusPoint.isUnit()) {
//            if (unitToFocus < optimalDist || unitToFocus <= 0.15) return DistFromFocus.TOO_CLOSE;
//        }
//
//        if (unitToFocus > (optimalDist + MARGIN)) return DistFromFocus.TOO_FAR;
//
//        return DistFromFocus.OPTIMAL;
//    }

    protected boolean isAroundChoke() {
        return focusPoint != null && focusPoint.isAroundChoke();
    }

}

