package atlantis.production.constructing.position.conditions;

import atlantis.game.A;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.map.region.MainRegion;
import atlantis.production.constructing.position.AbstractPositionFinder;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.util.We;

public class IsProbablyInAnotherRegion {
    public static boolean differentRegion(AUnit builder, AUnitType building, APosition position, HasPosition nearTo) {
        if (handleIsBadRegionForFirstBunker(building, position)) return true;

        if (building.isBase() || building.isCombatBuilding()) return false;

        if (
            nearTo != null
                && (A.supplyTotal() <= 50 || building.isCombatBuilding())
                && !nearTo.regionsMatch(position)
        ) {
            AbstractPositionFinder._CONDITION_THAT_FAILED = "Region mismatch";
            return true;
        }

        double groundDistance = builder.position().groundDistanceTo(position);

        boolean result = groundDistance >= 30
            && builder.distTo(position) * 2 >= groundDistance
            && position.regionsMatch(nearTo);

        if (result) {
            AbstractPositionFinder._CONDITION_THAT_FAILED = "Probably in another region";
        }

        return result;
    }

    private static boolean handleIsBadRegionForFirstBunker(AUnitType building, APosition position) {
        if (!We.terran() || !building.isBunker()) return false;
        if (Count.bunkers() >= 1) return false;

//        System.err.println("MISMATCH = " + !position.regionsMatch(MainRegion.mainRegion()));
        if (!position.regionsMatch(MainRegion.mainRegion())) {
            AbstractPositionFinder._CONDITION_THAT_FAILED = "Bad region for first bunker";
            return true;
        }

        return false;
    }
}
