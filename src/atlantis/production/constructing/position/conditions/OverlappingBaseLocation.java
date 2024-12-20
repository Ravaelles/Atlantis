package atlantis.production.constructing.position.conditions;

import atlantis.config.AtlantisRaceConfig;
import atlantis.game.A;
import atlantis.map.base.ABaseLocation;
import atlantis.map.base.BaseLocations;
import atlantis.map.position.APosition;
import atlantis.production.constructing.ConstructionRequests;
import atlantis.production.constructing.position.AbstractPositionFinder;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import atlantis.util.We;

public class OverlappingBaseLocation {
    public static boolean isOverlappingBaseLocation(AUnitType building, APosition position) {
        if (building.isBase()) return forBase(position);

        return forNonBaseBuilding(position, building);
    }

    private static boolean forBase(APosition position) {
        if (
            checkExistingBasesIncludingUnfinished(position)
                || checkExistingConstructionsOfOtherBase(position)
        ) {
            AbstractPositionFinder._CONDITION_THAT_FAILED = "Base to close to anotha base";
            return failed("Base to close to anotha base");
        }

        return false;
    }

    private static boolean failed(String reason) {
        AbstractPositionFinder._CONDITION_THAT_FAILED = reason;
        return true;
    }

    private static boolean checkExistingConstructionsOfOtherBase(APosition position) {
        return ConstructionRequests.hasNotStartedNear(AtlantisRaceConfig.BASE, position, 8);
    }

    private static boolean forNonBaseBuilding(APosition position, AUnitType building) {
        double minDist = building.isBunker() ? 4 : 6;

        if (We.protoss() && A.supplyUsed() <= 40) minDist = 5;

//        if (building.isBunker()) System.err.println("@ " + A.now() + " - bunker ");

        for (ABaseLocation base : BaseLocations.baseLocations()) {
//            if (building.isBunker())
//                System.err.println("@ " + A.now() + " - " + base + " / " + (base.translateByTiles(3, 1).distTo(position)));
            if (
//                !base.isStartLocation()
//                    (natural != null && natural.translateByTiles(3, 1).distTo(position) <= minDist)
                base.translateByTiles(We.terran() ? 4 : 2, 1).distTo(position) <= minDist
            ) {
//                System.err.println("   --- BASE " + base.position() + " Out with dist: " + base.translateByTiles(4,
//                    1).distTo(position) + " / " + position);
//                APosition natural = Bases.natural();
                return failed("Overlaps base location");
            }
        }

        return false;
    }

    private static boolean checkExistingBasesIncludingUnfinished(APosition position) {
        if (Select.ourBuildingsWithUnfinished().bases().inRadius(10, position).isNotEmpty()) {
            return failed("Base already exists here");
        }

        return false;
    }
}
