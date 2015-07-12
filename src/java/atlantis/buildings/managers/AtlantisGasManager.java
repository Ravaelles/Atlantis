package atlantis.buildings.managers;

import java.util.Collection;

import jnibwapi.Unit;
import atlantis.AtlantisConfig;
import atlantis.wrappers.SelectUnits;

public class AtlantisGasManager {

	private static final int MIN_GAS_WORKERS_PER_BUILDING = 3;

	// =========================================================

	/**
	 * Returns first gas extracting building that needs a worker (because has less than 3 units assigned) or null if
	 * every gas building has 3 workers assigned.
	 */
	public static Unit getOneGasBuildingNeedingWorker() {
		Collection<Unit> gasBuildings = SelectUnits.ourBuildings().ofType(AtlantisConfig.GAS_BUILDING).list();
		Collection<Unit> workers = SelectUnits.ourWorkers().list();

		for (Unit gasBuilding : gasBuildings) {
			int numberOfWorkersAssigned = countWorkersAssignedTo(gasBuilding, workers);
			if (numberOfWorkersAssigned < MIN_GAS_WORKERS_PER_BUILDING) {
				return gasBuilding;
			}
		}

		return null;
	}

	// =========================================================

	/**
	 * Returns number of workers that are assigned to gather gas in given building.
	 */
	private static int countWorkersAssignedTo(Unit gasBuilding, Collection<Unit> workers) {
		int total = 0;
		for (Unit worker : workers) {
			if (worker.getTarget() != null && worker.getTarget().equals(gasBuilding)) {
				total++;
			}
		}
		return total;
	}

}
