package atlantis.combat.micro.attack;

import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.actions.Actions;
import atlantis.units.select.Select;
import atlantis.util.We;

public class MoveNextToTankWhenAttackingThem {
    public static boolean check(AUnit unit, AUnit enemy) {
        if (!enemy.isTankSieged()) return false;
        if (We.terran()) return false;
        if (unit.cooldown() <= 6) return false;

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

//            if (unit.attackUnit(enemy)) {
            if (unit.move(enemy, Actions.MOVE_ENGAGE, "Soyuz")) {
                unit.setTooltip("Soyuz" + unit.distToDigit(enemy) + "/" + count);
                return true;
            }

//            if (unit.move(enemy, Actions.MOVE_ATTACK, "Soyuz" + A.dist(enemy, unit) + "/" + count, false)) {
//                return true;
//            }
        }

        return false;
    }
}
