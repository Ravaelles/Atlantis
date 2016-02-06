package atlantis.constructing.position;

import atlantis.Atlantis;
import atlantis.constructing.AtlantisConstructingManager;
import atlantis.constructing.ConstructionOrder;
import atlantis.constructing.ConstructionOrderStatus;
import atlantis.information.AtlantisMap;
import atlantis.wrappers.SelectUnits;
import jnibwapi.Position;
import jnibwapi.Unit;
import jnibwapi.types.UnitType;
import jnibwapi.types.UnitType.UnitTypes;

public abstract class AbstractPositionFinder {
    
    protected static String _CONDITION_THAT_FAILED = null;

    // =========================================================
    // Hi-level methods
    
    /**
     * Returns true if game says it's possible to build given building at this position.
     */
    public static boolean canPhysicallyBuildHere(Unit builder, UnitType building, Position position) {
        return Atlantis.getBwapi().canBuildHere(builder, position, building, false);
    }

    /**
     * Returns true if game says it's possible to build given building at this position.
     */
    protected static boolean canPhysicallyBuildHere(UnitType building, Position position) {
        return Atlantis.getBwapi().canBuildHere(position, building, true);
    }

    /**
     * Returns true if building at this position would be too close to either a mineral field or to a geyser.
     */
    protected static boolean isTooCloseToMineralsOrGeyser(UnitType building, Position position) {
        
        // We have problem only if building is both close to base and to minerals or to geyser
        Unit nearestBase = SelectUnits.ourBases().nearestTo(position);
        if (nearestBase != null && nearestBase.distanceTo(position) <= 9) {
            for (Unit mineral : SelectUnits.minerals().inRadius(8, position).list()) {
                if (mineral.distanceTo(position) <= 4) {
                    return true;
                }
            }
            for (Unit mineral : SelectUnits.geysers().inRadius(8, position).list()) {
                if (mineral.distanceTo(position) <= 4) {
                    return true;
                }
            }
        }
        
        // Disallow buildings at the edge of the map
        if (position.getBX() <= 1 || position.getBY() <= 1) {
            return true;
        }
        if (position.getBX() >= AtlantisMap.getMap().getSize().getBX() - 1 || 
                position.getBY() >= AtlantisMap.getMap().getSize().getBY() - 1) {
            return true;
        }
        
        return false;
    }
    
    /**
     * Returns true if any other building is too close to this building or if two buildings would overlap
     * add-on place of another. Buildings can be stacked, but it needs to be done properly e.g. Supply Depots
     * could be stacked.
     */
    protected static boolean otherBuildingsTooClose(Unit builder, UnitType building, Position position) {
        
        // Compare against existing buildings
        for (Unit otherBuilding : SelectUnits.ourBuildings().list()) {
            int status = areTwoBuildingsTooClose(otherBuilding, position, building);
            if (status >= STATUS_BUILDINGS_ADDON_COLLIDE) {
                AbstractPositionFinder._CONDITION_THAT_FAILED = "BUILDING TOO CLOSE (" + otherBuilding + ")";
                return true;
            }
        }
        
        // Compare against planned construction places
        for (ConstructionOrder constructionOrder : AtlantisConstructingManager.getAllConstructionOrders()) {
            if (ConstructionOrderStatus.CONSTRUCTION_NOT_STARTED.equals(constructionOrder.getStatus())
                    && !builder.equals(constructionOrder.getBuilder())) {
                if (constructionOrder.getPositionToBuild() != null) {
                    double distance = constructionOrder.getPositionToBuild().distanceTo(position);
                    if (distance <= 4) {
                        AbstractPositionFinder._CONDITION_THAT_FAILED = "PLANNED BUILDING TOO CLOSE (" 
                                + constructionOrder.getBuildingType() + ", DIST: " + distance + ")";
                        return true;
                    }
                }
            }
        }

        // No collisions detected
        return false;
    }

    private static final int STATUS_BUILDINGS_OK = 100;
    private static final int STATUS_BUILDINGS_STICK = 200;
    private static final int STATUS_BUILDINGS_ADDON_COLLIDE = 300;

    private static int areTwoBuildingsTooClose(Unit otherBuilding, Position position, UnitType building) {
        double edgeToEdgeDistance = getEdgeToEdgeDistanceBetween(otherBuilding, position, building);
        // System.out.println("   --- Dist bitw " + otherBuilding.getType().getName() + " and " + building.getName()
        // + " is " + edgeToEdgeDistance);

        // If buildings are dangerously close
        if (edgeToEdgeDistance < 0.1) {

            // Allow stacking of depots
            if (building.isType(UnitTypes.Terran_Supply_Depot) && otherBuilding.isType(UnitTypes.Terran_Supply_Depot)) {
                return STATUS_BUILDINGS_STICK;
            }
            else {
                return STATUS_BUILDINGS_ADDON_COLLIDE;
            }
        }

        return STATUS_BUILDINGS_OK;
    }

    // =========================================================
    // Lo-level methods
    /**
     * Returns edge-to-edge distance (in build tiles) between one existing building and the other one not yet
     * existing.
     */
    protected static double getEdgeToEdgeDistanceBetween(Unit building, Position positionForNewBuilding,
            UnitType newBuildingType) {
        int targetRight = positionForNewBuilding.getPX() + newBuildingType.getDimensionRight();
        int targetLeft = positionForNewBuilding.getPX() - newBuildingType.getDimensionLeft();
        int targetTop = positionForNewBuilding.getPY() - newBuildingType.getDimensionUp();
        int targetBottom = positionForNewBuilding.getPY() + newBuildingType.getDimensionDown();

        int xDist = building.getLeftPixelBoundary() - (targetRight + 1);
        if (xDist < 0) {
            xDist = targetLeft - (building.getRightPixelBoundary() + 1);
            if (xDist < 0) {
                xDist = 0;
            }
        }
        int yDist = building.getTopPixelBoundary() - (targetBottom + 1);
        if (yDist < 0) {
            yDist = targetTop - (building.getBottomPixelBoundary() + 1);
            if (yDist < 0) {
                yDist = 0;
            }
        }
        return new Position(0, 0).distanceTo(new Position(xDist, yDist));
    }

}
