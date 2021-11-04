package atlantis.map;

import atlantis.position.APosition;
import atlantis.position.HasPosition;
import bwapi.Pair;
import bwapi.Position;
import bwapi.WalkPosition;
import bwem.Area;
import bwem.ChokePoint;

import java.util.List;
import java.util.Objects;

public class AChoke implements HasPosition {

    private ChokePoint choke;
    private Position[] sides;
    private APosition center;
    private double width;

    public static AChoke create(ChokePoint chokepoint) {
        AChoke wrapper = new AChoke();
        wrapper.choke = chokepoint;
        wrapper.sides = calculateSides(wrapper);
        wrapper.center = calculateCenter(wrapper);
        wrapper.width = calculateWidth(wrapper);

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
        return choke.equals(that.choke);
    }

    @Override
    public int hashCode() {
        return Objects.hash(choke);
    }

    // =========================================================
    // BWTA consistent methods missing from BWEM

    private static double calculateWidth(AChoke wrapper) {
        return wrapper.sides[0].getDistance(wrapper.sides[1]);
    }

    private static APosition calculateCenter(AChoke wrapper) {
        return new APosition(
                (wrapper.sides[0].x + wrapper.sides[1].x) / 2,
                (wrapper.sides[0].y + wrapper.sides[1].y) / 2
        );
    }

    private static Position[] calculateSides(AChoke wrapper) {
        List<WalkPosition> wp = wrapper.choke.getGeometry();
        WalkPosition p1 = wp.get(0);
        WalkPosition p2 = wp.get(0);
        double d_max = -1.0D;

        for(int i = 0; i < wp.size(); ++i) {
            for(int j = i + 1; j < wp.size(); ++j) {
                double d = (wp.get(i)).getDistance(wp.get(j));
                if (d > d_max) {
                    d_max = d;
                    p1 = wp.get(i);
                    p2 = wp.get(j);
                }
            }
        }

        return new Position[] { p1.toPosition(), p2.toPosition() };
    }

    // =========================================================

    public APosition getCenter() {
        return center;
    }

    public int getWidth() {
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

    public ARegion firstRegion() {
        return regions().getFirst();
    }

    public ARegion secondRegion() {
        return regions().getSecond();
    }

    @Override
    public String toString() {
        return "AChoke{" +
                "width=" + width +
                ", chokepoint=" + center +
                '}';
    }
}
