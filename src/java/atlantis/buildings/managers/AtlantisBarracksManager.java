package atlantis.buildings.managers;

import java.util.ArrayList;

import jnibwapi.Unit;
import jnibwapi.types.UnitType;
import jnibwapi.types.UnitType.UnitTypes;
import atlantis.AtlantisGame;
import atlantis.production.ProductionOrder;

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

		// =========================================================
		// Check PRODUCTION QUEUE

		ArrayList<ProductionOrder> unitsToProduce = AtlantisGame.getProductionStrategy().getThingsToProduceRightNow(
				true);

		// EMPTY PRODUCTION QUEUE - can build
		if (unitsToProduce.isEmpty()) {
			return true;
		}

		// Production queue not empty
		else {
			for (ProductionOrder order : unitsToProduce) {
				if (order.getUnitType().isInfantry()) {
					return true;
				}
			}
		}

		return false;
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
