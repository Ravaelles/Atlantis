package atlantis.constructing;

import atlantis.constructing.position.AtlantisPositionFinder;
import atlantis.production.ProductionOrder;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.Select;
import atlantis.wrappers.APosition;

/**
 * Represents construction of a building, including ones not yet started.
 */
public class ConstructionOrder implements Comparable<ConstructionOrder> {

    private static int _firstFreeId = 1;
    private int ID = _firstFreeId++;
    private AUnitType buildingType;
    private AUnit construction;
    private AUnit builder;
    private APosition positionToBuild;
    private ProductionOrder productionOrder;
    private ConstructionOrderStatus status;

    // private int issueFrameTime;
    // =========================================================
    
    public ConstructionOrder(AUnitType buildingType) {
        this.buildingType = buildingType;

        status = ConstructionOrderStatus.CONSTRUCTION_NOT_STARTED;
        // issueFrameTime = AtlantisGame.getTimeFrames();
    }

    // =========================================================
    
    /**
     * If it's impossible to build in given position (e.g. occupied by units), find new position.
     */
    public APosition findNewBuildPosition() {
        return AtlantisPositionFinder.getPositionForNew(builder, buildingType, this);
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
     * @return  AUnit for convenience it returns
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
            builder.stop();
            builder = null;
        }
        
        AtlantisConstructingManager.removeOrder(this);
//        status = ConstructionOrderStatus.CONSTRUCTION_FINISHED;
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
        final ConstructionOrder other = (ConstructionOrder) obj;
        if (this.ID != other.ID) {
            return false;
        }
        return true;
    }
    
    @Override
    public int compareTo(ConstructionOrder o) {
        return Integer.compare(ID, o.ID);
    }

    @Override
    public String toString() {
        return "ConstructionOrder{" + "ID=" + ID + ", buildingType=" + buildingType + ", construction=" + construction + ", builder=" + builder + ", positionToBuild=" + positionToBuild + ", productionOrder=" + productionOrder + ", status=" + status + '}';
    }
    
    // =========================================================
    
    public AUnitType getBuildingType() {
        return buildingType;
    }

    public void setBuildingType(AUnitType buildingType) {
        this.buildingType = buildingType;
    }

    public AUnit getBuilder() {
        return builder;
    }

    public void setBuilder(AUnit builder) {
        this.builder = builder;
    }

    public ConstructionOrderStatus getStatus() {
        return status;
    }

    public void setStatus(ConstructionOrderStatus status) {
        this.status = status;
    }

    public APosition getPositionToBuild() {
        return positionToBuild;
    }

    public void setPositionToBuild(APosition positionToBuild) {
        this.positionToBuild = positionToBuild;
    }

    public AUnit getConstruction() {
        return construction;
    }

    public void setConstruction(AUnit construction) {
        this.construction = construction;
    }

    public int getID() {
        return ID;
    }

    public ProductionOrder getProductionOrder() {
        return productionOrder;
    }

    public void setProductionOrder(ProductionOrder productionOrder) {
        this.productionOrder = productionOrder;
    }
    
}
