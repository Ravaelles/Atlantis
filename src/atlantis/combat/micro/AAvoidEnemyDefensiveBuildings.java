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
        double distanceMargin = enemyDistance - enemyWeaponRange;
//        System.out.println(enemyBuildingThatCanAttackThisUnit.type().getShortName() + " // " +  enemyBuildingThatCanAttackThisUnit.getWeaponAgainst(unit)+ " // " + enemyWeaponRange + " // " + enemyDistance + " // " +distanceMargin);

        // =========================================================

        // Way too close, in shooting range of building, have to back out
        if (distanceMargin <= 1.2) {
            if (unit.isMoving()) {
                unit.holdPosition("AvoidCriticalHold(" + String.format("%.1f", distanceMargin) + ")");
            }

            unit.setTooltip("AvoidHoldOk(" + String.format("%.1f", distanceMargin) + ")");
            return true;
        }

        // Very close, but it should be okay to hold
        else if (1.2 < distanceMargin && distanceMargin <= 1.8) {
            return unit.moveAwayFrom(
                    enemyBuildingThatCanAttackThisUnit.getPosition(),
                    1.0,
                    "AvoidMove(" + String.format("%.1f", distanceMargin) + ")"
            );
        }

        // Very close, but it should be okay to hold
        else if (1.8 < distanceMargin && distanceMargin <= 2.6) {
            unit.holdPosition("AvoidHoldOk(" + String.format("%.1f", distanceMargin) + ")");
            return true;
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
