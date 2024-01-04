package starengine.sc_logic;

import atlantis.game.A;
import atlantis.units.interrupt.UnitAttackWaitFrames;
import atlantis.util.log.ErrorLog;
import starengine.units.state.EngineUnitState;
import tests.unit.FakeUnit;

public class ProcessAttackUnit {
    public static boolean update(FakeUnit unit) {
        if (!unit.isAttacking()) return false;

        if (!unit.hasTarget()) {
            ErrorLog.printErrorOnce("Unit " + unit + " is attacking but has no target");
            return false;
        }

        FakeUnit target = (FakeUnit) unit.target();
        if (unit.distTo(target) > unit.weaponRangeAgainst(target)) {
            return ProcessMoveUnit.update(unit);
        }

        return processAttackUnit(unit, target);
    }

    private static boolean processAttackUnit(FakeUnit unit, FakeUnit target) {
        unit.lastCommand = "AttackUnit";

        if (!unit.previousState.equals(EngineUnitState.ATTACKING)) {
            return isAttackIsNotPending(unit, target);
        }

        return isAttackIsPending(unit);
    }

    private static boolean isAttackIsPending(FakeUnit unit) {
        if (isInReadyState(unit)) {
            return inReadyState(unit);
        }

        if (isInStartingAttackState(unit)) {
            return inStartingAttackState(unit);
        }

        if (isInAttackFrameState(unit)) {
            return inAttackFrameState(unit);
        }

        ErrorLog.printErrorOnce("Unknown attack state: " + unit.attackState + " for unit " + unit);
        return false;
    }

    // =========================================================

    private static boolean isInAttackFrameState(FakeUnit unit) {
        int startedAgo = attackStartedAgo(unit);

        return startedAgo > UnitAttackWaitFrames.stopFrames(unit.type())
            && startedAgo <= UnitAttackWaitFrames.effectiveStopFrames(unit.type());

//        return attackStartedAgo(unit) > UnitAttackWaitFrames.effectiveStopFrames(unit.type());
    }

    private static boolean isInStartingAttackState(FakeUnit unit) {
        return attackStartedAgo(unit) <= UnitAttackWaitFrames.stopFrames(unit.type());
//        return attackStartedAgo(unit) <= UnitAttackWaitFrames.effectiveStopFrames(unit.type());
    }

    private static boolean isInReadyState(FakeUnit unit) {
//        return false;
        return attackStartedAgo(unit) <= 1;
    }

    // =========================================================

    private static void attackHasEnded(FakeUnit unit) {
        unit.attackState = AttackState.READY;
        unit.attackStartedAt = -1;

        CreateEnemyHit.createHit(unit, unit.target);
    }

    private static boolean inAttackFrameState(FakeUnit unit) {
        unit.attackState = AttackState.ATTACK_FRAME;
//        A.println(unit + " in #ATTACK_FRAME# state, cooldown = " + unit.cooldown + ", attackState = " + unit.attackState);

        attackHasEnded(unit);

        return true;
    }

    private static boolean inStartingAttackState(FakeUnit unit) {
        unit.attackState = AttackState.STARTING_ATTACK;

        if (attackStartedAgo(unit) == 1 + UnitAttackWaitFrames.stopFrames(unit.type())) {
            unit.injectCooldown();
        }

//        A.println(unit + " in SA state, cooldown = " + unit.cooldown + ", attackState = " + unit.attackState);
        return true;
    }

    private static boolean inReadyState(FakeUnit unit) {
        unit.attackState = AttackState.READY;
//        A.println(unit + " in READY state, cooldown = " + unit.cooldown + ", attackState = " + unit.attackState);
        return true;
    }

    // =========================================================

    private static int attackStartedAgo(FakeUnit unit) {
        return A.now() - unit.attackStartedAt;
    }

    private static boolean isAttackIsNotPending(FakeUnit unit, FakeUnit target) {
        discardPreviousAttackAndStartNewOne(unit, target);
        return true;
    }

    private static void discardPreviousAttackAndStartNewOne(FakeUnit unit, FakeUnit target) {
        unit.state = EngineUnitState.ATTACKING;
        unit.attackState = AttackState.READY;
        unit.attackStartedAt = A.now();
        unit.previousState = EngineUnitState.ATTACKING;
        unit.target = target;
    }
}
