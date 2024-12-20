package atlantis.production.constructing.position.conditions;

import atlantis.game.A;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.production.constructing.position.AbstractPositionFinder;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import atlantis.util.We;

public class TooCloseToMainBase {
    public static boolean isTooCloseToMainBase(AUnitType building, APosition position) {
        if (building.isCombatBuilding()) return false;

        HasPosition base = Select.main();

//        APainter.paintCircle(position, 10, Color.Green);
        if (base != null) {
            double minDistFromBase = minDistFromBase(building);

            if (We.terran()) base = base.translateByTiles(2, 0);

            if (base.distTo(position) <= minDistFromBase) {
                return failed("Too close to main base");
            }
        }

        return false;
    }

    private static double minDistFromBase(AUnitType building) {
        if (We.terran()) return 4;

        if (We.protoss()) {
            return building.isPylon() ? (A.supplyTotal() <= 16 ? 5 : 4) : 5;
        }

        return 0.6;
    }

    private static boolean failed(String reason) {
        AbstractPositionFinder._STATUS = reason;
        return true;
    }
}