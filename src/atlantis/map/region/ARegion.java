package atlantis.map.region;

import atlantis.map.base.ABaseLocation;
import atlantis.map.choke.AChoke;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.map.position.Positions;
import atlantis.util.cache.Cache;
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

    private Area area;
    private APosition center;

    public static ARegion create(Area area) {
        if (instances.containsKey(area)) {
            return instances.get(area);
        }

        ARegion region = new ARegion();
        region.area = area;

        instances.put(area, region);

        return region;
    }

    // =========================================================

    @Override
    public APosition position() {
        return APosition.create(center());
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
        if (!(o instanceof ARegion)) return false;
        if (area == null) {
//            System.err.println("Region area is NULL");
            return false;
        }
        ARegion aRegion = (ARegion) o;
        return area.equals(aRegion.area);
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
        if (center == null && area != null) {
            center = APosition.create(
                (area.getTopLeft().x + area.getBottomRight().x) / 2,
                (area.getTopLeft().y + area.getBottomRight().y) / 2
            );
        }

        return center;
    }

    public double apprxWidth() {
        return (area.getTopLeft().x + area.getBottomRight().x) / 64.0;
    }

    public List<AChoke> chokes() {
        return area.getChokePoints().stream().map(AChoke::create).collect(Collectors.toList());
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

}
