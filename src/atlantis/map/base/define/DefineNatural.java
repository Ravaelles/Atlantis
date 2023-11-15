package atlantis.map.base.define;

import atlantis.map.base.ABaseLocation;
import atlantis.map.base.Bases;
import atlantis.map.position.APosition;
import atlantis.map.position.Positions;
import atlantis.units.AUnit;
import atlantis.units.select.Select;
import atlantis.util.cache.Cache;

public class DefineNatural {
    private static Cache<Object> cache = new Cache<>();

    /**
     * Returns nearest base location (by the actual ground distance) to the given base location.
     */
    public static APosition natural() {
        return (APosition) cache.get(
            "natural",
            -1,
            () -> {
                if (Select.main() == null) {
                    return null;
                }

                ABaseLocation naturalLocation = natural(Select.main().position());
                if (naturalLocation != null) {
                    return naturalLocation.position();
                }

                return null;
            }
        );
    }

    /**
     * Returns nearest base location (by the actual ground distance) to the given base location.
     */
    public static ABaseLocation natural(APosition nearestTo) {
        return (ABaseLocation) cache.get(
            "natural:" + nearestTo,
            -1,
            () -> {
                // Get all base locations, sort by being closest to given nearestTo position
                Positions<ABaseLocation> baseLocations = new Positions<>();
                baseLocations.addPositions(Bases.baseLocations());
                baseLocations.sortByGroundDistanceTo(nearestTo, true);

                AUnit main = Select.mainOrAnyBuilding();

                for (ABaseLocation baseLocation : baseLocations.list()) {
//            if (baseLocation.isStartLocation() || !nearestTo.hasPathTo(baseLocation.position())) {
                    if ((main == null || main.distTo(baseLocation) > 5) && !nearestTo.hasPathTo(baseLocation.position())) {
                        continue;
                    }
                    return baseLocation;
                }

                return null;
            }
        );
    }
}
