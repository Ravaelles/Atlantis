package atlantis.units;

import atlantis.architecture.Manager;
import atlantis.game.AGame;
import atlantis.units.select.Select;

public class UnitStateManager extends Manager {
    public static final int UPDATE_UNIT_POSITION_EVERY_FRAMES = 30;
    private int timeNow;
    private boolean shouldUpdatePosition;

    public UnitStateManager(AUnit unit) {
        super(unit);

        timeNow = AGame.now();
        shouldUpdatePosition = AGame.everyNthGameFrame(UPDATE_UNIT_POSITION_EVERY_FRAMES);
    }

    @Override
    protected Manager handle() {
        if (shouldUpdatePosition) {
            unit._lastX = unit.x();
            unit._lastY = unit.y();
        }

        if (unit.isAttackFrame()) {
            unit._lastAttackFrame = timeNow;
//            APainter.paintCircleFilled(unit, 8, Color.Yellow);
//            if (unit.isFirstCombatUnit()) {

//            }
        }

        if (unit.isAttackingOrMovingToAttack()) {
            unit._lastAttackOrder = timeNow;
        }

        unit._lastCooldown = unit.cooldownRemaining();

        if (unit.isStartingAttack() && unit.cooldownRemaining() > unit._lastCooldown) {
//            APainter.paintCircleFilled(unit, 8, Color.Orange);
            unit._lastFrameOfStartingAttack = timeNow;
//            if (unit.isFirstCombatUnit()) {

//            }
        }

        unit._lastHitPoints.add(unit.hp());

        if (unit.isStartingAttack()) {
            unit._lastStartedAttack = timeNow;
//            if (unit.isFirstCombatUnit()) {

//            }
        }

        AUnit _oldLastTargetToAttack = unit._lastTargetToAttack;
        unit._lastTargetToAttack = unit.isAttackingOrMovingToAttack() ? unit.target() : null;
        if (unit.target() != null && !unit.target().equals(_oldLastTargetToAttack)) {
            unit._lastTargetToAttackAcquired = timeNow;
        }

        if (unit.isUnderAttack(3)) {
            unit._lastUnderAttack = timeNow;

            AUnit enemy = Select.enemy().nearestTo(unit);



        }
//        if (unit.id() == Select.ourCombatUnits().first().getID()) {

//                    + unit._lastAttackOrder + " // " + unit._lastAttackFrame + " // " + unit._lastStartingAttack);
//        }

        return null;
    }
}
