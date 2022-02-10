package atlantis.combat.micro;

import atlantis.combat.targeting.ATargeting;
import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.actions.Actions;
import atlantis.units.select.Count;
import atlantis.units.select.Select;

public class AAttackEnemyUnit {

    private static final double MAX_DIST_TO_ATTACK = 40;

    public static boolean handleAttackNearEnemyUnits(AUnit unit) {
        return handleAttackNearEnemyUnits(unit, MAX_DIST_TO_ATTACK);
    }

    /**
     * Selects the best enemy unit and issues attack order.
     * @return <b>true</b> if unit has found valid target and is currently busy with either starting
     * an attack or just attacking the enemy<br />
     * <b>false</b> if no valid enemy to attack could be found
     */
    public static boolean handleAttackNearEnemyUnits(AUnit unit, double maxDistFromEnemy) {
        if (shouldSkip(unit)) {
            return false;
        }

        AUnit enemy = ATargeting.defineBestEnemyToAttackFor(unit, maxDistFromEnemy);

        if (enemy == null) {
            return false;
        }
        if (!isValidTargetAndAllowedToAttackUnit(unit, enemy)) {
            return false;
        }

        return processAttackUnit(unit, enemy);
    }

    private static boolean shouldSkip(AUnit unit) {
        if (unit.hasNoWeaponAtAll()) {
            return true;
        }

        if (unit.isTerranInfantry() && !unit.medicInHealRange()) {
            if (Count.medics() >= 2) {
                return true;
            }
        }

        return false;
    }

//    public static boolean shouldNotAttack(AUnit unit) {
////        if (AAvoidUnits.shouldAvoidAnyUnit(unit)) {
////            return false;
////        }
//
//        return unit.isUnitUnableToDoAnyDamage()
//
//                // =========================================================
//                // =========================================================
//                // THESE SHOULDNT BE USED! RESPECT DECISIONS OF TOP SUPERIOR MANAGER!!!
////                || unit.lastActionLessThanAgo(4, UnitActions.ATTACK_UNIT)
////                || (unit.isTankUnsieged() && (!unit.isMoving() && unit.woundPercent() > 15));
//        ;
//    }

    private static boolean isValidTargetAndAllowedToAttackUnit(AUnit unit, AUnit target) {
        if (!missionAllowsToAttack(unit, target)) {
            return false;
        }

        if (!unit.canAttackTarget(target, false, true)) {
            unit.setTooltipTactical("Invalid target");
            System.err.println("Invalid target for " + unit + ": " + target + " (" + unit.distTo(target) + ")");
            return false;
        }

        // Prevent units from switching attack of the same unit, to another unit of the same type
        if (unit.target() != null && unit.target().isTank() && unit.isAttackingOrMovingToAttack()) {
            if (unit.distToLessThan(unit.target(), 3)) {
                return false;
            }
        }

        return true;
    }

    private static boolean processAttackUnit(AUnit unit, AUnit target) {
        if (handleMoveNextToTanksWhenAttackingThem(unit, target)) {
            return true;
        }

        if (target.isBase() && unit.distToMoreThan(target, 4)) {
            return unit.move(target, Actions.MOVE_ENGAGE, "BaseAttack", false);
        }

//        unit.setTooltip("@" + target.name());
        return unit.attackUnit(target);
    }

    private static boolean handleMoveNextToTanksWhenAttackingThem(AUnit unit, AUnit enemy) {
        if (!enemy.isTank()) {
            return false;
        }

        int count = Select.all().inRadius(0.4, unit).exclude(unit).exclude(enemy).count();
        if (
                !unit.isAir()
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

            if (unit.move(enemy, Actions.MOVE_ENGAGE, "Soyuz" + A.dist(enemy, unit) + "/" + count, false)) {
                return true;
            }
        }

        return false;
    }

    private static boolean missionAllowsToAttack(AUnit unit, AUnit enemy) {
        return unit.mission() == null
                || unit.mission().allowsToAttackEnemyUnit(unit, enemy)
                || (unit.isRanged() && enemy.isMelee());
    }

}
