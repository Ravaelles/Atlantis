package atlantis.units.fix;

import atlantis.architecture.Manager;
import atlantis.combat.advance.focus.TooCloseToFocusPoint;
import atlantis.combat.advance.focus.TooFarFromFocusPoint;
import atlantis.combat.micro.avoid.DoAvoidEnemies;
import atlantis.combat.squad.SquadTargeting;
import atlantis.game.A;
import atlantis.information.enemy.EnemyUnits;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Select;
import bwapi.Color;

public class DoPreventFreezesLogic {
    public static boolean handle(AUnit unit) {
//        if (unit.lastUnderAttackLessThanAgo(30 * 3)) {
//            unit.paintCircleFilled(22, Color.Orange);
//        }

        if (shouldAvoidEnemies(unit)) {
            if (avoidEnemies(unit)) {
                unit.paintCircleFilled(22, Color.Orange);
                return true;
            }
        }

        if (goToSquadTarget(unit)) {
            return true;
        }

        if (goToNearestCombatFriend(unit)) {
            return true;
        }

        if (!unit.isStopped() && unit.lastActionMoreThanAgo(44, Actions.STOP)) {
            unit.stop("DoPreventStop");
            return true;
        }

        Manager manager;
        if ((manager = new TooFarFromFocusPoint(unit)).invoke(unit) != null) return true;
        if ((manager = new TooCloseToFocusPoint(unit)).invoke(unit) != null) return true;

        unit.paintCircleFilled(22, Color.Yellow);
//        if (goToNearestEnemy(unit)) return true;
        if (goToCombatUnit(unit)) return true;

        return false;
    }

    private static boolean goToSquadTarget(AUnit unit) {
        SquadTargeting targeting = unit.squad().targeting();
        AUnit target = targeting.lastTargetIfAlive();

        if (target == null) return false;

//        if (target == null) {
//            target = EnemyUnits.discovered().groundUnits().nearestTo(unit);
//            if (target == null) return false;
//            else targeting.forceTarget(target);
//        }

        if (
            target.distTo(unit) > 6
                && unit.hasWeaponToAttackThisUnit(target)
                && unit.move(target, Actions.MOVE_FORMATION, "ToSquadTarget")
        ) {
            return true;
        }

        return false;
    }

    private static boolean goToNearestCombatFriend(AUnit unit) {
        AUnit nearest = unit.friendsNear().combatUnits().groundUnits().nearestTo(unit);
        if (nearest != null && nearest.distTo(unit) >= 2) {
            if (unit.move(nearest, Actions.MOVE_FORMATION, "PreventMove2Friend")) {
                return true;
            }
        }

        return false;
    }

    private static boolean goToCombatUnit(AUnit unit) {
        AUnit nearest = Select.ourCombatUnits().exclude(unit).first();
        if (nearest == null) return false;

        unit.move(nearest, Actions.MOVE_FORMATION, "DoSomething");
//        if (A.seconds() % 2 == 0)
//        else unit.attackUnit(nearest);

        return true;
    }

    private static boolean shouldAvoidEnemies(AUnit unit) {
        return unit.cooldown() >= 13
            || unit.woundPercent() >= 30;
    }

    private static boolean avoidEnemies(AUnit unit) {
        DoAvoidEnemies avoidEnemies = new DoAvoidEnemies(unit);

        return avoidEnemies.handle() != null;
    }

    private static boolean goToNearestEnemy(AUnit unit) {
        AUnit nearestEnemy = EnemyUnits.discovered().groundUnits().havingPosition().nearestTo(unit);
        if (nearestEnemy == null) return false;

        unit.move(nearestEnemy, Actions.MOVE_ENGAGE, "EngageAnyEnemy");
//        if (A.seconds() % 2 == 0)
//        else unit.attackUnit(nearestEnemy);

        return true;
    }
}
