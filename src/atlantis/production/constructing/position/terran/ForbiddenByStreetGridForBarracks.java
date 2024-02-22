package atlantis.production.constructing.position.terran;

import atlantis.map.position.APosition;
import atlantis.production.constructing.ConstructionRequests;
import atlantis.production.constructing.position.AbstractPositionFinder;
import atlantis.production.constructing.position.protoss.ProtossForbiddenByStreetGrid;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;

import static atlantis.units.AUnitType.Terran_Barracks;
import static atlantis.units.AUnitType.Terran_Supply_Depot;

public class ForbiddenByStreetGridForBarracks {
    private static int X1 = 5;
    private static int X2 = 9;
    private static int Y1 = 5;
    private static int Y2 = 8;

    public static boolean isForbidden(AUnit builder, AUnitType building, APosition position) {
        if (!building.isBarracks()) return false;

        int GRID_SIZE = TerranForbiddenByStreetGrid.GRID_VALUE_X;

        if (!gridMatches(position, GRID_SIZE)) {
            AbstractPositionFinder._CONDITION_THAT_FAILED = "Barracks grid does NOT allow it";
            return true;
        }

        return false;
//        return !isNextToAPylon(builder, building, position);
    }

    private static boolean gridMatches(APosition position, int GRID_SIZE) {
        return (position.tx() % GRID_SIZE == X1 || position.tx() % GRID_SIZE == X2)
            && (position.ty() % GRID_SIZE == Y1 || position.ty() % GRID_SIZE == Y2);
    }
}
