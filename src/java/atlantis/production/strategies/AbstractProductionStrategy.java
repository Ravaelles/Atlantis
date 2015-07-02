package atlantis.production.strategies;

import java.util.ArrayList;

import jnibwapi.types.UnitType;
import jnibwapi.types.UnitType.UnitTypes;
import atlantis.production.ProductionOrder;
import atlantis.util.RUtilities;

public abstract class AbstractProductionStrategy {

	/**
	 * List of production orders as initially read from the file.
	 */
	private ArrayList<ProductionOrder> orders = new ArrayList<>();

	// =========================================================
	// Constructor

	public AbstractProductionStrategy() {
		initializeProductionQueue();
	}

	// =========================================================
	// Abstract methods

	protected abstract String getFilename();

	public abstract void update();

	// =========================================================
	// Public defined methods

	/**
	 * Returns list of units that we can (afford) and should train/build now. E.g. it can be one CSV or two CSV and one
	 * marine. This represents all affordable and available @TODO
	 */
	public ArrayList<UnitType> getUnitsThatShouldBeProducedNow() {
		ArrayList<UnitType> result = new ArrayList<>();
		// @TODO
		result.add(UnitTypes.Terran_SCV);
		result.add(UnitTypes.Terran_Barracks);
		result.add(UnitTypes.Terran_Marine);

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
			String unitTypeString = row[inRowCounter++];
			UnitType unitType = UnitType.getByName(unitTypeString);
			if (unitType == null) {
				System.err.println("Invalid unit name: " + unitTypeString);
				System.exit(-1);
			}
			// System.out.println("unitTypeString = " + unitTypeString + " / unitType = " + unitType);

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
