package atlantis.combat.micro.avoid.buildings.protoss;

import atlantis.combat.eval.protoss.ProtossEvaluateAgainstCombatBuildings;
import atlantis.combat.micro.avoid.buildings.AvoidCombatBuildingKeepFar;
import atlantis.decisions.Decision;
import atlantis.game.A;
import atlantis.information.generic.Army;
import atlantis.units.AUnit;
import atlantis.units.select.Count;

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

        double minDist = minDist(unit);
        
        if (unit.distTo(combatBuilding) >= minDist) return Decision.FALSE("FarEnough");

        if (Count.ourCombatUnits() <= 7) return Decision.TRUE("FewUnits");
        if (A.supplyUsed() < 140 && Army.strength() <= 250) return Decision.TRUE("TooWeak");
//        if (A.s <= 60 * 9 && Count.ourCombatUnits() <= 10 && EnemyInfo.combatBuildingsAntiLand() >= 2) {
//            return Decision.TRUE;
//        }

        return ProtossEvaluateAgainstCombatBuildings.chancesLookGood(unit, combatBuilding)
            ? Decision.FALSE("GoodChances")
            : Decision.TRUE("BadChances");

//        return Decision.INDIFFERENT;
    }

    private static double minDist(AUnit unit) {
        if (AvoidCombatBuildingKeepFar.shouldKeepFar()) return AvoidCombatBuildingKeepFar.DIST(unit);

//        return Count.ourCombatUnits() <= 8 ? 8.4 : 14;
        return Army.strength() >= 200 ? 8.4 : 14;
    }

//    private static boolean looksStrong(AUnit unit, AUnit combatBuilding) {
//        int enemies = 1 + unit.enemiesNear().combatUnits().countInRadius(9, unit);
//        int ours = combatBuilding.enemiesNear().combatUnits().size();
//
//        return ours - 3 >= enemies && ((double) ours / enemies >= 1.1);
//    }
}
