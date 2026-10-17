package atlantis.production.v2;

import atlantis.units.AUnitType;

/** Immutable Protoss identities for {@link ProductionRacePolicy}. */
public final class ProtossProductionRacePolicy implements ProductionRacePolicy {

    public static final ProtossProductionRacePolicy INSTANCE = new ProtossProductionRacePolicy();

    private ProtossProductionRacePolicy() {
    }

    @Override
    public AUnitType workerType() {
        return AUnitType.Protoss_Probe;
    }

    @Override
    public AUnitType armyUnitType() {
        return AUnitType.Protoss_Zealot;
    }

    @Override
    public AUnitType armyUnitTypeAdvanced() {
        return AUnitType.Protoss_Dragoon;
    }

    @Override
    public AUnitType baseType() {
        return AUnitType.Protoss_Nexus;
    }

    @Override
    public AUnitType supplyProviderType() {
        return AUnitType.Protoss_Pylon;
    }

    @Override
    public AUnitType gasBuildingType() {
        return AUnitType.Protoss_Assimilator;
    }
}
