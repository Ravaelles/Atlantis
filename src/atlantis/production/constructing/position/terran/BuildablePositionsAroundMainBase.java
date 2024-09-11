package atlantis.production.constructing.position.terran;

import atlantis.map.position.APosition;
import atlantis.map.position.Positions;
import atlantis.map.region.ARegion;
import atlantis.map.region.ARegionBoundary;
import atlantis.units.AUnit;
import atlantis.units.select.Select;
import atlantis.util.cache.Cache;

public class BuildablePositionsAroundMainBase {
    private static Cache<Positions<APosition>> cache = new Cache<>();

    public static Positions<APosition> get() {
        return cache.get(
            "get",
            -1,
            () -> define()
        );
    }

    private static Positions<APosition> define() {
        Positions<APosition> raw = PositionsAroundMainBase.get();
        Positions<APosition> result = new Positions<>();

        for (APosition position : raw.list()) {
            APosition buildable = position.makeBuildable(2);
            if (buildable != null) result.addPosition(buildable);
        }

        return result;
    }
}
