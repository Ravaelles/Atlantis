package atlantis.map;

import atlantis.position.APosition;
import atlantis.position.HasPosition;
import bwem.Area;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ARegion implements HasPosition {

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
        ARegion region = new ARegion();
        region.area = area;

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
        ARegion aRegion = (ARegion) o;
        return area.equals(aRegion.area);
    }

    @Override
    public int hashCode() {
        return Objects.hash(area);
    }

    // =========================================================

    public APosition center() {
        if (center == null && area != null) {
            center = new APosition(
                    area.getTopLeft().x + area.getBottomRight().x / 2,
                    area.getTopLeft().y + area.getBottomRight().y / 2
            );
        }

        return center;
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
