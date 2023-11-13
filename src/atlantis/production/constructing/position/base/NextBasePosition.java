package atlantis.production.constructing.position.base;

import atlantis.config.AtlantisRaceConfig;
import atlantis.map.position.APosition;
import atlantis.production.constructing.position.APositionFinder;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import atlantis.util.cache.Cache;
import atlantis.util.log.ErrorLog;

public class NextBasePosition {
    protected static Cache<APosition> cache = new Cache<>();

    public static APosition nextBasePosition() {
        return cache.get(
            "nextBasePosition",
            273,
            () -> {
                AUnit builder = Select.ourWorkers().first();
                AUnitType building = AtlantisRaceConfig.BASE;
                APosition basePosition = FindPositionForBaseNearestFree.find(building, builder, null);

                if (basePosition == null) {
                    ErrorLog.printMaxOncePerMinutePlusPrintStackTrace("Null base position, exiting");
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
}
