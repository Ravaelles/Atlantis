package atlantis.units;

import atlantis.architecture.Manager;
import atlantis.combat.squad.Squad;
import atlantis.game.AGame;

public class UnitStateManager extends Manager {
    private int timeNow;

    public UnitStateManager(AUnit unit) {
        super(unit);

        timeNow = AGame.now();
    }

    @Override
    protected Manager handle() {
        rememberLastPositionAndLastPositionChange();

        if (unit.isAttackFrame()) {
//            System.err.println("%%%%%%%%% ATTACK FRAME - " + timeNow);
            unit._lastAttackFrame = timeNow;
//            APainter.paintCircleFilled(unit, 8, Color.Yellow);
//            if (unit.isFirstCombatUnit()) {

//            }
        }

        if (unit.isAttackingOrMovingToAttack()) {
            unit._lastAttackOrder = timeNow;
        }

        unit._lastCooldown = unit.cooldownRemaining();

        if (unit.isStartingAttack()) {
//            APainter.paintCircleFilled(unit, 8, Color.Orange);
            if (unit.cooldownRemaining() > unit._lastCooldown) {
                unit._lastFrameOfStartingAttack = timeNow;
            }
//            System.err.println("@@@@@@@@@@@@@@@@@ UPDATED STARTING ATTACK - " + timeNow);
//            if (unit.isFirstCombatUnit()) {

//            }
        }

        unit._lastHitPoints.add(unit.hp());

        if (unit.isStartingAttack()) {
            unit._lastStartedAttack = timeNow;
        }

        AUnit _oldLastTargetToAttack = unit._lastTarget;
        unit._lastTarget = unit.isAttackingOrMovingToAttack() ? unit.target() : null;

        if (unit.target() != null && !unit.target().equals(_oldLastTargetToAttack)) {
            unit._lastTargetToAttackAcquired = timeNow;
            unit._lastTargetType = unit.target().type();
        }

        Squad squad = unit.squad();

        if (unit.isUnderAttack(3)) {
            unit._lastUnderAttack = timeNow;
            if (squad != null) {
                squad.markLastUnderAttackNow();
            }

            if (unit.isUnderAttack(2)) {
                unit.increaseHitCount();
//                System.err.println("@ " + A.now() + " - " + unit.typeWithUnitId() + " - UNDER ATTACK - " + unit.hitCount());
            }
        }

        if (unit.isAttacking()) {
            if (squad != null) {
                squad.markLastAttackedNow();
            }
        }

        return null;
    }

    private void rememberLastPositionAndLastPositionChange() {
        if (unit._lastX != unit.x() || unit._lastY != unit.y()) {
            unit._lastPositionChanged = timeNow;

            unit._lastX = unit.x();
            unit._lastY = unit.y();
        }
    }
}
