package atlantis.production.constructing.position.base;

import atlantis.config.AtlantisRaceConfig;
import atlantis.map.position.APosition;
import atlantis.production.constructing.position.APositionFinder;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import atlantis.util.cache.Cache;

public class NextBasePosition {
    protected static Cache<APosition> cache = new Cache<>();

    public static APosition nextBasePosition() {
        return cache.get(
            "nextBasePosition",
            273,
            () -> {
                AUnit builder = Select.ourWorkers().first();
                AUnitType building = AtlantisRaceConfig.BASE;

                return APositionFinder.findStandardPosition(
                    builder,
                    building,
                    FindPositionForBaseNearestFree.find(building, builder, null),
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
