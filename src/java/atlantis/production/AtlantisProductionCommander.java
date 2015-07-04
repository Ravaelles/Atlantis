package atlantis.production;

import atlantis.AtlantisGame;
import atlantis.constructing.AtlantisConstructingManager;

/**
 * Manages construction of new buildings.
 */
public class AtlantisProductionCommander {

	public static void update() {
		AtlantisProduceUnitManager.update();

		// Dont build for first three frames, some errors occuring then.
		if (AtlantisGame.getTimeFrames() >= 3) {
			AtlantisConstructingManager.update();
		}
	}

}
