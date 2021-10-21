package atlantis.combat.micro;

import atlantis.combat.targeting.AEnemyTargeting;
import atlantis.debug.APainter;
import atlantis.units.AUnit;
import atlantis.units.Select;
import atlantis.units.actions.UnitActions;
import atlantis.util.A;
import bwapi.Color;

public class AAttackEnemyUnit {

    public static boolean handleAttackNearbyEnemyUnits(AUnit unit) {
        return handleAttackNearbyEnemyUnits(unit, 999999);
    }

    /**
     * Selects the best enemy unit and issues attack order.
     * @return <b>true</b> if unit has found valid target and is currently busy with either starting
     * an attack or just attacking the enemy<br />
     * <b>false</b> if no valid enemy to attack could be found
     */
    public static boolean handleAttackNearbyEnemyUnits(AUnit unit, double maxDistFromEnemy) {
        AUnit enemy = AEnemyTargeting.defineBestEnemyToAttackFor(unit, maxDistFromEnemy);
        if (enemy == null) {
            return false;
        }

        if (!unit.canAttackThisUnit(enemy, false, true)) {
            unit.setTooltip("Invalid target");
            System.err.println("Invalid target for " + unit + ": " + enemy + " (" + unit.distTo(enemy) + ")");
            return false;
        }

        if (!missionAllowsToAttack(unit, enemy)) {
            return false;
        }

        if (enemy != null) {
            unit.setTooltip("->" + enemy.shortName() + "(" + unit.cooldownRemaining() + ")");
            APainter.paintLine(unit, enemy, Color.Red);
            processAttackUnit(unit, enemy);
            return true;
        } 
        
        return false;
    }

    private static boolean processAttackUnit(AUnit unit, AUnit enemy) {
        if (handleMoveNextToTanksWhenAttackingThem(unit, enemy)) {
            return true;
        }

        unit.attackUnit(enemy);
        unit.setTooltip("ShootTank(" + unit.cooldownRemaining() + ")");
        return true;
    }

    private static boolean handleMoveNextToTanksWhenAttackingThem(AUnit unit, AUnit enemy) {
        int count = Select.all().inRadius(0.4, unit).exclude(unit).exclude(enemy).count();
        if (
                enemy.isTank()
//                        && (!unit.ranged() || unit.lastStartedAttackLessThanAgo(12) || unit.cooldownRemaining() > 0)
                        && (enemy.distToMoreThan(unit, unit.melee() ? 1 : 1.3))
                        && Select.all().inRadius(0.4, unit).exclude(unit).exclude(enemy).atMost(2)
                        && (unit.melee() || Select.all().inRadius(0.7, enemy).exclude(unit).exclude(enemy).atMost(3))
        ) {
            if (unit.move(enemy, UnitActions.MOVE_TO_ENGAGE, "Soyuz(" + A.dist(enemy, unit) + "/" + count + ")")) {
                return true;
            }
        }

        return false;
    }

    private static boolean missionAllowsToAttack(AUnit unit, AUnit enemy) {
        return unit.mission() == null || unit.mission().allowsToAttackEnemyUnit(unit, enemy);
    }

}
