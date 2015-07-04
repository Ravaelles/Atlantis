package atlantis.production.strategies;

import java.util.ArrayList;

import jnibwapi.types.UnitType;
import atlantis.AtlantisGame;
import atlantis.constructing.AtlantisConstructingManager;
import atlantis.information.AtlantisInformationCommander;
import atlantis.production.ProductionOrder;
import atlantis.util.RUtilities;
import atlantis.wrappers.MappingCounter;

public abstract class AbstractProductionStrategy {

	/**
	 * Ordered list of production orders as initially read from the file. It never changes
	 */
	private final ArrayList<ProductionOrder> initialProductionQueue = new ArrayList<>();

	/**
	 * Ordered list of next units we should build. It is re-generated when events like
	 * "started training/building new unit"
	 */
	private ArrayList<ProductionOrder> currentProductionQueue = new ArrayList<>();

	// /**
	// * Order list counter.
	// */
	// private int lastCounterInOrdersQueue = 0;

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
	 * If new unit is created (it doesn't need to exist, it's enough that it's just started training) or your unit is
	 * destroyed, we need to rebuild the production orders queue from the beginning (based on initial queue read from
	 * file). <br />
	 * This method will detect which units we lack and assign to <b>currentProductionQueue</b> list next units that we
	 * need. Note this method doesn't check if we can afford them, it only sets up proper sequence of next units to
	 * produce.
	 */
	public void rebuildQueue() {

		// Clear old production queue.
		currentProductionQueue.clear();

		// It will store [UnitType->(int)howMany] mapping as we gonna process initial production queue and check if we
		// currently have units needed
		MappingCounter<UnitType> virtualCounter = new MappingCounter<>();

		System.out.println("---------------");
		for (ProductionOrder order : initialProductionQueue) {
			UnitType type = order.getUnitType();
			virtualCounter.incrementValueFor(type);

			int shouldHaveThisManyUnits = virtualCounter.getValueFor(type);
			int weHaveThisManyUnits = AtlantisInformationCommander.countOurUnitsOfType(type);

			if (order.getUnitType().isBuilding()) {
				weHaveThisManyUnits += AtlantisConstructingManager.countNotStartedConstructionsOfType(type);
				// System.out.println("@@@ Not started constructions of '" + type + "': "
				// + AtlantisConstructingManager.countNotStartedConstructionsOfType(type));
			}

			// If we don't have this unit, add it to the current production queue.
			if (weHaveThisManyUnits < shouldHaveThisManyUnits) {
				currentProductionQueue.add(order);
				if (currentProductionQueue.size() >= 8) {
					break;
				}
			}
		}
	}

	/**
	 * Returns list of units that we should produce (train or build) now. It iterates over latest build orders and
	 * returns these orders that we can build in this very moment (we can afford them and they match our strategy).
	 */
	public ArrayList<UnitType> getUnitsToProduceRightNow() {
		ArrayList<UnitType> result = new ArrayList<>();
		int mineralsNeeded = 0;
		int gasNeeded = 0;

		// The idea as follows: as long as we can afford next enqueued production order, add it to the
		// CurrentToProduceList.
		for (ProductionOrder order : currentProductionQueue) {
			UnitType type = order.getUnitType();
			mineralsNeeded += type.getMineralPrice();
			gasNeeded += type.getGasPrice();

			// If we can afford this order and the previous, add it to CurrentToProduceList.
			if (AtlantisGame.canAfford(mineralsNeeded, gasNeeded) && AtlantisGame.canAfford(type)) {
				result.add(type);
			}

			// We can't afford to produce this order along with all previous ones. Return currently list.
			else {
				break;
			}
		}

		return result;
	}

	/**
	 * Returns <b>howMany</b> of next units to build, no matter if we can afford them or not.
	 */
	public ArrayList<UnitType> getProductionQueueNextUnits(int howMany) {
		ArrayList<UnitType> result = new ArrayList<>();

		for (int i = 0; i < howMany && i < currentProductionQueue.size(); i++) {
			result.add(currentProductionQueue.get(i).getUnitType());
		}

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
			initialProductionQueue.add(order);
			currentProductionQueue.add(order);
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
