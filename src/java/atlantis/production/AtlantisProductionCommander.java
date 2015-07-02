package atlantis.production;

import java.util.ArrayList;

import jnibwapi.Unit;
import jnibwapi.types.UnitType;
import atlantis.AtlantisConfig;
import atlantis.production.strategies.AbstractProductionStrategy;
import atlantis.wrappers.SelectUnits;

/**
 * Manages construction of new buildings.
 */
public class AtlantisProductionCommander {

	public static void update() {
		// AtlantisConfig.getProductionStrategy().update();
		AbstractProductionStrategy productionStrategy = AtlantisConfig.getProductionStrategy();

		ArrayList<UnitType> produceNow = productionStrategy.getUnitsThatShouldBeProducedNow();
		System.out.println("produceNow = " + produceNow.size());
		for (UnitType type : produceNow) {
			System.out.println("   " + type);
		}

		for (UnitType unitType : produceNow) {
			if (unitType.equals(AtlantisConfig.WORKER)) {
				produceWorker();
			} else if (unitType.isTerranInfantry()) {
				produceInfantry(unitType);
			}
		}
	}

	// =========================================================

	private static void produceWorker() {
		Unit building = SelectUnits.ourOneIdle(AtlantisConfig.BASE);
		if (building != null) {
			building.train(AtlantisConfig.WORKER);
		}
	}

	private static void produceInfantry(UnitType infantryType) {
		Unit building = SelectUnits.ourOneIdle(AtlantisConfig.BARRACKS);
		if (building != null) {
			building.train(infantryType);
		}
	}
}
