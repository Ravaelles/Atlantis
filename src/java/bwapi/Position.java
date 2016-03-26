package bwapi;

import java.util.HashMap;
import java.util.Map;

/**
 * Positions are measured in pixels and are the highest resolution.
 */
public class Position extends AbstractPoint<Position> {

    private int x, y;

    public Position(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public String toString() {
        return "[" + x + ", " + y + "]";
    }

    public native boolean isValid();

    public native Position makeValid();

    public native int getApproxDistance(Position position);

    public native double getLength();

    @Override
    public int getX() {
        return x;
    }

    @Override
    public int getY() {
        return y;
    }

    public static Position Invalid;

    public static Position None;

    public static Position Unknown;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Position)) {
            return false;
        }

        Position position = (Position) o;

        if (x != position.x) {
            return false;
        }
        if (y != position.y) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = x;
        result = 31 * result + y;
        return result;
    }

    private static Map<Long, Position> instances = new HashMap<>();

    private Position(long pointer) {
        this.pointer = pointer;
    }

    private static Position get(long pointer) {
        Position instance = instances.get(pointer);
        if (instance == null) {
            instance = new Position(pointer);
            instances.put(pointer, instance);
        }
        return instance;
    }

    private long pointer;

    @Override
    public Position getPoint() {
        return this;
    }

    public TilePosition toTilePosition() {
        return new TilePosition(x / TilePosition.SIZE_IN_PIXELS, y / TilePosition.SIZE_IN_PIXELS);
    }
    
    
    // =========================================================
    // ===== Start of ATLANTIS CODE ============================
    // =========================================================
    
    public Position() { }
    
}
