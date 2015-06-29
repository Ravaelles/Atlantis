package atlantis.buildings;

import jnibwapi.Unit;
import atlantis.AtlantisConfig;
import atlantis.buildings.managers.AtlantisBarracksManager;
import atlantis.buildings.managers.AtlantisBaseManager;
import atlantis.buildings.managers.AtlantisSupplyManager;
import atlantis.wrappers.SelectUnits;

/**
 * Manages all existing-buildings actions, but training new units depends on AtlantisProductionCommander.
 */
public class AtlantisBuildingsCommander {

	/**
	 * Executed once every frame.
	 */
	public static void update() {
		for (Unit building : SelectUnits.ourBuildings().list()) {

			// If building is busy, don't disturb.
			if (building.getTrainingQueueSize() > 0) {
				continue;
			}

			// BASE (Command Center / Nexus / Hatchery / Lair / Hive)
			if (building.isBase()) {
				AtlantisBaseManager.update(building);
			}

			// BARRACKS (Barracks, Gateway, Spawning Pool)
			else if (building.isType(AtlantisConfig.BARRACKS)) {
				AtlantisBarracksManager.update(building);
			}

			// SUPPLY (Supply Depot, Pylon, Overlord)
			else if (building.isType(AtlantisConfig.SUPPLY)) {
				AtlantisSupplyManager.update();
			}
		}
	}

}
