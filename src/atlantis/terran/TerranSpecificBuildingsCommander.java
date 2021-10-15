package atlantis.terran;

import atlantis.buildings.managers.TerranFlyingBuildingManager;
import atlantis.repair.ARepairCommander;

public class TerranSpecificBuildingsCommander {

    public static void update() {
        TerranFlyingBuildingManager.update();
        ARepairCommander.update();
    }

}
