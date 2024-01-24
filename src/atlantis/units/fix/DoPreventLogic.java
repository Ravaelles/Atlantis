package atlantis.units.fix;

import atlantis.combat.micro.attack.AttackNearbyEnemies;
import atlantis.combat.micro.avoid.AvoidEnemiesIfNeeded;
import atlantis.combat.micro.avoid.DoAvoidEnemies;
import atlantis.game.A;
import atlantis.information.enemy.EnemyUnits;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import bwapi.Color;

public class DoPreventLogic {
    public static boolean handle(AUnit unit) {
//        if (unit.lastUnderAttackLessThanAgo(30 * 3)) {
//            unit.paintCircleFilled(22, Color.Orange);
//        }
        DoAvoidEnemies avoidEnemies = new DoAvoidEnemies(unit);

        if (avoidEnemies.handle() != null) {
            unit.paintCircleFilled(22, Color.Orange);
            return true;
        }

//        AvoidEnemiesIfNeeded avoidEnemies = new AvoidEnemiesIfNeeded(unit);
//        avoidEnemies.forceAvoid = true;
//
//        if (avoidEnemies.avoidEnemiesIfNeeded() != null) {
//            unit.paintCircleFilled(22, Color.Orange);
//            return true;
//        }

//        System.err.println("Interesting - fight!?");
//        if ((new AttackNearbyEnemies(unit)).forceHandle() != null) return true;

        unit.paintCircleFilled(22, Color.Yellow);
        if (goToNearestEnemy(unit)) return true;

        return false;
    }

    private static boolean goToNearestEnemy(AUnit unit) {
        AUnit nearestEnemy = EnemyUnits.discovered().groundUnits().havingPosition().nearestTo(unit);
        if (nearestEnemy == null) return false;

        unit.move(nearestEnemy, Actions.MOVE_ENGAGE, "EngageAnEnemy");
//        if (A.seconds() % 2 == 0)
//        else unit.attackUnit(nearestEnemy);

        return true;
    }
}
