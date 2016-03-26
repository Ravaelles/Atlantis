package atlantis.constructing;

import atlantis.constructing.position.AtlantisPositionFinder;
import atlantis.production.ProductionOrder;
import atlantis.wrappers.Select;
import bwapi.Position;
import bwapi.Unit;
import bwapi.UnitType;

/**
 * Represents construction of a building, including ones not yet started.
 */
public class ConstructionOrder implements Comparable<ConstructionOrder> {

    private static int _firstFreeId = 1;
    private int ID = _firstFreeId++;
    private UnitType buildingType;
    private Unit construction;
    private Unit builder;
    private Position positionToBuild;
    private ProductionOrder productionOrder;
    private ConstructionOrderStatus status;

    // private int issueFrameTime;
    // =========================================================
    
    public ConstructionOrder(UnitType buildingType) {
        this.buildingType = buildingType;

        status = ConstructionOrderStatus.CONSTRUCTION_NOT_STARTED;
        // issueFrameTime = AtlantisGame.getTimeFrames();
    }

    // =========================================================
    
    /**
     * If it's impossible to build in given position (e.g. occupied by units), find new position.
     */
    public Position findNewBuildPosition() {
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
     * @return Unit for convenience it returns
     */
    protected Unit assignOptimalBuilder() {
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
            builder.stop(false);
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
    public UnitType getBuildingType() {
        return buildingType;
    }

    public void setBuildingType(UnitType buildingType) {
        this.buildingType = buildingType;
    }

    public Unit getBuilder() {
        return builder;
    }

    public void setBuilder(Unit builder) {
        this.builder = builder;
    }

    public ConstructionOrderStatus getStatus() {
        return status;
    }

    public void setStatus(ConstructionOrderStatus status) {
        this.status = status;
    }

    public Position getPositionToBuild() {
        return positionToBuild;
    }

    public void setPositionToBuild(Position positionToBuild) {
        this.positionToBuild = positionToBuild;
    }

    public Unit getConstruction() {
        return construction;
    }

    public void setConstruction(Unit construction) {
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
