package atlantis.combat.micro.attack;

import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.actions.Actions;
import atlantis.units.select.Select;
import atlantis.util.We;

public class ProtossMoveNextToTankWhenAttackingThem {
    public static boolean check(AUnit unit, AUnit tank) {
        if (!tank.isTankSieged()) return false;
        if (unit.isAir()) return false;
        if (unit.cooldown() <= 7) return false;
        if (unit.distTo(tank) <= 0.1) return false;
        if (unit.groundDist(tank) > 12.2) return false;
        if (tankIsSurroundedByTooManyEnemies(unit, tank)) return false;

//        private boolean moveComeCloserToTank() {
//            if (!tank.isSieged()) return false;
//
//            return unit.cooldown() >= 7
//                && unit.distTo(tank) >= 0.3
//                && unit.groundDist(tank) <= 11
//                && tankIsSurroundedByTooManyEnemies();
//        }

//        int count = Select.all().inRadius(0.4, unit).exclude(unit).exclude(tank).count();
//        if (
//            !unit.isAir()
//                && !unit.is(
//                AUnitType.Terran_Siege_Tank_Siege_Mode,
//                AUnitType.Terran_Siege_Tank_Tank_Mode,
//                AUnitType.Protoss_Archon,
//                AUnitType.Protoss_Reaver
//            )
//                && (tank.distToMoreThan(unit, unit.isMelee() ? 0.8 : 1.15))
//                && Select.all().inRadius(0.4, unit).exclude(unit).exclude(tank).atMost(2)
//                && (unit.isMelee() || Select.all().inRadius(0.7, tank).exclude(unit).exclude(tank).atMost(3))
//        ) {
//            if (unit.isRanged() && Select.enemy().tanksSieged().inRadius(12.2, unit).isEmpty()) return false;

//            if (unit.attackUnit(tank)) {
            if (unit.move(tank, Actions.MOVE_ENGAGE, "Soyuz")) {
                unit.setTooltip("Soyuz" + unit.distToDigit(tank));
                return true;
            }

//            if (unit.move(tank, Actions.MOVE_ATTACK, "Soyuz" + A.dist(tank, unit) + "/" + count, false)) {
//                return true;
//            }
//        }

        return false;
    }

    private static boolean tankIsSurroundedByTooManyEnemies(AUnit unit, AUnit tank) {
        return tank.enemiesNear().combatUnits().nonTanks().countInRadius(3, unit) >= 4;
    }
}
