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

public class TooCloseToBase {
    public static boolean isTooCloseToBase(AUnitType building, APosition position) {
        if (building.isGasBuilding()) return false;

        double minDistToBase = building.isSupplyDepot() ? 4.1 : 3.1;

        if (building.isCombatBuilding()) minDistToBase = 2.4;

        if (Select.ourBasesWithUnfinished().inRadius(minDistToBase, position).notEmpty()) {
            AbstractPositionFinder._CONDITION_THAT_FAILED = "Too close to base";
            return true;
        }

        return false;
    }
}
