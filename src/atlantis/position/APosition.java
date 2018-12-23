package atlantis.position;

import atlantis.debug.APainter;
import atlantis.information.AMap;
import atlantis.units.AUnit;
import atlantis.util.PositionUtil;
import bwem.area.Area;
import org.openbw.bwapi4j.Position;
import org.openbw.bwapi4j.WalkPosition;
import org.openbw.bwapi4j.type.Color;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * <b>APosition</b> class is a wrapper around Position and contains numerous helpers.
 * <br /><br />
 * <b>Notice:</b> whenever possible, try to use APosition in place of Position.
 *
 * @author Rafal Poniatowski <ravaelles@gmail.com>
 */
public class APosition extends Position implements Comparable<Position> {
    
//    public static final int PIXELS_TO_MAP_BOUNDARIES_CONSIDERED_CLOSE = 110;
    public static final int PIXELS_TO_MAP_BOUNDARIES_CONSIDERED_CLOSE = 12;
    
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

    public APosition(WalkPosition walkPosition) {
        super(walkPosition.getX(), walkPosition.getY());
        this.p = new Position(walkPosition.getX(), walkPosition.getY());
    }

    public APosition(Position p) {
        super(p.getX(), p.getY());
        this.p = p;
    }

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

    public APosition getPoint() {
        return new APosition(getX(), getY());
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
        paintIt(widthTiles, heightTiles, Color.RED);
    }
    
    /**
     * Paint it on screen for testing.
     */
    public void paintIt(int widthTiles, int heightTiles, Color color) {
        APainter.paintRectangle(this, widthTiles * 32, heightTiles * 32, color);
    }
    
    // === High-abstraction ========================================
    
    /**
     * Returns new position which is moved e.g. 15% in direction of the natural base (for bunker placement). Useful shit.
     */
    public APosition translatePercentTowards(Position towards, int percentTowards) {
        return PositionOperationsWrapper.getPositionMovedPercentTowards(
                this, towards, percentTowards
        );
    }
    
    /**
     * Returns new position which is moved e.g. 0.5 tiles towards <b>towards</b>.
     */
    public APosition translateTilesTowards(Position towards, double tiles) {
        return PositionOperationsWrapper.getPositionMovedTilesTowards(
                this, towards, tiles
        );
    }
    
    // =========================================================
    
    /**
     * Ensures that position's [x,y] are valid map coordinates.
     */
    public APosition makeValid() {
//        p = p.makeValid();

        boolean somethingChanged = false;
        int px = p.getX();
        int py = p.getY();
        
        if (px < 1) {
            px = 1;
            somethingChanged = true;
        }
        else if (px >= 32 * AMap.getMapWidthInTiles()) {
            px = 32 * AMap.getMapWidthInTiles() - 1;
            somethingChanged = true;
        }
        
        if (py < 1) {
            py = 1;
            somethingChanged = true;
        }
        else if (py >= 32 * AMap.getMapHeightInTiles()) {
            py = 32 * AMap.getMapHeightInTiles() - 1;
            somethingChanged = true;
        }
        
        if (somethingChanged) {
//            p = new APosition(px, py);
            return new APosition(px, py);
        }
        else {
            return this;
        }
    }
    
    /**
     * Ensures that position's [x,y] are valid map coordinates and are "quite" far from map boundaries.
     */
    public APosition makeValidFarFromBounds() {
        boolean somethingChanged = false;
        int px = p.getX();
        int py = p.getY();
        
        // =========================================================
        
        if (px < PIXELS_TO_MAP_BOUNDARIES_CONSIDERED_CLOSE) {
            px = PIXELS_TO_MAP_BOUNDARIES_CONSIDERED_CLOSE;
            somethingChanged = true;
        }
        else if (px >= (32 * AMap.getMapWidthInTiles() - PIXELS_TO_MAP_BOUNDARIES_CONSIDERED_CLOSE)) {
            px = 32 * AMap.getMapWidthInTiles() - PIXELS_TO_MAP_BOUNDARIES_CONSIDERED_CLOSE;
            somethingChanged = true;
        }
        
        if (py < PIXELS_TO_MAP_BOUNDARIES_CONSIDERED_CLOSE) {
            py = PIXELS_TO_MAP_BOUNDARIES_CONSIDERED_CLOSE;
            somethingChanged = true;
        }
        else if (py >= (32 * AMap.getMapHeightInTiles() - PIXELS_TO_MAP_BOUNDARIES_CONSIDERED_CLOSE)) {
            py = 32 * AMap.getMapHeightInTiles() - PIXELS_TO_MAP_BOUNDARIES_CONSIDERED_CLOSE;
            somethingChanged = true;
        }
        
//        APainter.paintCircle(new APosition(
//                32 * AMap.getMapWidthInTiles() - PIXELS_TO_MAP_BOUNDARIES_CONSIDERED_CLOSE,
//                32 * AMap.getMapHeightInTiles() - PIXELS_TO_MAP_BOUNDARIES_CONSIDERED_CLOSE
//        ), 32, Color.YELLOW);
//        
//        APainter.paintCircle(new APosition(
//                32 * AMap.getMapWidthInTiles() - PIXELS_TO_MAP_BOUNDARIES_CONSIDERED_CLOSE,
//                PIXELS_TO_MAP_BOUNDARIES_CONSIDERED_CLOSE
//        ), 32, Color.YELLOW);
//        
//        APainter.paintCircle(new APosition(
//                PIXELS_TO_MAP_BOUNDARIES_CONSIDERED_CLOSE,
//                32 * AMap.getMapHeightInTiles() - PIXELS_TO_MAP_BOUNDARIES_CONSIDERED_CLOSE
//        ), 32, Color.YELLOW);
//        
//        APainter.paintCircle(new APosition(
//                PIXELS_TO_MAP_BOUNDARIES_CONSIDERED_CLOSE,
//                PIXELS_TO_MAP_BOUNDARIES_CONSIDERED_CLOSE
//        ), 32, Color.YELLOW);
        
        // =========================================================
        
        if (somethingChanged) {
            return new APosition(px, py);
        }
        else {
            return this;
        }
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
    public int compareTo(Position o) {
        int compare = Integer.compare(getX(), o.getX());
        if (compare == 0) {
            return Integer.compare(getY(), o.getY());
        }
        return compare;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (! (obj instanceof Position)) {
            return false;
        }
        
        int otherX = ((Position) obj).getX();
        int otherY = ((Position) obj).getY();
        return this.getX() == otherX && this.getY() == otherY;
    }
    
    /**
     * Returns true if given position has land connection to given point.
     */
    public boolean hasPathTo(Position point) {
        return AMap.hasPath(this, point);
    }

    public boolean isCloseToMapBounds() {
        int px = p.getX();
        int py = p.getY();
        
        if (px < PIXELS_TO_MAP_BOUNDARIES_CONSIDERED_CLOSE) {
            return true;
        }
        else if (px >= (32 * AMap.getMapWidthInTiles() - PIXELS_TO_MAP_BOUNDARIES_CONSIDERED_CLOSE)) {
            return true;
        }
        
        if (py < PIXELS_TO_MAP_BOUNDARIES_CONSIDERED_CLOSE) {
            return true;
        }
        else if (py >= (32 * AMap.getMapHeightInTiles() - PIXELS_TO_MAP_BOUNDARIES_CONSIDERED_CLOSE)) {
            return true;
        }
        
        return false;
    }    

    /**
     * Return BWTA area for this position.
     */
    public Area getArea() {
        return AMap.getArea(this);
    }

}
