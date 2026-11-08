package atlantis.placement.core;

import atlantis.units.AUnitType;

/**
 * A race support building the planner cannot place itself (Protoss Pylon
 * for an unpowered tile). A declaration, not an act: the strategy names
 * what and where, the composition root that asked for the placement
 * decides whether and how to order it. Returning data instead of writing
 * the production queue keeps geometry from performing production.
 *
 * <p>Since owner order 2026-11-08 only a Cannon demands support - a
 * Gateway sits on pre-planned power instead. The demander travels with
 * the request so twins name who asked (Cannon) instead of a hardcoded
 * building.</p>
 */
public final class SupportRequest {

    private final AUnitType unitType;
    private final String demanderTypeId;
    private final int tileX;
    private final int tileY;

    public SupportRequest(AUnitType unitType, int tileX, int tileY) {
        this(unitType, null, tileX, tileY);
    }

    public SupportRequest(AUnitType unitType, String demanderTypeId, int tileX, int tileY) {
        this.unitType = unitType;
        this.demanderTypeId = demanderTypeId;
        this.tileX = tileX;
        this.tileY = tileY;
    }

    public AUnitType unitType() {
        return unitType;
    }

    public String demanderTypeId() {
        return demanderTypeId;
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
