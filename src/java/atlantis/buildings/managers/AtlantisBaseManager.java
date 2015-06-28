package atlantis.buildings.managers;

import jnibwapi.Unit;
import atlantis.AtlantisConfig;
import atlantis.AtlantisGame;

public class AtlantisBaseManager {

	public static void update(Unit base) {

		// Train new workers if allowed
		if (shouldTrainWorkers(base)) {
			base.train(AtlantisConfig.WORKER);
		}
	}

	// =========================================================

	private static boolean shouldTrainWorkers(Unit base) {

		// Check FREE SUPPLY
		if (AtlantisGame.getSupplyFree() == 0) {
			return false;
		}

		// Check MINERALS
		if (AtlantisGame.getMinerals() < 50) {
			return false;
		}

		// If not forbidden, allow.
		return true;
	}

}
