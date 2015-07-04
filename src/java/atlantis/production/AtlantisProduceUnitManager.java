package atlantis.production;

import java.util.ArrayList;

import jnibwapi.Unit;
import jnibwapi.types.UnitType;
import atlantis.AtlantisConfig;
import atlantis.constructing.AtlantisConstructingManager;
import atlantis.production.strategies.AbstractProductionStrategy;
import atlantis.wrappers.SelectUnits;

public class AtlantisProduceUnitManager {

	/**
	 * Is responsible for training new units and issuing construction requests for buildings.
	 */
	protected static void update() {
		AbstractProductionStrategy productionStrategy = AtlantisConfig.getProductionStrategy();

		ArrayList<UnitType> produceNow = productionStrategy.getUnitsThatShouldBeProducedNow();
		for (UnitType unitType : produceNow) {
			if (unitType.isBuilding()) {
				AtlantisConstructingManager.requestConstructionOf(unitType);
			} else {
				produceUnit(unitType);
			}
		}
	}

	// =========================================================

	private static void produceUnit(UnitType unitType) {

		// Worker
		if (unitType.equals(AtlantisConfig.WORKER)) {
			produceWorker();
		}

		// Infantry
		else if (unitType.isTerranInfantry()) {
			produceInfantry(unitType);
		}

		// Unknown example
		else {
			System.err.println("UNHANDLED UNIT TO BUILD: " + unitType);
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
