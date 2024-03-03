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

        if (building.isBase()) return false;
        if (building.isPylon() && A.supplyTotal() >= 60) return false;

//        if (
//            nearTo != null
////                && (A.supplyTotal() <= 50 || building.isCombatBuilding())
//                && !nearTo.regionsMatch(position)
//        ) {
        if (
            !position.hasPathTo(nearTo.position())
                ||
                position.groundDist(nearTo.position()) >= 40
        ) {
            return failed("Region mismatch");
        }
//        }

        return false;

//        double groundDistance = builder.position().groundDistanceTo(position);
//
//        boolean result = groundDistance >= 20
//            && builder.distTo(position) * 1.5 >= groundDistance
//            && position.regionsMatch(nearTo);
//
//        if (result) {
//            AbstractPositionFinder._CONDITION_THAT_FAILED = "Probably in another region";
//        }
//
//        return result;
    }

    private static boolean failed(String reason) {
        AbstractPositionFinder._CONDITION_THAT_FAILED = reason;
        return true;
    }

    private static boolean handleIsBadRegionForFirstBunker(AUnitType building, APosition position) {
        if (!We.terran() || !building.isBunker()) return false;
        if (Count.bunkers() >= 2) return false;

//        System.err.println("MISMATCH = " + !position.regionsMatch(MainRegion.mainRegion()));
        if (!position.regionsMatch(MainRegion.mainRegion())) {
            return failed("Bad region for first bunker");
        }

//        System.out.println("position = " + position.region());
//        System.out.println("MainRegion.mainRegion() = " + MainRegion.mainRegion());
//        System.out.println("position.regionsMatch(MainRegion.mainRegion()) = " + position.regionsMatch(MainRegion.mainRegion()));

        return false;
    }
}
