package bweb;

import java.util.ArrayList;
import java.util.List;

public class BWEM {
    public static List<Area> allAreas = new ArrayList<>();
    public static List<Area> getAreas() { return allAreas; }
    public static class Base {
        public boolean Starting() { return false; }
        public List<Mineral> Minerals() { return new ArrayList<>(); }
        public List<Geyser> Geysers() { return new ArrayList<>(); }
        public BWAPI.TilePosition Location() { return new BWAPI.TilePosition(0, 0); }
        public BWAPI.Position Center() { return new BWAPI.Position(0, 0); }
        public Area GetArea() { return null; }
        public boolean hasGas() { return false; }
        public boolean hasMinerals() { return false; }
    }
    public static class ChokePoint {
        public BWAPI.Position Center() { return new BWAPI.Position(0, 0); }
        public BWAPI.Position Pos(int end) { return new BWAPI.Position(0, 0); }
        public List<BWAPI.Position> Geometry() { return new ArrayList<>(); }
        public Pair<Area, Area> GetAreas() { return null; }
        public boolean Blocked() { return false; }
    }
    public static class Area {
        public List<ChokePoint> ChokePoints() { return new ArrayList<>(); }
        public List<Base> Bases() { return new ArrayList<>(); }
        public List<Area> AccessibleNeighbours() { return new ArrayList<>(); }
        public BWAPI.TilePosition Top() { return new BWAPI.TilePosition(0, 0); }
    }
    public static class Mineral {
        public BWAPI.Position Pos() { return new BWAPI.Position(0, 0); }
        public BWAPI.TilePosition TopLeft() { return new BWAPI.TilePosition(0, 0); }
        public Object Unit() { return null; }
    }
    public static class Geyser {
        public BWAPI.Position Pos() { return new BWAPI.Position(0, 0); }
        public BWAPI.TilePosition TopLeft() { return new BWAPI.TilePosition(0, 0); }
        public Object Unit() { return null; }
    }
    public static class Pair<A, B> {
        public A first; public B second;
        public Pair(A a, B b) { first = a; second = b; }
    }
} 