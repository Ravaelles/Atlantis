package atlantis.combat.micro.attack.enemies;

import atlantis.architecture.Manager;

import atlantis.combat.micro.attack.ProcessAttackUnit;
import atlantis.combat.micro.avoid.always.ProtossAlwaysAvoidEnemy;
import atlantis.combat.squad.Squad;
import atlantis.combat.targeting.generic.ATargeting;
import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.units.interrupt.PreventAttacksDuringRetreat;
import atlantis.units.interrupt.PreventAttacksInMissionDefend;
import atlantis.util.log.ErrorLog;

public class AttackNearbyEnemies extends Manager {
    //    private static Cache<AUnit> cache = new Cache<>();
//    private static Cache<Object> cacheObject = new Cache<>();
    public static String reasonNotToAttack;
    private static ProcessAttackUnit processAttackUnit;
    private final AllowedToAttack allowedToAttack;
    public static String _failReason = "_none_";
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
        if (unit.cooldown() >= 13) return false;
        if (unit.lastCommandIssuedAgo() <= 1) return false;

        if ((new AttackNearbyEnemiesApplies(unit)).applies()) {
            return true;
//            return allowedToAttack.canAttackNow();
        }

        return false;
    }

    @Override
    public Manager handle() {
        if (PreventAttacksInMissionDefend.prevent(unit)) return null;
        if (PreventAttacksDuringRetreat.prevent(unit)) return null;

        _failReason = "";

//        if (unit.isDancingAway() && unit.lastCommandIssuedAgo() <= 2) {
//            A.printStackTrace("Dancing away, no attack for now: " + unit + " / " + getParent());
//        }

//        if (ProtossAttackForbiddenByCohesion.forbiddenToAttack(unit)) {
//            _failReason = "ProtossAttackForbiddenByCohesion";
//            return null;
//        }

        if ((new ProtossAlwaysAvoidEnemy(unit)).applies()) {
            _failReason = "ProtossAlwaysAvoidEnemy";
            return null;
        }
        if (unit.leaderIsRetreating()) {
            _failReason = "leaderIsRetreating";
            return null;
        }

//        printParentsStack();
//        unit.managerLogs().print();
//        unit.log().print();
//        System.out.println(A.now + ", AttackNearbyEnemies for " + unit);

        if (handleAttackNearEnemyUnits()) {
            AUnit target = unit.target();
            if (target == null) {
                _failReason = "nullTarget";
                return null;
            }
            if (target.hp() <= 0) {
                _failReason = "targetHp0,vis:" + target.effVisible() + ",dead:" + target.isDead();
                return null;
            }

//            A.printStackTrace("Why attack now? @" + A.now);
//            printParentsStack();
//            if (!unit.isLeader()) A.printStackTrace("Why attack now? ");
            return usedManager(this);
        }

        _failReason = "generic";
        return null;
    }

    private void why() {
//        if (unit.combatEvalRelative() < 1) {
        if (unit.isWounded()) {
            A.printStackTrace("Why is this unit attacking? " + unit);
        }
    }

    /**
     * Selects the best enemy unit and issues attack order.
     *
     * @return <b>true</b> if unit has found valid target and is currently busy with either starting
     * an attack or just attacking the enemy<br />
     * <b>false</b> if no valid enemy to attack could be found
     */
    public boolean handleAttackNearEnemyUnits() {
//        if (!applies()) return false;

        AUnit target = this.defineBestEnemyToAttack(unit);
        if (target == null) return false;
        if (target.hp() <= 0) {
            ErrorLog.printMaxOncePerMinutePlusPrintStackTrace("Target has hp(" + target.hp() + ") - " + target);
            return false;
        }

        // =========================================================

//        if (unit.distToTarget() >= 8) {
//            System.err.println(A.minSec() + " - " + unit.typeWithUnitId() + " - \"FarFromTarget\" - "
//                + unit.distToTarget() + " / " + unit.target() + " / " + unit.target().hp());
//            PauseAndCenter.on(unit, true);
//        }

//                why();

        if (!unit.canAttackTarget(target) || !unit.isAlive()) {
            ErrorLog.printMaxOncePerMinute(unit.type() + " can't attack " + target);
//                    ErrorLog.printMaxOncePerMinutePlusPrintStackTrace(unit.type() + " can't attack " + target);
            _failReason = "cannotAttackTarget:" + target.typeWithUnitId();
            return false;
        }

        // =========================================================

//            unit.manager().printParentsStack();
//            A.printStackTrace(unit + ": Why ATACC? ");

//            if (target.isOverlord()) A.printStackTrace("THAT OVERLORD targetToAttack " + target);

            //        if (unit.distTo(target) >= 8 && unit.distToTarget() <= 888) {
//            if (unit.distTo(target) >= 8 && unit.enemiesNear(6).notEmpty()) {
//                System.err.println(A.minSec() + " - " + unit.typeWithUnitId() + " --> " + target
//                    + " (hp:" + target.hp() + ") - " + unit.distTo(target));
//                PauseAndCenter.on(unit, true);
//            }

        if (!unit.mission().allowsToAttackEnemyUnit(unit, target)) {
            _failReason = "missionNotAllows:" + target.typeWithUnitId();
            return false;
        }

        if (processAttackUnit.processAttackOtherUnit(target)) {
            return true;
        }

        _failReason = "generic_failed";
        return false;
    }

//    public String canAttackEnemiesNowString() {
//        return allowedToAttack.canAttackEnemiesNowString();
//    }

    // =========================================================

    protected AUnit defineBestEnemyToAttack(AUnit unit) {
        reasonNotToAttack = null;

        AUnit enemy = bestTargetToAttack();
//        System.err.println("ANE enemy = " + enemy);

        if (enemy == null) {
//            if (A.isUms() && unit.enemiesNear().canBeAttackedBy(unit, 4).notEmpty()) {
//                ErrorLog.printMaxOncePerMinute("null enemy to attack for " + unit);
//            }
            return null;
        }
        if (enemy.hp() == 0) {
            if (A.isUms()) ErrorLog.printMaxOncePerMinute("Enemy with no hp to attack for " + unit);
            return null;
        }

        if (!unit.hasWeaponToAttackThisUnit(enemy)) {
            ErrorLog.printMaxOncePerMinute(unit.type() + " has no weapon to attack " + enemy);
            enemy = null;
        }

        if (!allowedToAttack.isValidTargetAndAllowedToAttackUnit(enemy)) {
//            ErrorLog.printMaxOncePerMinute(enemy + " is not valid target for " + unit.type());
            return null;
        }

        return enemy;
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

//    @Override
//    public String toString() {
//        String target = unit.target() == null ? "NULL_TARGET" : unit.target().type().name();
//        return super.toString() + "(" + target + ")";
//    }
}
