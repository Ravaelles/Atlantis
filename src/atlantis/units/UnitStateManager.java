package atlantis.units;

import atlantis.architecture.Manager;
import atlantis.combat.micro.generic.Unfreezer;
import atlantis.game.AGame;
import atlantis.units.select.Select;

public class UnitStateManager extends Manager {
    private int timeNow;
    private boolean shouldUpdatePosition;

    public UnitStateManager(AUnit unit) {
        super(unit);

        timeNow = AGame.now();
        shouldUpdatePosition = AGame.everyNthGameFrame(Unfreezer.UPDATE_UNIT_POSITION_EVERY_FRAMES);
    }

    @Override
    public Manager handle() {
        if (shouldUpdatePosition) {
            unit._lastX = unit.x();
            unit._lastY = unit.y();
        }

        if (unit.isAttackFrame()) {
            unit._lastAttackFrame = timeNow;
//            APainter.paintCircleFilled(unit, 8, Color.Yellow);
//            if (unit.isFirstCombatUnit()) {
//                System.out.println("unit._lastAttackFrame = " + unit._lastAttackFrame);
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
//                System.out.println("unit._lastFrameOfStartingAttack = " + unit._lastFrameOfStartingAttack);
//            }
        }

        unit._lastHitPoints.add(unit.hp());

        if (unit.isStartingAttack()) {
            unit._lastStartedAttack = timeNow;
//            if (unit.isFirstCombatUnit()) {
//                System.out.println("unit._lastStartedAttack = " + unit._lastStartedAttack);
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
//            System.out.println("-------------- (" + A.dist(enemy, unit));
//            System.out.println(unit.getPosition());
//            System.out.println(enemy.getPosition());
        }
//        if (unit.id() == Select.ourCombatUnits().first().getID()) {
//            System.out.println(AGame.getTimeFrames() + " ### "
//                    + unit._lastAttackOrder + " // " + unit._lastAttackFrame + " // " + unit._lastStartingAttack);
//        }

        return null;
    }
}
