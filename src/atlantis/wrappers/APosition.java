package atlantis.wrappers;

import atlantis.debug.AtlantisPainter;
import atlantis.information.AtlantisMap;
import atlantis.units.AUnit;
import atlantis.util.PositionUtil;
import bwapi.Color;
import bwapi.Position;
import bwapi.TilePosition;
import bwta.BWTA;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Atlantis uses wrapper for BWMirror native classes which can't extended due to private constructors.
 * <br /><br />
 * I've decided to implement a solution which allows to use the .jar of BWMirror library, because from
 * my experience it turns out, that it's extremely tedious to upgrade Atlantis to use newer version of
 * BWMirror if you work with the source code rather than the .jar library release.
 * <br /><br />
 * <b>APosition</b> class contains numerous helper methods, but if you think some methods are missing
 * you can create them here or reference original Position class via p() method. 
 * <br /><br />
 * <b>Notice:</b> whenever possible, try to use APosition in place of Position.
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
     * you can create them here or reference original Position class via p() method. 
     * <br /><br />
     * <b>Notice:</b> whenever possible, try to use APosition in place of Position.
     */
    public static APosition create(Position p) {
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
     * <b>APosition</b> class contains numerous helper methods, but if you think some methods are missing
     * you can create them here or reference original Position class via p() method. 
     * <br /><br />
     * <b>Notice:</b> whenever possible, try to use APosition in place of Position.
     * <br /><br />
     * @return APosition object from (build) tile coordinates (32 pixels = 1 tile).
     */
    public static APosition create(int tileX, int tileY) {
        return new APosition(tileX * 32, tileY * 32);
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
//        return PositionUtil.distanceTo(getPoint(), unit.getPosition());
        return PositionUtil.distanceTo(getPoint(), unit);
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
     * Returns new position object that is translated in [x,y] pixels.
     */
    public APosition translateByPixels(int pixelDX, int pixelDY) {
        return new APosition(getX() + pixelDX, getY() + pixelDY);
    }
    
    /**
     * Returns new position object that is translated in [x,y] tiles.
     */
    public APosition translateByTiles(int tileDX, int tileDY) {
        return new APosition(getX() + tileDX * 32, getY() + tileDY * 32);
    }
    
    /**
     * Paint it on screen for testing.
     */
    public void paintIt(int widthTiles, int heightTiles) {
        paintIt(widthTiles, heightTiles, Color.Red);
    }
    
    /**
     * Paint it on screen for testing.
     */
    public void paintIt(int widthTiles, int heightTiles, Color color) {
        AtlantisPainter.paintRectangle(this, widthTiles * 32, heightTiles * 32, color);
    }
    
    // =========================================================
    
    /**
     * Ensures that position's [x,y] are valid map coordinates.
     */
    @Override
    public APosition makeValid() {
//        p = p.makeValid();

        boolean somethingChanged = false;
        int px = p.getX();
        int py = p.getY();
        
        if (px < 1) {
            px = 1;
            somethingChanged = true;
        }
        else if (px >= 32 * AtlantisMap.getMapWidthInTiles()) {
            px = 32 * AtlantisMap.getMapWidthInTiles() - 1;
            somethingChanged = true;
        }
        
        if (py < 1) {
            py = 1;
            somethingChanged = true;
        }
        else if (py >= 32 * AtlantisMap.getMapHeightInTiles()) {
            py = 32 * AtlantisMap.getMapHeightInTiles() - 1;
            somethingChanged = true;
        }
        
        if (somethingChanged) {
            p = new APosition(px, py);
        }

        return this;
    }
    
    /**
     * Ensures that position's [x,y] are valid map coordinates and are "quite" far from map boundaries.
     */
    public APosition makeValidFarFromBounds() {
        boolean somethingChanged = false;
        int px = p.getX();
        int py = p.getY();
        
        int safetyMargin = 70;
        
        if (px < safetyMargin) {
            px = safetyMargin;
            somethingChanged = true;
        }
        else if (px >= (32 * AtlantisMap.getMapWidthInTiles() - safetyMargin)) {
            px = 32 * AtlantisMap.getMapWidthInTiles() - safetyMargin;
            somethingChanged = true;
        }
        
        if (py < safetyMargin) {
            py = safetyMargin;
            somethingChanged = true;
        }
        else if (px >= (32 * AtlantisMap.getMapHeightInTiles() - safetyMargin)) {
            py = 32 * AtlantisMap.getMapHeightInTiles() - safetyMargin;
            somethingChanged = true;
        }
        
        if (somethingChanged) {
            p = new APosition(px, py);
        }

        return this;
    }

    @Override
    public String toString() {
        return "(" + getTileX() + ", " + getTileY() + ")";
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 29 * hash + Objects.hashCode(this.p);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof APosition) && !(obj instanceof Position)) {
            return false;
        }
        int otherX = ((Position) obj).getX();
        int otherY = ((Position) obj).getY();
        final Position other = (Position) obj;
        if (this.getX() != otherX || this.getY() != otherY) {
            return false;
        }
        return true;
    }
    
    /**
     * Returns true if given position has land connection to given point.
     */
    public boolean hasPathTo(APosition point) {
        return BWTA.isConnected(this.toTilePosition(), point.toTilePosition());
    }
    
}
