package atlantis.constructing;

import java.util.concurrent.ConcurrentLinkedQueue;

import jnibwapi.Position;
import jnibwapi.Unit;
import jnibwapi.types.UnitType;
import atlantis.AtlantisGame;
import atlantis.constructing.position.ConstructionBuildPositionFinder;
import atlantis.information.AtlantisUnitInformationManager;

public class AtlantisConstructingManager {

	/**
	 * List of all unfinished (started or pending) constructions.
	 */
	private static ConcurrentLinkedQueue<ConstructionOrder> constructionOrders = new ConcurrentLinkedQueue<>();

	// =========================================================

	/**
	 * Issues request of constructing new building. It will automatically find position and builder unit for it.
	 */
	public static void requestConstructionOf(UnitType building) {
		// System.out.println("@@@@ REQUESTED: " + building);
		if (!building.isBuilding()) {
			throw new RuntimeException("Requested construction of not building!!! Type: " + building);
		}

		// Create ConstructionOrder object, assign random worker for the time being
		ConstructionOrder newConstructionOrder = new ConstructionOrder(building);
		newConstructionOrder.assignRandomBuilderForNow();

		if (newConstructionOrder.getBuilder() == null) {
			return;
		}

		// Find place for new building
		Position positionToBuild = ConstructionBuildPositionFinder.findPositionForNew(
				newConstructionOrder.getBuilder(), building);
		// System.out.println("@@ " + building + " at " + positionToBuild);

		// Successfully found position for new building
		Unit optimalBuilder = null;
		if (positionToBuild != null) {

			// Update construction order with found position for building
			newConstructionOrder.setPositionToBuild(positionToBuild);

			// Assign optimal builder for this building
			optimalBuilder = newConstructionOrder.assignOptimalBuilder();
			// System.out.println("@@ BUILDER = " + optimalBuilder);

			// Add to list of pending orders
			constructionOrders.add(newConstructionOrder);

			// Rebuild production queue as new building is about to be built
			AtlantisGame.getProductionStrategy().rebuildQueue();
		}

		// Couldn't find place for building! That's f'g bad.
		else {
			System.err.println("requestConstruction HAS FAILED! DETAILS: POSITION TO BUILD: " + positionToBuild
					+ " / BUILDER = " + optimalBuilder);
		}
	}

	// =========================================================

	/**
	 * Manages all pending construction orders. Ensures builders are assigned to constructions, removes finished objects
	 * etc.
	 */
	public static void update() {
		for (ConstructionOrder constructionOrder : constructionOrders) {
			checkForConstructionStatusChange(constructionOrder, constructionOrder.getConstruction());
			checkForBuilderStatusChange(constructionOrder, constructionOrder.getBuilder());
		}
	}

	// =========================================================

	/**
	 * If builder has died when constructing, replace him with new one.
	 */
	private static void checkForBuilderStatusChange(ConstructionOrder constructionOrder, Unit builder) {
		if (builder == null || !builder.isAlive()) {
			constructionOrder.assignOptimalBuilder();
		}
	}

	/**
	 * If building is completed, mark construction as finished and remove it.
	 */
	private static void checkForConstructionStatusChange(ConstructionOrder constructionOrder, Unit building) {

		// If building is not assigned, check if we can get building-unit reference assigned from the builder
		if (building == null || !building.isExists()) {
			Unit builder = constructionOrder.getBuilder();
			if (builder != null) {
				Unit buildUnit = builder.getBuildUnit();
				if (buildUnit != null) {
					constructionOrder.setConstruction(buildUnit);
					building = buildUnit;
				}
			}
		}

		// If building is finished, remove it from the list
		if (building != null && building.isCompleted()) {
			constructionOrder.setStatus(ConstructionOrderStatus.CONSTRUCTION_FINISHED);
			constructionOrders.remove(constructionOrder);

			// @FIX to fix bug with Refineries not being shown as created, because they're kinda changed.
			if (building.getType().isGasBuilding()) {
				AtlantisUnitInformationManager.rememberUnit(building);
			}
		}
	}

	// =========================================================no
	// Public class access methods

	/**
	 * Returns true if given worker has been assigned to construct new building or if the constructions is already in
	 * progress.
	 */
	public static boolean isBuilder(Unit worker) {
		if (worker.isConstructing()) {
			return true;
		}

		for (ConstructionOrder constructionOrder : constructionOrders) {
			if (worker.equals(constructionOrder.getBuilder())) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Returns ConstructionOrder object for given builder.
	 */
	public static ConstructionOrder getConstructionOrderFor(Unit builder) {
		for (ConstructionOrder constructionOrder : constructionOrders) {
			if (builder.equals(constructionOrder.getBuilder())) {
				return constructionOrder;
			}
		}

		return null;
	}

	/**
	 * If we requested to build building A and even assigned worker who's travelling to the building site, it's still
	 * doesn't count as unitCreated. We need to manually count number of constructions and only then, we can e.g. tell
	 * "how many unfinished barracks we have".
	 */
	public static int countNotStartedConstructionsOfType(UnitType type) {
		int total = 0;
		for (ConstructionOrder constructionOrder : constructionOrders) {
			if (constructionOrder.getStatus() != ConstructionOrderStatus.CONSTRUCTION_FINISHED
					&& constructionOrder.getBuildingType().equals(type)) {
				total++;
			}
		}
		return total;
	}

}
