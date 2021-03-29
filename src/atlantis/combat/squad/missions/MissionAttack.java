package atlantis.combat.squad.missions;

import atlantis.AGame;
import atlantis.Atlantis;
import atlantis.enemy.AEnemyUnits;
import atlantis.information.AFoggedUnit;
import atlantis.map.AMap;
import atlantis.position.APosition;
import static atlantis.scout.AScoutManager.getUmtFocusPoint;
import atlantis.units.AUnit;
import atlantis.units.Select;
import atlantis.units.actions.UnitActions;
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
        
        AUnit ourCenterUnit = Select.mainBase();
        if (ourCenterUnit == null) {
            ourCenterUnit = unit;
        }
        
        if (ourCenterUnit != null) {
            AUnit nearestEnemy = Select.enemy().visible().nearestTo(ourCenterUnit);
            if (nearestEnemy != null) {
                focusPoint = nearestEnemy.getPosition();
            }
        }
        
        // =========================================================
        
        // Focus point is well known
        if (focusPoint != null) {
            return attackFocusPoint(unit, focusPoint);
        } 

        // =========================================================
        // Invalid focus point, no enemy can be found, scatter
        else {
            APosition position = AMap.getRandomInvisiblePosition(unit.getPosition());
            if (position != null) {
                unit.attackPosition(position);	
                Atlantis.game().drawLineMap(unit.getPosition(), position, Color.Red); //TODO DEBUG
                unit.setTooltip("#MA:Forward!");
                return true;
            }
            else {
                System.err.println("No invisible position found");
            }
        }
        
        unit.setTooltip("#MA:Nothing");
        return false;
    }

    private boolean attackFocusPoint(AUnit unit, APosition focusPoint) {
        Select<AUnit> nearbyAllies = Select.ourCombatUnits().inRadius(10, unit);
        if (nearbyAllies.count() <= 4) {
            unit.move(nearbyAllies.first().getPosition(), UnitActions.TOGETHER);
            unit.setTooltip("#MA:Concentrate!"); //unit.setTooltip("Mission focus");	//TODO: DEBUG
            return true;
        }

        if (unit.distanceTo(focusPoint) > 6) {
//                unit.attackPosition(focusPoint);
            unit.move(focusPoint, UnitActions.MOVE);
            unit.setTooltip("#MA:Forward!"); //unit.setTooltip("Mission focus");	//TODO: DEBUG
            return true;
        }

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
            return enemyBase;
        }

        // Try going near any enemy building
        AFoggedUnit enemyBuilding = AEnemyUnits.getNearestEnemyBuilding();
        if (enemyBuilding != null) {
            return enemyBuilding.getPosition();
        }

        // Try going to any known enemy unit
        AUnit anyEnemyUnit = Select.enemy().first();
        if (anyEnemyUnit != null) {
            return anyEnemyUnit.getPosition();
        }
        
        // Try to go to some starting location, hoping to find enemy there.
        APosition startLocation = AMap.getNearestUnexploredStartingLocation(Select.mainBase().getPosition());
        if (startLocation != null) {
        	//System.out.println("focus on start location");	//TODO debug
            return startLocation;
        }

        // Absolutely no enemy unit can be found
        return null;
    }
    
    // =========================================================
    
    public static MissionAttack getInstance() {
        return instance;
    }

}