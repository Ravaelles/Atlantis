package atlantis.placement.strategy;

import atlantis.units.AUnitType;

/**
 * Which strategy owns a building type (owner 2026-11-09): Pylons own
 * range, Gateways sit on existing power, everything else follows the
 * street grid. One place, so the planner, the funnel and the refresh
 * can never disagree on who decides.
 */
public final class BuildingPlacementStrategies {

    private static final BuildingPlacementStrategy PYLON = new PylonPlacementStrategy();
    private static final BuildingPlacementStrategy GATEWAY = new GatewayPlacementStrategy();
    private static final BuildingPlacementStrategy DEFAULT = new DefaultPlacementStrategy();

    private BuildingPlacementStrategies() {
    }

    public static BuildingPlacementStrategy forType(AUnitType type) {
        if (StreetCells.isPylonGroup(type)) return PYLON;
        if (StreetCells.isGatewayGroup(type)) return GATEWAY;

        return DEFAULT;
    }
}
