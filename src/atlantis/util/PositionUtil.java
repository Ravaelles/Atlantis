package atlantis.util;

import atlantis.units.AUnit;
import bwapi.Position;
import bwapi.TilePosition;

import bwta.Chokepoint;

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
    public static double distanceTo(AUnit one, AUnit other) {
        return distanceTo(one.getPosition(), other.getPosition());
    }

    /**
     * Returns a <b>new</b> Position that represents the effect of moving this position by [deltaX, deltaY].
     */
    public static Position translate(Position p, int deltaPixelX, int deltaPixelY) {
        return new Position(p.getX() + deltaPixelX, p.getY() + deltaPixelY);
    }
}
