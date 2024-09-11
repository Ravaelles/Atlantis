package atlantis.units;

import atlantis.architecture.Manager;
import atlantis.game.AGame;
import atlantis.units.select.Select;

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

        AUnit _oldLastTargetToAttack = unit._lastTargetToAttack;
        unit._lastTargetToAttack = unit.isAttackingOrMovingToAttack() ? unit.target() : null;
        if (unit.target() != null && !unit.target().equals(_oldLastTargetToAttack)) {
            unit._lastTargetToAttackAcquired = timeNow;
        }

        if (unit.isUnderAttack(3)) {
            unit._lastUnderAttack = timeNow;
        }

//            AUnit enemy = Select.enemy().nearestTo(unit);
//        if (unit.id() == Select.ourCombatUnits().first().getID()) {

//                    + unit._lastAttackOrder + " // " + unit._lastAttackFrame + " // " + unit._lastStartingAttack);
//        }

        return null;
    }

    private void rememberLastPositionAndLastPositionChange() {
        if (unit._lastX != unit.x() || unit._lastY != unit.y()) {
            unit._lastPositionChanged = timeNow;
        }

        unit._lastX = unit.x();
        unit._lastY = unit.y();
    }
}
