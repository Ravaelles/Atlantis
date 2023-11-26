package atlantis.production.orders.production.queue.order;

import atlantis.combat.missions.Mission;
import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.production.orders.production.Requirements;
import atlantis.production.orders.production.queue.Queue;
import atlantis.production.orders.production.queue.events.OrderStatusWasChanged;
import atlantis.production.orders.production.queue.updater.IsReadyToProduceOrder;
import atlantis.units.AUnitType;
import bwapi.TechType;
import bwapi.UpgradeType;

public class ProductionOrder implements Comparable<ProductionOrder> {
    private static int firstFreeId = 1;
    /**
     * Upgrade type to research. Can be null if this production order is for something else than upgrade.
     */
    private final UpgradeType upgrade;
    /**
     * Tech type to research. Can be null if this production order is for something else than upgrade.
     */
    private final TechType tech;
    private int id;
    private OrderStatus status = OrderStatus.NOT_READY;
    /**
     * How much supply has to be used for this order to become active
     */
    private int minSupply;
    /**
     * AUnit type to be built. Can be null if this production order is for something else than upgrade.
     */
    private AUnitType unitOrBuilding = null;
    /**
     * Makes sense only for buildings.
     */
    private HasPosition position = null;
    /**
     * Maximum distance from a given position to build this building.
     * -1 means no limit.
     */
    private int maximumDistance = -1;
    /**
     * At this supply we should change global mission to this.
     */
    private Mission mission;

    /**
     * Special modifier e.g. base position modifier. See ConstructionSpecialBuildPositionFinder constants.
     */
    private String modifier = null;

    private boolean dynamic = true;

    /**
     * If true, then the building will be built exactly at given position, without any modifications.
     * If false, then the building will be built at the nearest possible position.
     */
    private boolean usingExactPosition = false;

    /**
     * Contains first column
     */
    private String rawFirstColumnInFile;

    private ProductionOrderPriority priority = ProductionOrderPriority.STANDARD;

    private OrderReservations orderReservations = new OrderReservations(this);

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

    public ProductionOrder(AUnitType unitOrBuilding, HasPosition position, int minSupply) {
        this(unitOrBuilding, position, null, null, minSupply);
    }

    public ProductionOrder(AUnitType type, HasPosition position, TechType tech, UpgradeType upgrade, int minSupply) {
        assert type != null || tech != null || upgrade != null;

        this.id = firstFreeId++;
        this.unitOrBuilding = type;
        this.position = position;
        this.minSupply = minSupply;
        this.tech = tech;
        this.upgrade = upgrade;

//        if (unitOrBuilding != null && unitOrBuilding.isBunker()) {
//            System.err.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
//            A.printStackTrace("BUNKER ORDER CREATED at " + position);
//            System.err.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
//        }
    }

    // =========================================================

    public boolean canAffordWithReserved() {
        return IsReadyToProduceOrder.canAffordWithReserved(this);
    }

    // =========================================================

    @Override
    public boolean equals(Object object) {
        if (object == null) return false;
        if (!(object instanceof ProductionOrder)) return false;
        ProductionOrder otherOrder = (ProductionOrder) object;

        if (otherOrder.id == id) return true;

        if (otherOrder.minSupply == minSupply) {
//            if (otherOrder.isDynamic() || otherOrder.isDynamic()) {
//                if (otherOrder.unitType() != null && otherOrder.unitType().equals(unitType())) return true;
//            }
            if (otherOrder.tech() != null && otherOrder.tech().equals(tech())) return true;
            if (otherOrder.upgrade() != null && otherOrder.upgrade().equals(upgrade())) return true;
        }

        return false;
    }

    @Override
    public int hashCode() {
        return id * 7;
    }

    @Override
    public String toString() {
        String suffix = (isDynamic() ? "*" : "") + " " + statusString() + "(#" + id() + ")";

        if (position != null) suffix += " @ " + position;

        if (unitOrBuilding != null) {
            return "At " + minSupply + " " + name() + (modifier != null ? " " + modifier : "") + suffix;
        }
        else if (upgrade != null) {
            return "At " + minSupply + " " + name() + suffix;
        }
        else if (tech != null) {
            return "At " + minSupply + " " + name() + suffix;
        }
        else if (mission != null) {
            return "At " + minSupply + " " + mission.name();
        }
        else {
            return "InvalidEmptyOrder";
        }
    }

    private String statusString() {
        if (isStatus(OrderStatus.NOT_READY)) return "";
        else if (isStatus(OrderStatus.IN_PROGRESS)) return "(IN_PROGRESS)";
        else if (isStatus(OrderStatus.READY_TO_PRODUCE)) return "(READY_TO_PRODUCE)";
        else if (isStatus(OrderStatus.COMPLETED)) return "(COMPLETED)";
        else return "UNKNOWN";
    }

    @Override
    public int compareTo(ProductionOrder o) {
        return Integer.compare(id, o.id);
    }

    // =========================================================

    public String name() {
        if (unitOrBuilding != null) {
            return unitOrBuilding.name();
        }
        else if (upgrade != null) {
            return upgrade.toString().replace("_", " ");
        }
        else if (tech != null) {
            return tech.toString().replace("_", " ");
        }
        else if (mission != null) {
            return mission.toString().replace("_", " ");
        }
        else {
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

    public boolean supplyRequirementFulfilled() {
        int bonus = unitOrBuilding != null && A.supplyUsed() >= 10 && unitOrBuilding.isABuilding() ? 1 : 0;

        return AGame.supplyUsed() + bonus >= minSupply;
    }

    public void cancel() {
        Queue.get().removeOrder(this);
    }

    // === Getters =============================================

    public int mineralPrice() {
        if (unitOrBuilding != null) {
            return unitOrBuilding.getMineralPrice();
        }
        else if (upgrade != null) {
            return upgrade.mineralPrice();
        }
        else if (tech != null) {
            return tech.mineralPrice();
        }
        else {
            return 0;
        }
    }

    public int gasPrice() {
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
    public AUnitType unitType() {
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

    public boolean checkIfHasWhatRequired() {
        return Requirements.hasRequirements(this);
    }

    public HasPosition atPosition() {
        return position;
    }

    public ProductionOrderPriority priority() {
        return priority;
    }

    public void setPriority(ProductionOrderPriority priority) {
        this.priority = priority;
    }

    public int minSupply() {
        return minSupply;
    }

    public boolean isInProgress() {
        return isStatus(OrderStatus.IN_PROGRESS);
    }

    public boolean isCompleted() {
        return isStatus(OrderStatus.COMPLETED);
    }

    public boolean isReadyToProduce() {
        return isStatus(OrderStatus.READY_TO_PRODUCE);
    }

    public Mission mission() {
        return mission;
    }

    public void setMission(Mission mission) {
        this.mission = mission;
    }

    public int maximumDistance() {
        return maximumDistance;
    }

    public ProductionOrder setMaximumDistance(int maximumDistance) {
        this.maximumDistance = maximumDistance;
        return this;
    }

    public int id() {
        return id;
    }

    public OrderStatus status() {
        return status;
    }

    public boolean isStatus(OrderStatus orderStatus) {
        return status.equals(orderStatus);
    }

    public OrderStatus setStatus(OrderStatus newStatus) {
        if (this.status != newStatus) {
            this.status = newStatus;

            OrderStatusWasChanged.update(this, newStatus);
        }

        return status;
    }

    public void makeSureResourcesAreReserved() {
        orderReservations.reserveResources();
    }

    public void makeSureToClearReservedResources() {
        orderReservations.clearResourcesReserved();
    }

    public OrderReservations reservations() {
        return orderReservations;
    }

    public boolean isBuilding() {
        return unitOrBuilding != null && unitOrBuilding.isABuilding();
    }

    public void markAsNotDynamic() {
        dynamic = false;
    }

    public boolean isDynamic() {
        return dynamic;
    }

    public boolean isUnitOrBuilding() {
        return unitOrBuilding != null;
    }

    public void markAsUsingExactPosition() {
        usingExactPosition = true;
    }

    public boolean isUsingExactPosition() {
        return usingExactPosition;
    }

    public void forceSetPosition(APosition position) {
        this.position = position;
    }

    public void setUnitType(AUnitType type) {
        this.unitOrBuilding = type;
    }
}
