package atlantis;

import jnibwapi.types.RaceType.RaceTypes;

public class AtlantisGame {

	/**
	 * Returns approximate number of in-game seconds elapsed.
	 */
	public static int getTimeSeconds() {
		return Atlantis.getBwapi().getFrameCount() / 30;
	}

	/**
	 * Returns number of frames elapsed.
	 */
	public static int getTimeFrames() {
		return Atlantis.getBwapi().getFrameCount();
	}

	/**
	 * Number of minerals.
	 */
	public static int getMinerals() {
		return Atlantis.getBwapi().getSelf().getMinerals();
	}

	/**
	 * Number of gas.
	 */
	public static int getGas() {
		return Atlantis.getBwapi().getSelf().getGas();
	}

	/**
	 * Number of free supply.
	 */
	public static int getSupplyFree() {
		return getSupplyTotal() - getSupplyUsed();
	}

	/**
	 * Number of supply used.
	 */
	public static int getSupplyUsed() {
		return Atlantis.getBwapi().getSelf().getSupplyUsed();
	}

	/**
	 * Number of supply totally available.
	 */
	public static int getSupplyTotal() {
		return Atlantis.getBwapi().getSelf().getSupplyTotal();
	}

	/**
	 * Returns true if user plays as Terran.
	 */
	public static boolean playsAsTerran() {
		return AtlantisConfig.MY_RACE.equals(RaceTypes.Terran);
	}

	/**
	 * Returns true if user plays as Protoss.
	 */
	public static boolean playsAsProtoss() {
		return AtlantisConfig.MY_RACE.equals(RaceTypes.Protoss);
	}

	/**
	 * Returns true if user plays as Zerg.
	 */
	public static boolean playsAsZerg() {
		return AtlantisConfig.MY_RACE.equals(RaceTypes.Zerg);
	}

}
