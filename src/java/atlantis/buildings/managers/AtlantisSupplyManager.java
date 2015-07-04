package atlantis.buildings.managers;

import atlantis.AtlantisConfig;
import atlantis.AtlantisGame;
import atlantis.constructing.AtlantisConstructingManager;

public class AtlantisSupplyManager {

	private static int supply;
	private static int supplyFree;

	// =========================================================

	public static void update() {
		supply = AtlantisGame.getSupplyTotal();
		supplyFree = AtlantisGame.getSupplyFree();
		if (supply <= 20) {
			supply1to20();
		} else if (supply <= 60) {
			supply21to60();
		} else {
			supply61up();
		}
	}

	// =========================================================

	private static void supply1to20() {
		if (supplyFree <= 2) {
			requestAdditionalSupply();
		}
	}

	private static void supply21to60() {
		if (supplyFree <= 9) {
			requestAdditionalSupply();
		}
	}

	private static void supply61up() {
		if (supplyFree <= 20 && supply < 200) {
			requestAdditionalSupply();
		}
	}

	// =========================================================

	private static void requestAdditionalSupply() {
		AtlantisConstructingManager.requestConstructionOf(AtlantisConfig.SUPPLY);
	}

}
