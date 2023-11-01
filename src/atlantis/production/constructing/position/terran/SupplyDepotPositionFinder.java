package atlantis.production.constructing.position.terran;

import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.map.position.Positions;
import atlantis.production.constructing.Construction;
import atlantis.production.constructing.position.APositionFinder;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.util.cache.Cache;

import static atlantis.units.AUnitType.Terran_Supply_Depot;

public class SupplyDepotPositionFinder {
    private static Cache<APosition> cache = new Cache<>();

    public static APosition findPosition(AUnit builder, Construction construction, HasPosition nearTo) {
        if (Count.ofType(Terran_Supply_Depot) <= 3) return null;

        return cache.get(
            "findPosition:" + builder.id() + "," + construction.id() + "," + nearTo,
            81,
            () -> findNewPosition(builder, construction, nearTo)
        );
    }

    private static APosition findNewPosition(AUnit builder, Construction construction, HasPosition nearTo) {
//        nearTo = nearTo();

        return APositionFinder.findStandardPosition(builder, AUnitType.Terran_Supply_Depot, nearTo, 20);
    }

//    private static APosition nearTo() {
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
//    }
}
