package atlantis.scout;

import jnibwapi.BaseLocation;
import jnibwapi.Unit;
import atlantis.AtlantisConfig;
import atlantis.information.AtlantisEnemyInformationManager;
import atlantis.information.AtlantisMapInformationManager;
import atlantis.information.AtlantisUnitInformationManager;
import atlantis.wrappers.SelectUnits;

public class AtlantisScoutManager {

	/**
	 * Current scout unit.
	 */
	private static Unit scout = null;

	// =========================================================

	/**
	 * If we don't have unit scout assigns one of workers to become one and then, <b>scouts and harasses</b> the enemy
	 * base or tries to find it if we still don't know where the enemy is.
	 */
	public static void update() {
		assignScoutIfNeeded();

		// We don't know any enemy building, scout nearest starting location.
		if (!AtlantisEnemyInformationManager.hasDiscoveredEnemyBuilding()) {
			tryToFindEnemy();
		}

		// We know enemy building, but don't know any base.
		Unit enemyBase = AtlantisEnemyInformationManager.hasDiscoveredEnemyBase();
		if (enemyBase == null) {
			// @TODO
		}

		// We know the exact location of enemy's base.
		else {
			handleScoutWhenKnowEnemyBase(enemyBase);
		}
	}

	/**
	 * Behavior for the scout if we know enemy base location.
	 */
	private static void handleScoutWhenKnowEnemyBase(Unit enemyBase) {

		// Scout already attacking
		if (scout.isAttacking()) {

			// Scout is relatively healthy
			if (scout.getHPPercent() >= 99) {
				// OK
			}

			// Scout is wounded
			else {
				scout.move(SelectUnits.mainBase(), false);
			}
		}

		// Attack
		else {
			if (!scout.isStartingAttack()) {
				scout.attack(enemyBase, false);
			}
		}
	}

	/**
	 * We don't know any enemy building, scout nearest starting location.
	 */
	private static void tryToFindEnemy() {

		// Don't interrupt when moving
		if (scout.isMoving()) {
			return;
		}

		// Define center point for our searches
		Unit ourMainBase = SelectUnits.mainBase();
		if (ourMainBase == null) {
			return;
		}

		// Get nearest unexplored starting location and go there
		BaseLocation startingLocation = AtlantisMapInformationManager.getNearestUnexploredStartingLocation(ourMainBase);
		if (startingLocation != null) {
			scout.move(startingLocation, false);
		}
	}

	/**
	 * If we have no scout unit assigned, make one of our units a scout.
	 */
	private static void assignScoutIfNeeded() {
		if (scout == null && AtlantisUnitInformationManager.countOurWorkers() >= AtlantisConfig.SCOUT_IS_NTH_WORKER) {
			scout = SelectUnits.ourWorkers().first();
		}
	}

}
