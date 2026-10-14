package atlantis.production.v2;

import atlantis.units.AUnitType;

/** Immutable Zerg identities for {@link ProductionRacePolicy}. */
public final class ZergProductionRacePolicy implements ProductionRacePolicy {

    public static final ZergProductionRacePolicy INSTANCE = new ZergProductionRacePolicy();

    private ZergProductionRacePolicy() {
    }

    @Override
    public AUnitType workerType() {
        return AUnitType.Zerg_Drone;
    }

    @Override
    public AUnitType armyUnitType() {
        return AUnitType.Zerg_Zergling;
    }

    @Override
    public AUnitType baseType() {
        return AUnitType.Zerg_Hatchery;
    }

    @Override
    public AUnitType supplyProviderType() {
        return AUnitType.Zerg_Overlord;
    }

    @Override
    public AUnitType gasBuildingType() {
        return AUnitType.Zerg_Extractor;
    }
}
