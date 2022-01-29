package atlantis.production.constructing.position;

import atlantis.map.ABaseLocation;
import atlantis.map.Bases;
import atlantis.map.position.APosition;
import atlantis.production.constructing.ConstructionOrder;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import atlantis.util.Cache;

public class ASpecialPositionFinder {
    
    /**
     * Constant used as a hint to indicate that base should be built in the nearest base location 
     * (to the main base) that's still free.
     */
    public static final String BASE_AT_NEAREST_FREE = "NEAREST_FREE";
    
    /**
     * Constant used as a hint to indicate that building should be placed in the main base region.
     */
    public static final String NEAR_MAIN = "MAIN";
    
    /**
     * Constant used as a hint to indicate that building should be placed in the chokepoints of the main base.
     */
    public static final String NEAR_MAIN_CHOKEPOINT = "MAIN_CHOKEPOINT";
    
    /**
     * Constant used as a hint to indicate that building should be placed in the "natural" 
     * (also called the "expansion").
     */
    public static final String AT_NATURAL = "NATURAL";

    private static Cache<APosition> cache = new Cache<>();
    
    // =========================================================

    /**
     * Returns build position for next Refinery/Assimilator/Extractor. It will be chosen for the oldest base
     * that doesn't have gas extracting building.
     */
    protected static APosition findPositionForGasBuilding(AUnitType building) {
        AUnit builder = Select.ourWorkers().first();
        for (AUnit base : Select.ourBases().list()) {
            AUnit geyser = Select.neutral().ofType(AUnitType.Resource_Vespene_Geyser).nearestTo(base);

            if (geyser != null && geyser.distTo(base) < 12) {
                APosition position = geyser.translateByPixels(-64, -32);
                return position;
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
                        if (modifier.equals(NEAR_MAIN) || modifier.equals("NEAR_MAIN")) {
                            if (constructionOrder.maxDistance() < 0) {
                                constructionOrder.setMaxDistance(40);
                            }
                            return findPositionForBase_nearMainBase(building, builder, constructionOrder);
                        } else if (modifier.equals(AT_NATURAL)) {
                            if (constructionOrder.maxDistance() < 0) {
                                constructionOrder.setMaxDistance(30);
                            }
                            return findPositionForBase_natural(building, builder, constructionOrder);
                        }
                    }

                    return findPositionForBase_nearestFreeBase(building, builder, constructionOrder);
                }
        );
    }

    // =========================================================
    
    private static APosition findPositionForBase_nearestFreeBase(AUnitType building, AUnit builder, ConstructionOrder constructionOrder) {
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

    private static APosition findPositionForBase_nearMainBase(AUnitType building, AUnit builder, ConstructionOrder constructionOrder) {
        APosition near = Select.main().translateByPixels(-64, -64);
//        APosition near = Select.mainBase().position();

        constructionOrder.setNearTo(near);
        constructionOrder.setMaxDistance(40);
        
        return APositionFinder.findStandardPosition(builder, building, near, constructionOrder.maxDistance());
    }

    private static APosition findPositionForBase_natural(AUnitType building, AUnit builder, ConstructionOrder constructionOrder) {
        APosition near = APosition.create(
                Bases.expansionFreeBaseLocationNearestTo(Select.main().position())
        ).translateByPixels(0, 0);
//        APosition near = APosition.create(Bases.getExpansionFreeBaseLocationNearestTo(Select.mainBase().position()).position());

        constructionOrder.setNearTo(near);
        constructionOrder.setMaxDistance(4);
        
        return APositionFinder.findStandardPosition(builder, building, near, constructionOrder.maxDistance());
    }

}
