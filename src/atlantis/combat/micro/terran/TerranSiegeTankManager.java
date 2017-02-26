package atlantis.combat.micro.terran;

import atlantis.information.AMap;
import atlantis.units.AUnit;
import atlantis.units.Select;
import atlantis.util.AtlantisUtilities;
import bwta.Chokepoint;

/**
 *
 * @author Rafal Poniatowski <ravaelles@gmail.com>
 */
public class TerranSiegeTankManager {

    public static boolean update(AUnit tank) {
        if (!tank.isInterruptible()) {
            tank.setTooltip("Can't interrupt");
            return true;
        }
        
        // =========================================================
        
        AUnit nearestAttackableEnemy = Select.enemy().combatUnits().canBeAttackedBy(tank).nearestTo(tank);
        double distanceToEnemy = nearestAttackableEnemy != null ? tank.distanceTo(nearestAttackableEnemy) : -1;
        
//        String string = (enemy != null ? enemy.getShortName() : "NULL");
//        if (enemy != null) {
//             string += " (" + enemy.distanceTo(tank) + ")";
//        }
//        AtlantisPainter.paintTextCentered(tank.getPosition().translateByPixels(0, 16), 
//                string, 
//                Color.Red);

        // =========================================================
        
        if (tank.isSieged()) {
            return updateWhenSieged(tank, nearestAttackableEnemy, distanceToEnemy);
        }
        else {
            return updateWhenUnsieged(tank, nearestAttackableEnemy, distanceToEnemy);
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
    private static boolean updateWhenSieged(AUnit tank, AUnit enemy, double distanceToEnemy) {
        if (enemy == null || distanceToEnemy < 0 || distanceToEnemy >= 14) {
            tank.setTooltip("Considers unsiege");
            
            if (!tank.getSquad().isMissionDefend() && AtlantisUtilities.rand(1, 100) <= 10) {
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
    private static boolean updateWhenUnsieged(AUnit tank, AUnit nearestAttackableEnemy, double distanceToEnemy) {
        
        // === Siege on hold =======================================
        
        // If tank is holding position, siege
        if (tank.isHoldingPosition() && canSiegeHere(tank)) {
            tank.siege();
            tank.setTooltip("Hold & siege");
            return true;
        }
        
        // === Enemy is BUILDING ========================================
        
        if (nearestAttackableEnemy != null) {

            // === Enemy is COMBAT UNIT ========================================
            
            if (Select.ourCombatUnits().inRadius(10, tank).count() >= 4) {
                return nearestEnemyIsUnit(tank, nearestAttackableEnemy, distanceToEnemy);
            }
            
            // Enemy is BUILDING
            else if (nearestAttackableEnemy.isBuilding()) {
                return nearestEnemyIsBuilding(tank, nearestAttackableEnemy, distanceToEnemy);
            } 
        }
        
        return false;
    }
    
    // =========================================================
    
    private static boolean nearestEnemyIsBuilding(AUnit tank, AUnit nearestAttackableEnemy, double distanceToEnemy) {
        if (distanceToEnemy <= 10.3) {
            tank.siege();
            tank.setTooltip("Siege - building");
            return true;
        }

        return false;
    }

    private static boolean nearestEnemyIsUnit(AUnit tank, AUnit enemy, double distanceToEnemy) {
        
        // Don't siege when enemy is too close
        if (distanceToEnemy < 5) {
            tank.setTooltip("Dont siege");
            return false;
        }
        
        // =========================================================
        
        if (distanceToEnemy < 14) {
            if ((AtlantisUtilities.rand(1, 100) < 8 || enemy.getType().isDangerousGroundUnit()) && canSiegeHere(tank)) {
                tank.siege();
                tank.setTooltip("Better siege");
                return true;
            }
        }

        if (distanceToEnemy <= 10.8 && canSiegeHere(tank)) {
            tank.siege();
            tank.setTooltip("Siege!");
            return true;
        }

        return false;
    }
    
    // =========================================================
    
    private static boolean canSiegeHere(AUnit tank) {
        Chokepoint choke = AMap.getNearestChokepoint(tank.getPosition());
        if (choke == null) {
            return true;
        }
        else {
            return tank.distanceTo(choke.getCenter()) > 1 || choke.getWidth() / 32 > 3.5;
        }
    }

}
