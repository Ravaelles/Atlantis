package atlantis.position;

import atlantis.map.AChokepoint;
import atlantis.map.AMap;
import atlantis.map.ARegion;
import atlantis.units.AUnit;
import bwapi.Point;
import bwapi.Position;
import bwta.BWTA;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Atlantis uses wrapper for BWAPI  classes which can't extended due to private constructors.
 * <br /><br />
 * I've decided to implement a solution which allows to use the .jar of BWMirror library, because from
 * my experience it turns out, that it's extremely tedious to upgrade Atlantis to use newer version of
 * BWMirror if you work with the source code rather than the .jar library release.
 * <br /><br />
 * <b>APosition</b> class contains numerous helper methods, but if you think some methods are missing
 * you can create them here or reference original Position class via p() method. 
 * <br /><br />
 * <b>Notice:</b> whenever possible, try to use APosition in place of Position.
 */
public class APosition extends Position implements HasPosition, Comparable<Point<Position>> {
    
//    public static final int PIXELS_TO_MAP_BOUNDARIES_CONSIDERED_CLOSE = 110;
    public static final int PIXELS_TO_MAP_BOUNDARIES_CONSIDERED_CLOSE = 20;
    
    private static final Map<Object, APosition> instances = new HashMap<>();
    
    private final Position p;
    
    // =========================================================

    public APosition(APosition position) {
        super(position.getX(), position.getY());
        this.p = position.p;
    }

    private APosition(HasPosition p) {
        super(p.getPosition().getX(), p.getPosition().getY());
        this.p = p.getPosition();
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
     * <b>Notice:</b> whenever possible, use APosition instead of Position.
     *
     * Atlantis uses wrapper for bridge native classes.<br />
     * <b>APosition</b> class contains numerous helper methods, but if you think some methods are missing
     * you can create them here or reference original Position class via p() method.
     */
    public static APosition create(Object p) {
        if (instances.containsKey(p)) {
            return instances.get(p);
        }
        else {
            APosition position = null;

            if (p instanceof HasPosition) {
                position = new APosition(((HasPosition) p).getPosition());
            } else if (p instanceof Position) {
                position = new APosition((Position) p);
            } else {
                throw new RuntimeException("APosition::create invalid param " + p);
            }

            instances.put(p, position);
            return position;
        }
    }
    
    /**
     * <b>Notice:</b> whenever possible, use APosition instead of Position.
     *
     * Atlantis uses wrapper for bridge native classes.<br />
     * <b>APosition</b> class contains numerous helper methods, but if you think some methods are missing
     * you can create them here or reference original Position class via p() method.
     *
     * @return APosition object from (build) tile coordinates (32 pixels = 1 tile).
     */
    public static APosition create(int tileX, int tileY) {
        return new APosition(tileX * 32, tileY * 32);
    }

    // =========================================================

    /**
     * APosition class should be used always instead of Position when possible.
     */
    protected Position p() {
        return p;
    }

    public APosition getPosition() {
        return this;
    }

    /**
     * Returns distance from one position to other in build tiles. One build tile equals to 32 pixels. Usage
     * of build tiles instead of pixels is preferable, because it's easier to imagine distances if one knows
     * building dimensions.
     */
    public double distTo(Position position) {
        return PositionUtil.distanceTo(p, position);
    }

    public double distTo(APosition position) {
        return PositionUtil.distanceTo(p, position);
    }

    public double groundDistanceTo(APosition position) {
        return PositionUtil.groundDistanceTo(this, position);
    }

    public double distTo(AChokepoint choke) {
        return PositionUtil.distanceTo(p, choke);
    }

    /**
     * Returns distance from one position to other in build tiles. One build tile equals to 32 pixels. Usage
     * of build tiles instead of pixels is preferable, because it's easier to imagine distances if one knows
     * building dimensions.
     */
    public double distTo(AUnit unit) {
        return PositionUtil.distanceTo(p, unit);
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
//    public void paintIt(int widthTiles, int heightTiles) {
//        paintIt(widthTiles, heightTiles, Color.Red);
//    }
    
    /**
     * Paint it on screen for testing.
     */
//    public void paintIt(int widthTiles, int heightTiles, Color color) {
//        APainter.paintRectangle(this, widthTiles, heightTiles, color);
//    }
    
    // === High-abstraction ========================================
    
    /**
     * Returns new position which is moved e.g. 15% in direction of the natural base (for bunker placement).
     */
    public APosition translatePercentTowards(HasPosition towards, int percentTowards) {
        return PositionHelper.getPositionMovedPercentTowards(
                this, towards, percentTowards
        );
    }
    
    /**
     * Returns new position which is moved e.g. 0.5 tiles towards <b>towards</b>.
     */
    public APosition translateTilesTowards(Point<Position> towards, double tiles) {
        return PositionHelper.getPositionMovedTilesTowards(
                this, towards, tiles
        );
    }
    
    // =========================================================
    
    /**
     * Ensures that position's [x,y] are valid map coordinates.
     */
//    @Override
    public APosition makeValid() {
//        p = p.makeValid();

        boolean somethingChanged = false;
        int px = p.getX();
        int py = p.getY();
        
        if (px <= 1) {
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
//        ), 32, Color.Yellow);
//        
//        APainter.paintCircle(new APosition(
//                32 * AMap.getMapWidthInTiles() - PIXELS_TO_MAP_BOUNDARIES_CONSIDERED_CLOSE,
//                PIXELS_TO_MAP_BOUNDARIES_CONSIDERED_CLOSE
//        ), 32, Color.Yellow);
//        
//        APainter.paintCircle(new APosition(
//                PIXELS_TO_MAP_BOUNDARIES_CONSIDERED_CLOSE,
//                32 * AMap.getMapHeightInTiles() - PIXELS_TO_MAP_BOUNDARIES_CONSIDERED_CLOSE
//        ), 32, Color.Yellow);
//        
//        APainter.paintCircle(new APosition(
//                PIXELS_TO_MAP_BOUNDARIES_CONSIDERED_CLOSE,
//                PIXELS_TO_MAP_BOUNDARIES_CONSIDERED_CLOSE
//        ), 32, Color.Yellow);
        
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
    
//    public int compareTo(Position o) {
    @Override
    public int compareTo(Point o) {
        int compare = Integer.compare(getX(), o.getX());
        if (compare == 0) {
            compare = Integer.compare(getY(), o.getY());
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
//        if (!(obj instanceof APosition) && !(obj instanceof Position)) {
//            return false;
//        }
        if (!(obj instanceof Point)) {
            return false;
        }
        
        int otherX = ((Point) obj).getX();
        int otherY = ((Point) obj).getY();
        final Position other = (Position) obj;
        return this.getX() == otherX && this.getY() == otherY;
    }
    
    /**
     * Returns true if given position has land connection to given point.
     */
    public boolean hasPathTo(APosition point) {
        return BWTA.isConnected(this.toTilePosition(), point.toTilePosition());
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
        else return py >= (32 * AMap.getMapHeightInTiles() - PIXELS_TO_MAP_BOUNDARIES_CONSIDERED_CLOSE);
    }    

    /**
     * Return BWTA region for this position.
     */
    public ARegion getRegion() {
        return AMap.getRegion(this);
    }

}
