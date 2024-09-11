package atlantis.production.constructing.position.terran;

import atlantis.map.position.APosition;
import atlantis.map.position.Positions;
import atlantis.map.region.ARegion;
import atlantis.map.region.ARegionBoundary;
import atlantis.units.AUnit;
import atlantis.units.select.Select;
import atlantis.util.cache.Cache;

public class PositionsAroundMainBase {
    private static Cache<Positions<APosition>> cache = new Cache<>();

    public static Positions<APosition> get() {
        return cache.get(
            "get",
            -1,
            () -> define()
        );
    }

    private static Positions<APosition> define() {
        Positions<APosition> positions = new Positions<>();

        AUnit main = Select.main();
        if (main == null) return positions;

        ARegion region = main.position().region();
        if (region == null) {
            return positions;
        }

        for (ARegionBoundary regionBoundary : region.boundaries()) {
            positions.addPosition(regionBoundary.position());
        }

        return positions;
    }
}
