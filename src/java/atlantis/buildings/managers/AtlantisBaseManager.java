package atlantis.buildings.managers;

import jnibwapi.Unit;
import atlantis.AtlantisConfig;
import atlantis.AtlantisGame;
import atlantis.information.AtlantisUnitInformationManager;

public class AtlantisBaseManager {

	public static void update(Unit base) {

		// Train new workers if allowed
		if (shouldTrainWorkers(base)) {
			base.train(AtlantisConfig.WORKER);
		}
	}

	// =========================================================

	private static boolean shouldTrainWorkers(Unit base) {

		// Check MINERALS
		if (AtlantisGame.getMinerals() < 50) {
			return false;
		}

		// Check if not TOO FEW WORKERS and EMPTY PRODUCTION QUEUE
		if (AtlantisUnitInformationManager.countOurWorkers() <= 27 * AtlantisUnitInformationManager.countOurBases()) {
			return true; // @AutoProduce
		}

		// Check if ALLOWED TO PRODUCE IN PRODUCTION QUEUE
		if (!AtlantisGame.getProductionStrategy().shouldProduceNow(AtlantisConfig.WORKER)) {
			return false;
		}

		// // Check if not TOO MANY WORKERS
		// if (AtlantisUnitInformationManager.countOurWorkers() >= 27 * AtlantisUnitInformationManager.countOurBases())
		// {
		// return false;
		// }

		// If not forbidden, allow.
		return true;
	}

}
