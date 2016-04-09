package atlantis.combat.group.missions;

import atlantis.Atlantis;
import atlantis.combat.micro.AtlantisRunning;
import atlantis.debug.tooltip.TooltipManager;
import atlantis.enemy.AtlantisEnemyUnits;
import atlantis.information.AtlantisMap;
import atlantis.information.UnitData;
import atlantis.units.AUnit;
import atlantis.units.Select;
import atlantis.wrappers.APosition;
import bwta.BaseLocation;
import bwapi.Color;
import bwapi.Position;

/**
 * This is the mission object that is used by battle groups and it indicates that we should attack 
 * the enemy at the <b>getFocusPoint</b>.
 */
public class MissionAttack extends Mission {

    public MissionAttack(String name) {
        super(name);
    }
    
    // =========================================================
    
    @Override
    public boolean update(AUnit unit) {
        Position focusPoint = getFocusPoint();
        //System.out.println("Focus point: " + focusPoint);	//TODO DEBUG
        // Focus point is well known
        if (focusPoint != null) {
        	//System.out.println("-Dist to focus point: " + PositionUtil.distanceTo(focusPoint, unit.getPosition()));
            if (unit.distanceTo(focusPoint) > 5) {
                unit.attack(focusPoint);
                TooltipManager.setTooltip(unit, "Concentrate!"); //unit.setTooltip("Mission focus");	//TODO: DEBUG
                return true;
            }
        } 

        // =========================================================
        // Invalid focus point, no enemy can be found, scatter
        else {
            Position position = AtlantisMap.getRandomInvisiblePosition(unit.getPosition());
            if (position != null) {
                unit.attack(position);	
                Atlantis.getBwapi().drawLineMap(unit.getPosition(), position, Color.Red); //TODO DEBUG
                TooltipManager.setTooltip(unit, "Attack!"); //TODO: DEBUG
//                unit.setTooltip("Mission spread");
                return true;
            }
        }
        return false;
    }

    // =========================================================
    // =========================================================
    
    /**
     * Do not interrupt unit if it is engaged in combat.
     */
    @Override
    protected boolean canIssueOrderToUnit(AUnit unit) {
        if (unit.isAttacking() || unit.isStartingAttack() || AtlantisRunning.isRunning(unit)) {
            return false;
        }

        return true;
    }

    /**
     * Returns the <b>position</b> (not the unit itself) where we should point our units to in hope 
     * because as far as we know, the enemy is/can be there and it makes sense to attack in this region.
     */
    public static APosition getFocusPoint() {

        // Try going near enemy base
//        Position enemyBase = AtlantisEnemyInformationManager.getEnemyBase();
        APosition enemyBase = AtlantisEnemyUnits.getEnemyBase();
        if (enemyBase != null) {
        	System.out.println("focus on enemy base " + enemyBase);	//TODO debug
            return enemyBase;
        }

        // Try going near any enemy building
        UnitData enemyBuilding = AtlantisEnemyUnits.getNearestEnemyBuilding();
        if (enemyBuilding != null) {
        	System.out.println("focus on enemy bldg " + enemyBuilding.getPosition());	//TODO debug
            return enemyBuilding.getPosition();
        }

        // Try going to any known enemy unit
        AUnit anyEnemyUnit = Select.enemy().first();
        if (anyEnemyUnit != null) {
        	//System.out.println("focus on enemy unit");	//TODO debug
            return anyEnemyUnit.getPosition();
        }
        
        // Try to go to some starting location, hoping to find enemy there.
        BaseLocation startLocation = AtlantisMap.getNearestUnexploredStartingLocation(Select.mainBase().getPosition());
        if (startLocation != null) {
        	//System.out.println("focus on start location");	//TODO debug
            return APosition.createFrom(startLocation.getPosition());
        }

        // Absolutely no enemy unit can be found
        return null;
    }

}