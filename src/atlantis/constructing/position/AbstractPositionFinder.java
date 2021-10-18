package atlantis.constructing.position;

import atlantis.AGame;
import atlantis.Atlantis;
import atlantis.constructing.AConstructionRequests;
import atlantis.constructing.ConstructionOrder;
import atlantis.constructing.ConstructionOrderStatus;
import atlantis.map.ABaseLocation;
import atlantis.map.AMap;
import atlantis.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.position.PositionUtil;
import bwapi.Position;

public abstract class AbstractPositionFinder {
    
    public static String _CONDITION_THAT_FAILED = null;

    // =========================================================
    // Hi-level methods
    
    /**
     * Returns true if game says it's possible to build given building at this position.
     */
    public static boolean isForbiddenByStreetGrid(AUnit builder, AUnitType building, APosition position) {
        
        // Special buildings can be build anywhere
        if (building.isBase() || building.isGasBuilding() || building.isBunker()) {
            return false;
        }
        
        // =========================================================


        // Leave entire vertical (same tileX) corridor free for units
        if (
                position.getTileX() % 8 <= 1
                || (position.getTileX() + building.getDimensionRight() / 32) % 8 <= 1
        ) {
        System.out.println(building.getShortName() + "   " + position.getTileX() + " // (" + position.getTileX() % 7 + ") // "
                + (position.getTileX() + building.getDimensionRight() / 32) + " // (" +
                (position.getTileX() + building.getDimensionRight() / 32) % 7 + ")");
            _CONDITION_THAT_FAILED = "LEAVE_PLACE_VERTICALLY";
            return true;
        }

        // Leave entire horizontal (same tileY) corridor free for units
        if (
                position.getTileY() % 7 <= 1
                || (position.getTileY() + building.getDimensionDown() / 32) % 7 <= 1
        ) {
            _CONDITION_THAT_FAILED = "LEAVE_PLACE_HORIZONTALLY";
            return true;
        }
        
        // Position okay
        return false;
    }
    
    /**
     * Returns true if game says it's possible to build given building at this position.
     */
    public static boolean canPhysicallyBuildHere(AUnit builder, AUnitType building, APosition position) {
        if (position == null) {
            _CONDITION_THAT_FAILED = "POSITION IS NULL";
            return false;
        }
        if (builder == null) {
            _CONDITION_THAT_FAILED = "BUILDER IS NULL";
            return false;
        }

        return Atlantis.game().canBuildHere(position.toTilePosition(), building.ut(), builder.u());
    }

    /**
     * Returns true if any other building is too close to this building or if two buildings would overlap
     * add-on place of another. Buildings can be stacked, but it needs to be done properly e.g. Supply Depots
     * could be stacked.
     */
    protected static boolean isOtherConstructionTooClose(AUnit builder, AUnitType building, Position position) {
        
        // Compare against planned construction places
        for (ConstructionOrder constructionOrder : AConstructionRequests.getAllConstructionOrders()) {
            if (ConstructionOrderStatus.CONSTRUCTION_NOT_STARTED.equals(constructionOrder.getStatus())
                    && !builder.equals(constructionOrder.getBuilder())) {
                if (constructionOrder.getPositionToBuild() != null) {
                    double distance = PositionUtil.distanceTo(constructionOrder.getPositionToBuild(), position);
                    boolean areBasesTooCloseOneToAnother = (distance <= 8 && !AGame.isPlayingAsZerg()
                            && building.isBase() && constructionOrder.getBuildingType().isBase());
                    
                    // Look for two bases that would be built too close one to another
                    if (distance <= 4 || areBasesTooCloseOneToAnother) {
                        _CONDITION_THAT_FAILED = "PLANNED BUILDING TOO CLOSE (" 
                                + constructionOrder.getBuildingType() + ", DIST: " + distance + ")";
                        return true;
                    }
                }
            }
        }

        // No collisions detected
        return false;
    }

    protected static boolean isOverlappingBaseLocation(AUnitType building, APosition position) {
        if (building.isBase()) {
            return false;
        }

        for (ABaseLocation base : AMap.getBaseLocations()) {
            if (!base.isStartLocation() && base.getPosition().distanceTo(position) <= 5) {
                _CONDITION_THAT_FAILED = "Overlaps base location";
                return true;
            }
        }

        return false;
    }

}
