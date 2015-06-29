package atlantis.production;

import atlantis.AtlantisConfig;

/**
 * Manages construction of new buildings.
 */
public class AtlantisProductionCommander {

	public static void update() {
		AtlantisConfig.getProductionStrategy().update();
	}

}
