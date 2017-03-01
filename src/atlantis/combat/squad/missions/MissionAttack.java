package atlantis.combat.squad.missions;

import atlantis.AGame;
import atlantis.Atlantis;
import atlantis.enemy.AEnemyUnits;
import atlantis.information.AFoggedUnit;
import atlantis.information.AMap;
import atlantis.position.APosition;
import static atlantis.scout.AScoutManager.getUmtFocusPoint;
import atlantis.units.AUnit;
import atlantis.units.Select;
import bwapi.Color;
import bwta.BaseLocation;

/**
 * This is the mission object that is used by battle squads and it indicates that we should attack 
 * the enemy at the <b>getFocusPoint</b>.
 */
public class MissionAttack extends Mission {
    
    private static MissionAttack instance;
    
    // =========================================================

    protected MissionAttack(String name) {
        super(name);
        instance = this;
    }
    
    // =========================================================
    
    @Override
    public boolean update(AUnit unit) {
        APosition focusPoint = getFocusPoint();
        unit.setTooltip("#MA");
        
        // === Attack units nears main =============================
        
        AUnit mainBase = Select.mainBase();
        if (mainBase != null) {
            AUnit nearestEnemy = Select.enemy().visible()
//                    .canBeAttackedBy(unit)
//                    .inRadius(50, mainBase)
                    .nearestTo(mainBase);
//            System.out.println(nearestEnemy);
            if (nearestEnemy != null) {
                focusPoint = nearestEnemy.getPosition();
            }
        }
        
        // =========================================================
        
        // Focus point is well known
        if (focusPoint != null) {
            if (unit.distanceTo(focusPoint) > 10 && !unit.isAttacking() && !unit.isMoving()) {
                unit.attackPosition(focusPoint);
                unit.setTooltip("#MA:Concentrate!"); //unit.setTooltip("Mission focus");	//TODO: DEBUG
                return true;
            }
        } 

        // =========================================================
        // Invalid focus point, no enemy can be found, scatter
        else {
            APosition position = AMap.getRandomInvisiblePosition(unit.getPosition());
            if (position != null) {
                unit.attackPosition(position);	
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
    @Override
    public APosition getFocusPoint() {

        // === Handle UMT ==========================================
        
        if (AGame.isUmtMode()) {
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
        APosition enemyBase = AEnemyUnits.getEnemyBase();
        if (enemyBase != null) {
//        	System.out.println("focus on enemy base " + enemyBase);	//TODO debug
            return enemyBase;
        }

        // Try going near any enemy building
        AFoggedUnit enemyBuilding = AEnemyUnits.getNearestEnemyBuilding();
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
        BaseLocation startLocation = AMap.getNearestUnexploredStartingLocation(Select.mainBase().getPosition());
        if (startLocation != null) {
        	//System.out.println("focus on start location");	//TODO debug
            return APosition.create(startLocation.getPosition());
        }

        // Absolutely no enemy unit can be found
        return null;
    }
    
    // =========================================================
    
    public static MissionAttack getInstance() {
        return instance;
    }

}