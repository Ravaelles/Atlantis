package atlantis.production.constructions.position.conditions;

import atlantis.config.AtlantisRaceConfig;
import atlantis.map.base.ABaseLocation;
import atlantis.map.base.BaseLocations;
import atlantis.map.position.APosition;
import atlantis.production.constructions.ConstructionRequests;
import atlantis.production.constructions.position.AbstractPositionFinder;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import atlantis.util.We;

public class TooCloseToBaseLocation {
    public static boolean TooCloseAndOverlapsBaseLocation(AUnitType building, APosition position) {
        if (building.isGasBuilding()) return false;
        if (building.isBase()) return forBase(position);

        return forNonBaseBuilding(position, building);
    }

    private static boolean forBase(APosition position) {
        if (
            checkExistingBasesIncludingUnfinished(position)
                || checkExistingConstructionsOfOtherBase(position)
        ) {
            AbstractPositionFinder._STATUS = "Base to close to anotha base";
            return failed("Base to close to anotha base");
        }

        return false;
    }

    private static boolean failed(String reason) {
        AbstractPositionFinder._STATUS = reason;
        return true;
    }

    private static boolean checkExistingConstructionsOfOtherBase(APosition position) {
        return ConstructionRequests.hasNotStartedNear(AtlantisRaceConfig.BASE, position, 8);
    }

    private static boolean forNonBaseBuilding(APosition position, AUnitType building) {
        double minDist = 6;

        if (We.protoss()) {
            if (building.isPylon()) minDist = 4.5;
            if (building.isGateway()) minDist = 5;
            if (building.isForge()) minDist = 2.9;
        }

        if (We.terran()) {
            if (building.isBunker()) minDist = 3.5;
        }

//        if (building.isBunker()) System.err.println("@ " + A.now() + " - bunker ");

        for (ABaseLocation base : BaseLocations.baseLocations()) {
            if (
                base.translateByTiles(We.terran() ? 4 : 2, 1).distTo(position) <= minDist
            ) {
                if (Select.ourBasesWithUnfinished().inRadius(6, position).notEmpty()) return false;
//                System.err.println("   --- BASE " + base.position() + " Out with dist: " + base.translateByTiles(4,
//                    1).distTo(position) + " / " + position);
//                APosition natural = Bases.natural();
                return failed("Overlaps base location");
            }
        }

        return false;
    }

    private static boolean checkExistingBasesIncludingUnfinished(APosition position) {
        int minDist = We.terran() ? 10 : 0;

        if (Select.ourBuildingsWithUnfinished().bases().inRadius(minDist, position).isNotEmpty()) {
            return failed("Base already exists here");
        }

        return false;
    }
}
