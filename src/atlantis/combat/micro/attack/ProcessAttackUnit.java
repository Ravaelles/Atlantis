package atlantis.combat.micro.attack;

import atlantis.architecture.Manager;
import atlantis.combat.micro.dancing.hold.ProtossAttackHoldToShoot;
import atlantis.combat.state.AttackState;
import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.actions.Actions;
import atlantis.units.select.Select;
import atlantis.util.We;
import atlantis.util.log.ErrorLog;
import bwapi.Color;

public class ProcessAttackUnit extends Manager {
    public ProcessAttackUnit(AUnit unit) {
        super(unit);
    }

    public boolean processAttackOtherUnit(AUnit target) {
//        A.printStackTrace("ProcessAttackUnit.processAttackOtherUnit() " + unit.idWithHash());

//        if (unit.isWounded()) {
//            ErrorLog.debug("Wounded unit attacking " + unit + " / " + target + " / dist: " + unit.distToDigit(target));
//            unit.managerLogs().print("Managers logs");
//            printParentsStack();
//            GameSpeed.pauseGame();
//        }

        if (target == null) {
//            ErrorLog.printMaxOncePerMinute(unit.type() + " AttackUnit got null target");
            ErrorLog.printMaxOncePerMinutePlusPrintStackTrace(unit.type() + " AttackUnit got null target");
            return false;
        }
        if (target.hp() <= 0 && !target.isABuilding() && !unit.isDarkTemplar()) {
//            ErrorLog.printMaxOncePerMinute(
            ErrorLog.printMaxOncePerMinutePlusPrintStackTrace(
                unit.type() + " AttackUnit got target.hp = " + target.hp() + " - " + target.type()
            );
            return false;
        }
        if (!target.hasPosition()) {
            ErrorLog.printMaxOncePerMinute(
                unit.type() + " AttackUnit got target with no position" + target.position() + " " + target.type()
            );
            return false;
        }

//        if (target.isOverlord()) A.printStackTrace("OooOverlord targetToAttack " + target);

        // =========================================================

        if (ProcessAttackUnitAsTank.forTank(this, unit, target)) return true;

        if (target.isFoggedUnitWithKnownPosition()) {
            if (unit.distTo(target) > unit.weaponRangeAgainst(target)) {
//                unit.move(target, Actions.MOVE_ATTACK, "ToFogged", false);
                if (unit.move(target, Actions.MOVE_ATTACK, "ToFogged", false)) {
                    return true;
                }
            }
            return false;
        }

        if (MoveNextToTankWhenAttackingThem.check(unit, target)) return true;

        // Come closer when attacking enemy bases
        if (comeCloserToBuildingsWhenAttackingThem(target)) {
            if (unit.move(target, Actions.MOVE_ENGAGE, "GetClosa", false)) {
                return true;
            }
        }

//        unit.setTooltip("@" + target.name());
//        unit.addLog("Attacking" + target.name());
        unit.addLog("Attacking" + target);

        // Ranged HOLD TO SHOOT
//        if (unit.isRanged() && unit.lastActionMoreThanAgo(10, Actions.MOVE_FORMATION)) {
//            int range = unit.weaponRangeAgainst(target);
//            double dist = unit.distTo(target);
//            double distBonus = distBonus(target);
//            if (
//                dist + distBonus < range
//                    && unit.cooldownRemaining() <= 8
//                    && !unit.isHoldingPosition()
//            ) {
//                unit.addLog("HoldToShoot");
//                unit.holdPosition("HoldToShoot", false);
//                return true;
//            }
//        }

        // Melee
        return confirmAttack(target);
    }

    private boolean comeCloserToBuildingsWhenAttackingThem(AUnit target) {
        return target.isABuilding()
            && unit.cooldown() >= 6
            && !target.isCombatBuilding()
            && unit.distToMoreThan(target, 1.5);
    }

    private boolean confirmAttack(AUnit target) {
        if (ProtossAttackHoldToShoot.holdInsteadAttack(unit, target)) {
            return confirmHoldingToShoot(target);
        }

        return unit.attackUnit(target);
    }

    private boolean confirmHoldingToShoot(AUnit target) {
        //            System.err.println(A.now() + " - HOLD " +
//                "- speed(" + unit.speed() + ") " +
//                "- enemy:" + unit.nearestEnemyDist());

        unit.paintCircle(18, Color.Orange);
        unit.paintCircle(17, Color.Orange);
        unit.paintCircle(16, Color.Orange);

        unit.forceLastTarget(target);
        unit.setAttackState(AttackState.TARGET_ACQUIRED);

        return true;
    }
}
