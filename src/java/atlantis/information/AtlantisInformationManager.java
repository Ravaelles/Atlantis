package atlantis.information;

import java.util.ArrayList;

import jnibwapi.Unit;
import jnibwapi.types.UnitType;
import atlantis.wrappers.MappingCounter;

public class AtlantisInformationManager {

	private static MappingCounter<UnitType> ourUnitsFininised = new MappingCounter<>();
	private static MappingCounter<UnitType> ourUnitsUnfininised = new MappingCounter<>();

	private static MappingCounter<UnitType> enemyUnitsDiscoveredCounter = new MappingCounter<>();
	private static MappingCounter<UnitType> enemyUnitsVisibleCounter = new MappingCounter<>();

	private static ArrayList<Unit> enemyUnitsDiscovered = new ArrayList<>();
	private static ArrayList<Unit> enemyUnitsVisible = new ArrayList<>();

	// =========================================================
	// Number of units changed

	/**
	 * Saves information about our new unit being trained, so counting units works properly.
	 */
	public static void addOurUnfinishedUnit(UnitType type) {
		ourUnitsUnfininised.incrementValueFor(type);
	}

	/**
	 * Saves information about new unit being created successfully, so counting units works properly.
	 */
	public static void addOurFinishedUnit(UnitType type) {
		ourUnitsFininised.incrementValueFor(type);
	}

	/**
	 * Saves information about enemy unit that we see for the first time.
	 */
	public static void discoveredEnemyUnit(Unit unit) {
		enemyUnitsDiscovered.add(unit);
		enemyUnitsDiscoveredCounter.incrementValueFor(unit.getType());
	}

	/**
	 * Saves information about given unit being destroyed, so counting units works properly.
	 */
	public static void unitDestroyed(Unit unit) {
		if (unit.getPlayer().isSelf()) {
			if (unit.isCompleted()) {
				ourUnitsFininised.decrementValueFor(unit.getType());
			} else {
				ourUnitsUnfininised.decrementValueFor(unit.getType());
			}
		} else if (unit.getPlayer().isEnemy()) {
			enemyUnitsDiscoveredCounter.decrementValueFor(unit.getType());
			enemyUnitsVisibleCounter.decrementValueFor(unit.getType());
			enemyUnitsDiscovered.remove(unit);
			enemyUnitsVisible.remove(unit);
		}
	}

	public static void addEnemyUnitVisible(Unit unit) {
		enemyUnitsVisible.add(unit);
		enemyUnitsVisibleCounter.incrementValueFor(unit.getType());
	}

	public static void removeEnemyUnitVisible(Unit unit) {
		enemyUnitsVisible.remove(unit);
		enemyUnitsVisibleCounter.decrementValueFor(unit.getType());
	}

	// =========================================================
	// GET

	/**
	 * Returns cached amount of our units of given type.
	 */
	public static int countOurUnitsOfType(UnitType type) {
		return ourUnitsUnfininised.getValueFor(type);
	}

	/**
	 * Returns number of discovered and alive enemy units of given type. Some of them (maybe even all of them) may not
	 * be visible right now.
	 */
	public static int countEnemyUnitsOfType(UnitType type) {
		return enemyUnitsDiscoveredCounter.getValueFor(type);
	}

}
