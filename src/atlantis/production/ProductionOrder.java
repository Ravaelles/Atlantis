package atlantis.production;

import atlantis.combat.missions.Mission;
import atlantis.position.APosition;
import atlantis.production.orders.ProductionOrderPriority;
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
    private int id;

    /**
     * How much supply has to be used for this order to become active
     */
    private int minSupply;

    /**
     * AUnit type to be build. Can be null if this production order is for something else than upgrade.
     */
    private AUnitType unitOrBuilding = null;

    /**
     * Makes sense only for buildings.
     */
    private APosition position = null;

    /**
     * Upgrade type to research. Can be null if this production order is for something else than upgrade.
     */
    private UpgradeType upgrade;

    /**
     * Tech type to research. Can be null if this production order is for something else than upgrade.
     */
    private TechType tech;

    /**
     * At this supply we should change global mission to this.
     */
    private Mission mission;

    /**
     * Special modifier e.g. base position modifier. See ConstructionSpecialBuildPositionFinder constants.
     */
    private String modifier = null;

    private boolean currentlyInProduction = false;

    /**
     * Contains first column 
     */
    private String rawFirstColumnInFile;
    
    /**
     * Number of row columns of line in build orders file.
     */
    private int numberOfColumnsInRow;

    /**
     * Metadata - used in APainter to show if we can afford it or not.
     */
    private boolean hasWhatRequired;

    /**
     * Metadata - if we have enough minerals and gas to build it (including reserved for other untis, higher in queue).
     */
    private boolean canAffordNow;

    /**
     *
     */
    private ProductionOrderPriority priority = ProductionOrderPriority.STANDARD;

    /**
     * If true, no other order that comes after this order in the ProductionQueue can be started.
     */
//    private boolean blocking = false;

    // =========================================================

    public ProductionOrder(AUnitType unitOrBuilding, int minSupply) {
        this(unitOrBuilding, null, null, null, minSupply);
    }

    public ProductionOrder(UpgradeType upgrade, int minSupply) {
        this(null, null, null, upgrade, minSupply);
    }

    public ProductionOrder(TechType tech, int minSupply) {
        this(null, null, tech, null, minSupply);
    }

    public ProductionOrder(Mission mission, int minSupply) {
        this(null, null, null, null, minSupply);
        setMission(mission);
    }

    public ProductionOrder(AUnitType unitOrBuilding, APosition position, int minSupply) {
        this(unitOrBuilding, position, null, null, minSupply);
    }

    public ProductionOrder(AUnitType unitOrBuilding, APosition position, TechType tech, UpgradeType upgrade, int minSupply) {
        assert unitOrBuilding != null || tech != null || upgrade != null;

        this.id = firstFreeId++;
        this.unitOrBuilding = unitOrBuilding;
        this.position = position;
        this.minSupply = minSupply;
        this.tech = tech;
        this.upgrade = upgrade;
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
            return "Order at " + minSupply + " " + shortName();
        }
        else if (upgrade != null) {
            return "Order at " + minSupply + " " + shortName();
        }
        else if (tech != null) {
            return "Order at " + minSupply + " " + shortName();
        }
        else if (mission != null) {
            return "Order at " + minSupply + " " + mission.name();
        }
        else {
            return "InvalidEmptyOrder";
        }
    }

    public String shortName() {
        if (unitOrBuilding != null) {
            return unitOrBuilding.shortName();
        } else if (upgrade != null) {
            return upgrade.toString().replace("_", " ");
        } else if (tech != null) {
            return tech.toString().replace("_", " ");
        } else if (mission != null) {
            return mission.toString().replace("_", " ");
        } else {
            return "Unknown - BUG";
        }
    }

//    public ProductionOrder copy() {
//        ProductionOrder clone = new ProductionOrder(unitOrBuilding, position, tech, upgrade, minSupply);
//
//        clone.id = firstFreeId++;
//        clone.modifier = this.modifier;
//        clone.numberOfColumnsInRow = this.numberOfColumnsInRow;
//        clone.rawFirstColumnInFile = this.rawFirstColumnInFile;
//        clone.upgrade = this.upgrade;
//
//        return clone;
//    }
    
    // === Getters =============================================
    
    public int getGasRequired() {
        if (unitOrBuilding != null) {
            return unitOrBuilding.getGasPrice();
        }
        else if (upgrade != null) {
            return upgrade.gasPrice();
        }
        else if (tech != null) {
            return tech.gasPrice();
        }
        else {
            return 0;
        }
    }
    
    /**
     * If this production order concerns unit to be build (or building, AUnit class), it will return non-null
     * value being unit type.
     */
    public AUnitType unit() {
        return unitOrBuilding;
    }

    /**
     * If this production order concerns upgrade (UpgradeType class) to be researched, it will return non-null
     * value being unit type.
     */
    public UpgradeType upgrade() {
        return upgrade;
    }

    /**
     * If this production order concerns technology (TechType class) to be researched, it will return non-null
     * value being unit type.
     */
    public TechType tech() {
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
        if (modifier != null && modifier.charAt(0) == '@') {
            modifier = modifier.substring(1);
        }
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

    public void setNumberOfColumnsInRow(int numberOfColumnsInRow) {
        this.numberOfColumnsInRow = numberOfColumnsInRow;
    }

    public void setHasWhatRequired(boolean hasWhatRequired) {
        this.hasWhatRequired = hasWhatRequired;
    }

    public boolean canHasWhatRequired() {
        return hasWhatRequired;
    }

    public APosition getPosition() {
        return position;
    }

    public ProductionOrderPriority priority() {
        return priority;
    }

    public void setPriority(ProductionOrderPriority priority) {
        this.priority = priority;
    }

    public void setCanAffordNow(boolean canAffordNow) {
        this.canAffordNow = canAffordNow;
    }

    public boolean canAffordNow() {
        return canAffordNow;
    }

    public int minSupply() {
        return minSupply;
    }

    public boolean currentlyInProduction() {
        return currentlyInProduction;
    }

    public void setCurrentlyInProduction(boolean currentlyInProduction) {
        this.currentlyInProduction = currentlyInProduction;
    }

    public Mission mission() {
        return mission;
    }

    public void setMission(Mission mission) {
        this.mission = mission;
    }
}
