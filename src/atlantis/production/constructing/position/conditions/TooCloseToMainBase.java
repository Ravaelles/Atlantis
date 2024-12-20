package atlantis.production.constructing.position.conditions;

import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.production.constructing.position.AbstractPositionFinder;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import atlantis.util.We;

public class TooCloseToMainBase {
    public static boolean isTooCloseToMainBase(AUnitType building, APosition position) {
        if (!We.terran()) return false;
        if (building.isCombatBuilding()) return false;

        HasPosition base = Select.main();

//        APainter.paintCircle(position, 10, Color.Green);
        if (base != null) {
            double minDistFromBase = minDistFromBase();

            if (We.terran()) base = base.translateByTiles(2, 0);

            if (base.distTo(position) <= minDistFromBase) {
                return failed("Too close to main base");
            }
        }

        return false;
    }

    private static double minDistFromBase() {
        if (We.terran()) return 3;
        if (We.protoss()) return 0.6;
        return 0.6;
    }

    private static boolean failed(String reason) {
        AbstractPositionFinder._CONDITION_THAT_FAILED = reason;
        return true;
    }
}