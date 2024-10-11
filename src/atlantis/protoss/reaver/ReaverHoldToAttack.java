package atlantis.protoss.reaver;

import atlantis.architecture.Manager;
import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Selection;
import bwapi.Color;

public class ReaverHoldToAttack extends Manager {
    public ReaverHoldToAttack(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (!unit.isAttacking() && !unit.isMoving()) return false;
        if (unit.isStartingAttack()) return false;
        if (unit.isAttackFrame()) return false;
//        if (unit.isHoldingPosition()) return false;

        for (AUnit cb : unit.enemiesNear().combatBuildingsAntiLand().list()) {
            unit.paintLine(cb, Color.Orange);
            cb.paintTextCentered(cb.translateByTiles(0, 1.5), A.digit(unit.distTo(cb)), Color.Orange);
        }

        AUnit target = unit.target();
        if (target == null) return false;
        double dist = unit.distTo(target);
//        System.err.println(target.type() + " = " + dist);
        unit.setTooltip(target.type() + " = " + A.digit(dist));
        if (dist >= 7.91) return false;
        if (dist <= 6.6) return false;
//        if (dist <= 6.75) {
//            A.errPrintln("@ " + A.now() + " - ENFORCE HOLDING " + unit.typeWithUnitId() + " (" + A.digit(dist) + ")");
//            return true;
//        }

//        if (dist <= 6.78 && !unit.isRunning() && unit.lastPositionChangedLessThanAgo(120)) {
        if (dist <= 6.78) {
            A.errPrintln("@ " + A.now() + " - force hold " + unit.typeWithUnitId() + " (" + A.digit(dist) + ")");
            A.errPrintln(unit.isTargetInWeaponRangeAccordingToGame(unit.target()));
            A.errPrintln(unit.isTargetInWeaponRangeAccordingToGame());
            A.errPrintln("EN = " + unit.target().isTargetInWeaponRangeAccordingToGame());
            A.errPrintln(unit.canAttackTargetWithBonus(unit.target(), 0));
            A.errPrintln(unit.canAttackTargetWithBonus(unit.target(), 0.5));
            return true;
        }

        return false;
//        return unit.lastActionMoreThanAgo(12, Actions.HOLD_POSITION);
    }

    @Override
    public Manager handle() {
        System.err.println("@ " + A.now() + " - " + unit.typeWithUnitId() + " - HoldToAttack");
        unit.holdPosition("HoldToAttack");
        return usedManager(this);
    }
}
