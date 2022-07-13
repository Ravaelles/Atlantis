package atlantis.combat.micro;

import atlantis.combat.targeting.ATargeting;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import atlantis.util.Enemy;
import atlantis.util.cache.Cache;

public class AAttackEnemyUnit {

    public static final double MAX_DIST_TO_ATTACK = 17;
//    public static final double MAX_DIST_TO_ATTACK = 500;

    public static String reasonNotToAttack;

    private static Cache<AUnit> cache = new Cache<>();

//    public static boolean handleAttackNearEnemyUnits(AUnit unit) {
//        return handleAttackNearEnemyUnits(unit, MAX_DIST_TO_ATTACK);
//    }

    /**
     * Selects the best enemy unit and issues attack order.
     *
     * @return <b>true</b> if unit has found valid target and is currently busy with either starting
     * an attack or just attacking the enemy<br />
     * <b>false</b> if no valid enemy to attack could be found
     */
    public static boolean handleAttackNearEnemyUnits(AUnit unit) {
        if (!canAttackNow(unit)) {
            return false;
        }

//        if (unit.shouldRetreat()) {
//            A.printStackTrace("Interesting, " + unit + " attacking despite RETREAT?!?");
//        }

        AUnit enemy = defineEnemyToAttackFor(unit);
        if (enemy == null) {
//            AAdvancedPainter.paintCircleFilled(unit, 7, Color.White);
            unit.addLog("NothingToAttack");
            return false;
        }

        return ProcessAttackUnit.processAttackUnit(unit, enemy);
    }

    private static boolean canAttackNow(AUnit unit) {
        if (unit.hasNoWeaponAtAll()) {
            return false;
        }

        if (unit.lastActionLessThanAgo(30 * 5, Actions.RUN_RETREAT)) {
            return false;
        }

        boolean shouldRetreat = unit.shouldRetreat();
        if (unit.isMelee() && shouldRetreat) {
            return false;
        }

        if (
            unit.isZergling()
                && (
                (Enemy.protoss() && unit.hp() <= 18) || shouldRetreat
            )
        ) {
            return false;
        }

        if (unit.isMelee()) {
            Selection combatBuildings = Select.ourCombatUnits().buildings();
            if (
                combatBuildings.inRadius(12, unit).notEmpty()
                    && combatBuildings.inRadius(6.8, unit).isEmpty()
            ) {
                return false;
            }
        }

        return true;
    }

    public static boolean canAttackEnemiesNow(AUnit unit) {
        if (AAttackEnemyUnit.reasonNotToAttack == null) {
            return true;
        }

        return defineEnemyToAttackFor(unit) != null;
    }

    public static String canAttackEnemiesNowString(AUnit unit) {
        return "(" + (AAttackEnemyUnit.canAttackEnemiesNow(unit)
            ? "v"
            : "DONT-" + AAttackEnemyUnit.reasonNotToAttack)
            + ")";
    }

    // =========================================================

    private static boolean allowedToAttack(AUnit unit) {
        if (unit.hasNoWeaponAtAll()) {
            reasonNotToAttack = "NoWeapon";
            return false;
        }

        if (unit.isTerranInfantry() && Count.medics() >= 2) {
            if (!unit.medicInHealRange() && (unit.isWounded() || unit.combatEvalRelative() < 1.5)) {
//                if (unit.cooldownRemaining() >= 2) {
                reasonNotToAttack = "NoMedics";
                return false;
//                }
            }
        }

        // @Problematic - Vultures dont attack from far
//        if (Count.ourCombatUnits() >= 5 && unit.outsideSquadRadius()) {
//            reasonNotToAttack = "Outside";
//            return false;
//        }

        if (unit.hasSquad() && unit.squad().cohesionPercent() <= 80 && unit.isAttackingOrMovingToAttack()) {
            if (unit.enemiesNear().ranged().notEmpty() && unit.lastStartedAttackMoreThanAgo(90)) {
                reasonNotToAttack = "Cautious";
                return false;
            }
        }

        return true;
    }

    private static AUnit defineEnemyToAttackFor(AUnit unit) {
        return cache.get(
            "defineEnemyToAttackFor",
            0,
            () -> {
                reasonNotToAttack = null;

//                if (!allowedToAttack(unit)) {
//                    return null;
//                }

                AUnit enemy = ATargeting.defineBestEnemyToAttackFor(unit, MAX_DIST_TO_ATTACK);
//                System.out.println("enemy = " + enemy);

                if (enemy == null) {
                    return null;
                }
                if (!isValidTargetAndAllowedToAttackUnit(unit, enemy)) {
                    System.out.println("Not allowed to attack: " + enemy + " (" + AAttackEnemyUnit.reasonNotToAttack + ")");
                    return null;
                }

                return enemy;
            }
        );
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
        if (target == null || target.position() == null) {
            return false;
        }

        if (!missionAllowsToAttackEnemyUnit(unit, target)) {
            reasonNotToAttack = "MissionForbids" + target.name();
            unit.setTooltipTactical(reasonNotToAttack);
            unit.addLog(reasonNotToAttack);
            return false;
        }

        if (!unit.canAttackTarget(target, false, true)) {
            reasonNotToAttack = "InvalidTarget";
            unit.setTooltipTactical(reasonNotToAttack);
            unit.addLog(reasonNotToAttack);
            System.err.println(reasonNotToAttack + " for " + unit + ": " + target + " (" + unit.distTo(target) + ")");
            return false;
        }

        // Prevent units from switching attack of the same unit, to another unit of the same type
//        unit.target().isTank() &&
        AUnit currentTarget = unit.target();
        if (unit.isMelee() && currentTarget != null && !currentTarget.equals(target) && unit.isAttackingOrMovingToAttack()) {
            if (currentTarget.isWorker() || currentTarget.isCombatUnit()) {
                if (unit.distToLessThan(currentTarget, 1.03)) {
                    reasonNotToAttack = "DontSwitch";
                    unit.addLog(reasonNotToAttack);
                    return false;
                }
            }
        }

        if (!target.effVisible()) {
            System.err.println(unit + " got not visible target to attack: " + target);
            return false;
        }

        return true;
    }

    private static boolean missionAllowsToAttackEnemyUnit(AUnit unit, AUnit enemy) {
        return unit.mission() == null
            || unit.isTank()
            || unit.mission().allowsToAttackEnemyUnit(unit, enemy);
//            || (unit.isRanged() && enemy.isMelee());
    }

}
