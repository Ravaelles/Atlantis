package atlantis.production.constructing.position.conditions;

import atlantis.Atlantis;
import atlantis.debug.painter.AAdvancedPainter;
import atlantis.game.A;
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

        // Fix to allow UNEXPLORED positions and treat them as buildable
        if (
            (!building.isGasBuilding())
//            building.isBase()
                && A.supplyTotal() >= 60
                && (!position.isExplored() || !position.isPositionVisible())
        ) return true;

//        if (!position.isExplored() && position.regionsMatch(MainRegion.mainRegion())) return true;

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
            if (positionUnexplorerAndNotVisibleLetsDoit(position, building)) return true;

            AbstractPositionFinder._CONDITION_THAT_FAILED = "Can't physically build here";
            return false;
        }

        return true;
    }

    private static boolean positionUnexplorerAndNotVisibleLetsDoit(APosition position, AUnitType building) {
        return We.terran()
            && !position.isExplored()
            && !position.isPositionVisible()
            && !building.isCombatBuilding();
    }
}
