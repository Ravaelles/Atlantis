package atlantis.production.constructions.position.conditions;

import atlantis.game.A;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.production.constructions.Construction;
import atlantis.production.constructions.ConstructionRequests;
import atlantis.production.constructions.position.AbstractPositionFinder;
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

        // Compare against planned construction places
        for (Construction otherConstr : ConstructionRequests.notStarted()) {
            HasPosition otherPosition = otherConstr.buildPosition();

            if (position != null && otherPosition != null) {
                double distance = position.distTo(otherPosition);

//                if (building.isCannon()) {
//                    System.err.println(building + " Other cons: " + otherConstr.buildingType() + ", dist: " + A.digit(distance));
//                }

                if (distance >= 2) {
                    if (distance >= 6) return false;

                    if (We.protoss()) {
                        if (building.isPylon() && distance <= 3 && A.supplyTotal() <= 40) {
                            return failed("Spread early pylons");
                        }

                        if (building.isGateway()) {
                            if (distance < 3.5) {
                                return failed("Gateway too close to other building(" + building + ", dist: " + distance + ")");
                            }
                        }

                        if (building.isCannon()) {
                            if (otherConstr.buildingType().isCannon()) return false;
                            if (otherConstr.buildingType().isGateway() && otherConstr.notStarted()) {
                                return false;
                            }
                        }

                        if (building.isForge()) {
                            if (otherConstr.buildingType().isForge()) return false;
                            if (otherConstr.buildingType().isGateway()) return false;
                        }
                    }

                    if (We.zerg() && building.isSunkenOrCreep() && otherConstr.buildingType().isSunkenOrCreep()) {
                        return false;
                    }
                }

                // Look for two positions that could overlap one another
                if (distance <= (building.canHaveAddon() ? 4 : (building.isPylon() ? 2.4 : 3.1))) {
                    return failed("Planned building too close (" + building + ", dist: " + distance + ")");
                }
            }
        }

        // No collisions detected
        return false;
    }

    private static boolean failed(String reason) {
        AbstractPositionFinder._STATUS = reason;
        return true;
    }
}
