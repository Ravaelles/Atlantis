package atlantis.production;

import atlantis.units.AUnitType;
import bwapi.TechType;
import bwapi.UpgradeType;

public class ProductionOrder {
    
    public static final String BASE_POSITION_NATURAL = "NATURAL";
    public static final String BASE_POSITION_MAIN = "MAIN";

    private static final int PRIORITY_LOWEST = 1;
    private static final int PRIORITY_NORMAL = 4;
    private static final int PRIORITY_HIGHEST = 8;

    // =========================================================
    private static int firstFreeId = 1;
    private int id = firstFreeId++;

    /**
     * AUnit type to be build. Can be null if this production order is for something else than upgrade.
     */
    private AUnitType unitOrBuilding = null;

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
    
    public ProductionOrder(AUnitType unitOrBuilding) {
        this();
        this.unitOrBuilding = unitOrBuilding;
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
        return "Order: " + unitOrBuilding; //TODO: test replacement of getName();
    }

    public String getShortName() {
        if (unitOrBuilding != null) {
            return unitOrBuilding.getShortName();
        } else if (upgrade != null) {
            return upgrade.toString();
        } else {
            return "Unknown";
        }
    }

    // =========================================================
    // Getters
    
    /**
     * If this production order concerns unit to be build (or building, AUnit class), it will return non-null
     * value being unit type.
     */
    public AUnitType getUnitOrBuilding() {
        return unitOrBuilding;
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
     * Special modifier e.g. base position modifier. See ProductionOrder constants.
     */
    public void setModifier(String modifier) {
        if (modifier != null) {
            modifier = modifier.trim();
        }
        this.modifier = modifier;
    }
    
}
