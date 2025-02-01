package atlantis.combat.micro.avoid.buildings.protoss;

import atlantis.combat.eval.protoss.ProtossEvaluateAgainstCombatBuildings;
import atlantis.decisions.Decision;
import atlantis.units.AUnit;

public class ShouldAvoidCannonAsProtoss {
    public static Decision shouldAvoid(AUnit unit, AUnit combatBuilding) {
//        if (unit.eval() >= 1.3 && unit.distTo(combatBuilding) <= 9) {
//            if (looksStrong(unit, combatBuilding)) {
//                return Decision.FALSE;
//            }
//        }

        return ProtossEvaluateAgainstCombatBuildings.chancesLookGood(unit, combatBuilding)
            ? Decision.FALSE : Decision.TRUE;

//        return Decision.INDIFFERENT;
    }

//    private static boolean looksStrong(AUnit unit, AUnit combatBuilding) {
//        int enemies = 1 + unit.enemiesNear().combatUnits().countInRadius(9, unit);
//        int ours = combatBuilding.enemiesNear().combatUnits().size();
//
//        return ours - 3 >= enemies
//            && ((double) ours / enemies >= 1.5);
//    }
}
