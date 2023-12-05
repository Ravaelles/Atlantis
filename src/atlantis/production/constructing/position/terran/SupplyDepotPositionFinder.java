package atlantis.production.constructing.position.terran;

import atlantis.game.A;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.map.position.Positions;
import atlantis.map.region.MainRegion;
import atlantis.production.constructing.Construction;
import atlantis.production.constructing.position.APositionFinder;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.util.cache.Cache;
import atlantis.util.cache.CacheKey;

import static atlantis.units.AUnitType.Terran_Supply_Depot;

public class SupplyDepotPositionFinder {
    private static Cache<APosition> cache = new Cache<>();

    public static APosition findPosition(AUnit builder, Construction construction, HasPosition nearTo) {
//        if (Count.ofType(Terran_Supply_Depot) <= 3) return null;

        return cache.get(
            CacheKey.create("findPosition", builder, construction, nearTo),
            91,
            () -> findNewPosition(builder, construction, nearTo)
        );
    }

    private static APosition findNewPosition(AUnit builder, Construction construction, HasPosition nearTo) {
        nearTo = nearTo();

        return APositionFinder.findStandardPosition(builder, AUnitType.Terran_Supply_Depot, nearTo, 50);
    }

    private static HasPosition nearTo() {
        if (A.supplyTotal() <= 30) return Select.mainOrAnyBuilding();

        if (A.chance(40)) return Select.ourOfType(Terran_Supply_Depot).last();
//        else return Select.ourBuildings().random();
        return MainRegion.mainRegion().center();

//        Positions<APosition> positions = BuildablePositionsAroundMainBase.get()
//            .sortByDistanceTo(Select.mainOrAnyBuilding(), true);
//
//        for (APosition position : positions.list()) {
//            if (position.isBuildable()) {
//                return position;
//            }
//        }
//
//        return null;
    }
}
