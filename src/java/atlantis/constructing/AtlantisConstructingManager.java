package atlantis.constructing;

import java.util.concurrent.ConcurrentLinkedQueue;

import jnibwapi.Position;
import jnibwapi.Unit;
import jnibwapi.types.UnitType;
import atlantis.constructing.position.ConstructionBuildPositionFinder;

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

		// Create ConstructionOrder object, assign random worker for the time being
		ConstructionOrder newConstructionOrder = new ConstructionOrder(building);
		newConstructionOrder.assignRandomBuilderForNow();

		// Find place for new building
		Position positionToBuild = ConstructionBuildPositionFinder.findPositionForNew(building);
		System.out.println("@@ " + building + " at " + positionToBuild);

		// Successfully found position for new building
		Unit optimalBuilder = null;
		if (positionToBuild != null) {

			// Update construction order with found position for building
			newConstructionOrder.setPositionToBuild(positionToBuild);

			// Assign optimal builder for this building
			optimalBuilder = newConstructionOrder.assignOptimalBuilder();

			// Add to list of pending orders
			constructionOrders.add(newConstructionOrder);
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
			checkForBuilderStatusChange(constructionOrder.getBuilder());
		}
	}

	// =========================================================

	private static void checkForBuilderStatusChange(Unit builder) {
	}

	private static void checkForConstructionStatusChange(ConstructionOrder constructionOrder, Unit building) {
		// If building is finished, remove it from the list
		if (building != null && building.isCompleted()) {
			constructionOrder.setStatus(ConstructionOrderStatus.CONSTRUCTION_FINISHED);
			constructionOrders.remove(constructionOrder);
		}
	}

	// =========================================================
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

}
