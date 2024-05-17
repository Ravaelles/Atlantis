package atlantis.production.constructing.position;

import atlantis.map.position.APosition;
import atlantis.units.AUnitType;

public class BuildingTileHelper {
    private static final int SEPARATION_IN_PX = 32;

    public static APosition tileLeftFrom(AUnitType building, APosition position) {
        return position.translateByPixels(-building.dimensionLeftPixels() - SEPARATION_IN_PX, 0);
    }

    public static APosition tiles2LeftFrom(AUnitType building, APosition position) {
        return position.translateByPixels(-building.dimensionLeftPixels() - SEPARATION_IN_PX * 2, 0);
    }

    public static APosition tiles3LeftFrom(AUnitType building, APosition position) {
        return position.translateByPixels(-building.dimensionLeftPixels() - SEPARATION_IN_PX * 3, 0);
    }

    public static APosition tileRightFrom(AUnitType building, APosition position) {
        return position.translateByPixels(building.dimensionRightPixels() + SEPARATION_IN_PX, 0);
    }

    public static APosition tiles2RightFrom(AUnitType building, APosition position) {
        return position.translateByPixels(building.dimensionRightPixels() + SEPARATION_IN_PX * 2, 0);
    }

    public static APosition tiles3RightFrom(AUnitType building, APosition position) {
        return position.translateByPixels(building.dimensionRightPixels() + SEPARATION_IN_PX * 3, 0);
    }

    public static APosition tileDownFrom(AUnitType building, APosition position) {
        return position.translateByPixels(0, building.dimensionDownPixels() + SEPARATION_IN_PX);
    }

    public static APosition tiles2DownFrom(AUnitType building, APosition position) {
        return position.translateByPixels(0, building.dimensionDownPixels() + 2 * SEPARATION_IN_PX);
    }

    public static APosition tiles3DownFrom(AUnitType building, APosition position) {
        return position.translateByPixels(0, building.dimensionDownPixels() + 3 * SEPARATION_IN_PX);
    }

    public static APosition tileUpFrom(AUnitType building, APosition position) {
        return position.translateByPixels(0, -building.dimensionUpPixels() - SEPARATION_IN_PX);
    }

    public static APosition tiles2UpFrom(AUnitType building, APosition position) {
        return position.translateByPixels(0, -building.dimensionUpPixels() - 2 * SEPARATION_IN_PX);
    }

    public static APosition tiles3UpFrom(AUnitType building, APosition position) {
        return position.translateByPixels(0, -building.dimensionUpPixels() - 3 * SEPARATION_IN_PX);
    }
}
