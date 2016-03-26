package atlantis.constructing.position;

import atlantis.AtlantisGame;
import atlantis.combat.micro.zerg.ZergCreepColony;
import atlantis.constructing.ConstructionOrder;
import atlantis.util.UnitUtil;
import atlantis.wrappers.Select;
import bwapi.Position;
import bwapi.Unit;
import bwapi.UnitType;

public class AtlantisPositionFinder {

    protected static UnitType building;
    protected static Position nearTo;
    protected static double maxDistance;

    // =========================================================
    
    /**
     * Returns build position for next building of given type.
     */
    public static Position getPositionForNew(Unit builder, UnitType building, ConstructionOrder constructionOrder) {
        return getPositionForNew(builder, building, constructionOrder, null, -1);
    }

    /**
     * Returns build position for next building of given type. If <b>nearTo</b> is not null, it forces to find
     * position
     * <b>maxDistance</b> build tiles from given position.
     */
    public static Position getPositionForNew(Unit builder, UnitType building, 
            ConstructionOrder constructionOrder, Position nearTo, double maxDistance) {

        // =========================================================
        // Buildings extracting GAS
        if (UnitUtil.isGasBuilding(building)) {
            return AtlantisSpecialPositionFinder.findPositionForGasBuilding(building);
        } 

        // =========================================================
        // BASE
        else if (UnitUtil.isBase(building)) {
            return AtlantisSpecialPositionFinder.findPositionForBase(building, builder, constructionOrder);
        } 

        // =========================================================
        // BASE
        else if (building.equals(UnitType.Zerg_Creep_Colony)) {
            return ZergCreepColony.findPosition(building, builder, constructionOrder);
        } 

        // =========================================================
        // STANDARD BUILDINGS
        else {

            // If we didn't specify location where to build, build somewhere near the main base
            if (nearTo == null) {
                nearTo = Select.mainBase().getPosition();
            }

            // If all of our bases have been destroyed, build somewhere near our first unit alive
            if (nearTo == null) {
                nearTo = Select.our().first().getPosition();
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
    public static Position findStandardPosition(Unit builder, UnitType building, Position nearTo, double maxDistance) {
        
        // =========================================================
        // Handle standard building position according to the race as every race uses completely different approach
        // =========================================================
        
        // Terran
        if (AtlantisGame.playsAsTerran()) {
            return TerranPositionFinder.findStandardPositionFor(builder, building, nearTo, maxDistance);
        } // Protoss
        else if (AtlantisGame.playsAsProtoss()) {
            return ProtossPositionFinder.findStandardPositionFor(builder, building, nearTo, maxDistance);
        } // Zerg
        else if (AtlantisGame.playsAsZerg()) {
            return ZergPositionFinder.findStandardPositionFor(builder, building, nearTo, maxDistance);
        }
        else {
            System.err.println("Invalid race: " + AtlantisGame.getPlayerUs().getRace());
            System.exit(-1);
            return null;
        }
    }

}
