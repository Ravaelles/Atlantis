package atlantis.combat.micro.attack;

import atlantis.combat.targeting.ATargeting;
import atlantis.decions.Decision;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.architecture.Manager;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import atlantis.util.Enemy;
import atlantis.util.cache.Cache;

public class AttackNearbyEnemies extends Manager {

    public static final double MAX_DIST_TO_ATTACK = 25;
    private static ProcessAttackUnit processAttackUnit;

    public static String reasonNotToAttack;



//    private AUnit unit;
    private static Cache<AUnit> cache = new Cache<>();
    private static Cache<Object> cacheObject = new Cache<>();

    // =========================================================

    public AttackNearbyEnemies(AUnit unit) {
        super(unit);
        processAttackUnit = (new ProcessAttackUnit(unit));
    }

    // =========================================================

    public Manager handle() {
        if (handleAttackNearEnemyUnits(unit)) {
            return usedManager(this);
        }

        return null;
    }

    /**
     * Selects the best enemy unit and issues attack order.
     *
     * @return <b>true</b> if unit has found valid target and is currently busy with either starting
     * an attack or just attacking the enemy<br />
     * <b>false</b> if no valid enemy to attack could be found
     */
    public static boolean handleAttackNearEnemyUnits(AUnit unit) {
        return (boolean) cacheObject.getIfValid(
            "handleAttackNearEnemyUnits: " + unit.id(),
            4,
            () -> {
                AttackNearbyEnemies service = new AttackNearbyEnemies(unit);

                if (!service.canAttackNow()) return false;

                AUnit enemy = service.defineEnemyToAttackFor();
                if (enemy == null) return false;

                return processAttackUnit.processAttackOtherUnit(enemy);
            }
        );
    }

    private boolean canAttackNow() {
        if (unit.hasNoWeaponAtAll()) {
            return false;
        }

        // === Mission =============================================

        Decision decision = unit.mission().permissionToAttack(unit);
        if (decision.notIndifferent()) {
            return decision.toTrueOrFalse();
        }

        // =========================================================

        if (unit.looksIdle() && unit.noCooldown()) {
            return true;
        }

        if (unit.lastActionLessThanAgo(70, Actions.RUN_RETREAT)) {
            return false;
        }

        boolean shouldRetreat = unit.shouldRetreat();
        if (unit.isMelee() && shouldRetreat) {
            return false;
        }

        if (
            unit.isZergling()
                && (
                (Enemy.protoss() && unit.hp() <= 19) || shouldRetreat
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

    public boolean canAttackEnemiesNow() {
        if (reasonNotToAttack == null) {
            return true;
        }

        return defineEnemyToAttackFor() != null;
    }

    public String canAttackEnemiesNowString() {
        return "(" + (canAttackEnemiesNow()
            ? "v"
            : "DONT-" + reasonNotToAttack)
            + ")";
    }

    // =========================================================

    private boolean allowedToAttack() {
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

    private AUnit defineEnemyToAttackFor() {
        return cache.get(
            "defineEnemyToAttackFor",
            0,
            () -> {
                reasonNotToAttack = null;

//                if (!allowedToAttack()) {
//                    return null;
//                }

                AUnit enemy = ATargeting.defineBestEnemyToAttackFor(unit, MAX_DIST_TO_ATTACK);
//                System.out.println("enemy = " + enemy);

                if (enemy == null) {
                    return null;
                }
                if (!isValidTargetAndAllowedToAttackUnit(enemy)) {
//                    System.out.println("Not allowed to attack: " + enemy + " (" + reasonNotToAttack + ")");
                    return null;
                }

                return enemy;
            }
        );
    }

//    public  boolean shouldNotAttack() {
////        if (AAvoidUnits.shouldAvoidAnyUnit()) {
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

    private boolean isValidTargetAndAllowedToAttackUnit(AUnit target) {
        if (target == null || target.position() == null) {
            return false;
        }

        if (!missionAllowsToAttackEnemyUnit(target)) {
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

    private boolean missionAllowsToAttackEnemyUnit(AUnit enemy) {
        return unit.mission() == null
            || (unit.isTank() && unit.noCooldown())
            || (unit.isWraith() && unit.isHealthy() && !enemy.isCombatBuilding())
            || unit.mission().allowsToAttackEnemyUnit(unit, enemy);
//            || (unit.isRanged() && enemy.isMelee());
    }

}
