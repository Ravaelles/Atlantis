package atlantis.production.constructing.position.base;

import atlantis.game.A;
import atlantis.map.base.BaseLocations;
import atlantis.map.base.define.DefineNaturalBase;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.production.constructing.Construction;
import atlantis.production.constructing.position.APositionFinder;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.util.log.ErrorLog;

public class NearestFreeBase {
    protected static APosition find(AUnitType building, AUnit builder, Construction construction) {
        HasPosition nearTo = defineNearTo(construction);
        double maxDistance = handleMaxDistance(construction);

        APosition nearestBase = APositionFinder.findStandardPosition(
            builder, building, nearTo, maxDistance
        );

        if (nearestBase == null) {
            HasPosition nearestFree = nearToNearestFree();

            if (nearestFree != null) nearestBase = nearestFree.position();
        }

        if (nearestBase == null && Select.ourBases().notEmpty()) {
            ErrorLog.printMaxOncePerMinute(
                "Could not find nearest base."
                    + "\nnearTo = " + nearTo
                    + "\nconstruction = " + construction
            );
            return null;
        }

        return nearestBase;
    }

    private static double handleMaxDistance(Construction construction) {
        if (construction != null) construction.setMaxDistance(3);

        double maxDistance = construction != null && construction.maxDistance() >= 0 ? construction.maxDistance() : 3;
        return maxDistance;
    }

    private static HasPosition defineNearTo(Construction construction) {
        HasPosition nearTo = null;
        int ourBasesCount = Count.basesWithUnfinished();

        if (A.seconds() <= 1500 && ourBasesCount <= 1) {
            nearTo = nearToNatural();
        }

        if (nearTo == null) {
//            nearTo = nearToNearestFree();
            nearTo = nearToMostDistantToEnemy();
        }

        // =========================================================

        if (nearTo == null) {
            if (Count.basesWithUnfinished() <= 3 && !A.isUms()) {
                ErrorLog.printMaxOncePerMinute("findPositionForBase_nearestFreeBase is null");
            }
            return null;
        }

        return nearTo;
    }

    private static HasPosition nearToNearestFree() {
        AUnit mainBase = Select.mainOrAnyBuilding();

        return BaseLocations.expansionFreeBaseLocationNearestTo(mainBase != null ? mainBase.position() : null);
    }

    private static HasPosition nearToMostDistantToEnemy() {
//        AUnit mainBase = Select.mainOrAnyBuilding();

        return BaseLocations.expansionBaseLocationMostDistantToEnemy();
    }

    private static APosition nearToNatural() {
        return DefineNaturalBase.natural();
    }
}
