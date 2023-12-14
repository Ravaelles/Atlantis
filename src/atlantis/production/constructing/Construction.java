package atlantis.production.constructing;

import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.production.constructing.builders.GetOptimalBuilder;
import atlantis.production.constructing.position.APositionFinder;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.workers.FreeWorkers;
import atlantis.util.cache.Cache;
import atlantis.util.log.ErrorLog;

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
    private AUnit build;
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

//        if (id() >= 1000) {
//            System.out.println("@ " + A.now() + " - new constr: " + id() + " / " + buildingType);
//        }
    }

    // =========================================================

    /**
     * If it's impossible to build in given position (e.g. occupied by units), find new position.
     */
    public APosition findPositionForNewBuilding() {
        return (APosition) cache.get(
            "findPositionForNewBuilding:" + genericCacheKey(),
            57,
            () -> APositionFinder.findPositionForNew(builder, buildingType, this)
        );
    }

    private String genericCacheKey() {
        String cacheKey = "";

        if (buildingType != null) cacheKey += buildingType.id() + ",";
        if (builder != null) cacheKey += builder.id() + ",";
        if (build != null) cacheKey += build.position();

        return cacheKey;
    }

    /**
     * In order to find a tile for building, one worker must be assigned as builder. We can assign any worker
     * and we're cool, bro.
     */
    protected void assignRandomBuilderForNow() {
        builder = FreeWorkers.get().first();
    }

    /**
     * Assigns optimal builder for this building. Worker closest to this place.
     *
     * @return AUnit for convenience it returns
     */
    public AUnit assignOptimalBuilder() {
        AUnit optimalBuilder = GetOptimalBuilder.forPosition(this, productionOrder);

        if (optimalBuilder != null) builder = optimalBuilder;
        else ErrorLog.printMaxOncePerMinute("No optimal builder for " + buildingType);

        return builder;
    }

    /**
     * Fully delete this construction, remove the building if needed by cancelling it.
     */
    public void cancel() {
        if (build != null) {
            build.cancelConstruction();
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
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        final Construction other = (Construction) obj;

        if (buildingType == null || positionToBuild == null) return false;

        return this.ID == other.ID
            || (
            this.buildingType.equals(other.buildingType) && positionToBuild.equals(other.positionToBuild)
        );
    }

    @Override
    public int compareTo(Construction o) {
        return Integer.compare(ID, o.ID);
    }

    @Override
    public String toString() {
        return "Construction{" + "#" + ID + ", " + buildingType + ", build=" + build + ", builder=" + builder + ", positionToBuild=" + positionToBuild + ", status=" + status + '}';
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

    public AUnit buildingUnit() {
        return build;
    }

    public void setBuild(AUnit build) {
        this.build = build;
    }

    public int id() {
        return ID;
    }

    public ProductionOrder productionOrder() {
        return productionOrder;
    }

    public void setProductionOrder(ProductionOrder productionOrder) {
        this.productionOrder = productionOrder;
        if (productionOrder != null) this.productionOrder.setConstruction(this);
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
