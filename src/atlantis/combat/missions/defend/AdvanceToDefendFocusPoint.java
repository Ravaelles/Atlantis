package atlantis.combat.missions.defend;

import atlantis.architecture.Manager;
import atlantis.combat.advance.focus.AFocusPoint;
import atlantis.combat.advance.focus.HandleUnitPositioningOnMap;
import atlantis.combat.advance.focus.MoveToFocusPoint;
import atlantis.combat.advance.focus.OptimalDistanceToFocusPoint;
import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.units.select.Select;
import atlantis.util.We;

public class AdvanceToDefendFocusPoint extends MoveToFocusPoint {
    public AdvanceToDefendFocusPoint(AUnit unit) {
        super(unit);
    }

    @Override
    protected Class<? extends Manager>[] managers() {
        return new Class[]{
//            EarlyGameTooClustered.class,
//            TerranTooClustered.class,
//            ProtossTooFarFromLeader.class,
//            ProtossAsLeaderTooFarFromOthers.class,

            HandleUnitPositioningOnMap.class,
        };
    }

    // =========================================================

    public double optimalDist(AFocusPoint focusPoint) {
//        if (unit.isZealot()) {
//            private final double SPARTA_MODE_DIST_FROM_FOCUS = 0.55;
//            return SPARTA_MODE_DIST_FROM_FOCUS + letWorkersComeThroughBonus();
//        }

        double optimalDist = OptimalDistanceToFocusPoint.forUnit(unit, focusPoint);
        if (optimalDist > 0.05) return optimalDist;

        return Math.max(
            baseForUnit(),
            letWorkersComeThroughBonus()
        );
    }

    private double baseForUnit() {
        if (unit.isDragoon()) return 0.3;

        return unit.isRanged() ? 1.7 : 0;
    }

    private double letWorkersComeThroughBonus() {
        if (We.protoss() && A.seconds() >= 150) {
            return 0;
        }

        return unit.enemiesNear().combatUnits().isEmpty()
            && Select.ourWorkers().inRadius(7, unit).atLeast(1)
            ? 3 : 0;
    }

}
