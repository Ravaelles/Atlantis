package atlantis.wrappers;

import atlantis.units.AUnit;
import atlantis.util.PositionUtil;
import bwapi.Position;
import java.util.HashMap;
import java.util.Map;

/**
 * Atlantis uses wrapper for BWMirror native classes which can't extended.<br /><br />
 * <b>APosition</b> class contains numerous helper methods, but if you think some methods are missing
 * you can create missing method here and you can reference original Position class via p() method.
 *
 * @author Rafal Poniatowski <ravaelles@gmail.com>
 */
public class APosition extends Position {
    
    private static final Map<Position, APosition> instances = new HashMap<>();
    
    private Position p;
    
    // =========================================================

    public APosition(APosition position) {
        super(position.getX(), position.getY());
        this.p = new Position(position.getX(), position.getY());
    }

    public APosition(int pixelX, int pixelY) {
        super(pixelX, pixelY);
        this.p = new Position(pixelX, pixelY);
    }

    private APosition(Position p) {
        super(p.getX(), p.getY());
        this.p = p;
    }
    
    /**
     * Atlantis uses wrapper for BWMirror native classes which aren't extended.<br />
     * <b>APosition</b> class contains numerous helper methods, but if you think some methods are missing
     * you can create missing method here and you can reference original Position class via p() method.
     */
    public static APosition createFrom(Position p) {
        if (instances.containsKey(p)) {
            return instances.get(p);
        }
        else {
            APosition position = new APosition(p);
            instances.put(p, position);
            return position;
        }
    }
    
    /**
     * <b>AVOID USAGE AS MUCH AS POSSIBLE</b> outside APosition class.
     * APosition class should be used always in place of Position when possible.
     */
    public Position p() {
        return p;
    }

    // =========================================================
    
    /**
     * Returns distance from one position to other in build tiles. One build tile equals to 32 pixels. Usage
     * of build tiles instead of pixels is preferable, because it's easier to imagine distances if one knows
     * building dimensions.
     */
    public double distanceTo(Position position) {
        return PositionUtil.distanceTo(getPoint(), position);
    }
    
    /**
     * Returns distance from one position to other in build tiles. One build tile equals to 32 pixels. Usage
     * of build tiles instead of pixels is preferable, because it's easier to imagine distances if one knows
     * building dimensions.
     */
    public double distanceTo(AUnit unit) {
        return PositionUtil.distanceTo(getPoint(), unit.getPosition());
    }
    
    /**
     * Returns X coordinate in tiles, 1 tile = 32 pixels.
     */
    public int getTileX() {
        return getX() / 32;
    }
    
    /**
     * Returns Y coordinate in tiles, 1 tile = 32 pixels.
     */
    public int getTileY() {
        return getY() / 32;
    }
    
    /**
     * Returns new position object that is translated in x,y by given values.
     */
    public APosition translate(int pixelDX, int pixelDY) {
        return new APosition(getX() + pixelDX, getY() + pixelDY);
    }
    
    /**
     * Ensures that position's [x,y] are valid map coordinates.
     */
    @Override
    public APosition makeValid() {
        p = p.makeValid();
        return this;
    }
    
    // =========================================================

    @Override
    public String toString() {
        return "(" + getTileX() + ", " + getTileY() + ")";
    }
    
}
