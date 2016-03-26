package atlantis.constructing.position;

import atlantis.Atlantis;
import atlantis.constructing.AtlantisConstructingManager;
import atlantis.constructing.ConstructionOrder;
import atlantis.constructing.ConstructionOrderStatus;
import atlantis.util.PositionUtil;
import atlantis.wrappers.Select;
import bwapi.Position;
import bwapi.Unit;
import bwapi.UnitType;

public abstract class AbstractPositionFinder {
    
    protected static String _CONDITION_THAT_FAILED = null;

    // =========================================================
    // Hi-level methods
    /**
     * Returns true if game says it's possible to build given building at this position.
     */
    public static boolean canPhysicallyBuildHere(Unit builder, UnitType building, Position position) {
        return Atlantis.getBwapi().canBuildHere(position.toTilePosition(), building, builder, false);
    }

    /**
     * Returns true if game says it's possible to build given building at this position.
     *
    protected static boolean canPhysicallyBuildHere(UnitType building, Position position) {
    	return Atlantis.getBwapi().canBuildHere(arg0, arg1, arg2, arg3)
        return Atlantis.getBwapi().canBuildHere(position.toTilePosition(), building, true);
        
    }*/

    /**
     * Returns true if any other building is too close to this building or if two buildings would overlap
     * add-on place of another. Buildings can be stacked, but it needs to be done properly e.g. Supply Depots
     * could be stacked.
     */
    protected static boolean otherBuildingsTooClose(Unit builder, UnitType building, Position position) {
        
        // Compare against existing buildings
        for (Unit otherBuilding : Select.ourBuildings().listUnits()) {
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
                    double distance = PositionUtil.distanceTo(constructionOrder.getPositionToBuild(), position);
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
            if (building.equals(UnitType.Terran_Supply_Depot) && otherBuilding.getType().equals(UnitType.Terran_Supply_Depot)) {
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
    	
    	
        int targetRight = positionForNewBuilding.getX() + newBuildingType.dimensionRight();	//dimension* returns distance in pixels
        int targetLeft = positionForNewBuilding.getX() - newBuildingType.dimensionLeft();
        int targetTop = positionForNewBuilding.getY() - newBuildingType.dimensionUp();
        int targetBottom = positionForNewBuilding.getY() + newBuildingType.dimensionDown();

        //TODO: check whether get{Left,Right,Top,Bottom}PixelBoundary replacements have expected behavior
        //get{left,right,top,bottom} returns distances in pixels
        int xDist = building.getLeft() - (targetRight + 1);
        if (xDist < 0) {
            xDist = targetLeft - (building.getRight()+ 1);
            if (xDist < 0) {
                xDist = 0;
            }
        }
        int yDist = building.getTop() - (targetBottom + 1);
        if (yDist < 0) {
            yDist = targetTop - (building.getBottom() + 1);
            if (yDist < 0) {
                yDist = 0;
            }
        }
        return PositionUtil.distanceTo(new Position(0, 0), new Position(xDist, yDist));
    }

}
