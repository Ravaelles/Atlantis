package atlantis.map;

import atlantis.position.APosition;
import atlantis.position.HasPosition;
import bwapi.Pair;
import bwta.Chokepoint;
import bwta.Region;

import java.util.Objects;

public class AChokepoint implements HasPosition {

    private Chokepoint chokepoint;

    public static AChokepoint create(Chokepoint chokepoint) {
        AChokepoint aChokepoint = new AChokepoint();
        aChokepoint.chokepoint = chokepoint;

        return aChokepoint;
    }


    // =========================================================

    @Override
    public APosition getPosition() {
        return APosition.create(chokepoint.getCenter());
    }

    @Override
    public int getX() {
        return getPosition().getX();
    }

    @Override
    public int getY() {
        return getPosition().getY();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AChokepoint)) return false;
        AChokepoint that = (AChokepoint) o;
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

}
