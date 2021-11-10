package atlantis.production.constructing.position;

import atlantis.AGame;
import atlantis.combat.micro.zerg.ZergCreepColony;
import atlantis.position.HasPosition;
import atlantis.production.constructing.ConstructionOrder;
import atlantis.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;

public class APositionFinder {

    public static int totalRequests = 0;
    
//    protected static AUnitType building;
//    protected static Position nearTo;
//    protected static double maxDistance;

    // =========================================================

    /**
     * Returns build position for next building of given type.
     */
    public static APosition getPositionForNew(AUnit builder, AUnitType building, ConstructionOrder constructionOrder) {
        HasPosition near = constructionOrder != null ? constructionOrder.getNearTo() : null;
        double maxDistance = constructionOrder != null ? constructionOrder.getMaxDistance() : 35;
        return getPositionForNew(builder, building, constructionOrder, near, maxDistance);
    }

    /**
     * Returns build position for next building of given type. If <b>nearTo</b> is not null, it forces to find
     * position
     * <b>maxDistance</b> build tiles from given position.
     */
    public static APosition getPositionForNew(
            AUnit builder, AUnitType building,
            ConstructionOrder constructionOrder,
            HasPosition nearTo, double maxDistance
    ) {
        totalRequests++;
        constructionOrder.setMaxDistance(maxDistance);

        // =========================================================
        // GAS extracting buildings

        if (building.isGasBuilding()) {
            return ASpecialPositionFinder.findPositionForGasBuilding(building);
        } 

        // =========================================================
        // BASE

        else if (building.isBase()) {
            return ASpecialPositionFinder.findPositionForBase(building, builder, constructionOrder);
        }

        // =========================================================
        // BUNKER

        else if (building.isBunker()) {
            return TerranBunkerPositionFinder.findPosition(building, builder, constructionOrder);
        } 

        // =========================================================
        // Creep colony

        else if (building.equals(AUnitType.Zerg_Creep_Colony)) {
            return ZergCreepColony.findPosition(building, builder, constructionOrder);
        } 

        // =========================================================
        // STANDARD BUILDINGS
        else {

            // If we didn't specify location where to build, build somewhere near the main base
            if (nearTo == null) {
                if (AGame.isPlayingAsZerg()) {
//                    nearTo = Select.secondBaseOrMainIfNoSecond().getPosition();
                    nearTo = Select.main().position();
                }
                else {
//                    nearTo = Select.mainBase().getPosition();
                    AUnit randomBase = Select.ourBases().random();
                    nearTo = randomBase != null ? randomBase.position() : Select.our().first().position();
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
        }

        else {
            System.err.println("Invalid race: " + AGame.getPlayerUs().getRace());
            System.exit(-1);
            return null;
        }
    }

}
