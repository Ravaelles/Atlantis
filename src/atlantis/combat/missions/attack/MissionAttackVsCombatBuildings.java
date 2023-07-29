package atlantis.combat.missions.attack;

import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.HasUnit;
import atlantis.units.select.Select;

public class MissionAttackVsCombatBuildings extends HasUnit {

    public MissionAttackVsCombatBuildings(AUnit unit) {
        super(unit);
    }

    public boolean allowsToAttackCombatBuildings(AUnit combatBuilding) {
        if (unit.isTerran()) {
            if (forbiddenForTerranInfantry(combatBuilding)) {
                return false;
            }

            // Tanks always allowed
            if (unit.isTank() && unit.distToMoreThan(combatBuilding, 7.9)) {
                return true;
            }
        }

        // Air units
        if (unit.isAir() && combatBuilding.isSunken()) {
            return true;
        }

        if (unit.friendsInRadiusCount(5) <= 8) {
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

    public boolean forbiddenForTerranInfantry(AUnit combatBuilding) {
        if (!unit.isTerranInfantry()) {
            return false;
        }

        if (unit.hp() <= 35) {
            return true;
        }

        double distTo = unit.distTo(combatBuilding);
        if (distTo > 8.2) {
            return false;
        }

        if (unit.friendsInRadiusCount(1.5) <= 0 || unit.friendsInRadiusCount(4) <= 6) {
            return true;
        }

        if (distTo >= 5 && unit.combatEvalRelative() <= 1.2) {
            return true;
        }

        return false;
    }
}
