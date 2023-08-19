package atlantis.production.constructing.position.conditions;

import atlantis.information.strategy.GamePhase;
import atlantis.map.choke.AChoke;
import atlantis.map.choke.Chokes;
import atlantis.map.position.APosition;
import atlantis.production.constructing.position.AbstractPositionFinder;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;

public class TooCloseToBunker {
    public static boolean isTooCloseToBunker(AUnitType building, APosition position) {
        if (building.isBase() || building.isMissileTurret()) return false;

        double minDist = 3.8;
        if (GamePhase.isLateGame()) {
            minDist = 2.5;
        }
        if (building.isSupplyDepot()) {
            minDist = 6;
        }

        for (AUnit bunker : Select.ourBuildingsWithUnfinished().ofType(AUnitType.Terran_Bunker).list()) {
            double distToBunker = bunker.distTo(position);
            if (distToBunker <= minDist) {
                AbstractPositionFinder._CONDITION_THAT_FAILED = "Too close to bunker (" + distToBunker + ")";
                return true;
            }
        }

        return false;
    }
}