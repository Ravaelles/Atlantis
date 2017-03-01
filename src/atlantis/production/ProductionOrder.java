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
     * Contains first column 
     */
    private String rawFirstColumnInFile;
    
    /**
     * Number of row columns of line in build orders file.
     */
    private int numberOfColumnsInRow;

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
        if (unitOrBuilding != null) {
            return "Order: " + unitOrBuilding;
        }
        else if (upgrade != null) {
            return "Order: " + upgrade;
        }
        else if (tech != null) {
            return "Order: " + tech;
        }
        else {
            return "InvalidEmptyOrder";
        }
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

    public ProductionOrder copy() {
        ProductionOrder clone = new ProductionOrder();
        
        clone.id = firstFreeId++;
        clone.modifier = this.modifier;
        clone.numberOfColumnsInRow = this.numberOfColumnsInRow;
        clone.rawFirstColumnInFile = this.rawFirstColumnInFile;
        clone.tech = this.tech;
        clone.unitOrBuilding = this.unitOrBuilding;
        clone.upgrade = this.upgrade;
        
        return clone;
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

    public String getRawFirstColumnInFile() {
        return rawFirstColumnInFile;
    }

    public void setRawFirstColumnInFile(String rawFirstColumnInFile) {
        this.rawFirstColumnInFile = rawFirstColumnInFile;
    }

    public int getNumberOfColumnsInRow() {
        return numberOfColumnsInRow;
    }

    public void setNumberOfColumnsInRow(int numberOfColumnsInRow) {
        this.numberOfColumnsInRow = numberOfColumnsInRow;
    }
    
}
