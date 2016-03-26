package atlantis.production;

import bwapi.TechType;
import atlantis.util.NameUtil;
import atlantis.util.UnitUtil;
import bwapi.UnitType;
import bwapi.UpgradeType;

public class ProductionOrder {

    private static final int PRIORITY_LOWEST = 1;
    private static final int PRIORITY_NORMAL = 4;
    private static final int PRIORITY_HIGHEST = 8;

    // =========================================================
    private static int firstFreeId = 1;
    private int id = firstFreeId++;

    /**
     * Unit type to be build. Can be null if this production order is for something else than upgrade.
     */
    private UnitType unitType = null;

    /**
     * Upgrade type to research. Can be null if this production order is for something else than upgrade.
     */
    private UpgradeType upgrade;

    /**
     * Tech type to research. Can be null if this production order is for something else than upgrade.
     */
    private TechType tech;
    
    /**
     * Special modifier e.g. base position modifier. See ConstructionSpecialBuildPositionFinder constants.
     */
    private String modifier = null;

    /**
     *
     */
//    private int priority;

    /**
     * If true, no other order that comes after this order in the ProductionQueue can be started.
     */
//    private boolean blocking = false;

    // =========================================================
    public ProductionOrder(UnitType unitType) {
        this();
        this.unitType = unitType;
    }

    public ProductionOrder(UpgradeType upgrade) {
        super();
        this.upgrade = upgrade;
    }

    public ProductionOrder(TechType tech) {
        super();
        this.tech = tech;
    }

    private ProductionOrder() {
//        priority = PRIORITY_NORMAL;
    }

    // =========================================================
    /**
     * If true, no other order that comes after this order in the ProductionQueue can be started.
     */
//    protected boolean isBlocking() {
//        return blocking;
//    }

    /**
     * If true, no other order that comes after this order in the ProductionQueue can be started.
     */
//    public ProductionOrder markAsBlocking() {
//        this.blocking = true;
//        this.priority = PRIORITY_HIGHEST;
//        return this;
//    }

//    public ProductionOrder priorityLowest() {
//        this.priority = PRIORITY_LOWEST;
//        return this;
//    }

//    public ProductionOrder priorityHighest() {
//        this.priority = PRIORITY_HIGHEST;
//        return this;
//    }

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
//        return "Order: " + unitType.getName() + ", blocking:" + blocking + ", priority:" + priority;
        return "Order: " + unitType; //TODO: test replacement of getName();
    }

    public String getShortName() {
        if (unitType != null) {
            return NameUtil.getShortName(unitType);
        } else if (upgrade != null) {
            return upgrade.toString(); //replaces .getName();
        } else {
            return "Unknown";
        }
    }

    // =========================================================
    // Getters
    /**
     * If this production order concerns unit to be build (or building, Unit class), it will return non-null
     * value being unit type.
     */
    public UnitType getUnitType() {
        return unitType;
    }

    /**
     * If this production order concerns upgrade (UpgradeType class) to be researched, it will return non-null
     * value being unit type.
     */
    public UpgradeType getUpgrade() {
        return upgrade;
    }

    /**
     * If this production order concerns technology (TechType class) to be researched, it will return non-null
     * value being unit type.
     */
    public TechType getTech() {
        return tech;
    }

    /**
     * Special modifier e.g. base position modifier. See ConstructionSpecialBuildPositionFinder constants.
     */
    public String getModifier() {
        return modifier;
    }

    /**
     * Special modifier e.g. base position modifier. See ConstructionSpecialBuildPositionFinder constants.
     */
    public void setModifier(String modifier) {
        this.modifier = modifier;
    }
    
}
