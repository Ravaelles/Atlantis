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
        region.id = area.id.val;
        region.area = area;
        region.position = APosition.create(area.getBoundingBoxSize());;

        instances.put(area, region);

        return region;
    }

    // =========================================================

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

    public APosition center() {
        if (center == null) {
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
            if (
                (regions.first == this || regions.second == otherRegion)
                    && (regions.first == otherRegion || regions.second == otherRegion)
            ) {
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
