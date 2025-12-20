package atlantis.units.interrupt;

import atlantis.architecture.Manager;
import atlantis.decisions.Decision;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.util.We;
import bwapi.UnitCommandType;

import static atlantis.units.actions.Actions.HOLD_TO_SHOOT;
import static atlantis.units.actions.Actions.MOVE_AVOID;

public class ContinueAttack extends Manager {
    public ContinueAttack(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (!unit.isAttacking()) return false;
        if (!unit.isOur()) return false;
        if (!unit.hasValidTarget()) return false;
//        if (!unit.isAttacking()) return false;
//        if (!unit.isCommand(UnitCommandType.Attack_Unit)) return false;

        if (PreventAttacksInMissionDefend.prevent(unit)) return false;
        if (PreventAttacksDuringRetreat.prevent(unit)) return false;

        if (unit.attackState().pending()) {
            return t("PendingAttackState");
        }

        if (unit.attackState().finishedShooting() && unit.cooldown() >= unit.cooldownAbsolute() / 2) {
            return false;
        }

        if (unit.hp() <= 60 && !unit.isTargetInWeaponRangeAccordingToGame()) return false;
        if (unit.woundPercent() >= 50 && unit.cooldown() >= 20) return false;

        if (!unit.attackState().finishedShooting() && unit.isTargetInWeaponRangeAccordingToGame()) {
            return t("NotFinishedShooting");
        }

        if (unit.cooldown() >= 15) return false;

        if (unit.hasTarget() && unit.target().isTankSieged()) return false;

//        if (!unit.isTargetInWeaponRangeAccordingToGame()) return false;
        if (unit.shotSecondsAgo(3)) {
            if (unit.lastCommandIssuedAgo() > unit.cooldownAbsolute()) return false;
            if (unit.cooldown() >= 15) return false;
        }

        if (true) return unit.isTargetInWeaponRangeAccordingToGame();

        if (unit.lastCommandIssuedAgo() >= 100) return false;

//        else {
//            if (!unit.isTargetInWeaponRangeAccordingToGame()) return false;
//
//            return true;
//        }

//        System.err.println(A.now() + " isAttacking = " + unit.isAttacking() + " / " + unit.u().getOrder() + " / " + unit.u().getOrder().getClass().getSimpleName());
//        if (!unit.isTargetInWeaponRangeAccordingToGame()) return false;

        boolean isHoldingToShoot = unit.isAction(HOLD_TO_SHOOT);
        if (!unit.isAttacking() && !isHoldingToShoot) return false;

        if (unit.isAction(MOVE_AVOID)) return false;

//        if (!unit.shotSecondsAgo(2) && !unit.isTargetInWeaponRangeAccordingToGame()) {
//            return false;
//        }

//        if (unit.hasCooldown() && !unit.attackState().finishedShooting()) {
//        if (!unit.attackState().finishedShooting()) {
//            return t("NotFinishedShooting");
//        }

        // =========================================================

        Decision decision;

        if (We.protoss()) {
            if ((decision = forDragoon()).notIndifferent()) return decision.toBoolean();
            if ((decision = forZealot()).notIndifferent()) return decision.toBoolean();
        }

        if (We.terran()) {
            if ((decision = forMarine()).notIndifferent()) return decision.toBoolean();
        }

        // =========================================================

        if (UnitAttackWaitFrames.unitAlreadyStartedAttackAnimation(unit)) return t("StartedAttackAnimation");

        if (unit.isStartingAttack()) return t("IsStartingAttack");
//        System.out.println(A.now() + " / " + unit.cooldown() + " / AF=" + unit.isAttackFrame());
        if (unit.isAttackFrame()) return t("IsAttackFrame");

//        System.err.println("---- unit.cooldown() = " + unit.cooldown());

//        if (unit.cooldown() >= 10) return false;

//        System.err.println("BBBB unit.cooldown() = " + unit.cooldown());

//        if (unit.meleeEnemiesNearCount(2.9) > 0) return false;

        if (unit.isActiveManager(this.getClass())) {
//            System.err.println("FA = " + unit.lastAttackFrameAgo());

            if (unit.lastAttackFrameMoreThanAgo(24)) {
                return false;
            }

//            System.err.println(unit.lastAttackFrameAgo());
            return unit.isTargetInWeaponRangeAccordingToGame() && t("ActiveManagerNInRange");
        }

//        if (
//            unit.isActiveManager(AttackNearbyEnemies.class)
//                && (unit.lastAttackFrameMoreThanAgo(30) || unit.lastPositionChangedLessThanAgo(10))
//        ) return unit.isTargetInWeaponRangeAccordingToGame();

        return false;
    }

    private Decision forDragoon() {
        /*
         * Warning: Careful here as too quickly stopping attacking will cause Goon to freeze!
         */
        if (!unit.isDragoon()) return Decision.INDIFFERENT;

//        if (unit.cooldown() <= 15) return Decision.FALSE;

//        if (!unit.attackState().finishedShooting() && t("G_NotFinishedShooting")) return Decision.TRUE;

        else return Decision.FALSE;
    }

    private Decision forMarine() {
        if (!unit.isMarine()) return Decision.INDIFFERENT;

//        if (true) return Decision.FALSE;
//        System.out.println(A.now + " - cooldown: " + unit.cooldown() + " / " + unit.cooldownAbsolute());

        if (unit.cooldown() >= 17) {
//            System.err.println("@ " + A.now() + " - DONT - " + unit.typeWithUnitId() + " - " + unit.cooldown());
            return Decision.FALSE;
        }

        if (true) return Decision.FALSE;

        if (
            unit.isAttacking()
                && unit.lastAttackFrameMoreThanAgo(20)
                && unit.lastActionLessThanAgo(30, Actions.ATTACK_UNIT)
//                && !unit.isTarget(Zerg_Lurker)
                && unit.hasValidTarget()
                && unit.isTargetInWeaponRangeAccordingToGame()
        ) {
//            unit.paintCircleFilled(10, Color.Red);
            return Decision.TRUE;
        }

        return Decision.INDIFFERENT;
    }

    private Decision forZealot() {
        if (unit.isZealot()) {
            if (unit.isTargetRanged() && t("Z_TargetRanged")) return Decision.TRUE;
//            System.err.println("unit.cooldown() = " + unit.cooldown());

            if (unit.cooldown() >= 5 && unit.cooldown() <= 22) return Decision.FALSE;

            if (
                unit.isAttacking()
                    && unit.hasValidTarget()
                    && unit.distTo(unit.target()) <= 1.2
                    && t("Z_CloseToTarget")
            ) return Decision.TRUE;
        }

        return Decision.INDIFFERENT;
    }

    public Manager handle() {
//        unit.paintCircleFilled(20, Color.White);

        return usedManager(this);
    }

    private boolean t(String reason) {
//        if (unit.woundHp() >= 10) {
//            System.err.println(reason);
//        }
        return true;
    }
}
