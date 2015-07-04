package atlantis.workers;

import jnibwapi.Unit;
import atlantis.constructing.AtlantisBuilderManager;
import atlantis.constructing.AtlantisConstructingManager;

public class AtlantisWorkerManager {

	/**
	 * Executed for every worker unit.
	 */
	public static void update(Unit unit) {
		unit.removeTooltip();

		// Act as BUILDER
		if (AtlantisConstructingManager.isBuilder(unit)) {
			AtlantisBuilderManager.update(unit);
		}

		// ORDINARY WORKER
		else {
			gatherResourcesIfNeeded(unit);
		}

		updateTooltip(unit);
	}

	// =========================================================

	private static void gatherResourcesIfNeeded(Unit unit) {

		// If basically unit is not doing a shit, send it to gather resources (minerals or gas).
		if (unit.isIdle()
				|| (!unit.isGatheringMinerals() && !unit.isGatheringGas() && !unit.isMoving() && !unit.isConstructing()
						&& !unit.isAttacking() && !unit.isRepairing())) {
			unit.setTooltip("Move ya ass!");
			AtlantisMineralGathering.gatherResources(unit);
		}
	}

	// =========================================================
	// Auxiliary

	private static void updateTooltip(Unit unit) {
		String tooltip = "";
		String newLine = "\r\n";
		if (unit.isGatheringMinerals()) {
			tooltip += "Minerals" + newLine;
		}
		if (unit.isGatheringGas()) {
			tooltip += "Gas" + newLine;
		}
		if (unit.isConstructing()) {
			tooltip += "Constructing" + newLine;
		}
		if (unit.isRepairing()) {
			tooltip += "Repairing" + newLine;
		}
		if (unit.isMoving()) {
			tooltip += "Moving" + newLine;
		}
		if (unit.isAttacking()) {
			tooltip += "Attacking" + newLine;
		}
		if (unit.isStartingAttack()) {
			tooltip += "StartingAttack" + newLine;
		}
		if (unit.isIdle()) {
			tooltip += "Idle" + newLine;
		}
		unit.setTooltip(tooltip);
	}

}
