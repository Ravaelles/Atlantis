package atlantis.combat.missions.defend;

import atlantis.combat.advance.focus.AFocusPoint;
import atlantis.combat.advance.focus.IsTargetOnWrongSideOfFocusPoint;
import atlantis.combat.micro.attack.DontAttackAlone;
import atlantis.combat.missions.defend.protoss.ProtossMissionDefendAllowsToAttack;
import atlantis.combat.missions.generic.MissionAllowsToAttackEnemyUnit;
import atlantis.decisions.Decision;
import atlantis.game.A;
import atlantis.information.enemy.EnemyUnits;
import atlantis.units.AUnit;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import atlantis.util.We;
import atlantis.util.log.ErrorLog;

public class MissionDefendAllowsToAttack extends MissionAllowsToAttackEnemyUnit {
    public MissionDefendAllowsToAttack(AUnit unit) {
        super(unit);
    }

    public boolean allowsToAttackEnemyUnit(AUnit enemy) {
        if (enemy == null) {
            ErrorLog.printMaxOncePerMinutePlusPrintStackTrace("MissionDefendAllowsToAttack: enemy is null!");
            return false;
        }

        if (We.protoss()) {
            Decision decision = (new ProtossMissionDefendAllowsToAttack(unit)).allowsToAttackEnemyUnit(enemy);
            if (decision.notIndifferent()) return decision.toBoolean();
        }

        if (unit.isAir()) return true;

        if (IsTargetOnWrongSideOfFocusPoint.isTargetOnWrongSideOfFocusPoint(unit, enemy)) {
            return false;
        }

        return true;

//        if (!unit.isMissionSparta() && DontAttackAlone.isAlone(unit)) return false;
//        if (unit.isMissionSparta() && unit.isMelee() && !enemy.hasCooldown()) {
//            AFocusPoint focusPoint = unit.mission().focusPoint();
//            if (focusPoint != null && unit.distTo(focusPoint) >= 2) return false;
//        }
//
//        if (enemy.isWorker() && unit.isRanged() && unit.cooldown() <= 5) return true;
//
//        AUnit leader = unit.squadLeader();
//        if (leader != null) {
//            if (leader.lastAttackFrameLessThanAgo(30 * 3)) return true;
//            if (leader.distTo(unit) > 10) return false;
//        }
//
//        if (!enemy.hasPosition() || enemy.effUndetected()) {
//            return false;
//        }
//        if (focusPoint == null) return true;
//
////        if (forbidAsTooFarFromFocusPoint(enemy)) return false;
//
//        if (
//            unit.isTargetInWeaponRangeAccordingToGame(enemy)
//                || (unit.noCooldown() && enemy.canAttackTarget(unit) && (unit.isRanged() || focusPoint.regionsMatch(enemy)))
//                || ourBuildingIsInDanger(unit, enemy)
//        ) return true;
//
//        return false;
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
