package atlantis.combat.micro.avoid.buildings.protoss;

import atlantis.combat.eval.protoss.ProtossEvaluateAgainstCombatBuildings;
import atlantis.decisions.Decision;
import atlantis.game.A;
import atlantis.information.generic.Army;
import atlantis.units.AUnit;
import atlantis.units.select.Selection;

public class ShouldAvoidSunkenAsProtoss {
    public static Decision shouldAvoid(AUnit unit, AUnit combatBuilding) {
//        Selection sunkens = unit.enemiesNear().sunkens();
//
//        if (sunkens.count() >= 2) {
//            Decision decision;
//            if ((decision = forMultipleSunkens()).notIndifferent()) return decision;
//        }
//
//        if (unit.eval() >= 1.1 && unit.distTo(combatBuilding) <= 9) {
//            if (looksStrong(unit, combatBuilding)) {
//                return Decision.FALSE;
//            }
//        }

        if (A.supplyUsed() < 140 && Army.strength() <= 160) return Decision.TRUE;

        return ProtossEvaluateAgainstCombatBuildings.chancesLookGood(unit, combatBuilding)
            ? Decision.FALSE : Decision.TRUE;

//        return Decision.INDIFFERENT;
    }

//    private static boolean looksStrong(AUnit unit, AUnit combatBuilding) {
//        int enemies = 1 + unit.enemiesNear().combatUnits().countInRadius(9, unit);
//        int ours = combatBuilding.enemiesNear().combatUnits().size();
//
//        return ours - 3 >= enemies && ((double) ours / enemies >= 1.1);
//    }
}
