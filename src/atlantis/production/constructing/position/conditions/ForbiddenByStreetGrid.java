package atlantis.production.constructing.position.conditions;

import atlantis.game.A;
import atlantis.map.position.APosition;
import atlantis.production.constructing.position.AbstractPositionFinder;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.util.We;

public class ForbiddenByStreetGrid {
    private static final int GRID_VALUE = 8;

    /**
     * Returns true if game says it's possible to build given building at this position.
     */
    public static boolean isForbiddenByStreetGrid(AUnit builder, AUnitType building, APosition position) {
        if (We.protoss() && A.supplyTotal() <= 10) {
            return false;
        }

        // Special buildings can be build anywhere
        if (building.isBase() || building.isGasBuilding() || building.isCombatBuilding()) {
            return false;
        }

        // =========================================================

        // Leave entire vertical (same tileX) corridor free for units
        if (
            position.tx() % GRID_VALUE <= 1
                || (position.tx() + building.dimensionRightPx() / 32) % GRID_VALUE <= 1
        ) {
//        System.out.println(building.name() + "   " + position.getTileX() + " // (" + position.getTileX() % GRID_VALUE + ") // "
//                + (position.getTileX() + building.getDimensionRight() / 32) + " // (" +
//                (position.getTileX() + building.getDimensionRight() / 32) % GRID_VALUE + ")");
            AbstractPositionFinder._CONDITION_THAT_FAILED = "LEAVE_PLACE_VERTICALLY";
            return true;
        }

        // Leave entire horizontal (same tileY) corridor free for units
        if (
            position.ty() % GRID_VALUE <= 1
                || (position.ty() + building.dimensionDownPx() / 32) % GRID_VALUE <= 0
        ) {
            AbstractPositionFinder._CONDITION_THAT_FAILED = "LEAVE_PLACE_HORIZONTALLY";
            return true;
        }

        return false;
    }
}