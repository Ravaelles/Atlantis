package atlantis.constructing.position;

import jnibwapi.Position;
import jnibwapi.Unit;
import jnibwapi.types.UnitType;
import jnibwapi.types.UnitType.UnitTypes;
import atlantis.wrappers.SelectUnits;

public class ConstructionSpecialBuildPositionFinder {

	/**
	 * Returns build position for next Refinery/Assimilator/Extractor. It will be chosen for the oldest base that
	 * doesn't have gas extracting building.
	 */
	protected static Position findPositionForGasBuilding(UnitType building) {
		for (Unit base : SelectUnits.ourBases().list()) {
			Unit geyser = SelectUnits.neutral().ofType(UnitTypes.Resource_Vespene_Geyser).nearestTo(base);

			System.out.println();
			System.out.println("GEYSER / BASE");
			System.out.println(geyser);
			System.out.println(base);
			if (geyser != null && geyser.distanceTo(base) < 10) {
				System.out.println("dist ok");
				return geyser.translated(-48, -32);
			}
		}

		return null;
	}

	/**
	 * Returns build position for next base. It will usually be next free BaseLocation that doesn't have base built.
	 */
	public static Position findPositionForBase(UnitType building) {
		// @TODO
		return null;
	}

}
