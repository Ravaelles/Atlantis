package atlantis.combat.advance.focus;

import atlantis.combat.missions.MissionManager;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;

public abstract class MoveToFocusPoint extends MissionManager {
    protected static final double MARGIN = 0.4;

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

    protected DistFromFocus evaluateDistFromFocusPoint() {
        if (unitToFocus < (optimalDist) && unitToFocus <= 0.15) return DistFromFocus.TOO_CLOSE;
        if (unitToFocus > (optimalDist + MARGIN)) return DistFromFocus.TOO_FAR;
        return DistFromFocus.OPTIMAL;
    }

    protected boolean isAroundChoke() {
        return focusPoint != null && focusPoint.isAroundChoke();
    }

}

