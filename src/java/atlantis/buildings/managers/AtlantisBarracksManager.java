package atlantis.buildings.managers;

import jnibwapi.Unit;
import jnibwapi.types.UnitType;
import jnibwapi.types.UnitType.UnitTypes;
import atlantis.AtlantisGame;

public class AtlantisBarracksManager {

	public static void update(Unit barracks) {
		if (shouldBuildArmyUnits(barracks)) {
			buildUnit(barracks);
		}
	}

	// =========================================================

	private static boolean shouldBuildArmyUnits(Unit barracks) {

		// Plays as TERRAN
		if (AtlantisGame.playsAsTerran()) {

			// Check MINERALS
			if (AtlantisGame.getMinerals() < 50) {
				return false;
			}

			// Check SUPPLY
			if (AtlantisGame.getSupplyFree() == 0) {
				return false;
			}
		}

		return true;
	}

	// =========================================================

	private static void buildUnit(Unit barracks) {
		UnitType unitToBuild = defineUnitToBuild(barracks);
		if (unitToBuild != null) {
			barracks.train(unitToBuild);
		}
	}

	private static UnitType defineUnitToBuild(Unit barracks) {
		return UnitTypes.Terran_Marine;
	}

}
