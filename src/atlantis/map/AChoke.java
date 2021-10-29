package atlantis.map;

import atlantis.position.APosition;
import atlantis.position.HasPosition;
import bwapi.Pair;
import bwta.Chokepoint;
import bwta.Region;

import java.util.Objects;

public class AChoke implements HasPosition {

    private Chokepoint chokepoint;

    public static AChoke create(Chokepoint chokepoint) {
        AChoke choke = new AChoke();
        choke.chokepoint = chokepoint;

        return choke;
    }


    // =========================================================

    @Override
    public APosition position() {
        return APosition.create(chokepoint.getCenter());
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
        return chokepoint.equals(that.chokepoint);
    }

    @Override
    public int hashCode() {
        return Objects.hash(chokepoint);
    }

    // =========================================================

    public APosition getCenter() {
        return APosition.create(chokepoint.getCenter());
    }

    public int getWidth() {
        return (int) chokepoint.getWidth() / 32;
    }

    public ARegion[] getRegions() {
        Pair<Region, Region> regions = chokepoint.getRegions();

        ARegion[] array = new ARegion[2];
        array[0] = ARegion.create(regions.getLeft());
        array[1] = ARegion.create(regions.getRight());

        return array;
    }

    public ARegion getFirstRegion() {
        return ARegion.create(chokepoint.getRegions().getFirst());
    }

    public ARegion getSecondRegion() {
        return ARegion.create(chokepoint.getRegions().getSecond());
    }

    @Override
    public String toString() {
        return "AChoke{" +
                "width=" + getWidth() +
                ", chokepoint=" + chokepoint.getCenter() +
                '}';
    }
}
