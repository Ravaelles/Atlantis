package atlantis.production;

import jnibwapi.types.UnitType;
import jnibwapi.types.UpgradeType;

public class ProductionOrder {

	private static final int PRIORITY_LOWEST = 1;
	private static final int PRIORITY_NORMAL = 4;
	private static final int PRIORITY_HIGHEST = 8;

	// =========================================================

	private static int firstFreeId = 1;
	private int id = firstFreeId++;

	/**
	 * Unit type to be build. Can be null if this production order is for upgrade, not unit.
	 */
	private UnitType unitType = null;

	/**
	 * Upgrade type to research. Can be null if this production order is for unit, not upgrade.
	 */
	private UpgradeType upgrade;

	/**
	 * 
	 */
	private int priority;

	/**
	 * If true, no other order that comes after this order in the ProductionQueue can be started.
	 */
	private boolean blocking = false;

	// =========================================================

	public ProductionOrder(UnitType unitType) {
		this();
		this.unitType = unitType;
	}

	public ProductionOrder(UpgradeType upgrade) {
		super();
		this.upgrade = upgrade;
	}

	private ProductionOrder() {
		priority = PRIORITY_NORMAL;
	}

	// =========================================================

	/**
	 * If true, no other order that comes after this order in the ProductionQueue can be started.
	 */
	protected boolean isBlocking() {
		return blocking;
	}

	/**
	 * If true, no other order that comes after this order in the ProductionQueue can be started.
	 */
	public ProductionOrder markAsBlocking() {
		this.blocking = true;
		this.priority = PRIORITY_HIGHEST;
		return this;
	}

	public ProductionOrder priorityLowest() {
		this.priority = PRIORITY_LOWEST;
		return this;
	}

	public ProductionOrder priorityHighest() {
		this.priority = PRIORITY_HIGHEST;
		return this;
	}

	// =========================================================
	// Override

	@Override
	public boolean equals(Object object) {
		if (object == null) {
			return false;
		}
		if (!(object instanceof ProductionOrder)) {
			return false;
		}
		return ((ProductionOrder) object).id == id;
	}

	@Override
	public int hashCode() {
		return hashCode() * 7;
	}

	@Override
	public String toString() {
		return "Order: " + unitType.getName() + ", blocking:" + blocking + ", priority:" + priority;
	}

	public String getShortName() {
		if (unitType != null) {
			return unitType.getShortName();
		} else if (upgrade != null) {
			return upgrade.getName();
		} else {
			return "Unknown";
		}
	}

	// =========================================================
	// Getters

	public UnitType getUnitType() {
		return unitType;
	}

	public UpgradeType getUpgrade() {
		return upgrade;
	}

}
