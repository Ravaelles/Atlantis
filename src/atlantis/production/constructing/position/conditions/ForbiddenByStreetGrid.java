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
        if (We.protoss() && A.supplyTotal() <= 10) return false;
        if (building.isBase() || building.isGasBuilding()) return false;

        // =========================================================

        if (building.isCombatBuilding()) {
            if (position.tx() % 6 <= 1) return true;
            if (position.ty() % 6 <= 1) return true;
        }
        else {
            if (position.tx() % GRID_VALUE <= 1) return true;
            if (position.ty() % GRID_VALUE <= 1) return true;
//            if (position.tx() % 9 <= 1) return true;
//            if (position.ty() % 9 <= 1) return true;
        }

        // =========================================================

        // Leave entire vertical (same tileX) corridor free for units
        if (
            buildingLeftTx(building, position) % GRID_VALUE <= 1
                || buildingRightTx(building, position) % GRID_VALUE <= 1
        ) {
            if (
                !position.translateByTiles(-1 - building.dimensionLeftTx(), 0).isWalkable()
                    && !position.translateByTiles(+1 + building.dimensionRightTx(), 0).isWalkable()
            ) {
                AbstractPositionFinder._CONDITION_THAT_FAILED = "LEAVE_PLACE_VERTICALLY";
                return true;
            }
        }

        // Leave entire horizontal (same tileY) corridor free for units
        if (
            position.ty() % GRID_VALUE <= 1
                || (position.ty() + building.dimensionDownPx() / 32) % GRID_VALUE <= 0
        ) {
            if (
                !position.translateByTiles(0, -1 - building.dimensionUpTx()).isWalkable()
                    && !position.translateByTiles(0, +1 + building.dimensionDownTx()).isWalkable()
            ) {
                AbstractPositionFinder._CONDITION_THAT_FAILED = "LEAVE_PLACE_HORIZONTALLY";
                return true;
            }
        }

        return false;
    }

    private static int buildingLeftTx(AUnitType building, APosition position) {
        return position.tx() - building.dimensionLeftPx() / 32;
    }

    private static int buildingRightTx(AUnitType building, APosition position) {
        int addonBonus = building.addonWidthInPx();

        return (int) (position.tx() + (building.dimensionRightPx() + addonBonus) / 32);
    }
}
