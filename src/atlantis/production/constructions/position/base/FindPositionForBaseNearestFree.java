package atlantis.production.constructions.position.base;

import atlantis.game.A;
import atlantis.map.base.ABaseLocation;
import atlantis.map.base.BaseLocations;
import atlantis.map.position.APosition;
import atlantis.map.position.Positions;
import atlantis.production.constructions.Construction;
import atlantis.production.constructions.position.APositionFinder;
import atlantis.production.constructions.position.ASpecialPositionFinder;
import atlantis.production.constructions.position.modifier.PositionModifier;
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
            113,
            () -> {
                FindPositionForBase.cache.clear();

                APosition position = null;
                String modifier = modifier(construction);

                if (modifier != null) {
                    position = positionIfHasModifier(building, builder, construction, modifier);
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

                APosition result = null;

                result = returnNatural(result);

                if (result == null) {
//                    System.out.println("### NEAREST");
                    result = OurNextFreeExpansionMostDistantToEnemy.find();
                }

//                APosition result = NearestFreeBase.find(building, builder, construction);
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

    private static APosition returnNatural(APosition result) {
        APosition natural = BaseLocations.natural();

//                if (A.s <= 600 && Count.basesWithUnfinished() <= 1) {
        if (Select.ourBasesWithUnfinished().countInRadius(10, natural) == 0) {
//            System.out.println("---- USE NATURAL");
            result = BaseLocations.natural();
        }

        return result;
    }

    private static APosition positionIfHasModifier(AUnitType building, AUnit builder, Construction construction, String modifier) {
        return PositionModifier.toPosition(modifier, building, builder, construction);
    }

    private static String modifier(Construction construction) {
        return construction != null && construction.productionOrder() != null ?
            construction.productionOrder().getModifier() : null;
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
