package atlantis.production.constructing.position.conditions;

import atlantis.game.A;
import atlantis.map.position.APosition;
import atlantis.production.constructing.position.AbstractPositionFinder;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import atlantis.util.We;

public class TooCloseToMineralsOrGeyser {
    public static boolean isTooCloseToMineralsOrGeyser(AUnitType building, APosition position) {
        if (building.isMissileTurret()) return false;
        if (building.isBase()) return false;
        if (building.isCombatBuilding()) return false;
        if (Select.main() == null) return false;

        // We have problem only if building is both close to base and to minerals or to geyser
        AUnit nearestBase = Select.ourBases().nearestTo(position);

        if (nearestBase == null) return false;

        AUnit mineral = Select.minerals().nearestTo(position);
        double minDistToMineral = minDistToMineral(building);

        if (mineral != null && mineral.distTo(position) <= minDistToMineral) {
            return failed("Too close to mineral");
        }

        AUnit gasBuilding = Select.geysersAndGasBuildings().nearestTo(position);
        if (gasBuilding != null && gasBuilding.distTo(position) <= minDistToGeyser()) {
            return failed("Too close to geyser");
        }

        return false;
    }

    private static double minDistToGeyser() {
        if (We.protoss()) {
            return A.supplyUsed() >= 15 ? 1.1 : 4;
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

    private static double minDistToMineral(AUnitType building) {
//        if (building.isPylon()) return 3;

        if (We.protoss()) {
            return 2.6;
        }

        else if (We.terran()) {
            if (building.isBunker()) return 5;
            if (building.isBarracks()) return 3.5;

            return 3;
        }

        return 3;
    }
}
