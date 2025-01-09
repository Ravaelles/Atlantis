package bweb;

public class BWAPI {
    public static class Unit {}
    public static class UnitType {
        public int tileWidth() { return 0; }
        public int tileHeight() { return 0; }
        public boolean isBuilding() { return false; }
        public boolean requiresCreep() { return false; }
        public boolean isResourceDepot() { return false; }
        public int width() { return 0; }
        public int height() { return 0; }
        public double topSpeed() { return 0.0; }
        public String toString() { return "UnitType"; }
    }
    public static class TilePosition {
        public int x, y;
        public TilePosition(int x, int y) { this.x = x; this.y = y; }
        public TilePosition add(TilePosition other) { return new TilePosition(x + other.x, y + other.y); }
        public boolean isValid() { return true; }
        public int getDistance(TilePosition other) { return 0; }
    }
    public static class Position {
        public int x, y;
        public Position(int x, int y) { this.x = x; this.y = y; }
        public Position add(Position other) { return new Position(x + other.x, y + other.y); }
        public int getDistance(Position other) { return 0; }
    }
} 