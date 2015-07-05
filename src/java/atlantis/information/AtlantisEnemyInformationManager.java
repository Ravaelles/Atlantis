package atlantis.information;

import jnibwapi.Unit;

public class AtlantisEnemyInformationManager {

	/**
	 * Returns true if we learned the location of any still-existing enemy building.
	 */
	public static boolean hasDiscoveredEnemyBuilding() {
		return AtlantisUnitInformationManager.enemyUnitsDiscovered.isEmpty();
	}

	/**
	 * If we learned about at least one still existing enemy base it returns first of them. Returns null otherwise.
	 */
	public static Unit hasDiscoveredEnemyBase() {
		if (!hasDiscoveredEnemyBuilding()) {
			return null;
		}

		for (Unit enemyUnit : AtlantisUnitInformationManager.enemyUnitsDiscovered) {
			if (enemyUnit.isBase()) {
				return enemyUnit;
			}
		}

		return null;
	}

}
