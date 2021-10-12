package atlantis.combat.micro;

import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.Select;

public class AAvoidEnemyDefensiveBuildings {

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

        // =========================================================

        // Way too close, in shooting range of building, have to back out
        if (distanceMargin <= 0.8) {
            return unit.moveAwayFrom(
                    enemyBuildingThatCanAttackThisUnit.getPosition(),
                    2,
                    "AvoidCritical(" + String.format("%.1f", distanceMargin) + ")"
            );
        }

        // Very close, but it should be okay to hold
        else if (2 <= distanceMargin && distanceMargin <= 3.5) {
//            boolean result = unit.moveAwayFrom(
//                    enemyBuildingThatCanAttackThisUnit.getPosition(),
//                    2,
//                    "AvoidBack (" + String.format("%.1f", distanceMargin) + ")"
//            );
//            return result;
            unit.stop("AvoidStop(" + String.format("%.1f", distanceMargin) + ")");
            return true;
        }

//        double safetyMargin = unit.isVulture() ? 6 : 4;
//
//        if (safetyMargin < distanceMargin) {
//            return unit.moveAwayFrom(
//                    enemyBuildingThatCanAttackThisUnit.getPosition(),
//                    3,
//                    "AvoidBack(" + String.format("%.1f", distanceMargin) + ")"
//            );
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
            ).notConstructing().canAttack(unit, 5).nearestTo(unit);
        }
        else  {
            return Select.enemyOfType(
                    AUnitType.Terran_Bunker, AUnitType.Terran_Missile_Turret,
                    AUnitType.Protoss_Photon_Cannon,
                    AUnitType.Zerg_Sunken_Colony
            ).notConstructing().canAttack(unit, 5).nearestTo(unit);
        }
    }

}
