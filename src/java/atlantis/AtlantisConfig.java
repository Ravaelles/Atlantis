package atlantis;

import jnibwapi.types.RaceType;
import jnibwapi.types.UnitType;

/**
 * This class is used to set up your Atlantis framework by providing some basic
 * informations about your bot. Example code:
 * <p>
 * <b> AtlantisConfig.MY_RACE = RaceTypes.Terran; AtlantisConfig.BASE =
 * UnitTypes.Terran_Command_Center; AtlantisConfig.WORKER =
 * UnitTypes.Terran_SCV;
 * </p>
 * </b>
 */
public class AtlantisConfig {

	public static RaceType MY_RACE = null;
	public static UnitType BASE = null;
	public static UnitType WORKER = null;

	// =========================================================

	/**
	 * Makes sure all necessary variables are set (non-null).
	 */
	protected static void validate() {
		validate("MY_RACE", MY_RACE);
		validate("BASE", BASE);
		validate("WORKER", WORKER);
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
