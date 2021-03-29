package atlantis.combat.micro;

import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.Select;

/**
 *
 * @author Rafal Poniatowski <ravaelles@gmail.com>
 */
public class AAvoidDefensiveBuildings {

    public static boolean avoidCloseBuildings(AUnit unit) {
        AUnit buildingTooClose;
        if (unit.isGroundUnit()) {
            buildingTooClose = Select.enemyOfType(
                    AUnitType.Terran_Bunker, 
                    AUnitType.Protoss_Photon_Cannon, 
                    AUnitType.Zerg_Sunken_Colony
            ).canAttack(unit, 2.3).first();
        }
        else  {
            buildingTooClose = Select.enemyOfType(
                    AUnitType.Terran_Bunker, AUnitType.Terran_Missile_Turret, 
                    AUnitType.Protoss_Photon_Cannon, 
                    AUnitType.Zerg_Sunken_Colony
            ).canAttack(unit, 1.2).first();
        }
        
        // If there's enemy defensive building too close
        if (buildingTooClose != null) {
            int enemyWeaponRange = buildingTooClose.getWeaponRangeAgainst(unit);
            double enemyDistance = buildingTooClose.distanceTo(unit);
            double distanceMargin = enemyDistance - enemyWeaponRange;
            
//            System.out.println(unit + ", enemyDistance = " + enemyDistance + ", distanceMargin = " + distanceMargin);
            
            if (distanceMargin > 1.5) {
                return false;
            }
            
            if (distanceMargin > 0.9) {
                unit.setTooltip("Avoid building (" + String.format("%.1f", distanceMargin) + ")");
                return unit.moveAwayFrom(buildingTooClose.getPosition(), 1);
            }
            else if (distanceMargin > 0.3) {
                if (!unit.isAttackFrame() || unit.isMoving()) {
                    unit.holdPosition();
                    unit.setTooltip("Avoid building (" + String.format("%.1f", distanceMargin) + ") !!!");
                }
                return true;
            }
            else if (distanceMargin <= 0.1) {
                unit.setTooltip("Run from building");
                return unit.runFrom(buildingTooClose);
            }
        }
        
        return false;
    }
    
}
