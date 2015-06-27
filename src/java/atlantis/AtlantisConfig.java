package atlantis;

import jnibwapi.types.UnitType;

public class AtlantisConfig {

	public static UnitType BASE = null;
	public static UnitType WORKER = null;

	// =========================================================

	/**
	 * Makes sure all necessary variables are set (non-null).
	 */
	public void validate() {
		validate("BASE", BASE);
		validate("WORKER", WORKER);
	}

	// =========================================================

	private void validate(String title, Object variable) {
		if (variable == null) {
			error(title);
		}
	}

	private void error(String title) {
		System.err.println("#######################################");
		System.err.println("### ERROR IN ATLANTIS CONFIG ##########");
		System.err.println("#######################################");
		System.err.println("Please set variables for AtlantisConfig");
		System.err.println("before running your bot. Will exit now.");
		System.exit(-1);
	}

}
