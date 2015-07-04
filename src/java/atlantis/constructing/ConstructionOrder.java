package atlantis.constructing;

import jnibwapi.Position;
import jnibwapi.Unit;
import jnibwapi.types.UnitType;
import atlantis.AtlantisGame;
import atlantis.wrappers.SelectUnits;

public class ConstructionOrder {

	private UnitType buildingType;
	private Unit construction;
	private Unit builder;
	private Position positionToBuild;
	private ConstructionOrderStatus status;
	private int issueFrameTime;

	// =========================================================

	public ConstructionOrder(UnitType buildingType) {
		this.buildingType = buildingType;

		status = ConstructionOrderStatus.CONSTRUCTION_NOT_STARTED;
		issueFrameTime = AtlantisGame.getTimeFrames();
	}

	// =========================================================

	/**
	 * In order to find a tile for building, one worker must be assigned as builder. We can assign any worker and we're
	 * cool, bro.
	 */
	protected void assignRandomBuilderForNow() {
		builder = SelectUnits.ourWorkers().first();
	}

	/**
	 * Assigns optimal builder for this building. Worker closest to this place.
	 * 
	 * @return Unit for convenience it returns
	 */
	protected Unit assignOptimalBuilder() {
		builder = SelectUnits.ourWorkersFreeToBuildOrRepair().nearestTo(positionToBuild);
		return builder;
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

}
