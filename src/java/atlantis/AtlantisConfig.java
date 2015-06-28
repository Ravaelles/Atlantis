package atlantis;

import jnibwapi.types.RaceType;
import jnibwapi.types.RaceType.RaceTypes;
import jnibwapi.types.UnitType;
import jnibwapi.types.UnitType.UnitTypes;

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
	}

	// =========================================================

	private static void validate(String title, Object variable) {
		if (variable == null) {
			error(title);
		}
	}

	private static void error(String title) {
		System.err.println("#######################################");
		System.err.println("### ERROR IN ATLANTIS CONFIG ##########");
		System.err.println("#######################################");
		System.err.println("Please set variables for AtlantisConfig");
		System.err.println("before running your bot. Will exit now.");
		System.err.println("");
		System.err.println("Hint: read AtlantisConfig class javadoc");
		System.exit(-1);
	}

}
