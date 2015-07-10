package bot;

import atlantis.Atlantis;
import atlantis.AtlantisConfig;
import atlantis.production.strategies.DefaultTerranProductionStrategy;

public class AtlantisTide {

	/**
	 * Sets up Atlantis config and runs the bot.
	 */
	public static void main(String[] args) {

		// Set up some very basic config
		AtlantisConfig.useConfigForTerran();
		AtlantisConfig.USE_AUTO_SUPPLY_MANAGER_WHEN_SUPPLY_EXCEEDS = 15;

		// Set production strategy (build orders) to use.
		AtlantisConfig.useProductionStrategy(new DefaultTerranProductionStrategy());

		// Starts bot using Atlantis
		Atlantis atlantis = new Atlantis();
		atlantis.start();
	}

}
