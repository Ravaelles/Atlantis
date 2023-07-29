package atlantis.map.region;

import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;

import java.util.Objects;

public class ARegionBoundary implements HasPosition {

    private ARegion region;
    private APosition position;

    private ARegionBoundary(ARegion region, APosition position) {
        this.region = region;
        this.position = position;
    }

    public static ARegionBoundary create(ARegion region, APosition position) {
        ARegionBoundary boundary = new ARegionBoundary(region, position);
        return boundary;
    }

    // =========================================================

    @Override
    public APosition position() {
        return position;
    }

    @Override
    public int x() {
        return position.getX();
    }

    @Override
    public int y() {
        return position.getY();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof HasPosition)) return false;
        return position.equals(((HasPosition) o).position());
    }

    @Override
    public int hashCode() {
        return Objects.hash(position.hashCode());
    }

    @Override
    public String toString() {
        return "ARegionBoundary{" +
                "position=" + position +
                '}';
    }

    // =========================================================



}
