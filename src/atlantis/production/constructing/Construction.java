package atlantis.production.constructing;

import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.production.constructing.position.APositionFinder;
import atlantis.production.orders.production.ProductionOrder;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import atlantis.util.cache.Cache;

/**
 * Represents construction of a building, including ones not yet started.
 */
public class Construction implements Comparable<Construction> {
    private static Cache<Object> cache = new Cache<>();

    private static int _firstFreeId = 1;
    private final int ID = _firstFreeId++;
    private final int timeOrdered;
    private int timeBecameInProgress;
    private AUnitType buildingType;
    private AUnit construction;
    private AUnit builder;
    private APosition positionToBuild;
    private HasPosition near;
    private double maxDistance;
    private ProductionOrder productionOrder;
    private ConstructionOrderStatus status;

    // =========================================================

    public Construction(AUnitType buildingType) {
        this.buildingType = buildingType;

        status = ConstructionOrderStatus.CONSTRUCTION_NOT_STARTED;
        timeOrdered = AGame.now();
    }

    // =========================================================

    /**
     * If it's impossible to build in given position (e.g. occupied by units), find new position.
     */
    public APosition findPositionForNewBuilding() {
        return (APosition) cache.get(
            "buildingType,builder:" + builder.id(),
            57,
            () -> APositionFinder.findPositionForNew(builder, buildingType, this)
        );
//        return APositionFinder.findPositionForNew(builder, buildingType, this);
    }

    /**
     * In order to find a tile for building, one worker must be assigned as builder. We can assign any worker
     * and we're cool, bro.
     */
    protected void assignRandomBuilderForNow() {
        builder = Select.ourWorkers().first();
    }

    /**
     * Assigns optimal builder for this building. Worker closest to this place.
     *
     * @return AUnit for convenience it returns
     */
    protected AUnit assignOptimalBuilder() {
        builder = Select.ourWorkersFreeToBuildOrRepair().nearestTo(positionToBuild);

        return builder;
    }

    /**
     * Fully delete this construction, remove the building if needed by cancelling it.
     */
    public void cancel() {
        if (construction != null) {
            construction.cancelConstruction();
        }

        if (builder != null) {
            builder.cancelConstruction();
            builder = null;
        }

        ConstructionRequests.removeOrder(this);
    }

    // =========================================================

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + this.ID;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Construction other = (Construction) obj;
        return this.ID == other.ID;
    }

    @Override
    public int compareTo(Construction o) {
        return Integer.compare(ID, o.ID);
    }

    @Override
    public String toString() {
        return "ConstructionOrder{" + "ID=" + ID + ", buildingType=" + buildingType + ", construction=" + construction + ", builder=" + builder + ", positionToBuild=" + positionToBuild + ", productionOrder=" + productionOrder + ", status=" + status + '}';
    }

    // =========================================================

    public APosition positionToBuildCenter() {
        APosition positionToBuild = buildPosition();
        if (positionToBuild != null) {
            return positionToBuild.translateByPixels(
                buildingType().dimensionLeftPx(), buildingType().dimensionUpPx()
            );
        }
        else {
            return null;
        }
    }

    public AUnitType buildingType() {
        return buildingType;
    }

    public void setBuildingType(AUnitType buildingType) {
        this.buildingType = buildingType;
    }

    public AUnit builder() {
        return builder;
    }

    public void setBuilder(AUnit builder) {
        this.builder = builder;
    }

    public ConstructionOrderStatus status() {
        return status;
    }

    public void setStatus(ConstructionOrderStatus status) {
        this.status = status;

        if (status.equals(ConstructionOrderStatus.CONSTRUCTION_IN_PROGRESS)) {
            timeBecameInProgress = A.now();
        }
    }

    public APosition buildPosition() {
        return positionToBuild;
    }

    public void setPositionToBuild(APosition positionToBuild) {
        this.positionToBuild = positionToBuild;
    }

    public AUnit construction() {
        return construction;
    }

    public void setConstruction(AUnit construction) {
        this.construction = construction;
    }

    public int id() {
        return ID;
    }

    public ProductionOrder productionOrder() {
        return productionOrder;
    }

    public void setProductionOrder(ProductionOrder productionOrder) {
        this.productionOrder = productionOrder;
    }

    public HasPosition nearTo() {
        return near;
    }

    public void setNearTo(HasPosition near) {
        this.near = near;
    }

    public double maxDistance() {
        return maxDistance;
    }

    public void setMaxDistance(double maxDistance) {
        this.maxDistance = maxDistance;
    }

    public int timeOrdered() {
        return timeOrdered;
    }

    public int startedAgo() {
        return A.ago(timeBecameInProgress);
    }

    public boolean hasStarted() {
        return !status().equals(ConstructionOrderStatus.CONSTRUCTION_NOT_STARTED);
    }

    public boolean notStarted() {
        return ConstructionOrderStatus.CONSTRUCTION_NOT_STARTED.equals(status);
    }

    public static void clearCache() {
        cache.clear();
    }
}
