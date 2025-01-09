package atlantis.production.constructions.position.base;

import atlantis.config.AtlantisRaceConfig;
import atlantis.game.A;
import atlantis.map.base.ABaseLocation;
import atlantis.map.base.BaseLocations;
import atlantis.map.position.APosition;
import atlantis.production.constructions.position.APositionFinder;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import atlantis.units.workers.FreeWorkers;
import atlantis.util.cache.Cache;
import atlantis.util.log.ErrorLog;

public class NextBasePosition {
    protected static Cache<APosition> cache = new Cache<>();

    public static APosition nextBasePosition() {
        return cache.get(
            "nextBasePosition",
            273,
            () -> {
                AUnit builder = FreeWorkers.get().first();
                AUnitType building = AtlantisRaceConfig.BASE;

//                APosition basePosition = FindPositionForBaseNearestFree.find(building, builder, null);
                APosition basePosition = findApproximate(building, builder);

                if (basePosition == null) {
                    if (!A.isUms()) ErrorLog.printMaxOncePerMinutePlusPrintStackTrace("Null base position, exiting");
                    return null;
                }

                return APositionFinder.findStandardPosition(
                    builder,
                    building,
                    basePosition,
                    3
                );
            }

//            () -> FindPositionForBase.findPositionForBase_nearestFreeBase(
//                AtlantisRaceConfig.BASE, Select.ourWorkers().first(), null
//            )
//            () -> FindPositionForBaseNearestFree.find(
//                AtlantisRaceConfig.BASE, Select.ourWorkers().first(), null
//            )
        );
    }

    private static APosition findApproximate(AUnitType building, AUnit builder) {
        ABaseLocation baseLocation = BaseLocations.expansionFreeBaseLocationNearestTo(Select.mainOrAnyBuilding());

        return baseLocation != null ? baseLocation.position() : null;
    }
}
