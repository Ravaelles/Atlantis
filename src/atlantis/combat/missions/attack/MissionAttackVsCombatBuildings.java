package atlantis.combat.missions.attack;

import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;

public class MissionAttackVsCombatBuildings {
    
    public static boolean allowsToAttackCombatBuildings(AUnit unit, AUnit combatBuilding) {
        if (unit.isInfantry() && unit.hp() <= 39) {
            return false;
        }

        // Tanks always allowed
        if (unit.isTank() && unit.distToMoreThan(combatBuilding, 7.9)) {
            return true;
        }

        // Air units
        if (unit.isAir() && combatBuilding.isSunken()) {
            return true;
        }

        if (unit.friendsNearCount() <= 6) {
            return false;
        }

        // Standard infantry attack
//        boolean notStrongEnough = Select.ourCombatUnits().inRadius(6, unit).atMost(8);
//        if (notStrongEnough || unit.lastStoppedRunningLessThanAgo(30 * 10)) {
//        if (unit.lastStoppedRunningLessThanAgo(30 * 10)) {
//            return false;
//        }

        int buildings = Select.enemy().combatBuildings(false).inRadius(7, combatBuilding).count();

        return Select.ourRealUnits()
            .inRadius(6, unit)
            .excludeTypes(AUnitType.Terran_Medic)
            .atLeast(9 * buildings);
    }
}