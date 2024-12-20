package atlantis.combat.micro.attack.enemies;

import atlantis.architecture.Manager;

import atlantis.combat.micro.attack.ProcessAttackUnit;
import atlantis.combat.squad.Squad;
import atlantis.combat.targeting.basic.ATargeting;
import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.units.select.Select;
import atlantis.util.cache.Cache;
import atlantis.util.log.ErrorLog;

public class AttackNearbyEnemies extends Manager {
    //    private static Cache<AUnit> cache = new Cache<>();
//    private static Cache<Object> cacheObject = new Cache<>();
    public static String reasonNotToAttack;
    private static ProcessAttackUnit processAttackUnit;
    private final AllowedToAttack allowedToAttack;
//    private AUnit targetToAttack;

    // =========================================================

    public AttackNearbyEnemies(AUnit unit) {
        super(unit);
        processAttackUnit = (new ProcessAttackUnit(unit));
        allowedToAttack = (new AllowedToAttack(this, unit));
    }

    public static double maxDistToAttack(AUnit unit) {
        return unit.isAir() ? 876 : 765;
    }

    // =========================================================

    @Override
    public boolean applies() {
        return (new AttackNearbyEnemiesApplies(unit)).applies();
    }

    @Override
    public Manager handle() {
//        if (unit.woundHp() >= 16 && unit.lastUnderAttackLessThanAgo(55)) {
//            A.printStackTrace("Wounded, so why attacking?\n" + parentsStack());
//            unit.managerLogs().print();
//        }

//        if (ShouldRetreat.shouldRetreat(unit)) return null;

//        if (unit.isAttacking() && unit.lastActionLessThanAgo(2)) return usedManager(this);

//        targetToAttack = defineBestEnemyToAttack(unit);
//        if (targetToAttack == null || targetToAttack.hp() <= 0) {
////            ErrorLog.printMaxOncePerMinute(unit.type() + " targetToAttack NULL or DEAD " + targetToAttack);
//            return null;
//        }

//        System.err.println("@ " + A.now() + " - " + unit + " ATTACK " + targetToAttack);
//        if (targetToAttack.isOverlord()) {
//            A.printStackTrace("Overlord targetToAttack");
//            unit().managerLogs().print();
//            unit().manager().printParentsStack();
//        }

        if (handleAttackNearEnemyUnits()) {
            if (unit.isAttacking() && (unit.target() == null || unit.target().hp() <= 0)) {
                String error = unit + " handleAttackNearEnemyUnits got " + unit.target();

                if (unit.target() == null) ErrorLog.printMaxOncePerMinute(error);

                if (A.isUms()) {
                    System.err.println("Current manager: " + unit.manager());
                    unit.managerLogs().print();
                    A.printStackTrace(error);
                }
                return null;
            }
            return usedManager(this);
        }

        return null;
    }

    private void why() {
//        if (unit.combatEvalRelative() < 1) {
        if (unit.isWounded()) {
            A.printStackTrace("Why is this unit attacking? " + unit);
        }
    }

//    private Manager dedicatedManager() {
////        if (unit.isWraith()) return new AttackAsWraith(unit);
//
//        return null;
//    }

//    private boolean justHandledRecently() {
//        return unit.lastActionLessThanAgo(5, Actions.ATTACK_UNIT)
//            || unit.lastActionLessThanAgo(5, Actions.MOVE_ATTACK);
//    }

    /**
     * Selects the best enemy unit and issues attack order.
     *
     * @return <b>true</b> if unit has found valid target and is currently busy with either starting
     * an attack or just attacking the enemy<br />
     * <b>false</b> if no valid enemy to attack could be found
     */
    public boolean handleAttackNearEnemyUnits() {
//        return (boolean) cacheObject.getIfValid(
//            "handleAttackNearEnemyUnits: " + unit.id(),
//            3,
//            () -> {
//                if (true) return false; // Temp disable attacking

        if (!applies()) return false;

        AUnit target = this.defineBestEnemyToAttack(unit);
        if (target == null) return false;

        if (!unit.mission().allowsToAttackEnemyUnit(unit, target)) return false;
        if (!allowedToAttack.canAttackNow()) return false;

        // =========================================================

//                why();

        if (!unit.canAttackTarget(target) || !unit.isAlive()) {
//                    ErrorLog.printMaxOncePerMinute(unit.type() + " can't attack " + target);
//                    ErrorLog.printMaxOncePerMinutePlusPrintStackTrace(unit.type() + " can't attack " + target);
            return false;
        }

        // =========================================================

        if (unit.mission().allowsToAttackEnemyUnit(unit, target)) {
//            if (target.isOverlord()) A.printStackTrace("THAT OVERLORD targetToAttack " + target);

            return processAttackUnit.processAttackOtherUnit(target);
        }

        return false;
//            });
    }

//    public String canAttackEnemiesNowString() {
//        return allowedToAttack.canAttackEnemiesNowString();
//    }

    // =========================================================

    protected AUnit defineBestEnemyToAttack(AUnit unit) {
//        return cache.getIfValid(
//            "defineBestEnemyToAttack:" + unit.id(),
//            5,
//            () -> {
        reasonNotToAttack = null;

        AUnit enemy = bestTargetToAttack();

        if (enemy == null) {
            return null;
        }

        if (enemy != null && !unit.hasWeaponToAttackThisUnit(enemy)) {
            ErrorLog.printMaxOncePerMinute(unit.type() + " has no weapon to attack " + enemy);
            enemy = null;
        }

        if (!allowedToAttack.isValidTargetAndAllowedToAttackUnit(enemy)) {
            return null;
        }

        return enemy;
//            }
//        );
    }

    private AUnit fallbackToSquadLeaderTarget() {
        AUnit leader = unit.squadLeader();
        if (leader == null || unit.equals(leader)) return null;

        Squad squad = unit.squad();
        if (squad != null) {
            return squad.targeting().lastTargetIfAlive();
        }

        return null;
//        return (new AttackNearbyEnemies(leader)).defineBestEnemyToAttack(leader);
    }

    protected AUnit bestTargetToAttack() {
        return ATargeting.defineBestEnemyToAttack(unit);
    }

    @Override
    public String toString() {
        String target = unit.target() == null ? "NULL_TARGET" : unit.target().type().name();
        return super.toString() + "(" + target + ")";
    }

    public static void clearCache() {
//        cache.clear();
//        cacheObject.clear();
    }
}
