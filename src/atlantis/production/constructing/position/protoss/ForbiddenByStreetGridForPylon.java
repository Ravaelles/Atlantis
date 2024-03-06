package atlantis.production.constructing.position.protoss;

import atlantis.map.position.APosition;
import atlantis.production.constructing.ConstructionRequests;
import atlantis.production.constructing.position.AbstractPositionFinder;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;

import static atlantis.units.AUnitType.Terran_Supply_Depot;

public class ForbiddenByStreetGridForPylon {
    private static int X1 = 1;
    private static int X2 = 3;
    private static int Y1 = 1;
    private static int Y2 = 3;
    private static int Y3 = 5;

    public static boolean isForbidden(AUnit builder, AUnitType building, APosition position) {
        if (!building.isPylon()) return false;

        int modulo;
        if ((modulo = (position.tx()) % 2) != 0) return failed("TX modulo M = " + modulo);
        if ((modulo = (position.ty()) % 2) != 0) return failed("TY modulo N = " + modulo);

        if ((modulo = (position.tx()) % 9) >= 5) return failed("TX modulo M1 = " + modulo);
        if ((modulo = (position.ty()) % 9) >= 5) return failed("TY modulo N1 = " + modulo);


        return streetGridMatches(position);
    }

    private static boolean streetGridMatches(APosition position) {
        int GRID_SIZE_X = ProtossForbiddenByStreetGrid.GRID_VALUE_X;
        int GRID_SIZE_Y = ProtossForbiddenByStreetGrid.GRID_VALUE_Y;

        int moduloX = position.tx() % GRID_SIZE_X;
        if (moduloX != X1 && moduloX != X2) {
            AbstractPositionFinder._CONDITION_THAT_FAILED = "Pylon street grid forbidden (X)";
            return false;
        }

        int moduloY = position.ty() % GRID_SIZE_Y;
        if (moduloY != Y1 && moduloY != Y2 && moduloY != Y3) {
            AbstractPositionFinder._CONDITION_THAT_FAILED = "Pylon street grid forbidden (Y)";
            return false;
        }

        return true;

//        return (position.tx() % GRID_SIZE_X == A || position.tx() % GRID_SIZE_X == B)
//            && (position.ty() % GRID_SIZE_Y == A || position.ty() % GRID_SIZE_Y == B);

//            && position.tx() %  >= 1
//            && position.ty() % ProtossForbiddenByStreetGrid.GRID_VALUE_Y >= 1;
//        return position.tx() % 4 <= 1 && position.ty() % 6 <= 1;
//        return position.tx() % 3 == 1 && position.ty() % 2 == 0;
//            && position.tx() % 12 != 6 && position.ty() % 8 != 4;
//            && position.tx() % 9 != 0 && position.ty() % 6 != 0;
    }

    private static boolean failed(String reason) {
        AbstractPositionFinder._CONDITION_THAT_FAILED = reason;
        return true;
    }
}
