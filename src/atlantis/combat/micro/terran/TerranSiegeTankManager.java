package atlantis.combat.micro.terran;

import atlantis.map.AChokepoint;
import atlantis.map.AMap;
import atlantis.units.AUnit;
import atlantis.units.Select;
import atlantis.util.AtlantisUtilities;
import bwta.Chokepoint;


public class TerranSiegeTankManager {
    private static AUnit nearestEnemyUnit;
    private static double nearestEnemyUnitDist;
    private static AUnit nearestEnemyBuilding;
    private static double nearestEnemyBuildingDist;

    public static boolean update(AUnit tank) {
        if (!tank.isInterruptible()) {
            tank.setTooltip("Can't interrupt");
            return true;
        }
        
        // =========================================================
        
        nearestEnemyUnit = Select.enemyRealUnits().combatUnits().groundUnits().nearestTo(tank);
        nearestEnemyUnitDist = nearestEnemyUnit != null ? tank.distanceTo(nearestEnemyUnit) : 999;

        nearestEnemyBuilding = Select.enemy().buildings().nearestTo(tank);
        nearestEnemyBuildingDist = nearestEnemyBuilding != null ? tank.distanceTo(nearestEnemyBuilding) : 999;

//        String string = (enemy != null ? enemy.getShortName() : "NULL");
//        if (enemy != null) {
//             string += " (" + enemy.distanceTo(tank) + ")";
//        }
//        AtlantisPainter.paintTextCentered(tank.getPosition().translateByPixels(0, 16), 
//                string, 
//                Color.Red);

        // =========================================================
        
        if (tank.isSieged()) {
            return updateWhenSieged(tank);
        }
        else {
            return updateWhenUnsieged(tank);
        }
        
//        // =========================================================
//        
//        tank.setTooltip("Ta-ta-ta!");
//        return false;
    }

    // =========================================================
    
    /**
     * Sieged
     */
    private static boolean updateWhenSieged(AUnit tank) {
        if ((nearestEnemyUnit == null && nearestEnemyBuilding == null)
                || (nearestEnemyUnitDist >= 16 && nearestEnemyBuildingDist > 11)) {
            tank.setTooltip("Considers unsiege");
            
            if (!tank.getSquad().isMissionDefend() && AtlantisUtilities.rand(1, 100) <= 2) {
                tank.unsiege();
                tank.setTooltip("Unsiege");
                return true;
            }
        }
        
        return false;
    }

    /**
     * Not sieged
     */
    private static boolean updateWhenUnsieged(AUnit tank) {
        
        // If tank is holding position, siege
//        if (Missions.getGlobalMission().isMissionDefend() && canSiegeHere(tank)) {
//            tank.siege();
//            tank.setTooltip("Hold & siege");
//            return true;
//        }

        AUnit nearEnemyCombatBuilding = Select.enemy().combatBuildings().inRadius(10.9, tank).first();
        if (nearEnemyCombatBuilding != null) {
            return handleNearEnemyCombatBuilding(tank, nearEnemyCombatBuilding);
        }
        
        if (nearestEnemyUnit != null) {
            return nearestEnemyIsUnit(tank, nearestEnemyUnit, nearestEnemyUnitDist);
        }
        
        // =========================================================
        
        return false;
    }
    
    // =========================================================
    
    private static boolean handleNearEnemyCombatBuilding(AUnit tank, AUnit combatBuilding) {
        double distanceToEnemy = tank.distanceTo(combatBuilding);
        
        if (distanceToEnemy <= 10.1) {
            tank.siege();
            tank.setTooltip("Siege - building");
            return true;
        }

        return false;
    }

    private static boolean nearestEnemyIsUnit(AUnit tank, AUnit enemy, double distanceToEnemy) {
        int supportUnitsNearby = Select.ourCombatUnits().inRadius(10, tank).count();

        if (supportUnitsNearby <= 5) {
            return false;
        }
        
        // Don't siege when enemy is too close
        if (distanceToEnemy < 10 && !enemy.isRangedUnit()) {
            tank.setTooltip("Dont siege");
            return false;
        }
        
        if (distanceToEnemy < 12 && enemy.getType().isDangerousGroundUnit() && canSiegeHere(tank)) {
            tank.siege();
            tank.setTooltip("Better siege");
            return true;
        }

        if (distanceToEnemy <= 11 && canSiegeHere(tank)) {
            tank.siege();
            tank.setTooltip("Siege!");
            return true;
        }

        return false;
    }
    
    // =========================================================
    
    private static boolean canSiegeHere(AUnit tank) {
        AChokepoint choke = AMap.getNearestChokepoint(tank.getPosition());
        if (choke == null) {
            return true;
        }
        else {
            return tank.distanceTo(choke.getCenter()) > 4 || (choke.getWidth() / 32 > 3);
        }
    }

}
