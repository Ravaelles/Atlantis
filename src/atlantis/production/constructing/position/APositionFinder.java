package atlantis.production.constructing.position;

import atlantis.AGame;
import atlantis.combat.micro.zerg.ZergCreepColony;
import atlantis.position.APosition;
import atlantis.position.HasPosition;
import atlantis.production.constructing.ConstructionOrder;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.util.Cache;
import atlantis.util.We;

public class APositionFinder {

    public static Cache<APosition> cache = new Cache<>();

    // =========================================================

    /**
     * Returns build position for next building of given type.
     */
    public static APosition findPositionForNew(AUnit builder, AUnitType building, ConstructionOrder constructionOrder) {
        HasPosition near = constructionOrder != null ? constructionOrder.nearTo() : null;
        double maxDistance = constructionOrder != null ? constructionOrder.maxDistance() : 35;
        return findPositionForNew(builder, building, constructionOrder, near, maxDistance);
    }

    /**
     * Returns build position for next building of given type. If <b>nearTo</b> is not null, it forces to find
     * position
     * <b>maxDistance</b> build tiles from given position.
     */
    public static APosition findPositionForNew(
            AUnit builder, AUnitType building,
            ConstructionOrder constructionOrder,
            HasPosition nearTo, double maxDistance
    ) {
//        totalRequests++;
        constructionOrder.setMaxDistance(maxDistance);

        // =========================================================
        // GAS extracting buildings

        if (building.isGasBuilding()) {
            return ASpecialPositionFinder.findPositionForGasBuilding(building);
        } 

        // =========================================================
        // BASE

        else if (building.isBase()) {
            if (We.zerg()) {
                if (Count.bases() >= 3) {
                    return findStandardPosition(builder, building, nearTo, 30);
                }
            }

            return ASpecialPositionFinder.findPositionForBase(building, builder, constructionOrder);
        }

        // =========================================================
        // BUNKER

        else if (building.isBunker()) {
            return TerranBunkerPositionFinder.findPosition(building, builder, constructionOrder);
        } 

        // =========================================================
        // Creep colony

        else if (building.is(AUnitType.Zerg_Creep_Colony)) {
            return ZergCreepColony.findPosition(building, builder, constructionOrder);
        } 

        // =========================================================
        // STANDARD BUILDINGS
        else {

            // If we didn't specify location where to build, build somewhere near the main base
            if (nearTo == null) {
                if (AGame.isPlayingAsZerg()) {
                    nearTo = Select.main().position();
                }
                else {
                    if (Count.bases() >= 3) {
                        nearTo = Select.ourBases().random();
                    } else {
                        nearTo = Select.main().position();
                    }
                }
            }

            // If all of our bases have been destroyed, build somewhere near our first unit alive
            if (nearTo == null) {
                nearTo = Select.our().first().position();
            }

            // Hopeless case, all units have died, just quit.
            if (nearTo == null) {
                return null;
            }

            if (maxDistance < 0) {
                maxDistance = 50;
            }

            // =========================================================
            // Standard place
            
            return findStandardPosition(builder, building, nearTo, maxDistance);
        }
    }
    
    // =========================================================
    
    /**
     * Returns standard build position for building near given position.
     */
    public static APosition findStandardPosition(AUnit builder, AUnitType building, HasPosition nearTo, double maxDistance) {
        return cache.get(
                "findStandardPosition",
                40,
                () -> {


                    // ===========================================================
                    // = Handle standard building position according to the race =
                    // = as every race uses completely different approach        =
                    // ===========================================================

                    // Terran
                    if (AGame.isPlayingAsTerran()) {
                        return TerranPositionFinder.findStandardPositionFor(builder, building, nearTo, maxDistance);
                    }

                    // Protoss
                    else if (AGame.isPlayingAsProtoss()) {
                        return ProtossPositionFinder.findStandardPositionFor(builder, building, nearTo, maxDistance);
                    }

                    // Zerg
                    else if (AGame.isPlayingAsZerg()) {
                        return ZergPositionFinder.findStandardPositionFor(builder, building, nearTo, maxDistance);
                    } else {
                        System.err.println("Invalid race: " + AGame.getPlayerUs().getRace());
                        System.exit(-1);
                        return null;
                    }
                }
        );
    }

}
