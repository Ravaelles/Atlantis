package atlantis.production.constructing.position.terran;

import atlantis.information.strategy.GamePhase;
import atlantis.map.position.APosition;
import atlantis.production.constructing.ConstructionRequests;
import atlantis.production.constructing.position.AbstractPositionFinder;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import atlantis.util.We;

public class TooCloseToBunker {
    public static boolean isTooCloseToBunker(AUnitType building, APosition position) {
        if (!We.terran() || building.isBase() || building.isCombatBuilding()) return false;

        double minDist = 3.8;
        if (GamePhase.isLateGame()) minDist = 2.5;
        if (building.isSupplyDepot()) minDist = 6;

        for (AUnit bunker : Select.ourBuildingsWithUnfinished().ofType(AUnitType.Terran_Bunker).list()) {
            double distToBunker = bunker.distTo(position);
            if (distToBunker <= minDist) {
                AbstractPositionFinder._STATUS = "Too close to bunker (" + distToBunker + ")";
                return true;
            }
        }

//        HasPosition nextBunker = TerranBunker.get().nextPosition(position);
//        if (nextBunker != null && nextBunker.distToLessThan(position, 5)) return true;

        if (ConstructionRequests.hasNotStartedNear(AUnitType.Terran_Bunker, position, 5)) {
            AbstractPositionFinder._STATUS = "Has close bunker nearby";
            return true;
        }

        return false;
    }
}
