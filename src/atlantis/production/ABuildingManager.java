package atlantis.production;

import atlantis.debug.APainter;
import atlantis.units.AUnit;
import atlantis.units.Select;
import bwapi.Color;

/**
 * Auxiliary class.
 */
public class ABuildingManager {

    public static void update() {
        for (AUnit building : Select.ourBuildings().listUnits()) {
            // Paint center of building and its borders
//            APainter.paintCircleFilled(building.getPosition(), 3, Color.Red);
//            APainter.paintLine(building.getPosition(), building.getType().getDimensionRight(), 0, Color.Red);
        }
    }

}
