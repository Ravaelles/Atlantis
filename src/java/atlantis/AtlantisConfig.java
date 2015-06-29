package atlantis;

import jnibwapi.types.RaceType;
import jnibwapi.types.RaceType.RaceTypes;
import jnibwapi.types.UnitType;
import jnibwapi.types.UnitType.UnitTypes;
import atlantis.production.strategies.AbstractProductionStrategy;

/**
 * This class is used to set up your Atlantis framework by providing some basic informations about your bot. Example
 * code:
 * <p>
 * <b> useConfigForTerran() </b>
 * </p>
 * or for other race you are playing.
 */
public class AtlantisConfig {

	public static int INITIAL_GAME_SPEED = 0;

	public static RaceType MY_RACE = null;
	public static UnitType BASE = null;
	public static UnitType WORKER = null;
	public static UnitType BARRACKS = null;
	public static UnitType SUPPLY = null;

	private static AbstractProductionStrategy productionStrategy;

	// =========================================================

	/**
	 * Helper method for using Terran race.
	 */
	public static void useConfigForTerran() {
		AtlantisConfig.MY_RACE = RaceTypes.Terran;
		AtlantisConfig.BASE = UnitTypes.Terran_Command_Center;
		AtlantisConfig.WORKER = UnitTypes.Terran_SCV;
		AtlantisConfig.BARRACKS = UnitTypes.Terran_Barracks;
		AtlantisConfig.SUPPLY = UnitTypes.Terran_Supply_Depot;
	}

	// =========================================================

	/**
	 * Makes sure all necessary variables are set (non-null).
	 */
	protected static void validate() {
		validate("MY_RACE", MY_RACE);
		validate("BASE", BASE);
		validate("WORKER", WORKER);
		validate("BARRACKS", BARRACKS);
		validate("SUPPLY", SUPPLY);
		validate("You have to specify production strategy\n-> AtlantisConfig.useProductionStrategy", productionStrategy);
	}

	// =========================================================

	private static void validate(String title, Object variable) {
		if (variable == null) {
			error(title);
		}
	}

	private static void error(String title) {
		System.err.println("");
		System.err.println("#######################################");
		System.err.println("### ERROR IN ATLANTIS CONFIG ##########");
		System.err.println("#######################################");
		System.err.println("Please set variables for AtlantisConfig");
		System.err.println("before running your bot, read class doc");
		System.err.println("");
		System.err.println("### What went wrong ###################");
		System.err.println(title);
		System.err.println("");
		System.err.println("Program has stopped");
		System.exit(-1);
	}

	// =========================================================
	// Hi-level configs

	public static void useProductionStrategy(AbstractProductionStrategy productionStrategy) {
		AtlantisConfig.productionStrategy = productionStrategy;
	}

	public static AbstractProductionStrategy getProductionStrategy() {
		return productionStrategy;
	}

}
