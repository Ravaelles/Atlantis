package atlantis.production.constructions.position.terran;

import atlantis.map.position.APosition;
import atlantis.production.constructions.position.AbstractPositionFinder;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.util.We;

public class TerranForbiddenByStreetGrid {
    public static final int GRID_SIZE_X = 11;
    public static final int GRID_SIZE_Y = 5;

    /**
     * Returns true if game says it's possible to build given building at this position.
     * <p>
     * Leave entire horizontal (same tileX) and vertical (same tileY) corridors free for units to pass
     * So disallow building in e.g. 0,1, 6,7, 12,13, horizontally and vertically
     */
    public static boolean isForbiddenByStreetGrid(AUnit builder, AUnitType building, APosition position) {
        if (!We.terran()) return false;
        if (ignoreBuildingsOfThisType(building)) return false;

        int moduloX = (position.tx() % GRID_SIZE_X);
        int moduloY = (position.ty() % GRID_SIZE_Y);

        if (moduloX == 0) return failed("Terr_TX modulo is 0");
        if (moduloY == 0) return failed("Terr_TY modulo is 0");

//        if (moduloX % GRID_SIZE_X == 0) return failed("TX modulo zero = " + moduloX);
//        if (moduloY % GRID_SIZE_Y == 0) return failed("TY modulo zero = " + moduloY);

        if (building.isSupplyDepot() || building.isAcademy()) {
            if (moduloX != 1 && moduloX != 4 && moduloX != 7) return failed("TX_Sm modulo 3x2 = " + moduloY);
//            if (moduloX != 1 && moduloX != 4) return failed("TX_Sm modulo 3x2 = " + moduloY);
            if (moduloY != 1 && moduloY != 3) return failed("TY_Sm modulo 3x2 = " + moduloY);

            return false;
        }

        boolean factoryOrStarport = building.isFactory();
        if (building.isBarracks() || factoryOrStarport || building.isEngineeringBay()) {
            if (building.isBarracks()) {
                if (moduloX != 1 && moduloX != 5) return failed("TX failed for Barrakz = " + moduloX);
            }

            if (factoryOrStarport) {
                if (moduloX != 5) return failed("TX_Lg modulo failed for FactOrStar = " + moduloX);
            }

            if (moduloY != 1) return failed("TY_Lg modulo failed for Barrakz = " + moduloY);

            return false;
        }

        if (moduloX % 2 != 1) return failed("TX modulo EVEN = " + moduloX);
        if (moduloY % 2 != 1) return failed("TY modulo EVEN = " + moduloY);

//        if (building.isCombatBuilding()) {
//            return false;
//        }

//        if (building.isSupplyDepot() || building.isAcademy()) {
//            return ForbiddenByStreetGridForSupplyDepotAndAcademy.isForbidden(builder, building, position);
////            if (ForbiddenByStreetGridForSupplyDepotAndAcademy.isForbidden(builder, building, position)) return true;
//        }

        return false;
    }

    private static boolean ignoreBuildingsOfThisType(AUnitType building) {
        return building.isBase()
            || building.isGasBuilding()
            || building.isBunker();
    }

    private static boolean fail(String reason) {
        AbstractPositionFinder._STATUS = reason;
        return true;
    }

    private static int buildingLeftTx(AUnitType building, APosition position) {
        return position.tx() - building.dimensionLeftPixels() / 32;
    }

    private static int buildingRightTx(AUnitType building, APosition position) {
        int addonBonus = building.addonWidthInPx();

        return (int) (position.tx() + (building.dimensionRightPixels() + addonBonus) / 32);
    }

    private static boolean failed(String reason) {
        AbstractPositionFinder._STATUS = reason;
        return true;
    }
}
