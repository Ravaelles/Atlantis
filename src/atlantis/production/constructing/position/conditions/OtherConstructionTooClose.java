package atlantis.production.constructing.position.conditions;

import atlantis.game.A;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.production.constructing.Construction;
import atlantis.production.constructing.ConstructionRequests;
import atlantis.production.constructing.position.AbstractPositionFinder;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.util.We;

public class OtherConstructionTooClose {
    /**
     * Returns true if any other building is too close to this building or if two buildings would overlap
     * add-on place of another. Buildings can be stacked, but it needs to be done properly e.g. Supply Depots
     * could be stacked.
     */
    public static boolean isOtherConstructionTooClose(AUnit builder, AUnitType building, APosition position) {
        if (building.isBase()) return false;

        boolean isPylon = building.isPylon();

        // Compare against planned construction places
        for (Construction otherConstr : ConstructionRequests.notCompleted()) {
            HasPosition otherPosition = otherConstr.buildPosition();

            if (position != null && otherPosition != null) {
                double distance = position.distTo(otherPosition);

//                if (building.isCannon()) {
//                    System.err.println(building + " Other cons: " + otherConstr.buildingType() + ", dist: " + A.digit(distance));
//                }

                if (distance >= 2) {
                    if (We.protoss() && building.isCannon()) {
                        if (otherConstr.buildingType().isCannon()) return false;
                        if (otherConstr.buildingType().isGateway() && otherConstr.notStarted()) {
                            return false;
                        }
                    }

                    if (We.zerg() && building.isSunkenOrCreep() && otherConstr.buildingType().isSunkenOrCreep())
                        return false;
                }

                if (distance >= 2.2 && building.isGateway()) return false;

                // Look for two positions that could overlap one another
                if (distance <= (building.canHaveAddon() ? 4 : (building.isPylon() ? 2.4 : 3.1))) {
                    return failed("Planned building too close (" + building + ", dist: " + distance + ")");
                }

                if (isPylon && distance <= 3 && A.supplyTotal() <= 40) {
                    return failed("Spread early pylons");
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
