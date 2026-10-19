package atlantis.placement.core;

import atlantis.units.AUnitType;

/**
 * A race support building the planner cannot place itself (Protoss Pylon
 * for an unpowered tile). A declaration, not an act: the strategy names
 * what and where, the composition root that asked for the placement
 * decides whether and how to order it. Returning data instead of writing
 * the production queue keeps geometry from performing production.
 */
public final class SupportRequest {

    private final AUnitType unitType;
    private final int tileX;
    private final int tileY;

    public SupportRequest(AUnitType unitType, int tileX, int tileY) {
        this.unitType = unitType;
        this.tileX = tileX;
        this.tileY = tileY;
    }

    public AUnitType unitType() {
        return unitType;
    }

    public int tileX() {
        return tileX;
    }

    public int tileY() {
        return tileY;
    }

    @Override
    public String toString() {
        return "SupportRequest{" + unitType + " [" + tileX + "," + tileY + "]}";
    }
}
