package atlantis.workers;

import jnibwapi.Unit;
import atlantis.buildings.managers.AtlantisGasManager;
import atlantis.wrappers.SelectUnits;

/**
 * Manages all worker (SCV, Probe, Drone) actions.
 */
public class AtlantisWorkerCommander {

	/**
	 * Executed only once per frame.
	 */
	public static void update() {
		handleGasBuildings();

		for (Unit unit : SelectUnits.ourWorkers().list()) {
			AtlantisWorkerManager.update(unit);
		}
	}

	// =========================================================

	/**
	 * If any of our gas extracting buildings needs worker, it will assign exactly one worker per frame (until no more
	 * needed).
	 */
	private static void handleGasBuildings() {
		Unit gasBuildingNeedingWorker = AtlantisGasManager.getOneGasBuildingNeedingWorker();
		if (gasBuildingNeedingWorker != null) {
			Unit worker = SelectUnits.ourWorkers().gatheringMinerals(true).first();
			if (worker != null) {
				worker.gather(gasBuildingNeedingWorker, false);
			}
		}
	}

}
