package atlantis.buildings.managers;

import jnibwapi.Unit;
import atlantis.AtlantisConfig;
import atlantis.wrappers.SelectUnits;

public class AtlantisGasManager {

	/**
	 * Returns first gas extracting building that needs a worker (because has less than 3 units assigned) or null if
	 * every gas building has 3 workers assigned.
	 */
	public static Unit getOneGasBuildingNeedingWorker() {
		SelectUnits.ourBuildings().ofType(AtlantisConfig.GAS_BUILDING);

		// @TODO

		return null;
	}

}
