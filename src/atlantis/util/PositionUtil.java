package atlantis.util;

import atlantis.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import bwapi.Position;
import bwapi.Unit;

public class PositionUtil {

    /**
     * Returns distance from one position to other in build tiles. One build tile equals to 32 pixels. Usage
     * of build tiles instead of pixels is preferable, because it's easier to imagine distances if one knows
     * building dimensions.
     */
    public static double distanceTo(Object object1, Object object2) {
//        if (object1 == null || object2 == null) {
//            return -1;
//        }
        
        // === Convert object1 to position or unit ====================
        
        Position fromPosition = null;
        Unit fromUnit = null;
        
        if (object1 instanceof AUnit) {
            fromUnit = ((AUnit) object1).u();
        }
        else if (object1 instanceof Unit) {
            fromUnit = (Unit) object1;
        }
        else if (object1 instanceof APosition) {
            fromPosition = (APosition) object1;
        }
        else if (object1 instanceof Position) {
            fromPosition = (Position) object1;
        }
        
        if (fromPosition == null && fromUnit == null) {
            throw new RuntimeException("Invalid class for argument `from`: " + object1);
        }
        
        // === Convert object2 to position or unit ===================
        
        Position toPosition = null;
        Unit toUnit = null;
        
        if (object2 instanceof AUnit) {
            toUnit = ((AUnit) object2).u();
        }
        else if (object2 instanceof Unit) {
            toUnit = (Unit) object2;
        }
        else if (object2 instanceof APosition) {
            toPosition = (APosition) object2;
        }
        else if (object2 instanceof Position) {
            toPosition = (Position) object2;
        }
        
        if (toPosition == null && toUnit == null) {
            throw new RuntimeException("Invalid class for argument `to`: " + object2);
        }
        
        // =========================================================

        // From is POSITION
        if (fromPosition != null) {
            if (toPosition != null) {
                return (double) fromPosition.getDistance(toPosition) / 32;
            }
            else {
                return (double) fromPosition.getDistance(toUnit) / 32;
            }
        }
        
        // From is UNIT
        else {
            if (toPosition != null) {
                return (double) fromUnit.getDistance(toPosition) / 32;
            }
            
            // UNIT to UNIT distance - can be cached
            else {
//                return (double) fromUnit.getDistance(toUnit) / 32;
                return AUnit.unitDistancesCached.getDistanceBetweenUnits(fromUnit, toUnit);
            }
        }
    }
    
//    public static double distanceTo(Position one, Position other) {
//        int dx = one.getX() - other.getX();
//        int dy = one.getY() - other.getY();
//
//        // Calculate approximate distance between the units. If it's less than let's say X tiles, we probably should
//        // consider calculating more precise value
//        //TODO: check if approxDistance * Tile_Size is equivalent to getApproxBDistance
//        double distanceApprx = one.getApproxDistance(other) / TilePosition.SIZE_IN_PIXELS; // getApproxBDistance(other);
//        // Precision is fine, return approx value
//        if (distanceApprx > 4.5) {
//            return distanceApprx;
//        } // AUnit is too close and we need to know the exact distance, not approximation.
//        else {
//            return Math.sqrt(dx * dx + dy * dy) / TilePosition.SIZE_IN_PIXELS;
//        }
//    }

    /**
     * Returns distance from one position to other in build tiles. One build tile equals to 32 pixels. Usage
     * of build tiles instead of pixels is preferable, because it's easier to imagine distances if one knows
     * building dimensions.
     */
//    public static double distanceTo(APosition one, APosition other) {
//        int dx = one.getX() - other.getX();
//        int dy = one.getY() - other.getY();
//
//        // Calculate approximate distance between the units. If it's less than let's say X tiles, we probably should
//        // consider calculating more precise value
//        //TODO: check if approxDistance * Tile_Size is equivalent to getApproxBDistance
//        double distanceApprx = one.getApproxDistance(other) / TilePosition.SIZE_IN_PIXELS; // getApproxBDistance(other);
//        // Precision is fine, return approx value
//        if (distanceApprx > 4.5) {
//            return distanceApprx;
//        } // AUnit is too close and we need to know the exact distance, not approximation.
//        else {
//            return Math.sqrt(dx * dx + dy * dy) / TilePosition.SIZE_IN_PIXELS;
//        }
//    }

    /**
     * Returns distance from one position to other in build tiles. One build tile equals to 32 pixels. Usage
     * of build tiles instead of pixels is preferable, because it's easier to imagine distances if one knows
     * building dimensions.
     */
//    public static double distanceTo(AUnit one, AUnit other) {
//        return distanceTo(one.getPosition(), other.getPosition());
//    }

    /**
     * Returns edge-to-edge distance (in build tiles) between one existing building and the other one not yet
     * existing.
     */
    public static double getEdgeToEdgeDistanceBetween(AUnit building, Position positionForNewBuilding,
            AUnitType newBuildingType) {
        int targetRight = positionForNewBuilding.getX() + newBuildingType.ut().dimensionRight(); //dimension* returns distance in pixels
        int targetLeft = positionForNewBuilding.getX() - newBuildingType.ut().dimensionLeft();
        int targetTop = positionForNewBuilding.getY() - newBuildingType.ut().dimensionUp();
        int targetBottom = positionForNewBuilding.getY() + newBuildingType.ut().dimensionDown();

        //TODO: check whether get{Left,Right,Top,Bottom}PixelBoundary replacements have expected behavior
        //get{left,right,top,bottom} returns distances in pixels
        int xDist = building.getType().ut().dimensionLeft() - (targetRight + 1);
        if (xDist < 0) {
            xDist = targetLeft - (building.getType().ut().dimensionRight()+ 1);
            if (xDist < 0) {
                xDist = 0;
            }
        }
        int yDist = building.getType().ut().dimensionUp()- (targetBottom + 1);
        if (yDist < 0) {
            yDist = targetTop - (building.getType().ut().dimensionDown()+ 1);
            if (yDist < 0) {
                yDist = 0;
            }
        }
        return PositionUtil.distanceTo(new Position(0, 0), new Position(xDist, yDist));
    }
    
}
