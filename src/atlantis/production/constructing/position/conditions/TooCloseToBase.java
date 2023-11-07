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
        if (building.isCombatBuilding() || building.isGasBuilding()) return false;

        int minDistToBase = building.isSupplyDepot() ? (A.supplyTotal() >= 15 ? 8 : 4) : 5;

        return Select.ourBasesWithUnfinished().inRadius(minDistToBase, position).notEmpty();
    }
}