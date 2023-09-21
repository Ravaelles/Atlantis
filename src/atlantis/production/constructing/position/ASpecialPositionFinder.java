package atlantis.production.constructing.position;

import atlantis.game.A;
import atlantis.map.base.ABaseLocation;
import atlantis.map.choke.AChoke;
import atlantis.map.base.Bases;
import atlantis.map.choke.Chokes;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.production.constructing.Construction;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import atlantis.util.We;
import atlantis.util.cache.Cache;

public class ASpecialPositionFinder {

    private static Cache<APosition> cache = new Cache<>();

    // =========================================================

    /**
     * Returns build position for next base. It will usually be next free BaseLocation that doesn't have base
     * built.
     */
    public static APosition findPositionForBase(AUnitType building, AUnit builder, Construction construction) {
        return cache.get(
            "findPositionForBase:" + builder + "," + construction.id(),
            53,
            () -> {
                String modifier = construction.productionOrder() != null ?
                    construction.productionOrder().getModifier() : null;

                APosition position = null;
                if (modifier != null) {
                    position = positionModifierToPosition(modifier, building, builder, construction);
                }

                if (position == null) {
                    System.err.println("");
                    System.err.println(construction.productionOrder());
                    System.err.println(construction.maxDistance());
                    System.err.println("=== Base location error /" + modifier + "/ ===");

                    position = positionModifierToPosition("", building, builder, construction);
                    if (position != null) {
                        System.err.println("Used fix to build base anywhere.");
                    }
                }

                APosition result = findPositionForBase_nearestFreeBase(building, builder, construction);
//                System.err.println("result = " + result);
//                System.err.println("Bases.natural() = " + Bases.natural());

                if (result == null && We.zerg()) {
                    A.errPrintln("Fallback to standard building position");
                    return APositionFinder.findStandardPosition(builder, building, Select.main(), 50);
                }

                if (result != null) {
                    cache.clear();
                }

                return result;
            }
        );
    }

    public static APosition positionModifierToPosition(
        String modifier, AUnitType building, AUnit builder, Construction construction
    ) {
        if (modifier.equals(PositionModifier.MAIN) || modifier.equals("MAIN")) {
            if (construction.maxDistance() < 0) {
                construction.setMaxDistance(40);
            }
            return findPositionForBase_nearMainBase(building, builder, construction);
        }
        else if (modifier.equals(PositionModifier.NATURAL)) {
            if (construction.maxDistance() < 0) {
                construction.setMaxDistance(20);
            }
            return findPositionForBase_natural(building, builder);
        }

        if (Select.main() == null) {
            return null;
        }

        if (modifier.equals(PositionModifier.MAIN_CHOKE)) {
            AChoke mainChoke = Chokes.mainChoke();
            if (mainChoke != null) {
                return APosition.create(mainChoke.center()).translateTilesTowards(Select.main(), 3.3);
            }
        }
        else if (modifier.equals(PositionModifier.NATURAL_CHOKE)) {
            AChoke chokepointForNatural = Chokes.natural(Select.main().position());
            if (chokepointForNatural != null && Select.main() != null) {
                ABaseLocation natural = Bases.natural(Select.main().position());
                return APosition.create(chokepointForNatural.center()).translateTilesTowards(natural, 5);
            }
        }

        return null;
    }

    // =========================================================

    protected static APosition findPositionForBase_nearestFreeBase(AUnitType building, AUnit builder, Construction construction) {
//        ABaseLocation baseLocationToExpand;
        HasPosition near;
        int ourBasesCount = Select.ourBases().count();

        if (A.seconds() <= 800 && ourBasesCount <= 1) {
            near = Bases.natural();
        }
//        else if (ourBasesCount <= 2) {
        else {
            AUnit mainBase = Select.main();
            near = Bases.expansionFreeBaseLocationNearestTo(mainBase != null ? mainBase.position() : null);
        }
//        else {
//            baseLocationToExpand = Bases.expansionBaseLocationMostDistantToEnemy();
//        }

        if (near == null) {
            if (ourBasesCount <= 2) {
                System.err.println("baseLocationToExpand is null");
            }
            return null;
        }

//        APosition near = APosition.create(baseLocationToExpand.position()).translateByPixels(-64, -48);
//        near = APosition.create(baseLocationToExpand.position());
        construction.setMaxDistance(3);

        return APositionFinder.findStandardPosition(
            builder, building, near, construction.maxDistance()
        );
    }

    protected static APosition findPositionForBase_nearMainBase(AUnitType building, AUnit builder, Construction construction) {
        APosition near = Select.main().translateByPixels(-64, -64);

        construction.setNearTo(near);
        construction.setMaxDistance(15);

//        if (Select.main() != null) System.err.println("near = " + near + ", distToMain = " + A.dist(Select.main(), near));
//        if (true) A.printStackTrace("findPositionForBase_nearMainBase");

        return APositionFinder.findStandardPosition(builder, building, near, construction.maxDistance());
    }

    protected static APosition findPositionForBase_natural(AUnitType building, AUnit builder) {
        APosition near = Bases.natural();

        if (near == null) {
            System.err.println("Unknown natural position - it will break base position");
            return null;
        }

//        if (Select.main() != null) System.err.println("near NAT = " + near + ", distToMain = " + A.dist(Select.main(), near));
//        if (true) A.printStackTrace("findPositionForBase_natural");

        return APositionFinder.findStandardPosition(builder, building, near, 5);
    }

}
