package atlantis.production.v2;

import atlantis.units.AUnitType;

/**
 * Immutable Terran identities for {@link ProductionRacePolicy}.
 */
public final class TerranProductionRacePolicy implements ProductionRacePolicy {

    public static final TerranProductionRacePolicy INSTANCE = new TerranProductionRacePolicy();

    private TerranProductionRacePolicy() {
    }

    @Override
    public AUnitType workerType() {
        return AUnitType.Terran_SCV;
    }

    @Override
    public AUnitType armyUnitType() {
        return AUnitType.Terran_Marine;
    }

    @Override
    public AUnitType baseType() {
        return AUnitType.Terran_Command_Center;
    }

    @Override
    public AUnitType supplyProviderType() {
        return AUnitType.Terran_Supply_Depot;
    }

    @Override
    public AUnitType gasBuildingType() {
        return AUnitType.Terran_Refinery;
    }
}
