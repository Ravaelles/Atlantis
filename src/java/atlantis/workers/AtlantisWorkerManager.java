package atlantis.workers;

import jnibwapi.Unit;

public class AtlantisWorkerManager {

	/**
	 * Executed for every worker unit.
	 */
	public static void update(Unit unit) {
		unit.removeTooltip();

		// If basically unit is not doing a shit, send it to gather resources (minerals or gas).
		if (unit.isIdle()
				|| (!unit.isGatheringMinerals() && !unit.isGatheringGas() && !unit.isMoving() && !unit.isConstructing()
						&& !unit.isAttacking() && !unit.isRepairing())) {
			unit.setTooltip("Move ya ass!");
			AtlantisMineralGathering.gatherResources(unit);
		}
	}

}
