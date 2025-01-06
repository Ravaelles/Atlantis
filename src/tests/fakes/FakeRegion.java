package tests.fakes;

import atlantis.map.base.ABaseLocation;
import atlantis.map.base.BaseLocations;
import atlantis.map.position.APosition;
import atlantis.map.position.Positions;
import atlantis.map.region.ARegion;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class FakeRegion extends ARegion {
    private static Map<ABaseLocation, FakeRegion> regions = new HashMap<>();
    private final ABaseLocation location;

    public FakeRegion(ABaseLocation location) {
        this.location = location;
    }

    // =========================================================

    public static ARegion getByTxTy(int tx, int ty) {
        Positions<ABaseLocation> basePositions = new Positions<>();
        basePositions.addPositions(BaseLocations.baseLocations());

        ABaseLocation location = basePositions.nearestTo(APosition.create(tx * 32, ty * 32));

        if (regions.containsKey(location)) return regions.get(location);

        FakeRegion region = new FakeRegion(location);
        regions.put(location, region);
        return region;
    }

    // =========================================================

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FakeRegion)) return false;
        if (!super.equals(o)) return false;

        FakeRegion that = (FakeRegion) o;
        return Objects.equals(location, that.location);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + Objects.hashCode(location);
        return result;
    }
}
