package atlantis.production.constructing.position.protoss;

import atlantis.map.position.APosition;
import atlantis.production.constructing.position.AbstractPositionFinder;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import atlantis.util.We;

public class ProtossForProducerBuilding {
    private static int A = 0;
    private static int B = 8;

    public static boolean isForbidden(AUnit builder, AUnitType building, APosition position) {
        if (!We.protoss()) return false;
        if (!isProducingBuilding(building)) return false;

        int GRID_SIZE = ProtossForbiddenByStreetGrid.GRID_VALUE_X;

        return (position.tx() % GRID_SIZE == A || position.tx() % GRID_SIZE == B)
            && (position.ty() % GRID_SIZE == A || position.ty() % GRID_SIZE == B);

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
