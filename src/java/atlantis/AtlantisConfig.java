package atlantis;

import jnibwapi.types.RaceType;
import jnibwapi.types.RaceType.RaceTypes;
import jnibwapi.types.UnitType;
import jnibwapi.types.UnitType.UnitTypes;
import atlantis.production.strategies.AtlantisProductionStrategy;

/**
 * This class is used to set up your Atlantis framework by providing some basic informations about your bot. Example
 * code:
 * <p>
 * <b> useConfigForTerran() </b>
 * </p>
 * or for other race you are playing.
 */
public class AtlantisConfig {

	public static int GAME_SPEED = 0;

	public static RaceType MY_RACE = null;
	public static UnitType BASE = null;
	public static UnitType WORKER = null;
	public static UnitType BARRACKS = null;
	public static UnitType SUPPLY = null;

	/**
	 * If value less than 201 is passed, then you don't need to specify when to build supply buildings. They will be
	 * <b>automatically built only if your total supply exceeds this value</b>.
	 */
	public static int USE_AUTO_SUPPLY_MANAGER_WHEN_SUPPLY_EXCEEDS = 201;
	public static int SCOUT_IS_NTH_WORKER = 7;

	private static AtlantisProductionStrategy productionStrategy;

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

	/**
	 * Pass an object that will be responsible for the production queue. See e.g. class named
	 * DefaultTerranProductionStrategy.
	 */
	public static void useProductionStrategy(AtlantisProductionStrategy productionStrategy) {
		AtlantisConfig.productionStrategy = productionStrategy;
	}

	/**
	 * Returns object that is responsible for the production queue.
	 */
	public static AtlantisProductionStrategy getProductionStrategy() {
		return productionStrategy;
	}

}
