package atlantis.constructing.position;

import atlantis.AtlantisGame;
import atlantis.constructing.ConstructionOrder;
import atlantis.wrappers.SelectUnits;
import jnibwapi.Position;
import jnibwapi.Unit;
import jnibwapi.types.UnitType;

public class ConstructionBuildPositionFinder {

    protected static UnitType building;
    protected static Position nearTo;
    protected static double maxDistance;

    // =========================================================
    
    /**
     * Returns build position for next building of given type.
     */
    public static Position findPositionForNew(Unit builder, UnitType building, ConstructionOrder constructionOrder) {
        return findPositionForNew(builder, building, constructionOrder, null, -1);
    }

    /**
     * Returns build position for next building of given type. If <b>nearTo</b> is not null, it forces to find
     * position
     * <b>maxDistance</b> build tiles from given position.
     */
    public static Position findPositionForNew(Unit builder, UnitType building, 
            ConstructionOrder constructionOrder, Position nearTo, double maxDistance) {

        // Buildings extracting GAS
        if (building.isGasBuilding()) {
            return ConstructionSpecialBuildPositionFinder.findPositionForGasBuilding(building);
        } // BASE
        else if (building.isBase()) {
            return ConstructionSpecialBuildPositionFinder.findPositionForBase(building, builder, constructionOrder);
        } // STANDARD BUILDINGS
        else {

            // If we didn't specify location where to build, build somewhere near the main base
            if (nearTo == null) {
                nearTo = SelectUnits.mainBase();
            }

            // If all of our bases have been destroyed, build somewhere near our first unit alive
            if (nearTo == null) {
                nearTo = SelectUnits.our().first();
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
    
    /**
     * Returns standard build position for building near given position.
     */
    protected static Position findStandardPosition(Unit builder, UnitType building, Position nearTo, double maxDistance) {
        
        // =========================================================
        // Handle standard building position according to the race as every race uses completely different approach
        // =========================================================
        
        // Terran
        if (AtlantisGame.playsAsTerran()) {
            return TerranBuildPositionFinder.findStandardPositionFor(builder, building, nearTo, maxDistance);
        } // Protoss
        else if (AtlantisGame.playsAsProtoss()) {
            return ProtossBuildPositionFinder.findStandardPositionFor(builder, building, nearTo, maxDistance);
        } // Zerg
        else if (AtlantisGame.playsAsZerg()) {
            return ZergBuildPositionFinder.findStandardPositionFor(builder, building, nearTo, maxDistance);
        }
        else {
            System.err.println("Invalid race: " + AtlantisGame.getPlayerUs().getRace());
            System.exit(-1);
            return null;
        }
    }

}
