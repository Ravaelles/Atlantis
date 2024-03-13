package atlantis.combat.missions.defend;

import atlantis.combat.missions.generic.MissionAllowsToAttackEnemyUnit;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;

public class MissionDefendAllowsToAttack extends MissionAllowsToAttackEnemyUnit {
    public MissionDefendAllowsToAttack(AUnit unit) {
        super(unit);
    }

    public boolean allowsToAttackEnemyUnit(AUnit enemy) {
        if (enemy == null) {
            throw new RuntimeException("aaa");
//            if (true) return true;
        }

        AUnit leader = unit.squadLeader();
        if (leader != null && leader.lastAttackFrameLessThanAgo(30 * 3)) return true;

        if (!enemy.hasPosition() || enemy.effUndetected()) {
            return false;
        }
        if (focusPoint == null) return true;

//        if (forbidAsTooFarFromFocusPoint(enemy)) return false;

        if (
            unit.isTargetInWeaponRangeAccordingToGame(enemy)
                || (unit.noCooldown() && enemy.canAttackTarget(unit) && (unit.isRanged() || focusPoint.regionsMatch(enemy)))
                || ourBuildingIsInDanger(unit, enemy)
        ) return true;

//        System.err.println("@ " + A.now() + " - not allowed to att " + unit.id() + " / " + enemy.type());

        return false;

//        if (focusPoint.regionsMatch(enemy)) {
//            return whenTargetInSameRegion(unit, enemy);
//        }
//        else {
//            return whenTargetInDifferentRegions(unit, enemy);
//        }
    }

    private boolean forbidAsTooFarFromFocusPoint(AUnit enemy) {
        if (unit.distToFocusPoint() < 15) return false;
        if (unit.distToBase() < 10) return false;

        return unit.hp() <= 20 || unit.lastAttackFrameMoreThanAgo(30 * 4);
    }

    private boolean whenTargetInSameRegion(AUnit unit, AUnit enemy) {
        Selection sunkens = Select.ourOfType(AUnitType.Zerg_Sunken_Colony);

        if (
            unit.isMelee()
                && sunkens.inRadius(15, enemy).notEmpty()
                && sunkens.inRadius(7, enemy).empty()
        ) return false;

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

    private boolean whenTargetInDifferentRegions(AUnit unit, AUnit enemy) {
        return false;
    }

    private boolean ourBuildingIsInDanger(AUnit unit, AUnit enemy) {
        Selection ourBuildings = Select.ourBuildings().inRadius(enemy.groundWeaponRange() + 0.5, enemy);
        if (unit.isAir()) {
            if (ourBuildings.atLeast(2) || ourBuildings.combatBuildingsAntiLand().notEmpty()) {
                return true;
            }
        }

        if (ourBuildings.combatBuildings(true).notEmpty()) return true;

        return false;
    }
}
