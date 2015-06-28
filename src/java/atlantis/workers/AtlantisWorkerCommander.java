package atlantis.workers;

import jnibwapi.Unit;
import atlantis.wrappers.SelectUnits;

public class AtlantisWorkerCommander {

	/**
	 * Executed only once per frame.
	 */
	public static void update() {
		for (Unit unit : SelectUnits.ourWorkers().list()) {
			AtlantisWorkerManager.update(unit);
		}
	}

}
