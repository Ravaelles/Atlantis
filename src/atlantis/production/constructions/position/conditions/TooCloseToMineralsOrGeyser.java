package atlantis.production.constructions.position.conditions;

import atlantis.game.A;
import atlantis.map.base.Bases;
import atlantis.map.position.APosition;
import atlantis.production.constructions.position.AbstractPositionFinder;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import atlantis.util.We;

public class TooCloseToMineralsOrGeyser {
    public static boolean isTooCloseToMineralsOrGeyser(AUnitType building, APosition position) {
        if (building.isMissileTurret()) return false;
        if (building.isBase()) return false;
//        if (building.isCombatBuilding() && !building.isCannon()) return false;
//        if (Select.main() == null) return false;

        // We have problem only if building is both close to base and to minerals or to geyser
//        AUnit nearestBase = Select.ourBasesWithUnfinished().nearestTo(position);
//        if (nearestBase == null && !building.isPylon() && !building.isCannon()) return false;

        Selection allMinerals = Select.minerals();
        int minerals = allMinerals.countInRadius(minDistToMineral(building, position), position);
//        if (minerals > 0 && !allowCloseToBase(nearestBase, building, position)) {
        if (minerals > 0) {
            return failed("Too close to mineral");
        }

        AUnit gasBuilding = Select.geysersAndGasBuildings().nearestTo(position);
        if (
            gasBuilding != null
                && gasBuilding.distTo(position) <= minDistToGeyser(building)
//                && !allowCloseToBase(nearestBase, building, position)
        ) {
            return failed("Too close to geyser");
        }

        if (
            building.isCannon()
                && allMinerals.notEmpty()
                && Bases.natural() != null
                && position.distTo(Bases.natural()) <= 7
                && allMinerals.countInRadius(4, position) > 0
        ) {
            return failed("Cannon too close natural minerals");
        }

        return false;
    }

//    private static boolean allowCloseToBase(AUnit nearestBase, AUnitType building, APosition position) {
//        if (nearestBase == null) return false;
//        if (!building.isCannon()) return false;
//
//        return nearestBase.distTo(position) <= 3.5;
//    }

    private static double minDistToGeyser(AUnitType building) {
//        if (true) return 0.1;

        if (We.protoss()) {
            if (building.isGateway()) return 5.1;
            if (building.isPylon()) return 5.1;
            if (building.isCannon()) return 5.1;

            return 4.1;
        }

        else if (We.terran()) {
            return 3.2;
        }

        return 2;
    }

    private static boolean failed(String reason) {
        AbstractPositionFinder._STATUS = reason;
        return true;
    }

    private static double minDistToMineral(AUnitType building, APosition position) {
        if (We.protoss()) {
            if (A.supplyTotal() >= 60) return 6;

            if (A.supplyTotal() >= 40 && Select.ourBuildings().countInRadius(10, position) <= 1) return 5.2;

            if (building.isPylon() && position.distToMain() >= 15) return 6.5;
            if (building.isCannon() && position.distToMain() >= 15) return 3.2;
            return 2.8;
        }

        else if (We.terran()) {
            if (building.isBunker()) return 5;
            if (building.isBarracks()) return 3.5;

            return 3;
        }

        return 3;
    }
}
