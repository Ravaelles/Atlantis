package atlantis.constructing.position;

import jnibwapi.Position;
import jnibwapi.types.UnitType;
import atlantis.AtlantisGame;
import atlantis.wrappers.SelectUnits;

public class ConstructionBuildPositionFinder {

	/**
	 * Returns build position for next building of given type.
	 */
	public static Position findPositionForNew(UnitType building) {
		return findPositionForNew(building, null, -1);
	}

	/**
	 * Returns build position for next building of given type. If <b>nearTo</b> is not null, it forces to find position
	 * <b>maxDistance</b> build tiles from given position.
	 */
	public static Position findPositionForNew(UnitType building, Position nearTo, double maxDistance) {

		// Buildings extracting GAS
		if (building.isGasBuilding()) {
			return ConstructionSpecialBuildPositionFinder.findPositionForGasBuilding(building);
		}

		// BASE
		else if (building.isBase()) {
			return ConstructionSpecialBuildPositionFinder.findPositionForBase(building);
		}

		// STANDARD BUILDINGS
		else {

			// If we didn't specify location where to build, build somewhere near the main base
			if (nearTo == null) {
				nearTo = SelectUnits.mainBase();
			}

			// If all of our bases have been destroyed, build somewhere near our first unit alive
			if (nearTo == null) {
				nearTo = SelectUnits.our().first();
			}

			// Hopeless case, all units have died, just quit.
			if (nearTo == null) {
				return null;
			}

			if (maxDistance < 0) {
				maxDistance = 50;
			}

			// =========================================================
			// Handle standard building position according to the race as every race uses completely different approach

			// Terran
			if (AtlantisGame.playsAsTerran()) {
				return TerranBuildPositionFinder.findStandardPositionFor(building, nearTo, maxDistance);
			}

			// Protoss
			else if (AtlantisGame.playsAsProtoss()) {
				return ProtossBuildPositionFinder.findStandardPositionFor(building, nearTo, maxDistance);
			}

			// Zerg
			else {
				return ZergBuildPositionFinder.findStandardPositionFor(building, nearTo, maxDistance);
			}
		}
	}

}
