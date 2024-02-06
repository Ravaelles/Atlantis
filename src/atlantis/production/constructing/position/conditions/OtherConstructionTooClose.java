package atlantis.production.constructing.position.conditions;

import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.production.constructing.Construction;
import atlantis.production.constructing.ConstructionRequests;
import atlantis.production.constructing.position.AbstractPositionFinder;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;

public class OtherConstructionTooClose {
    /**
     * Returns true if any other building is too close to this building or if two buildings would overlap
     * add-on place of another. Buildings can be stacked, but it needs to be done properly e.g. Supply Depots
     * could be stacked.
     */
    public static boolean isOtherConstructionTooClose(AUnit builder, AUnitType building, APosition position) {
        if (building.isBase()) return false;

        // Compare against planned construction places
        for (Construction order : ConstructionRequests.all()) {
            HasPosition constructionPosition = order.buildPosition();

            if (
                position != null && constructionPosition != null
            ) {
                double distance = position.distTo(constructionPosition);

                if (distance >= 2) {
                    if (building.isSunkenOrCreep() && order.buildingType().isSunkenOrCreep()) return false;
                    if (building.isCannon() && order.buildingType().isCannon()) return false;
                }

                // Look for two bases that would be built too close one to another
                if (distance <= (building.canHaveAddon() ? 4 : 2.5)) {
                    return failed("Planned building too close (" + building + ", dist: " + distance + ")");
                }
            }
        }

        // No collisions detected
        return false;
    }

    private static boolean failed(String reason) {
        AbstractPositionFinder._CONDITION_THAT_FAILED = reason;
        return true;
    }
}
