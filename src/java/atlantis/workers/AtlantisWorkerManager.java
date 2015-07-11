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
			sendToGatherMinerals(unit);
		}

		updateTooltip(unit);
	}

	// =========================================================

	/**
	 * Assigns given worker unit (which is idle by now ar least doesn't have anything to do) to gather minerals.
	 */
	private static void sendToGatherMinerals(Unit worker) {

		// If basically unit is not doing a shit, send it to gather resources (minerals or gas).
		// But check for multiple conditions (like if isn't constructing, repairing etc).
		if (worker.isIdle()
				|| (!worker.isGatheringMinerals() && !worker.isGatheringGas() && !worker.isMoving()
						&& !worker.isConstructing() && !worker.isAttacking() && !worker.isRepairing())) {
			worker.setTooltip("Move ya ass!");
			AtlantisMineralGathering.gatherResources(worker);
		}
	}

	// =========================================================
	// Auxiliary

	private static void updateTooltip(Unit unit) {
		String tooltip = "";
		String newLine = "\r\n";
		if (unit.getTarget() != null) {
			tooltip += "Target: " + unit.getTarget();
		}
		if (unit.getOrderTarget() != null) {
			tooltip += "OrderTarget: " + unit.getOrderTarget();
		}
		// if (unit.isGatheringMinerals()) {
		// tooltip += "Minerals" + newLine;
		// }
		// if (unit.isGatheringGas()) {
		// tooltip += "Gas" + newLine;
		// }
		// if (unit.isConstructing()) {
		// tooltip += "Constructing" + newLine;
		// }
		// if (unit.isRepairing()) {
		// tooltip += "Repairing" + newLine;
		// }
		// if (unit.isMoving()) {
		// tooltip += "Moving" + newLine;
		// }
		// if (unit.isAttacking()) {
		// tooltip += "Attacking" + newLine;
		// }
		// if (unit.isStartingAttack()) {
		// tooltip += "StartingAttack" + newLine;
		// }
		// if (unit.isIdle()) {
		// tooltip += "Idle" + newLine;
		// }
		unit.setTooltip(tooltip);
	}

}
