package atlantis.constructing;

import jnibwapi.Position;
import jnibwapi.Unit;
import jnibwapi.types.UnitType;
import atlantis.AtlantisGame;
import atlantis.constructing.position.AbstractBuildPositionFinder;

public class AtlantisBuilderManager {

	public static void update(Unit builder) {
		if (builder == null) {
			return;
		}

		// Don't disturb builder that are already constructing
		if (builder.isConstructing()) {
			return;
		}

		handleConstruction(builder);
	}

	// =========================================================

	private static void handleConstruction(Unit builder) {
		ConstructionOrder constructionOrder = AtlantisConstructingManager.getConstructionOrderFor(builder);
		if (constructionOrder != null) {

			// Construction HASN'T STARTED YET, we're probably not even at the required place
			if (constructionOrder.getStatus() == ConstructionOrderStatus.CONSTRUCTION_NOT_STARTED) {
				travelToConstruct(builder, constructionOrder);
			}

			// Construction is IN PROGRESS
			else if (constructionOrder.getStatus() == ConstructionOrderStatus.CONSTRUCTION_IN_PROGRESS) {
				// Do nothing
			}

			// Construction has FINISHED
			else if (constructionOrder.getStatus() == ConstructionOrderStatus.CONSTRUCTION_FINISHED) {
				// Do nothing
			}
		}
	}

	private static void travelToConstruct(Unit builder, ConstructionOrder constructionOrder) {
		Position buildPosition = constructionOrder.getPositionToBuild();
		UnitType buildingType = constructionOrder.getBuildingType();
		
		if (builder == null) {
			throw new RuntimeException("Builder empty");
		}

		// Move builder to the build position
		if (builder.distanceTo(buildPosition) < 0.5) {
			builder.move(buildPosition, false);
		}

		// Unit is already at the build position, issue build order
		else {

			// If we can afford to construct this building exactly right now, issue build order which should be
			// immediate as unit is standing just right there
			if (AtlantisGame.hasMinerals(buildingType.getMineralPrice())
					&& AtlantisGame.hasGas(buildingType.getGasPrice())) {
				if (!AbstractBuildPositionFinder.canPhysicallyBuildHere(builder, buildingType, buildPosition)) {
					buildPosition = constructionOrder.findNewBuildPosition();
				}
				builder.build(buildPosition, buildingType);
			}
		}

	}

}
