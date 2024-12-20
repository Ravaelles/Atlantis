package atlantis.combat.micro.attack;

import atlantis.architecture.Manager;
import atlantis.combat.squad.Squad;
import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.actions.Actions;
import atlantis.units.select.Select;
import atlantis.util.We;
import atlantis.util.log.ErrorLog;

public class ProcessAttackUnit extends Manager {
    public ProcessAttackUnit(AUnit unit) {
        super(unit);
    }

    public boolean processAttackOtherUnit(AUnit target) {
//        A.printStackTrace("ProcessAttackUnit.processAttackOtherUnit() " + unit.idWithHash());

        if (target == null) {
//            ErrorLog.printMaxOncePerMinute(unit.type() + " AttackUnit got null target");
            ErrorLog.printMaxOncePerMinutePlusPrintStackTrace(unit.type() + " AttackUnit got null target");
            return false;
        }
        if (target.hp() <= 0) {
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

        if (handleMoveNextToTanksWhenAttackingThem(target)) return true;

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
//        if (unit.isRanged() && unit.lastActionMoreThanAgo(10, Actions.HOLD_POSITION)) {
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

    //    private  double distBonus(AUnit target) {
//        if (unit.isOtherUnitFacingThisUnit(target) && (target.isMoving() || target.isAttacking())) {
//            return -1.6;
//        }
//
//        return -0.5;
//    }

    private boolean confirmAttack(AUnit target) {
        return unit.attackUnit(target);
    }

    // =========================================================

    private boolean handleMoveNextToTanksWhenAttackingThem(AUnit enemy) {
        if (!enemy.isTank()) return false;
        if (We.terran()) return false;

        int count = Select.all().inRadius(0.4, unit).exclude(unit).exclude(enemy).count();
        if (
            !unit.isAir()
                && !unit.is(
                AUnitType.Terran_Siege_Tank_Siege_Mode,
                AUnitType.Terran_Siege_Tank_Tank_Mode,
                AUnitType.Protoss_Archon,
                AUnitType.Protoss_Reaver
            )
                && (enemy.distToMoreThan(unit, unit.isMelee() ? 0.8 : 1.15))
                && Select.all().inRadius(0.4, unit).exclude(unit).exclude(enemy).atMost(2)
                && (unit.isMelee() || Select.all().inRadius(0.7, enemy).exclude(unit).exclude(enemy).atMost(3))
        ) {
            if (unit.isRanged() && Select.enemy().tanksSieged().inRadius(12.2, unit).isEmpty()) return false;

            if (unit.attackUnit(enemy)) {
                unit.setTooltip("Soyuz" + A.dist(enemy, unit) + "/" + count);
                return true;
            }

//            if (unit.move(enemy, Actions.MOVE_ATTACK, "Soyuz" + A.dist(enemy, unit) + "/" + count, false)) {
//                return true;
//            }
        }

        return false;
    }

}
