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
            unit.unitState().setLastAttackFrame(timeNow);
//            APainter.paintCircleFilled(unit, 8, Color.Yellow);
//            if (unit.isFirstCombatUnit()) {

//            }
        }

        if (unit.isAttackingOrMovingToAttack()) {
            unit.unitState().setLastAttackOrder(timeNow);
        }

        unit.unitState().setLastCooldown(unit.cooldownRemaining());

        if (unit.isStartingAttack()) {
//            APainter.paintCircleFilled(unit, 8, Color.Orange);
            if (unit.cooldownRemaining() > unit.unitState().getLastCooldown()) {
                unit.unitState().setLastFrameOfStartingAttack(timeNow);
            }
//            System.err.println("@@@@@@@@@@@@@@@@@ UPDATED STARTING ATTACK - " + timeNow);
//            if (unit.isFirstCombatUnit()) {

//            }
        }

        unit.unitState().getLastHitPoints().add(unit.hp());

        if (unit.isStartingAttack()) {
            unit.unitState().setLastStartedAttack(timeNow);
        }

        AUnit _oldLastTargetToAttack = unit.unitState().getLastTarget();
        unit.unitState().setLastTarget(unit.isAttackingOrMovingToAttack() ? unit.target() : null);

        if (unit.target() != null && !unit.target().equals(_oldLastTargetToAttack)) {
            unit.unitState().setLastTargetToAttackAcquired(timeNow);
            unit.unitState().setLastTargetType(unit.target().type());
        }

        Squad squad = unit.squad();

        if (unit.isUnderAttack(3)) {
            unit.unitState().setLastUnderAttack(timeNow);
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
                if (unit.cooldown() + 2 >= unit.cooldownAbsolute()) squad.markLastShotNow();
            }
        }

        return null;
    }

    private void rememberLastPositionAndLastPositionChange() {
        if (unit.unitState().getLastX() != unit.x() || unit.unitState().getLastY() != unit.y()) {
            unit.unitState().setLastPositionChanged(timeNow);

            unit.unitState().setLastX(unit.x());
            unit.unitState().setLastY(unit.y());
        }
    }
}
