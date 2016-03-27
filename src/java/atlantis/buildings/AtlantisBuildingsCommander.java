package atlantis.buildings;

import atlantis.AtlantisConfig;
import atlantis.buildings.managers.AtlantisBarracksManager;
//import atlantis.buildings.managers.AtlantisBaseManager;
import atlantis.util.UnitUtil;
import atlantis.wrappers.Select;
import bwapi.Unit;

/**
 * Manages all existing-buildings actions, but training new units depends on AtlantisProductionCommander.
 */
public class AtlantisBuildingsCommander {

    /**
     * Executed once every frame.
     */
    public static void update() {
        for (Unit building : Select.ourBuildings().listUnits()) {

            // If building is busy, don't disturb.
            if (building.getTrainingQueue().size() > 0 || building.isUpgrading()) {
                continue;
            }

            // =========================================================
            // BARRACKS (Barracks, Gateway, Spawning Pool)
            else if (building.isType(AtlantisConfig.BARRACKS)) {
                AtlantisBarracksManager.update(building);
            }
        }
        
        // =========================================================
        // Handled separately to produce workers at the end
        
//        for (Unit building : Select.ourBases().listUnits()) {
//            
//            // =========================================================
//            // BASE (Command Center / Nexus / Hatchery / Lair / Hive)
//            if (UnitUtil.isBase(building.getType())) {
//                AtlantisBaseManager.update(building);
//            } 
//        }
    }

}
