package atlantis.combat.micro;

import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.actions.Actions;
import atlantis.units.select.Select;

public class ProcessAttackUnit {

    public static boolean processAttackUnit(AUnit unit, AUnit target) {
        if (handleMoveNextToTanksWhenAttackingThem(unit, target)) {
            return true;
        }

        if (target.isBase() && unit.distToMoreThan(target, 2.8)) {
            if (unit.move(target, Actions.MOVE_ATTACK, "BaseAttack", false)) {
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
//            double distBonus = distBonus(unit, target);
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
        return confirmAttack(unit, target);
    }

    private static double distBonus(AUnit unit, AUnit target) {
        if (unit.isOtherUnitFacingThisUnit(target) && (target.isMoving() || target.isAttacking())) {
            return -1.6;
        }

        return -0.5;
    }

    private static boolean confirmAttack(AUnit unit, AUnit target) {
        return unit.attackUnit(target);
    }

    // =========================================================

    private static boolean handleMoveNextToTanksWhenAttackingThem(AUnit unit, AUnit enemy) {
        if (!enemy.isTank()) {
            return false;
        }

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
            if (unit.isRanged() && Select.enemy().tanksSieged().inRadius(12.2, unit).isEmpty()) {
                return false;
            }

            if (unit.move(enemy, Actions.MOVE_ATTACK, "Soyuz" + A.dist(enemy, unit) + "/" + count, false)) {
                return true;
            }
        }

        return false;
    }

}
