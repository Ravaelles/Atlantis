package atlantis.util;

import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.wrappers.APosition;
import bwapi.Position;
import bwapi.TilePosition;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class PositionUtil {

    /**
     * Returns distance from one position to other in build tiles. One build tile equals to 32 pixels. Usage
     * of build tiles instead of pixels is preferable, because it's easier to imagine distances if one knows
     * building dimensions.
     */
    public static double distanceTo(Position one, Position other) {
        int dx = one.getX() - other.getX();
        int dy = one.getY() - other.getY();

        // Calculate approximate distance between the units. If it's less than let's say X tiles, we probably should
        // consider calculating more precise value
        //TODO: check if approxDistance * Tile_Size is equivalent to getApproxBDistance
        double distanceApprx = one.getApproxDistance(other) / TilePosition.SIZE_IN_PIXELS; // getApproxBDistance(other);
        // Precision is fine, return approx value
        if (distanceApprx > 4.5) {
            return distanceApprx;
        } // AUnit is too close and we need to know the exact distance, not approximation.
        else {
            return Math.sqrt(dx * dx + dy * dy) / TilePosition.SIZE_IN_PIXELS;
        }
    }

    /**
     * Returns distance from one position to other in build tiles. One build tile equals to 32 pixels. Usage
     * of build tiles instead of pixels is preferable, because it's easier to imagine distances if one knows
     * building dimensions.
     */
    public static double distanceTo(APosition one, APosition other) {
        int dx = one.getX() - other.getX();
        int dy = one.getY() - other.getY();

        // Calculate approximate distance between the units. If it's less than let's say X tiles, we probably should
        // consider calculating more precise value
        //TODO: check if approxDistance * Tile_Size is equivalent to getApproxBDistance
        double distanceApprx = one.getApproxDistance(other) / TilePosition.SIZE_IN_PIXELS; // getApproxBDistance(other);
        // Precision is fine, return approx value
        if (distanceApprx > 4.5) {
            return distanceApprx;
        } // AUnit is too close and we need to know the exact distance, not approximation.
        else {
            return Math.sqrt(dx * dx + dy * dy) / TilePosition.SIZE_IN_PIXELS;
        }
    }

    /**
     * Returns distance from one position to other in build tiles. One build tile equals to 32 pixels. Usage
     * of build tiles instead of pixels is preferable, because it's easier to imagine distances if one knows
     * building dimensions.
     */
    public static double distanceTo(AUnit one, AUnit other) {
        return distanceTo(one.getPosition(), other.getPosition());
    }

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
    
    // =========================================================
    
    /**
     * Returns a <b>new</b> Position that represents the effect of moving this position by [deltaX, deltaY].
     */
    public static APosition translate(APosition position, int deltaPixelX, int deltaPixelY) {
        return new APosition(position.getX() + deltaPixelX, position.getY() + deltaPixelY);
    }

    /**
     * Returns median PX and median PY for all passed units.
     */
    public static APosition medianPosition(Collection<AUnit> units) {
        if (units.isEmpty()) {
            return null;
        }

        ArrayList<Integer> xCoordinates = new ArrayList<>();
        ArrayList<Integer> yCoordinates = new ArrayList<>();
        for (AUnit unit : units) {
            xCoordinates.add(unit.getPosition().getX());	//TODO: check whether position is in Pixels
            yCoordinates.add(unit.getPosition().getX());
        }
        Collections.sort(xCoordinates);
        Collections.sort(yCoordinates);

        return new APosition(
                xCoordinates.get(xCoordinates.size() / 2),
                yCoordinates.get(yCoordinates.size() / 2)
        );
    }

    /**
     * Returns average PX and average PY for all passed units.
     */
    public static APosition averagePosition(Collection<AUnit> units) {
        if (units.isEmpty()) {
            return null;
        }

        int totalX = 0;
        int totalY = 0;
        for (AUnit unit : units) {
            totalX += unit.getPosition().getX();
            totalY += unit.getPosition().getY();
        }
        return new APosition(
            totalX / units.size(),
            totalY / units.size()
        );
    }
    
}
