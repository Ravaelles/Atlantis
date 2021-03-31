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
        AUnit buildingTooClose = defineNearestBuilding(unit);
        
        // If there's enemy defensive building too close
        if (buildingTooClose != null) {
            double enemyWeaponRange = buildingTooClose.getWeaponRangeAgainst(unit);
            double enemyDistance = buildingTooClose.distanceTo(unit);
            System.out.println("weapon " + buildingTooClose.type().getShortName() + " // " + enemyWeaponRange + " // " + enemyDistance);
            double distanceMargin = enemyDistance - enemyWeaponRange;

            if (distanceMargin < 2.5) {
                boolean result = unit.holdPosition();
                unit.setTooltip("AvoidHold (" + String.format("%.1f", distanceMargin) + ")");
                return result;
            }

            if (distanceMargin < 1) {
                boolean result = unit.moveAwayFrom(buildingTooClose.getPosition(), 1);
                unit.setTooltip("AvoidMove (" + String.format("%.1f", distanceMargin) + ")");
                return result;
            }
        }
        
        return false;
    }

    // =========================================================

    private static AUnit defineNearestBuilding(AUnit unit) {
        if (unit.isGroundUnit()) {
            return Select.enemyOfType(
                    AUnitType.Terran_Bunker,
                    AUnitType.Protoss_Photon_Cannon,
                    AUnitType.Zerg_Sunken_Colony
            ).canAttack(unit, 1).first();
        }
        else  {
            return Select.enemyOfType(
                    AUnitType.Terran_Bunker, AUnitType.Terran_Missile_Turret,
                    AUnitType.Protoss_Photon_Cannon,
                    AUnitType.Zerg_Sunken_Colony
            ).canAttack(unit, 1).first();
        }
    }

}
