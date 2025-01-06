package atlantis.production.constructing.position.conditions;

import atlantis.game.A;
import atlantis.map.position.APosition;
import atlantis.production.constructing.position.AbstractPositionFinder;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import atlantis.util.We;

public class TooCloseToTerranBase {
    public static boolean isTooCloseToBase(AUnitType building, APosition position) {
        if (!We.terran()) return false;
        if (building.isGasBuilding()) return false;

        double minDistToBase = 3;
        if (building.isSupplyDepot()) minDistToBase = 4;
        if (building.isBase()) minDistToBase = 10;
        if (A.supplyTotal() <= 10) minDistToBase = 3;

        if (building.isCombatBuilding()) minDistToBase = 1;

        AUnit base = Select.ourBasesWithUnfinished().inRadius(minDistToBase, position).nearestTo(position);
        if (base == null) return false;

        if (
            base.translateByTiles(3, 1).distTo(position) <= minDistToBase
                || base.distTo(position) <= minDistToBase
        ) {
            AbstractPositionFinder._STATUS = "Too close to base";
            return true;
        }

        return false;
    }
}
