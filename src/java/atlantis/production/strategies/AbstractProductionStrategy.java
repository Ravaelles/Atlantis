package atlantis.production.strategies;

import java.util.ArrayList;

import jnibwapi.types.UnitType;
import atlantis.AtlantisGame;
import atlantis.production.ProductionOrder;
import atlantis.util.RUtilities;

public abstract class AbstractProductionStrategy {

	/**
	 * List of production orders as initially read from the file.
	 */
	private ArrayList<ProductionOrder> initialOrders = new ArrayList<>();

	/**
	 * List of orders to be processed.
	 */
	private ArrayList<ProductionOrder> orders = new ArrayList<>();

	/**
	 * Order list counter.
	 */
	private int counterInOrdersQueue = 0;

	// =========================================================
	// Constructor

	public AbstractProductionStrategy() {
		initializeProductionQueue();
	}

	// =========================================================
	// Abstract methods

	protected abstract String getFilename();

	// =========================================================
	// Public defined methods

	/**
	 * If new unit is created (it doesn't need to exist, it's enough that it's trained) or is destroyed, we need to
	 * rebuild the production orders queue.
	 */
	public void rebuildQueue() {

	}

	/**
	 * Returns list of units that we can (afford) and should train/build now. E.g. it can be one CSV or two CSV and one
	 * marine. This represents all affordable and available @TODO
	 */
	public ArrayList<UnitType> getUnitsThatShouldBeProducedNow() {
		ArrayList<UnitType> result = new ArrayList<>();

		for (int i = counterInOrdersQueue; i <= 20; i++) {
			if (orders.size() <= i) {
				break;
			}

			UnitType unitType = orders.get(i).getUnitType();

			// Aleways try to produce first order in queue
			if (i == counterInOrdersQueue) {
				result.add(unitType);
			}

			// If we can afford next ones, produce them as well
			else if (AtlantisGame.canAfford(unitType)) {
				result.add(unitType);
			}

			// If we can't afford next order, breaks.
			else {
				break;
			}
		}

		// @TODO
		// result.add(UnitTypes.Terran_SCV);
		// result.add(UnitTypes.Terran_Barracks);
		// result.add(UnitTypes.Terran_Marine);

		return result;
	}

	// =========================================================
	// Private defined methods

	/**
	 * Populates <b>productionOrdersFromFile</b> with data from CSV file.
	 */
	private void createProductionOrderListFromStringArray(String[][] loadedFile) {

		// Skip first row as it's CSV header
		for (int i = 1; i < loadedFile.length; i++) {
			String[] row = loadedFile[i];
			int inRowCounter = 0;

			// =========================================================
			// Parse entire row of strings

			// Type of entry: Unit / Research etc
			@SuppressWarnings("unused")
			String entryType = row[inRowCounter++];

			// Unit type
			String unitTypeString = row[inRowCounter++].toLowerCase();
			UnitType unitType = UnitType.getByName(unitTypeString);
			if (unitType == null) {
				System.err.println("Invalid unit name: " + unitTypeString);
				System.exit(-1);
			}

			// Blocking
			boolean isBlocking;
			String blockingString = row[inRowCounter++];
			if (blockingString.isEmpty() || blockingString.equals("") || blockingString.toLowerCase().equals("No")) {
				isBlocking = false;
			} else {
				isBlocking = true;
			}

			// Priority
			boolean isLowestPriority = false;
			boolean isHighestPriority = false;
			String priorityString = row[inRowCounter++];
			if (!priorityString.isEmpty()) {
				priorityString = priorityString.toLowerCase();
				if (priorityString.contains("low")) {
					isLowestPriority = true;
				} else if (priorityString.contains("high")) {
					isHighestPriority = true;
				}
			}

			// =========================================================
			// Create ProductionOrder object from strings-row

			ProductionOrder order = new ProductionOrder(unitType);
			if (isBlocking) {
				order.markAsBlocking();
			}
			if (isHighestPriority) {
				order.priorityHighest();
			}
			if (isLowestPriority) {
				order.priorityLowest();
			}

			// Enqueue created order
			initialOrders.add(order);
			orders.add(order);
		}
	}

	/**
	 * Reads build orders from CSV file and converts them into ArrayList.
	 */
	private void initializeProductionQueue() {

		// Read file into 2D String array
		String path = "bwapi-data/read/build_orders/" + getFilename();
		String[][] loadedFile = RUtilities.loadCsv(path, 4);

		// We can display file here, if we want to
		// displayLoadedFile(loadedFile);

		// Convert 2D String array into ArrayList of ProductionOrder
		createProductionOrderListFromStringArray(loadedFile);
	}

	/**
	 * Auxiliary method that can be run to see what was loaded from CSV file.
	 */
	@SuppressWarnings("unused")
	private void displayLoadedFile(String[][] loadedFile) {
		int rowCounter = 0;
		for (String[] rows : loadedFile) {
			if (rowCounter == 0) {
				rowCounter++;
				continue;
			}

			// =========================================================

			for (String value : rows) {
				System.out.print(value + " | ");
			}
			System.out.println();

			// =========================================================

			rowCounter++;
		}

		System.exit(0);
	}

}
