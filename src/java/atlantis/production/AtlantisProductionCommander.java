package atlantis.production;

import atlantis.buildings.managers.AtlantisSupplyManager;
import atlantis.constructing.AtlantisConstructingManager;

/**
 * Manages construction of new buildings.
 */
public class AtlantisProductionCommander {

	public static void update() {
		AtlantisSupplyManager.update();
		AtlantisProduceUnitManager.update();
		AtlantisConstructingManager.update();
	}

}
