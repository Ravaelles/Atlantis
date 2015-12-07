package atlantis.combat.group.missions;

import atlantis.information.AtlantisMapInformationManager;
import atlantis.wrappers.SelectUnits;
import jnibwapi.ChokePoint;
import jnibwapi.Position;
import jnibwapi.Unit;

public class MissionPrepare extends Mission {

    @Override
    public boolean update(Unit unit) {
        if (moveUnitToDestinationIfNeeded(unit)) {
            return true;
        }

        return false;
    }

    // =========================================================
    /**
     * Unit will go towards important choke point near main base.
     */
    private boolean moveUnitToDestinationIfNeeded(Unit unit) {
        ChokePoint chokepoint = AtlantisMapInformationManager.getMainBaseChokepoint();
        if (chokepoint == null) {
            System.err.println("Couldn't define choke point.");
            return false;
        }

        // =========================================================
        // Normal orders
        // Check if shouldn't disturb unit
        if (canIssueOrderToUnit(unit)) {

            // Too close to
            if (isCriticallyCloseToChokePoint(unit, chokepoint)) {
                unit.moveAwayFrom(chokepoint, 1.0);
                unit.setTooltip("Get back");
                return true;
            }

            // Unit is quite close to the choke point
            if (isCloseEnoughToChokePoint(unit, chokepoint)) {

                // Too many stacked units
                if (isTooManyUnitsAround(unit, chokepoint)) {
                    unit.moveAwayFrom(chokepoint, 1.0);
                    unit.setTooltip("Stacked");
                } // Units aren't stacked too much
                else {
                }
            } // Unit is far from choke point
            else {
                unit.move(chokepoint, false);
            }
        }

        return false;
    }

    private boolean isTooManyUnitsAround(Unit unit, ChokePoint chokepoint) {
        return SelectUnits.ourCombatUnits().inRadius(1.0, unit).count() >= 3;
    }

    private boolean isCloseEnoughToChokePoint(Unit unit, ChokePoint chokepoint) {
        if (unit == null || chokepoint == null) {
            return false;
        }

        // Bigger this value is, further from choke will units stand
        double standFurther = 1.6;

        // Distance to the center of choke point
        double distToChoke = chokepoint.distanceTo(unit) - chokepoint.getRadiusInTiles();

        // How far can the unit shoot
        double unitShootRange = unit.getShootRangeGround();

        // Define max allowed distance from choke point to consider "still close"
        double maxDistanceAllowed = unitShootRange + standFurther;

        return distToChoke <= maxDistanceAllowed;
    }

    private boolean isCriticallyCloseToChokePoint(Unit unit, ChokePoint chokepoint) {
        if (unit == null || chokepoint == null) {
            return false;
        }

        // Distance to the center of choke point
//        double distToChoke = chokepoint.distanceTo(unit) - chokepoint.getRadiusInTiles();
        double distToChoke = chokepoint.distanceTo(unit);

        // Can't be closer than X from choke point
        if (distToChoke <= 2.8) {
            return true;
        }

        // Bigger this value is, further from choke will units stand
        double standFurther = 1;

        // How far can the unit shoot
        double unitShootRange = unit.getShootRangeGround();

        // Define max distance
        double maxDistance = unitShootRange + standFurther;

        return distToChoke <= maxDistance;
    }

    // =========================================================
    /**
     * Do not interrupt unit if it is engaged in combat.
     */
    @Override
    protected boolean canIssueOrderToUnit(Unit unit) {
        if (unit.isAttacking() || unit.isStartingAttack() || unit.isRunning() || unit.isAttackFrame()) {
            return false;
        }

        return true;
    }

    public static Position getFocusPoint() {
        return null;
    }
}
