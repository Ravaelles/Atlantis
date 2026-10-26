package atlantis.map.region;

import atlantis.map.base.ABaseLocation;
import atlantis.map.choke.AChoke;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.map.position.Positions;
import atlantis.util.cache.Cache;
import bwapi.Pair;
import bwem.Area;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ARegion implements HasPosition {
    private static HashMap<Area, ARegion> instances = new HashMap<>();
    private Cache<ArrayList<ARegionBoundary>> cachePolygons = new Cache<>();

//    private Region region;
//
//    public static ARegion create(Region region) {
//        ARegion aRegion = new ARegion();
//        aRegion.region = region;
//
//        return aRegion;
//    }

    private int id;
    private Area area;
    private APosition center;
    private APosition position = null;

    public static ARegion create(Area area) {
        if (instances.containsKey(area)) {
            return instances.get(area);
        }

        ARegion region = new ARegion();
        region.id = area.getId().intValue();
        region.area = area;
        region.position = APosition.create(area.getBoundingBoxSize());

        instances.put(area, region);

        return region;
    }

    // =========================================================

    /**
     * Test seam: stub regions have no BWEM area, so `create` never runs
     * for them and every id stays 0 - and `equals` compares ids, which
     * made every stub region equal to every other (a main-region-only
     * rule could never fire in tests). Production ids come from areas.
     */
    protected final void setIdForTests(int id) {
        this.id = id;
    }

    @Override
    public APosition position() {
        return position;
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
    public boolean equals(HasPosition o) {
        if (this == o) return true;
        if (!(o instanceof ARegion)) return false;

        return this.id == ((ARegion) o).id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(area);
    }

    @Override
    public String toString() {
        return "ARegion{" +
            "center=" + center() +
            '}';
    }

    // =========================================================

    public ARegionBoundary nearestBoundary(HasPosition nearestTo) {
        Positions<ARegionBoundary> boundaries = new Positions<>(boundaries());

        return boundaries.nearestTo(nearestTo);
    }

    public ArrayList<ARegionBoundary> boundaries() {
        return cachePolygons.get(
            "bounds",
            -1,
            () -> ARegionBoundaryCalculator.forRegion(this)
        );
    }

    /**
     * Bounding-box size in tiles, or null without map data. The box
     * overestimates winding regions (peninsulas read wide) - the 8-dir
     * ray probe, not this number, decides real narrowness.
     */
    public double[] boundingBoxTiles() {
        try {
            if (area == null) return null;

            int w = area.getBottomRight().x - area.getTopLeft().x;
            int h = area.getBottomRight().y - area.getTopLeft().y;
            if (w <= 0 || h <= 0) return null;

            return new double[] { w, h };
        } catch (Exception ignored) {
            return null;
        }
    }

    public APosition center() {
        if (center == null) {
            try {
                if (area != null && area.getTopLeft().x < 2000000) {
//                System.err.println("area.getTopLeft().x = " + area.getTopLeft().x);
//                System.err.println("area.getBottomRight().x = " + area.getBottomRight().x);
//                System.err.println("area.getTopLeft().y = " + area.getTopLeft().y);
//                System.err.println("area.getBottomRight().y = " + area.getBottomRight().y);
                center = APosition.create(
                    (area.getTopLeft().x + area.getBottomRight().x) * 32 / 2,
                    (area.getTopLeft().y + area.getBottomRight().y) * 32 / 2
                );
//                System.err.println("center CALC = " + center);

                if (!center.isWalkable()) {
                    center = center.makeWalkable(8, 2, this);
//                    System.err.println("WALKABLE center = " + center);
                }
                }
            } catch (Exception ignored) {
                center = null;
            }
            if (center == null) {
                // Degenerate bounding box (UMS maps carry sentinel boxes
                // the guard above rejects): the highest-altitude walk
                // tile is inside the area by construction, so it needs
                // no box at all. Without this every center reads null
                // and the whole ghost chain parks in silence.
                try {
                    if (area != null && area.getTop() != null) {
                        center = APosition.create(area.getTop());
                    }
                } catch (Exception ignored) {
                    center = null;
                }
            }
        }

        return center;
    }

    public double apprxWidth() {
        return (area.getTopLeft().x + area.getBottomRight().x) / 64.0;
    }

    public List<AChoke> chokes() {
        return area.getChokePoints().stream().map(AChoke::from).collect(Collectors.toList());
    }

    public List<ABaseLocation> getBaseLocations() {
        return area.getBases().stream().map(ABaseLocation::create).collect(Collectors.toList());
    }

    public boolean isReachable(ARegion otherRegion) {
        return area.isAccessibleFrom(otherRegion.area);
    }

    public List<ARegion> getReachableRegions() {
        return area.getAccessibleNeighbors().stream().map(ARegion::create).collect(Collectors.toList());
    }

    public AChoke chokeBetween(ARegion otherRegion) {
        if (otherRegion == null) {
            return null;
        }

        List<AChoke> chokes = chokes();
        for (AChoke choke : chokes) {
            Pair<ARegion, ARegion> regions = choke.regions();

            // Accessors, not the public fields: `Pair.first`/`second` were public in the
            // old vendored jar and are private in stock JBWAPI 2.2.0, while `getFirst()`/
            // `getSecond()` exist in both - so this stayed compilable across the swap
            // instead of turning a library upgrade into a code change.
            ARegion first = regions.getFirst();
            ARegion second = regions.getSecond();

            // The condition this used to carry was wrong and could not match a choke
            // unless the two regions were the same object:
            //   (first == this || second == otherRegion) && (first == otherRegion || second == otherRegion)
            // mixes `this` into the first half and `otherRegion` into both, so for
            // distinct regions it required `second == otherRegion` twice and never
            // checked that `this` was on the choke at all. The question the method name
            // asks is "does this choke join these two regions?", i.e. one side is us and
            // the other is the region we were asked about - in either order.
            boolean joinsThem = (first == this && second == otherRegion)
                || (second == this && first == otherRegion);

            if (joinsThem) {
                return choke;
            }
        }

        return null;
    }

    public boolean isIsland() {
        if (area == null) return false;

        if (area.getAccessibleNeighbors() == null) return true;

        return area.getAccessibleNeighbors().isEmpty();
    }
}
