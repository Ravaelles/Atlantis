package atlantis.combat.missions.defend;

import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;

public class MissionDefendAllowsToAttack {

    protected MissionDefend mission;

    public MissionDefendAllowsToAttack(MissionDefend missionDefend) {
        this.mission = missionDefend;
    }

    public boolean allowsToAttackEnemyUnit(AUnit unit, AUnit enemy) {
        if (mission.focusPoint == null || mission.main == null) {
            return true;
        }

        if (
            unit.canAttackTarget(enemy)
            || enemy.canAttackTarget(unit)
            || ourBuildingIsInDanger(unit, enemy)
        ) {
            return true;
        }

        boolean regionsAreDifferent = unit.position().region().equals(enemy.position().region());

//        APainter.paintCircleFilled(unit, 6, !regionsAreDifferent ? Color.Green : Color.Red);

        if (!regionsAreDifferent) {
            return whenDifferentRegions(unit, enemy);
        }
        else {
            return whenSameRegion(unit, enemy);
        }
    }

    private boolean whenSameRegion(AUnit unit, AUnit enemy) {
        Selection sunkens = Select.ourOfType(AUnitType.Zerg_Sunken_Colony);

        if (
            unit.isMelee()
            && sunkens.inRadius(15, enemy).notEmpty()
            && sunkens.inRadius(7, enemy).empty()
        ) {
            return false;
        }

        // =========================================================

        // =========================================================

        int friends = unit.friendsInRadiusCount(4);
        if (
            (
                enemy.isMelee()
                || (unit.squadSize() >= 4 && friends <= 1)
            )
            && !unit.enemiesNear().inRadius(9, unit).onlyMelee()
        ) {
            unit.setTooltip("TooScarce");
            return false;
        }

        // =========================================================

        return (friends >= 2 && unit.combatEvalRelative() >= 2.5)
            || (friends >= 5 && unit.woundPercentMax(15));
    }

    private boolean whenDifferentRegions(AUnit unit, AUnit enemy) {
        return false;
    }

    private boolean ourBuildingIsInDanger(AUnit unit, AUnit enemy) {
        Selection ourBuildings = Select.ourBuildings().inRadius(enemy.groundWeaponRange() + 0.5, enemy);
        if (unit.isAir()) {
            if (ourBuildings.atLeast(2)) {
                return true;
            }
        }

        if (ourBuildings.combatBuildings(true).notEmpty()) {
            return true;
        }

        return false;
    }
}
