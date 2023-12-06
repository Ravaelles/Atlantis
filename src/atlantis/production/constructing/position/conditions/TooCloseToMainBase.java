package atlantis.production.constructing.position.conditions;

import atlantis.game.A;
import atlantis.map.position.APosition;
import atlantis.production.constructing.position.AbstractPositionFinder;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import atlantis.util.We;

public class TooCloseToMainBase {
    public static boolean isTooCloseToMainBase(AUnitType building, APosition position) {
        if (We.protoss() && A.supplyTotal() <= 10) return false;
        if (building.isCombatBuilding()) return false;

        AUnit base = Select.main();

//        APainter.paintCircle(position, 10, Color.Green);
        if (base != null) {
            int minDistFromBase = We.terran() ? 3 : (We.zerg() ? 3 : 0);
            if (base.translateByTiles(minDistFromBase, 0).distTo(position) <= 3.5) {
                //            APainter.paintCircle(position, 10, Color.Red);
                AbstractPositionFinder._CONDITION_THAT_FAILED = "Too close to main base";
                return true;
            }
        }

        return false;
    }
}