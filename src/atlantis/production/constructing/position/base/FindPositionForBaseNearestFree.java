package atlantis.production.constructing.position.base;

import atlantis.game.A;
import atlantis.map.position.APosition;
import atlantis.production.constructing.Construction;
import atlantis.production.constructing.position.APositionFinder;
import atlantis.production.constructing.position.ASpecialPositionFinder;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import atlantis.util.We;
import atlantis.util.cache.Cache;

public class FindPositionForBaseNearestFree {
    /**
     * Returns build position for next base. It will usually be next free BaseLocation that doesn't have base
     * built.
     */
    public static APosition find(AUnitType building, AUnit builder, Construction construction) {
        return FindPositionForBase.cache.get(
            "findPositionForBase:" + builder + "," + (construction != null ? construction.id() : "-"),
            53,
            () -> {
                String modifier = construction != null && construction.productionOrder() != null ?
                    construction.productionOrder().getModifier() : null;

                APosition position = null;
                if (modifier != null) {
                    position = ASpecialPositionFinder.positionModifierToPosition(modifier, building, builder, construction);
                }

                if (position == null) {
//                    System.err.println("");
//                    System.err.println(construction.productionOrder());
//                    System.err.println(construction.maxDistance());
//                    System.err.println("=== Base location error /" + modifier + "/ ===");

                    position = ASpecialPositionFinder.positionModifierToPosition("", building, builder, construction);
                    if (position != null) {
                        System.err.println("Used fix to build base anywhere.");
                    }
                }

                APosition result = FindPositionForBase.findPositionForBase_nearestFreeBase(building, builder, construction);
//                System.err.println("result = " + result);
//                System.err.println("Bases.natural() = " + Bases.natural());

                if (result == null && We.zerg()) {
                    A.errPrintln("Fallback to standard building position");
                    return APositionFinder.findStandardPosition(builder, building, Select.main(), 50);
                }

                if (result != null) {
                    FindPositionForBase.cache.clear();
                }

                return result;
            }
        );
    }
}
