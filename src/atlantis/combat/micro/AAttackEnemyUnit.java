package atlantis.combat.micro;

import atlantis.combat.targeting.AEnemyTargeting;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import atlantis.units.actions.UnitActions;
import atlantis.util.A;

public class AAttackEnemyUnit {

    public static boolean handleAttackNearbyEnemyUnits(AUnit unit) {
        return handleAttackNearbyEnemyUnits(unit, 40);
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
//        System.out.println("enemy2 = " + enemy);
//        System.out.println(Select.enemy());
        if (enemy == null) {
//            System.out.println(A.now() + " empty...");
            return false;
        }

//        System.out.println("enemy = " + enemy + " // alive:" + enemy.isAlive());
//        System.out.println("----------------------------");

        if (enemy != null && isValidTargetAndAllowedToAttackUnit(unit, enemy)) {
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

    private static boolean isValidTargetAndAllowedToAttackUnit(AUnit unit, AUnit target) {
        if (!missionAllowsToAttack(unit, target)) {
            return false;
        }

        if (!unit.canAttackTarget(target, false, true)) {
            unit.setTooltip("Invalid target");
            System.err.println("Invalid target for " + unit + ": " + target + " (" + unit.distTo(target) + ")");
            return false;
        }

        // Prevent units from switching attack of the same unit, to another unit of the same type
        if (unit.isAttackingOrMovingToAttack() && unit.target() != null && unit.target().isTank()) {
            if (unit.distToLessThan(unit.target(), 3)) {
                return false;
            }
        }

        return true;
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
                        && !unit.isAirUnit()
                        && !unit.is(
                                AUnitType.Terran_Siege_Tank_Siege_Mode,
                                AUnitType.Terran_Siege_Tank_Tank_Mode,
                                AUnitType.Protoss_Archon,
                                AUnitType.Protoss_Reaver
                        )
                        && (enemy.distToMoreThan(unit, unit.isMelee() ? 0.8 : 1.15))
                        && Select.all().inRadius(0.4, unit).exclude(unit).exclude(enemy).atMost(2)
                        && (unit.isMelee() || Select.all().inRadius(0.7, enemy).exclude(unit).exclude(enemy).atMost(3))
        ) {
            if (unit.isRanged() && Select.enemy().tanksSieged().inRadius(12.2, unit).isEmpty()) {
                return false;
            }

            if (unit.move(enemy, UnitActions.MOVE_TO_ENGAGE, "Soyuz" + A.dist(enemy, unit) + "/" + count)) {
                return true;
            }
        }

        return false;
    }

    private static boolean missionAllowsToAttack(AUnit unit, AUnit enemy) {
        if (unit.isSquadScout()) {
            return Select.our().inRadius(4, unit).atLeast(3);
        }

        return unit.mission() == null
                || unit.mission().allowsToAttackEnemyUnit(unit, enemy)
                || (unit.isRanged() && enemy.isMelee());
    }

}
