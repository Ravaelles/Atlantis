package atlantis.production.constructions.position.protoss;

import atlantis.map.position.APosition;
import atlantis.production.constructions.position.AbstractPositionFinder;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;

public class ProtossTooCloseToOtherBuildings {
    public static boolean isTooClose(AUnit builder, AUnitType building, APosition position) {
        if (!building.isProtossImportantTechBuilding()) return false;

        int radius = building.isCannon() ? 6 : 5;

        int otherNear = Select.ourBasesWithUnfinished().inRadius(radius, position).count();

        return otherNear >= 1
            && failed("Other buildings too close (" + otherNear + ")");
    }

    private static boolean failed(String reason) {
        AbstractPositionFinder._STATUS = reason;
        return true;
    }
}
