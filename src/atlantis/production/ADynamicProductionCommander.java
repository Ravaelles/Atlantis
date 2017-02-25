package atlantis.production;

import atlantis.AGame;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.Select;
import bwapi.TilePosition;
import java.util.List;

/**
 *
 * @author Rafal Poniatowski <ravaelles@gmail.com>
 */
public class ADynamicProductionCommander {

    public static void update() {
        ADynamicUnitProductionManager.update();
        ADynamicConstructionManager.update();
    }
    
}
