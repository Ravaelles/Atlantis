package atlantis.production.constructing.position;

import atlantis.map.ABaseLocation;
import atlantis.map.AChoke;
import atlantis.map.Bases;
import atlantis.map.Chokes;
import atlantis.map.position.APosition;
import atlantis.production.constructing.ConstructionOrder;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Have;
import atlantis.units.select.Select;
import atlantis.util.Cache;

public class ASpecialPositionFinder {

    private static Cache<APosition> cache = new Cache<>();
    
    // =========================================================

    /**
     * Returns build position for next Refinery/Assimilator/Extractor. It will be chosen for the oldest base
     * that doesn't have gas extracting building.
     */
    protected static APosition findPositionForGasBuilding(AUnitType building) {
        for (AUnit base : Select.ourBases().list()) {
            AUnit geyser = Select.neutral().ofType(AUnitType.Resource_Vespene_Geyser).nearestTo(base);

            if (geyser != null && geyser.distTo(base) < 12) {
                return geyser.translateByPixels(-64, -32);
            }
        }

//        System.err.println("Couldn't find geyser for " + building);
        return null;
    }

    /**
     * Returns build position for next base. It will usually be next free BaseLocation that doesn't have base
     * built.
     */
    public static APosition findPositionForBase(AUnitType building, AUnit builder, ConstructionOrder constructionOrder) {
        return cache.get(
                "findPositionForBase:" + builder + "," + constructionOrder.id(),
                50,
                () -> {
                    String modifier = constructionOrder.productionOrder() != null ?
                            constructionOrder.productionOrder().getModifier() : null;

                    System.err.println("");
                    System.err.println(constructionOrder.productionOrder());
                    System.err.println(constructionOrder.maxDistance());
                    System.err.println("=== modifier /" + modifier + "/ ===");
                    if (modifier != null) {
                        return positionModifierToPosition(modifier, building, builder, constructionOrder);
                    }

                    return findPositionForBase_nearestFreeBase(building, builder, constructionOrder);
                }
        );
    }

    public static APosition positionModifierToPosition(String modifier, AUnitType building, AUnit builder, ConstructionOrder constructionOrder) {
        if (modifier.equals(PositionModifier.MAIN) || modifier.equals("MAIN")) {
            if (constructionOrder.maxDistance() < 0) {
                constructionOrder.setMaxDistance(40);
            }
            return findPositionForBase_nearMainBase(building, builder, constructionOrder);
        }
        else if (modifier.equals(PositionModifier.NATURAL)) {
            if (constructionOrder.maxDistance() < 0) {
                constructionOrder.setMaxDistance(30);
            }
            return findPositionForBase_natural(building, builder, constructionOrder);
        }

        if (Select.main() == null) {
            return null;
        }

        if (modifier.equals(PositionModifier.MAIN_CHOKE)) {
            AChoke mainChoke = Chokes.mainChoke();
            if (mainChoke != null) {
                return APosition.create(mainChoke.center()).translateTilesTowards(Select.main(), 3.5);
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

    protected static APosition findPositionForBase_nearestFreeBase(AUnitType building, AUnit builder, ConstructionOrder constructionOrder) {
        ABaseLocation baseLocationToExpand;
        int ourBasesCount = Select.ourBases().count();
        if (ourBasesCount <= 2) {
            AUnit mainBase = Select.main();

            baseLocationToExpand = Bases.expansionFreeBaseLocationNearestTo(mainBase != null ? mainBase.position() : null);
        }
        else {
            baseLocationToExpand = Bases.expansionBaseLocationMostDistantToEnemy();
        }
        
        if (baseLocationToExpand == null) {
            if (ourBasesCount <= 2) {
                System.err.println("baseLocationToExpand is null");
            }
            return null;
        }
        
//        APosition near = APosition.create(baseLocationToExpand.position()).translateByPixels(-64, -48);
        APosition near = APosition.create(baseLocationToExpand.position());
        constructionOrder.setMaxDistance(4);

//        System.out.println("Main base = " + Select.mainBase());
//        System.out.println("baseLocationToExpand = " + baseLocationToExpand);
//        System.out.println(builder + " / " + building + " / " +  APosition.createFrom(baseLocationToExpand.getPosition()));

        return APositionFinder.findStandardPosition(
                builder, building, near, constructionOrder.maxDistance()
        );
    }

    protected static APosition findPositionForBase_nearMainBase(AUnitType building, AUnit builder, ConstructionOrder constructionOrder) {
        APosition near = Select.main().translateByPixels(-64, -64);
//        APosition near = Select.mainBase().position();

        constructionOrder.setNearTo(near);
        constructionOrder.setMaxDistance(40);
        
        return APositionFinder.findStandardPosition(builder, building, near, constructionOrder.maxDistance());
    }

    protected static APosition findPositionForBase_natural(AUnitType building, AUnit builder, ConstructionOrder constructionOrder) {
        if (!Have.main()) {
            return null;
        }

        APosition near = APosition.create(
                Bases.expansionFreeBaseLocationNearestTo(Select.main().position())
        ).translateByPixels(0, 0);
//        APosition near = APosition.create(Bases.getExpansionFreeBaseLocationNearestTo(Select.mainBase().position()).position());

        constructionOrder.setNearTo(near);
        constructionOrder.setMaxDistance(4);
        
        return APositionFinder.findStandardPosition(builder, building, near, constructionOrder.maxDistance());
    }

}
