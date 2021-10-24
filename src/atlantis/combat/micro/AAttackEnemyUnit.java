package atlantis.combat.micro;

import atlantis.combat.micro.transport.ATransportManager;
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
        if (shouldNotAttack(unit)) {
            return false;
        }

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
//            APainter.paintLine(unit, enemy, Color.Red);
            processAttackUnit(unit, enemy);
            return true;
        } 
        
        return false;
    }

    public static boolean shouldNotAttack(AUnit unit) {
        return unit.isUnitUnableToDoAnyDamage()

                // =========================================================
                // =========================================================
                // THESE SHOULDNT BE USED! RESPECT DECISIONS OF TOP SUPERIOR MANAGER!!!
//                || unit.lastActionLessThanAgo(4, UnitActions.ATTACK_UNIT)
//                || (unit.isTankUnsieged() && (!unit.isMoving() && unit.woundPercent() > 15));
        ;
    }

    private static boolean processAttackUnit(AUnit unit, AUnit enemy) {
        if (handleMoveNextToTanksWhenAttackingThem(unit, enemy)) {
            return true;
        }

        unit.attackUnit(enemy);
        return true;
    }

    private static boolean handleMoveNextToTanksWhenAttackingThem(AUnit unit, AUnit enemy) {
        int count = Select.all().inRadius(0.4, unit).exclude(unit).exclude(enemy).count();
        if (
                enemy.isTank()
                        && (enemy.distToMoreThan(unit, unit.isMelee() ? 0.8 : 1.15))
                        && Select.all().inRadius(0.4, unit).exclude(unit).exclude(enemy).atMost(2)
                        && (unit.isMelee() || Select.all().inRadius(0.7, enemy).exclude(unit).exclude(enemy).atMost(3))
        ) {
            if (unit.isRanged() && Select.enemy().tanksSieged().inRadius(12.2, unit).isEmpty()) {
                return false;
            }

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
