package atlantis.map.base.define;

import atlantis.config.env.Env;
import atlantis.game.A;
import atlantis.map.base.ABaseLocation;
import atlantis.map.base.BaseLocations;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.map.position.Positions;
import atlantis.units.select.Select;
import atlantis.util.cache.Cache;
import atlantis.util.log.ErrorLog;

public class DefineNaturalBase {
    private static Cache<Object> cache = new Cache<>();

    /**
     * Returns nearest base location (by the actual ground distance) to the given base location.
     */
    public static APosition natural() {
        return (APosition) cache.get(
            "natural",
            -1,
            () -> {
                ABaseLocation naturalLocation = naturalIfMainIsAt(Select.mainOrAnyBuildingPosition());

                if (naturalLocation == null && !A.isUms()) {
                    ErrorLog.printMaxOncePerMinutePlusPrintStackTrace("Natural base can not be determined");
                }

                return naturalLocation != null ? naturalLocation.position() : null;
            }
        );
    }

    /**
     * Returns nearest base location (by the actual ground distance) to the given base location.
     */
    public static ABaseLocation naturalIfMainIsAt(HasPosition nearestTo) {
        if (nearestTo == null) return null;

        return (ABaseLocation) cache.get(
            "naturalIfMainIsAt:" + nearestTo,
            -1,
            () -> {
                // Get all base locations, sort by being closest to given nearestTo position
                Positions<ABaseLocation> baseLocations = new Positions<>();
                baseLocations.addPositions(BaseLocations.baseLocations());
                baseLocations.sortByGroundDistanceTo(nearestTo, true);

                for (ABaseLocation baseLocation : baseLocations.list()) {
                    if (nearestTo.distTo(baseLocation) <= 6 || !isConnected(nearestTo, baseLocation)) {
                        continue;
                    }
//                    if (!Chokes.fullfillsConditionsForNatural(choke, Chokes.mainChoke())) continue;

                    return baseLocation;
                }

                return null;
            }
        );
    }

    private static boolean isConnected(HasPosition nearestTo, ABaseLocation baseLocation) {
        if (Env.isTesting()) return true;

        return baseLocation.position().hasPathTo(nearestTo);
    }
}
