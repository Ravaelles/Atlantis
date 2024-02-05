package atlantis.production.constructing.position.base;

import atlantis.game.A;
import atlantis.map.base.ABaseLocation;
import atlantis.map.base.BaseLocations;
import atlantis.map.position.APosition;
import atlantis.map.position.Positions;
import atlantis.production.constructing.Construction;
import atlantis.production.constructing.position.APositionFinder;
import atlantis.production.constructing.position.ASpecialPositionFinder;
import atlantis.production.constructing.position.modifier.PositionModifier;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import atlantis.util.We;

public class FindPositionForBaseNearestFree {
    /**
     * Returns build position for next base. It will usually be next free BaseLocation that doesn't have base
     * built.
     */
    public static APosition find(AUnitType building, AUnit builder, Construction construction) {
        return FindPositionForBase.cache.get(
            "findPositionForBase:" + null + "," + (construction != null ? construction.id() : "-"),
            95,
            () -> {
                String modifier = construction != null && construction.productionOrder() != null ?
                    construction.productionOrder().getModifier() : null;

                APosition position = null;
                if (modifier != null) {
//                    position = ASpecialPositionFinder.positionModifierToPosition(modifier, building, builder, construction);
                    position = PositionModifier.toPosition(modifier, building, builder, construction);
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
                    A.errPrintln("Fallback to standard building position for " + building);
                    result = APositionFinder.findStandardPosition(builder, building, Select.main(), 50);
                }

                if (result != null) {
                    FindPositionForBase.cache.clear();
                }

                if (result == null) {
                    result = fallback();
                }

                return result;
            }
        );
    }

    private static APosition fallback() {
        if (Select.main() == null) return null;

        Positions<ABaseLocation> positions = new Positions<>(BaseLocations.baseLocations());
        positions = positions.sortByGroundDistanceTo(Select.mainOrAnyBuilding().position(), true);

        for (ABaseLocation baseLocation : positions.list()) {
            if (Select.all().inRadius(3, baseLocation).empty()) return baseLocation.position();
        }

        return null;
    }
}
