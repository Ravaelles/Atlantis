package atlantis.combat.missions.defend;

import atlantis.architecture.Manager;
import atlantis.combat.advance.focus.AFocusPoint;
import atlantis.combat.advance.focus.HandleFocusPointPositioning;
import atlantis.combat.advance.focus.MoveToFocusPoint;
import atlantis.combat.advance.focus.OptimalDistanceToFocusPoint;
import atlantis.combat.squad.positioning.protoss.ProtossTooFarFromLeader;
import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.units.select.Select;
import atlantis.util.Enemy;
import atlantis.util.We;

public class AdvanceToDefendFocusPoint extends MoveToFocusPoint {
    public AdvanceToDefendFocusPoint(AUnit unit) {
        super(unit);
    }

    @Override
    protected Class<? extends Manager>[] managers() {
        return new Class[]{
//            EarlyGameTooClustered.class,
//            TooClustered.class,
            ProtossTooFarFromLeader.class,

            HandleFocusPointPositioning.class,
        };
    }

    // =========================================================

    public double optimalDist(AFocusPoint focusPoint) {
//        if (unit.isZealot()) {
//            private final double SPARTA_MODE_DIST_FROM_FOCUS = 0.55;
//            return SPARTA_MODE_DIST_FROM_FOCUS + letWorkersComeThroughBonus();
//        }

        double focus = OptimalDistanceToFocusPoint.toFocus(unit, focusPoint);
        if (focus >= 0) return focus;

        double base = 0.0;

        if (We.zerg() && Enemy.protoss()) {
            base = 0.6;
        }

        if (unit.isTerran()) {
            base += (unit.isTank() ? 2.5 : 0)
                + (unit.isMedic() ? -2.5 : 0)
                + (unit.isFirebat() ? -1.5 : 0)
                + (unit.isRanged() ? 1 : 0)
                + Math.min(4, (Select.our().combatUnits().inRadius(8, unit).count() / 6));

            if (We.zerg() && this.focusPoint.isAroundChoke()) {
                base += (this.focusPoint.choke().width() <= 3) ? 3.5 : 0;
            }
        }

        return Math.max(
            baseForUnit(),
            base + letWorkersComeThroughBonus()
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
