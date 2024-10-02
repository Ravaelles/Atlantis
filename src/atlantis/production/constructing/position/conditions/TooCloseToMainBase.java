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
        if (!We.terran()) return false;
        if (building.isCombatBuilding()) return false;

        AUnit base = Select.main();

//        APainter.paintCircle(position, 10, Color.Green);
        if (base != null) {
            double minDistFromBase = getMinDistFromBase();
            if (base.translateByTiles(2, 0).distTo(position) <= minDistFromBase) {
                return failed("Too close to main base");
            }
        }

        return false;
    }

    private static double getMinDistFromBase() {
        if (We.protoss()) return 1;

        return 3;
    }

    private static boolean failed(String reason) {
        AbstractPositionFinder._CONDITION_THAT_FAILED = reason;
        return true;
    }
}