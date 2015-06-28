package atlantis;

import atlantis.buildings.AtlantisBuildingsCommander;
import atlantis.debug.AtlantisPainter;
import atlantis.workers.AtlantisWorkerCommander;

public class AtlantisGameCommander {

	/**
	 * Executed every time when game has new frame. It represents minimal passage of game-time (one action frame).
	 */
	public void update() {

		// =========================================================
		// Execute code of every Commander

		AtlantisBuildingsCommander.update();
		AtlantisWorkerCommander.update();

		// =========================================================
		// Execute extra paint methods at the end of all actions

		AtlantisPainter.paint();
	}

}
