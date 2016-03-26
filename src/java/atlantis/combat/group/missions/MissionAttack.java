package atlantis.combat.group.missions;

import atlantis.Atlantis;
import atlantis.combat.micro.AtlantisRunManager;
import atlantis.combat.micro.AtlantisRunning;
import atlantis.debug.tooltip.TooltipManager;
import atlantis.information.AtlantisEnemyInformationManager;
import atlantis.enemy.AtlantisMap;
import atlantis.information.UnitData;
import atlantis.util.PositionUtil;
import atlantis.wrappers.Select;
import bwta.BaseLocation;
import bwapi.Color;
import bwapi.Position;
import bwapi.Unit;

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
    public boolean update(Unit unit) {
        Position focusPoint = getFocusPoint();
        //System.out.println("Focus point: " + focusPoint);	//TODO DEBUG
        // Focus point is well known
        if (focusPoint != null) {
        	//System.out.println("-Dist to focus point: " + PositionUtil.distanceTo(focusPoint, unit.getPosition()));
            if (PositionUtil.distanceTo(focusPoint, unit.getPosition()) > 5) {
                unit.attack(focusPoint, false);
                TooltipManager.setTooltip(unit, "Concentrate!"); //unit.setTooltip("Mission focus");	//TODO: DEBUG
                System.out.println("--Concentrate");	//TODO DEBUG
                return true;
            }
        } 

        // =========================================================
        // Invalid focus point, no enemy can be found, scatter
        else {
            Position position = AtlantisMap.getRandomInvisiblePosition(unit.getPosition());
            if (position != null) {
                unit.attack(position, false);	
                Atlantis.getBwapi().drawLineMap(unit.getPosition(), position, Color.Red); //TODO DEBUG
                TooltipManager.setTooltip(unit, "Spread!"); //TODO: DEBUG
//                unit.setTooltip("Mission spread");
                System.out.println("--Spread");	//TODO DEBUG
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
    protected boolean canIssueOrderToUnit(Unit unit) {
        if (unit.isAttacking() || unit.isStartingAttack() || AtlantisRunning.isRunning(unit)) {
            return false;
        }

        return true;
    }

    /**
     * Returns the <b>position</b> (not the unit itself) where we should point our units to in hope 
     * because as far as we know, the enemy is/can be there and it makes sense to attack in this region.
     */
    public static Position getFocusPoint() {

        // Try going near enemy base
        Position enemyBase = AtlantisEnemyInformationManager.getEnemyBase();
        if (enemyBase != null) {
        	//System.out.println("focus on enemy base");	//TODO debug
            return enemyBase;
        }

        // Try going near any enemy building
        UnitData enemyBuilding = AtlantisEnemyInformationManager.getNearestEnemyBuilding();
        if (enemyBuilding != null) {
        	//System.out.println("focus on enemy bldg");	//TODO debug
            return enemyBuilding.getPosition();
        }

        // Try going to any known enemy unit
        Unit anyEnemyUnit = Select.enemy().first();
        if (anyEnemyUnit != null) {
        	//System.out.println("focus on enemy unit");	//TODO debug
            return anyEnemyUnit.getPosition();
        }
        
        // Try to go to some starting location, hoping to find enemy there.
        BaseLocation startLocation = AtlantisMap.getNearestUnexploredStartingLocation(Select.mainBase().getPosition());
        if (startLocation != null) {
        	//System.out.println("focus on start location");	//TODO debug
            return startLocation.getPosition();
        }

        // Absolutely no enemy unit can be found
        return null;
    }

}