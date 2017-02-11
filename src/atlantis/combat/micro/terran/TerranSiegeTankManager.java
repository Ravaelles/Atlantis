package atlantis.combat.micro.terran;

import atlantis.debug.AtlantisPainter;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.Select;
import atlantis.util.AtlantisUtilities;
import bwapi.Color;

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
        
        AUnit enemy = Select.enemy().combatUnits().canBeAttackedBy(tank).nearestTo(tank);
        double distanceToEnemy = enemy != null ? tank.distanceTo(enemy) : -1;
        
//        String string = (enemy != null ? enemy.getShortName() : "NULL");
//        if (enemy != null) {
//             string += " (" + enemy.distanceTo(tank) + ")";
//        }
//        AtlantisPainter.paintTextCentered(tank.getPosition().translateByPixels(0, 16), 
//                string, 
//                Color.Red);
        
        if (enemy != null) {
            if (!tank.isSieged()) {
                return updateWhenUnsieged(tank, enemy, distanceToEnemy);
            }
        }
        
        // === Siege on hold =======================================
        
        // If tank is holding position, siege
        if (tank.isHoldingPosition()) {
            tank.siege();
            tank.setTooltip("Hold & siege");
            return true;
        }
        
        // === Act when sieged =====================================
        
        if (updateWhenSieged(tank, enemy, distanceToEnemy)) {
            return true;
        }
        
        // =========================================================
        
        tank.setTooltip("Ta-ta-ta!");
        return false;
    }

    // =========================================================
    
    /**
     * Sieged
     */
    private static boolean updateWhenSieged(AUnit tank, AUnit enemy, double distanceToEnemy) {
        if (enemy == null || distanceToEnemy < 0 || distanceToEnemy >= 14) {
            tank.setTooltip("Considers unsiege");
            
            if (AtlantisUtilities.rand(1, 100) <= 10) {
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
    private static boolean updateWhenUnsieged(AUnit tank, AUnit enemy, double distanceToEnemy) {
        
        // === Enemy is BUILDING ========================================
        if (enemy.isBuilding()) {
            return nearestEnemyIsBuilding(tank, enemy, distanceToEnemy);
        } // === Enemy is UNIT ========================================
        else {
            return nearestEnemyIsUnit(tank, enemy, distanceToEnemy);
        }
    }
    
    // =========================================================
    
    private static boolean nearestEnemyIsBuilding(AUnit tank, AUnit enemy, double distanceToEnemy) {
        if (distanceToEnemy <= 10.3) {
            tank.siege();
            tank.setTooltip("Siege - building");
            return true;
        }

        return false;
    }

    private static boolean nearestEnemyIsUnit(AUnit tank, AUnit enemy, double distanceToEnemy) {
        if (distanceToEnemy < 14) {
            if (AtlantisUtilities.rand(1, 100) < 8 || enemy.getType().isDangerousGroundUnit()) {
                tank.siege();
                tank.setTooltip("Better siege");
                return true;
            }
        }

        if (distanceToEnemy <= 10.8) {
            tank.siege();
            tank.setTooltip("Siege!");
            return true;
        }

        return false;
    }

}
