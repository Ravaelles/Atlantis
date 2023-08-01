package atlantis.combat.advance.focus;

import atlantis.combat.missions.MissionManager;
import atlantis.map.position.APosition;
import atlantis.units.AUnit;

public abstract class MoveToFocusPoint extends MissionManager {
    protected static final double MARGIN = 0.15;

    protected double optimalDist;
    protected double unitToFocus;
    protected double unitToFromSide;
    protected double focusToFromSide;
    protected APosition fromSide;

    // =========================================================

    public MoveToFocusPoint(AUnit unit) {
        super(unit);

        if (focusPoint != null) {
            fromSide = focusPoint.fromSide();
            optimalDist = optimalDist();
            unitToFocus = unit.distTo(focusPoint);
            unitToFromSide = focusPoint.fromSide() == null ? -1 : unit.distTo(focusPoint.fromSide());
            focusToFromSide = focusPoint.fromSide() == null ? -1 : focusPoint.distTo(focusPoint.fromSide());
        }
    }

    @Override
    public boolean applies() {
        return focusPoint != null;
    }

    // =========================================================

    /**
     * Optimal distance to focus point or -1 if not defined.
     */
    public abstract double optimalDist();

    // =========================================================

    protected boolean isTooClose() {
        return unitToFocus < (optimalDist - MARGIN);
    }

    protected boolean isAroundChoke() {
        return focusPoint != null && focusPoint.isAroundChoke();
    }

}

