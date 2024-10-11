package atlantis.combat.missions.defend;

import atlantis.combat.advance.focus.AFocusPoint;
import atlantis.combat.micro.attack.DontAttackAlone;
import atlantis.combat.missions.defend.protoss.ProtossMissionDefendAllowsToAttack;
import atlantis.combat.missions.generic.MissionAllowsToAttackEnemyUnit;
import atlantis.units.AUnit;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import atlantis.util.We;

public class MissionDefendAllowsToAttack extends MissionAllowsToAttackEnemyUnit {
    public MissionDefendAllowsToAttack(AUnit unit) {
        super(unit);
    }

    public boolean allowsToAttackEnemyUnit(AUnit enemy) {
        if (enemy == null) {
            throw new RuntimeException("aaa");
//            if (true) return true;
        }

        if (We.protoss()) {
            return (new ProtossMissionDefendAllowsToAttack(unit)).allowsToAttackEnemyUnit(enemy);
        }

        if (!unit.isMissionSparta() && DontAttackAlone.isAlone(unit)) return false;
        if (unit.isMissionSparta() && unit.isMelee() && !enemy.hasCooldown()) {
            AFocusPoint focusPoint = unit.mission().focusPoint();
            if (focusPoint != null && unit.distTo(focusPoint) >= 2) return false;
        }

        AUnit leader = unit.squadLeader();
        if (leader != null) {
            if (leader.lastAttackFrameLessThanAgo(30 * 3)) return true;
            if (leader.distTo(unit) > 10) return false;
        }

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
