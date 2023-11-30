package atlantis.production.constructing.position.conditions;

import atlantis.Atlantis;
import atlantis.debug.painter.AAdvancedPainter;
import atlantis.map.position.APosition;
import atlantis.production.constructing.position.AbstractPositionFinder;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.util.We;
import bwapi.Color;

public class CanPhysicallyBuildHere {
    /**
     * Returns true if game says it's possible to build given building at this position.
     */
    public static boolean check(AUnit builder, AUnitType building, APosition position) {
        if (position == null) {
            AbstractPositionFinder._CONDITION_THAT_FAILED = "POSITION IS NULL";
            return false;
        }
        if (builder == null) {
            AbstractPositionFinder._CONDITION_THAT_FAILED = "BUILDER IS NULL";
            return false;
        }

        if (building.isGasBuilding()) {
            AAdvancedPainter.paintCircleFilled(position, 5, Color.Red);
        }

        // Fix for bases & bunkers - allow in unknown locations
        if (building.isBase() && (!position.isExplored() || !position.isPositionVisible())) return true;
        if (building.isCombatBuilding() && !position.isExplored()) return true;
//        if (
//            (!position.isExplored() || !position.isPositionVisible())
//                &&
//                (building.isBase() || building.isCombatBuilding())
//        ) return true;

        if (!We.zerg() && Atlantis.game().hasCreep(position.toTilePosition())) {
            AbstractPositionFinder._CONDITION_THAT_FAILED = "Ugly creep on it";
            return false;
        }

        if (!Atlantis.game().canBuildHere(position.toTilePosition(), building.ut(), builder.u())) {
//            if (!position.isPositionVisible() && !position.isExplored()) return true;

            // Allow building bases in unexplored areas - otherwise we only build in explored areas
            if (building.isBase() && !position.isExplored()) return true;

            AbstractPositionFinder._CONDITION_THAT_FAILED = "Can't physically build here";
            return false;
        }

        return true;
    }
}
