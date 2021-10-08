package atlantis.combat.micro;

import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.Select;

public class AAvoidDefensiveBuildings {

    public static boolean avoidCloseBuildings(AUnit unit, boolean allowToHold) {
        AUnit enemyBuildingThatCanAttackThisUnit = defineNearestBuilding(unit);
        if (enemyBuildingThatCanAttackThisUnit == null) {
            return false;
        }

        int ourUnits = Select.ourCombatUnits().inRadius(10, unit).count();
        if (ourUnits >= 20) {
            return false;
        }

        double enemyWeaponRange = enemyBuildingThatCanAttackThisUnit.getWeaponRangeAgainst(unit);
        double enemyDistance = enemyBuildingThatCanAttackThisUnit.distanceTo(unit);
//            System.out.println("weapon " + buildingTooClose.type().getShortName() + " // " + enemyWeaponRange + " // " + enemyDistance);
        double distanceMargin = enemyDistance - enemyWeaponRange;

        if (distanceMargin <= 2.0) {
            boolean result = unit.moveAwayFrom(
                    enemyBuildingThatCanAttackThisUnit.getPosition(),
                    2,
                    "AvoidBack (" + String.format("%.1f", distanceMargin) + ")"
            );
            return result;
        }

        double safeDist = unit.isVulture() ? 6 : 4;

        if (distanceMargin <= safeDist) {
            if (allowToHold && !unit.isJustShooting()) {
                if (unit.getOrderTarget() != null) {
                    unit.holdPosition("AvoidHold (" + String.format("%.1f", distanceMargin) + ")");
                }
                return true;
            }

            if (!allowToHold) {
                unit.moveAwayFrom(
                        enemyBuildingThatCanAttackThisUnit.getPosition(),
                        3,
                        "AvoidMove (" + String.format("%.1f", distanceMargin) + ")"
                );
                return true;
            }

            unit.setTooltip("Avoid (" + String.format("%.1f", distanceMargin) + ")");
        }

//        if (distanceMargin <= 4.5) {
//            boolean result = unit.moveAwayFrom(enemyBuildingThatCanAttackThisUnit.getPosition(), 1.5);
//            unit.setTooltip("AvoidMove (" + String.format("%.1f", distanceMargin) + ")");
//            return result;
//        }

        return false;
    }

    // =========================================================

    private static AUnit defineNearestBuilding(AUnit unit) {
        if (unit.isGroundUnit()) {
            return Select.enemyOfType(
                    AUnitType.Terran_Bunker,
                    AUnitType.Protoss_Photon_Cannon,
                    AUnitType.Zerg_Sunken_Colony
            ).canAttack(unit, 5).nearestTo(unit);
        }
        else  {
            return Select.enemyOfType(
                    AUnitType.Terran_Bunker, AUnitType.Terran_Missile_Turret,
                    AUnitType.Protoss_Photon_Cannon,
                    AUnitType.Zerg_Sunken_Colony
            ).canAttack(unit, 5).nearestTo(unit);
        }
    }

}
