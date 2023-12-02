package atlantis.map.base.define;

import atlantis.map.base.ABaseLocation;
import atlantis.map.base.BaseLocations;
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
                if (Select.main() == null) return null;

                ABaseLocation naturalLocation = naturalIfMainIsAt(Select.main().position());

                return naturalLocation != null ? naturalLocation.position() : null;
            }
        );
    }

    /**
     * Returns nearest base location (by the actual ground distance) to the given base location.
     */
    public static ABaseLocation naturalIfMainIsAt(APosition nearestTo) {
        if (nearestTo == null) return null;

        return (ABaseLocation) cache.get(
            "natural:" + nearestTo,
            -1,
            () -> {
                // Get all base locations, sort by being closest to given nearestTo position
                Positions<ABaseLocation> baseLocations = new Positions<>();
                baseLocations.addPositions(BaseLocations.baseLocations());
                baseLocations.sortByGroundDistanceTo(nearestTo, true);

//                AUnit main = Select.mainOrAnyBuilding();

                for (ABaseLocation baseLocation : baseLocations.list()) {
//            if (baseLocation.isStartLocation() || !nearestTo.hasPathTo(baseLocation.position())) {
                    if (nearestTo.distTo(baseLocation) <= 6 || !nearestTo.hasPathTo(baseLocation.position())) {
                        continue;
                    }

                    return baseLocation;
                }

                return null;
            }
        );
    }
}
