package atlantis.combat.micro.terran;

import atlantis.units.AUnit;
import atlantis.units.Select;
import atlantis.util.AtlantisUtilities;

/**
 *
 * @author Rafal Poniatowski <ravaelles@gmail.com>
 */
public class TerranSiegeTankManager {

    public static boolean update(AUnit tank) {
        AUnit enemy = Select.enemy().combatUnits().nearestTo(tank);
        if (enemy != null) {
            double distanceToEnemy = enemy.distanceTo(tank);

            if (tank.isSieged()) {
                return updateWhenSieged(tank, enemy, distanceToEnemy);
            } else {
                return updateWhenUnsieged(tank, enemy, distanceToEnemy);
            }
        }
        return false;
    }

    // =========================================================
    
    private static boolean updateWhenSieged(AUnit tank, AUnit enemy, double distanceToEnemy) {
        if (enemy == null || distanceToEnemy >= 14) {
            if (AtlantisUtilities.rand(1, 100) <= 10) {
                tank.unsiege();
                return true;
            }
        }
        
        return false;
    }

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
            return true;
        }

        return false;
    }

    private static boolean nearestEnemyIsUnit(AUnit tank, AUnit enemy, double distanceToEnemy) {
        if (distanceToEnemy <= 14) {
            if (AtlantisUtilities.rand(1, 100) < 8) {
                tank.siege();
            }
            return true;
        }

        if (distanceToEnemy <= 10.8) {
            tank.siege();
            return true;
        }

        return true;
    }

}
