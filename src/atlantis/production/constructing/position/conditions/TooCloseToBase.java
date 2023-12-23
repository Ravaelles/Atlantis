package atlantis.production.constructing.position.conditions;

import atlantis.game.A;
import atlantis.information.strategy.GamePhase;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.production.constructing.position.AbstractPositionFinder;
import atlantis.production.requests.AntiLandBuildingCommander;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import atlantis.util.We;

public class TooCloseToBase {
    public static boolean isTooCloseToBase(AUnitType building, APosition position) {
        if (!We.terran()) return false;

        if (building.isGasBuilding()) return false;

        double minDistToBase = building.isSupplyDepot() ? 4 : 3;
        if (A.supplyTotal() <= 10) minDistToBase = 3;

        if (building.isCombatBuilding()) minDistToBase = 1;

        AUnit base = Select.ourBasesWithUnfinished().inRadius(minDistToBase, position).nearestTo(position);
        if (base == null) return false;

        if (
            base.translateByTiles(3, 1).distTo(position) <= minDistToBase
            || base.distTo(position) <= minDistToBase
        ) {
            AbstractPositionFinder._CONDITION_THAT_FAILED = "Too close to base";
            return true;
        }

        return false;
    }
}
