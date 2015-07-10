package atlantis.workers;

import jnibwapi.Unit;
import atlantis.wrappers.SelectUnits;

/**
 * Manages all worker (SCV, Probe, Drone) actions.
 */
public class AtlantisWorkerCommander {

	/**
	 * Executed only once per frame.
	 */
	public static void update() {
		if (handleGasBuildings()) {
			return;
		}

		for (Unit unit : SelectUnits.ourWorkers().list()) {
			AtlantisWorkerManager.update(unit);
		}
	}

	private static boolean handleGasBuildings() {
		// Unit gasBuildingNeedingWorker = AtlantisGasManager.getOneGasBuildingNeedingWorker();
		// if (gasBuildingNeedingWorker != null) {
		// gasBuildingNeedingWorker
		// return true;
		// }

		return false;
	}

	private static int getOneRefineryNeedingWorker() {
		// TODO Auto-generated method stub
		return 0;
	}

}
