package tests.fakes;

import atlantis.map.base.ABaseLocation;
import atlantis.map.base.BaseLocations;
import atlantis.map.position.APosition;
import atlantis.map.position.Positions;
import atlantis.map.choke.AChoke;
import atlantis.map.region.ARegion;
import atlantis.map.region.Regions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class FakeRegion extends ARegion {
    private static Map<ABaseLocation, FakeRegion> regions = new HashMap<>();
    private final ABaseLocation location;

    public FakeRegion(ABaseLocation location) {
        this.location = location;
    }

    // =========================================================

    /**
     * Installs the stub world's answer to "which region is this tile in", so
     * {@link Regions} does not have to know that fakes exist.
     */
    public static void installAsSource() {
        Regions.useSource((tx, ty) -> getByTxTy(tx, ty));
    }

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

    /**
     * The stub world has no BWEM areas, so every inherited method that touches
     * {@code area} would throw NPE. Overriding them here keeps the fakes honest
     * about what they know: one region per base location, and nothing else.
     * Without this, {@code Chokes.mainChoke()} -> {@code DefineMainChoke} ->
     * {@code ARegion.getReachableRegions()} exploded in every test that asked
     * for a building position.
     */
    @Override
    public List<ARegion> getReachableRegions() {
        return new ArrayList<>();
    }

    @Override
    public List<AChoke> chokes() {
        return new ArrayList<>();
    }

    @Override
    public List<ABaseLocation> getBaseLocations() {
        return new ArrayList<>(Collections.singletonList(location));
    }

    @Override
    public boolean isReachable(ARegion otherRegion) {
        return false;
    }

    @Override
    public double apprxWidth() {
        return 0;
    }

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
