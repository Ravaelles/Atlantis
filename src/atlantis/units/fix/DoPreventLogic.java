package atlantis.units.fix;

import atlantis.architecture.Manager;
import atlantis.combat.advance.focus.TooCloseToFocusPoint;
import atlantis.combat.advance.focus.TooFarFromFocusPoint;
import atlantis.combat.micro.avoid.DoAvoidEnemies;
import atlantis.information.enemy.EnemyUnits;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Select;
import bwapi.Color;

public class DoPreventLogic {
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

        if (!unit.isStopped() && unit.lastActionMoreThanAgo(30 * 4, Actions.STOP)) {
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
