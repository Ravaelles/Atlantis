package atlantis.production.constructing.position.protoss;

import atlantis.map.position.APosition;
import atlantis.production.constructing.position.AbstractPositionFinder;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import atlantis.util.We;

public class ProtossForbiddenForProducerBuildings {
    private static int X1 = 5;
    private static int X2 = 9;
    private static int Y1 = 5;
    private static int Y2 = 8;

    public static boolean isForbidden(AUnit builder, AUnitType building, APosition position) {
        if (!We.protoss()) return false;
        if (!isProducingBuilding(building)) return false;

        int GRID_SIZE = ProtossForbiddenByStreetGrid.GRID_VALUE_X;

        return (position.tx() % GRID_SIZE == X1 || position.tx() % GRID_SIZE == X2)
            && (position.ty() % GRID_SIZE == Y1 || position.ty() % GRID_SIZE == Y2);

//        return !isNextToAPylon(builder, building, position);
    }

    private static boolean isNextToAPylon(AUnit builder, AUnitType building, APosition position) {
        return Select.ourOfType(AUnitType.Protoss_Pylon).inRadius(3, position).notEmpty();
    }

    private static boolean isProducingBuilding(AUnitType building) {
        return building.isGateway() || building.isRoboticsFacility();
    }

    private static boolean fail(String reason) {
        AbstractPositionFinder._CONDITION_THAT_FAILED = reason;
        return true;
    }
}