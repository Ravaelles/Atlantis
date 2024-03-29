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
//        System.out.println("============================");
//        System.out.println("position = " + position + ", not started = " + ConstructionRequests.notStarted().size()
//                + " // all = " + ConstructionRequests.all().size());

        // Compare against planned construction places
        for (Construction order : ConstructionRequests.all()) {
            HasPosition constructionPosition = order.buildPosition();
//            System.out.println("another = " + constructionPosition + " // " + order.buildingType());
            if (
                position != null && constructionPosition != null
            ) {
//                System.out.println("OK constructionPosition = " + constructionPosition);
                double distance = position.distTo(constructionPosition);
//                System.out.println("distance = " + distance + " // " + position);
//                System.out.println("------------");
//                boolean areBasesTooCloseOneToAnother = building.isBase() && order.buildingType().isBase()
//                        && (distance <= 5 && !We.zerg());

                if (distance >= 2) {
                    if (building.isSunkenOrCreep() && order.buildingType().isSunkenOrCreep()) {
                        return false;
                    }

                    if (building.isCannon() && order.buildingType().isCannon()) {
                        return false;
                    }
                }

                // Look for two bases that would be built too close one to another
                if (distance <= (building.canHaveAddon() ? 4 : 2.5)) {
                    AbstractPositionFinder._CONDITION_THAT_FAILED = "PLANNED BUILDING TOO CLOSE (" + building + ", DIST: " + distance + ")";
                    return true;
                }
            }
        }

        // No collisions detected
        return false;
    }
}