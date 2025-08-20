package atlantis.production.constructions.position.conditions;

import atlantis.game.A;
import atlantis.map.choke.AChoke;
import atlantis.map.choke.Chokes;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.production.constructions.ConstructionRequests;
import atlantis.production.constructions.position.AbstractPositionFinder;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import atlantis.util.We;

public class TooCloseToChoke {
    public static boolean isTooCloseToChoke(AUnitType building, APosition position) {
        if (building.isBase()) return false;

        double minDist = minDist(building, position);

        if (We.protoss() && building.isGateway()) {
            AChoke natural = Chokes.natural();
            if (natural != null && position.distTo(natural) <= 10) return failed("Overlaps naturalC");
        }

        for (AChoke choke : Chokes.chokes()) {
//            if (choke.width() >= 5) {
//                continue;
//            }

            double distToChoke = distToChoke(position, building, choke);
            if (distToChoke <= minDist && !isCbAndOnlyOneNearby(building, position, choke)) {
                return failed("Overlaps choke (" + distToChoke + ")");
            }
        }

        return false;
    }

    private static double minDist(AUnitType building, HasPosition position) {
        double minDist = (building.isCombatBuilding())
            ? minDistForCombatBuilding(building, position)
            : (A.supplyUsed() >= 20 ? 4.7 : 3);

        if (We.terran()) {
            if (building.isBunker()) minDist = A.supplyUsed() <= 30 ? 5.5 : 4.5;
            if (building.isMissileTurret()) minDist = 2;
        }

        else if (We.protoss()) {
//            if (building.isForge()) minDist = 1.9;
            if (building.isForge()) return 2.5;
            else if (building.isGateway() && A.supplyUsed() >= 18) return 7;
            else if (building.isCannon()) return 4.7;
            else if (building.is(AUnitType.Protoss_Shield_Battery)) minDist = 6;
            else if (A.supplyTotal() <= 10 && building.isPylon()) minDist = 1.2;
            if (isPylonForNaturalChoke(building, position)) minDist = 1.5;
        }

        return minDist;
    }

    private static double minDistForCombatBuilding(AUnitType type, HasPosition position) {
        return 3.5
            + (Select.ourWithUnfinished(type).countInRadius(6, position) >= 1 ? 2 : 0);
    }

    private static boolean isPylonForNaturalChoke(AUnitType building, HasPosition position) {
        if (!building.isPylon()) return false;
        Selection bases = Select.ourBases();
        if (bases.count() <= 1) return false;
        AUnit natural = bases.second();
        if (natural == null) return false;

        return natural.distTo(position) <= 12;
    }

    private static boolean isCbAndOnlyOneNearby(AUnitType building, APosition position, AChoke choke) {
        if (!building.isCombatBuilding()) return false;

        double minDistToChoke = distToChoke(position, building, choke);

        return ConstructionRequests.countExistingAndPlannedInRadius(building, minDistToChoke, choke) == 0;
    }

    private static boolean failed(String reason) {
        AbstractPositionFinder._STATUS = reason;
        return true;
    }

    private static double distToChoke(APosition position, AUnitType building, AChoke choke) {
        return choke.center().distTo(position);
    }
}