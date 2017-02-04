package atlantis.combat.squad.missions;

import atlantis.Atlantis;
import atlantis.AtlantisGame;
import atlantis.enemy.AtlantisEnemyUnits;
import atlantis.information.AtlantisMap;
import atlantis.information.UnitData;
import atlantis.scout.AtlantisScoutManager;
import static atlantis.scout.AtlantisScoutManager.getUmtFocusPoint;
import atlantis.units.AUnit;
import atlantis.units.Select;
import atlantis.units.missions.UnitMissions;
import atlantis.wrappers.APosition;
import bwapi.Color;
import bwta.BaseLocation;

/**
 * This is the mission object that is used by battle squads and it indicates that we should attack 
 * the enemy at the <b>getFocusPoint</b>.
 */
public class MissionAttack extends Mission {

    public MissionAttack(String name) {
        super(name);
    }
    
    // =========================================================
    
    @Override
    public boolean update(AUnit unit) {
        APosition focusPoint = getFocusPoint();
        unit.setTooltip("#MA");
        
        // Focus point is well known
        if (focusPoint != null) {
            if (unit.distanceTo(focusPoint) > 10 && !unit.isAttacking() && !unit.isMoving()) {
                unit.attack(focusPoint, UnitMissions.ATTACK_POSITION);
                unit.setTooltip("#MA:Concentrate!"); //unit.setTooltip("Mission focus");	//TODO: DEBUG
                return true;
            }
        } 

        // =========================================================
        // Invalid focus point, no enemy can be found, scatter
        else {
            APosition position = AtlantisMap.getRandomInvisiblePosition(unit.getPosition());
            if (position != null) {
                unit.attack(position, UnitMissions.ATTACK_POSITION);	
                Atlantis.getBwapi().drawLineMap(unit.getPosition(), position, Color.Red); //TODO DEBUG
                unit.setTooltip("#MA:Forward!");
                return true;
            }
        }
        
        unit.setTooltip("#MA:Nothing");
        
        return false;
    }

    // =========================================================

    /**
     * Returns the <b>position</b> (not the unit itself) where we should point our units to in hope 
     * because as far as we know, the enemy is/can be there and it makes sense to attack in this region.
     */
    public static APosition getFocusPoint() {

        // === Handle UMT ==========================================
        
        if (AtlantisGame.isUmtMode()) {
            AUnit firstUnit = Select.ourRealUnits().first();
            if (firstUnit != null) {
                return getUmtFocusPoint(firstUnit.getPosition());
            }
            else {
                return null;
            }
        }
        
        // =========================================================

        // Try going near enemy base
//        Position enemyBase = AtlantisEnemyInformationManager.getEnemyBase();
        APosition enemyBase = AtlantisEnemyUnits.getEnemyBase();
        if (enemyBase != null) {
//        	System.out.println("focus on enemy base " + enemyBase);	//TODO debug
            return enemyBase;
        }

        // Try going near any enemy building
        UnitData enemyBuilding = AtlantisEnemyUnits.getNearestEnemyBuilding();
        if (enemyBuilding != null) {
//        	System.out.println("focus on enemy bldg " + enemyBuilding.getPosition());	//TODO debug
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