package atlantis.map.choke;

import atlantis.game.A;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.map.region.ARegion;
import atlantis.units.AUnit;
import bwapi.Pair;
import bwapi.Position;
import bwapi.WalkPosition;
import bwem.Area;
import bwem.ChokePoint;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.TreeMap;

public class AChoke implements HasPosition {

    private static TreeMap<Integer, AChoke> all = new TreeMap<>();

    private ChokePoint choke;
    private Position[] sides;
    private APosition center;
    private double width;
    private List<APosition> perpendicular;
    private APosition firstPoint;
    private APosition lastPoint;

    /**
     * Index of this choke in path to enemy base, counting from our first building (which is main).
     * -1 means that this choke is not in path to enemy base.
     * 0 means that this choke is the first one in path to enemy base.
     * 1 means that this choke is the second one in path to enemy base and so on.
     */
    private int pathToEnemyBaseIndex = -1;

    public static AChoke from(ChokePoint chokepoint) {
        if (chokepoint == null) {
            return null;
        }

        if (all.containsKey(chokepoint.hashCode())) {
            return all.get(chokepoint.hashCode());
        }

//        assert chokepoint != null;

        AChoke wrapper = new AChoke();
        wrapper.choke = chokepoint;
        wrapper.sides = wrapper.calculateSides();
        wrapper.center = wrapper.calculateCenter();
        wrapper.width = wrapper.calculateWidth();
        wrapper.perpendicular = wrapper.createPerpendicular();
        wrapper.firstPoint = APosition.create(chokepoint.getGeometry().get(0));
        wrapper.lastPoint = APosition.create(chokepoint.getGeometry().get(chokepoint.getGeometry().size() - 1));

        all.put(chokepoint.hashCode(), wrapper);

        return wrapper;
    }

    // =========================================================

    @Override
    public APosition position() {
        return APosition.create(choke.getCenter().toPosition());
    }

    @Override
    public int x() {
        return position().getX();
    }

    @Override
    public int y() {
        return position().getY();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AChoke)) return false;
        AChoke that = (AChoke) o;
        return x() == that.x() && y() == that.y();
    }

    @Override
    public int hashCode() {
        return Objects.hash(choke);
    }

    @Override
    public String toString() {
        return "Choke{" +
            "width=" + A.digit(width) +
//                ",center=" + center +
            '}';
    }

    // =========================================================
    // BWTA consistent methods missing from BWEM

    private double calculateWidth() {
        return sides[0].getDistance(sides[1]) / 32;
    }

    private APosition calculateCenter() {
        return new APosition(
            (sides[0].x + sides[1].x) / 2,
            (sides[0].y + sides[1].y) / 2
        );
    }

    private Position[] calculateSides() {
        assert choke != null;
        assert choke.getGeometry() != null;

        List<WalkPosition> wp = choke.getGeometry();
        WalkPosition p1 = wp.get(0);
        WalkPosition p2 = wp.get(0);
        double d_max = -1.0D;

        for (int i = 0; i < wp.size(); ++i) {
            for (int j = i + 1; j < wp.size(); ++j) {
                double d = (wp.get(i)).getDistance(wp.get(j));
                if (d > d_max) {
                    d_max = d;
                    p1 = wp.get(i);
                    p2 = wp.get(j);
                }
            }
        }

        return new Position[]{p1.toPosition(), p2.toPosition()};
    }

    private List<APosition> createPerpendicular() {
        List<APosition> perpendicular = new ArrayList<>();
        for (WalkPosition walkPosition : choke.getGeometry()) {
            perpendicular.add(APosition.create(walkPosition));
        }
        return perpendicular;
    }

    // =========================================================

    public APosition center() {
        return center;
    }

    public int width() {
        return (int) width;
    }

    public Pair<ARegion, ARegion> regions() {
        Pair<Area, Area> regions = choke.getAreas();
        Pair<ARegion, ARegion> aRegions = new Pair<>(
            ARegion.create(regions.getLeft()),
            ARegion.create(regions.getRight())
        );

        return aRegions;
    }

    public List<APosition> perpendicularLine() {
        return perpendicular;
    }

    public ARegion firstRegion() {
        return regions().getFirst();
    }

    public ARegion secondRegion() {
        return regions().getSecond();
    }

    public APosition firstPoint() {
        return firstPoint;
    }

    public APosition lastPoint() {
        return lastPoint;
    }

    public ChokePoint rawChoke() {
        return choke;
    }

    public void setPathToEnemyBaseIndex(int index) {
        this.pathToEnemyBaseIndex = index;
    }

    public int pathToEnemyBaseIndex() {
        return pathToEnemyBaseIndex;
    }
}
