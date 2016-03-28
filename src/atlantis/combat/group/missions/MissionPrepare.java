package atlantis.combat.group.missions;

import atlantis.combat.micro.AtlantisRunning;
import atlantis.debug.tooltip.TooltipManager;
import atlantis.information.AtlantisMap;
import atlantis.units.AUnit;
import atlantis.util.PositionUtil;
import atlantis.util.UnitUtil;
import atlantis.units.Select;
import bwta.Chokepoint;
import bwapi.Position;


public class MissionPrepare extends Mission {

    public MissionPrepare(String name) {
        super(name);
    }
    
    // =========================================================
    
    @Override
    public boolean update(AUnit unit) {
        if (moveUnitToDestinationIfNeeded(unit)) {
            return true;
        }

        return false;
    }

    // =========================================================
    /**
     * AUnit will go towards important choke point near main base.
     */
    private boolean moveUnitToDestinationIfNeeded(AUnit unit) {
        Chokepoint chokepoint = AtlantisMap.getMainBaseChokepoint();
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
                unit.moveAwayFrom(chokepoint.getCenter(), 1.5);	//unit.moveAwayFrom(chokepoint, 1.5);
                TooltipManager.setTooltip(unit, "Get back");  //unit.setTooltip("Get back");
                System.out.println("get back -- prepare");	//TODO DEBUG
                return true;
            }

            // AUnit is quite close to the choke point
            if (isCloseEnoughToChokePoint(unit, chokepoint)) {

                // Too many stacked units
                if (isTooManyUnitsAround(unit, chokepoint)) {
                    unit.moveAwayFrom(chokepoint.getCenter(), 0.2);	//unit.moveAwayFrom(chokepoint, 0.2);
                    TooltipManager.setTooltip(unit, "Stacked"); //unit.setTooltip("Stacked");
                } // Units aren't stacked too much
                else {
                }
            } // AUnit is far from choke point
            else {
                unit.move(chokepoint.getCenter());
            }
        }

        return false;
    }

    private boolean isTooManyUnitsAround(AUnit unit, Chokepoint chokepoint) {
        return Select.ourCombatUnits().inRadius(0.8, unit.getPosition()).count() >= 4;
    }

    private boolean isCloseEnoughToChokePoint(AUnit unit, Chokepoint chokepoint) {
        if (unit == null || chokepoint == null) {
            return false;
        }

        // Distance to the center of choke point
        double distToChoke = PositionUtil.distanceTo(chokepoint.getCenter(), unit.getPosition()) - chokepoint.getWidth() / 32; //TODO: check consistency with getRadiusInTiles()

        // =========================================================
        // Close enough ::meme::
        if (distToChoke <= Math.max(2.5, 4.5 - chokepoint.getWidth() / 96)) {	//96 is 32*3, which was the previous denominator
            return true;
        }

        return false;

//        // Bigger this value is, further from choke will units stand
//        double standFurther = 1.6;
//
//        // Distance to the center of choke point
//        double distToChoke = chokepoint.distanceTo(unit) - chokepoint.getRadiusInTiles();
//
//        // How far can the unit shoot
//        double unitShootRange = unit.getShootRangeGround();
//
//        // Define max allowed distance from choke point to consider "still close"
//        double maxDistanceAllowed = unitShootRange + standFurther;
//
//        return distToChoke <= maxDistanceAllowed;
    }

    private boolean isCriticallyCloseToChokePoint(AUnit unit, Chokepoint chokepoint) {
        if (unit == null || chokepoint == null) {
            return false;
        }

        // =========================================================
        // Distance to the center of choke point
//        double distToChoke = chokepoint.distanceTo(unit) - chokepoint.getRadiusInTiles();
        double distanceToTarget = PositionUtil.distanceTo(chokepoint.getCenter(), unit.getPosition());

        // Can't be closer than X from choke point
        if (distanceToTarget <= 2 + 2 / chokepoint.getWidth() / 32) { //TODO: check consistency with getRadiusInTiles()
            return true;
        }

        return false;

        // =========================================================
        // Defiane max distance
//        double maxDistance = unit.getShootRangeGround();
//        return distanceToTarget <= maxDistance;
    }

    // =========================================================
    /**
     * Do not interrupt unit if it is engaged in combat.
     */
    @Override
    protected boolean canIssueOrderToUnit(AUnit unit) {
        if (AtlantisRunning.isRunning(unit) || unit.isStartingAttack() || unit.isAttacking() || unit.isAttackFrame() || unit.isMoving()) {
            return false;
        }

        return true;
    }

    public static Position getFocusPoint() {
        return null;
    }
}
