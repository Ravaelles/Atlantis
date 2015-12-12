package atlantis.production.strategies;

import atlantis.AtlantisConfig;
import atlantis.wrappers.SelectUnits;
import java.util.ArrayList;
import jnibwapi.Unit;
import jnibwapi.types.UnitType;
import jnibwapi.types.UnitType.UnitTypes;

public class ZergProductionStrategy extends AtlantisProductionStrategy {

    @Override
    protected String getFilename() {
        return "DefaultZerg.csv";
    }

    @Override
    public void produceWorker() {
        _produceUnit(AtlantisConfig.WORKER);
    }

    @Override
    public void produceInfantry(UnitType infantryType) {
        _produceUnit(UnitTypes.Zerg_Zergling);
    }

    @Override
    public ArrayList<UnitType> produceWhenNoProductionOrders() {
        ArrayList<UnitType> units = new ArrayList<>();
        units.add(UnitTypes.Zerg_Zergling);
        return units;
    }

    // --------------------------------------------------------------------
    /**
     * Produce zerg unit from free larva. Will do nothing if no free larva is available.
     */
    public void produceZergUnit(UnitType unitType) {
        _produceUnit(unitType);
    }

    // --------------------------------------------------------------------
    protected void _produceUnit(UnitType unitType) {
//        Unit freeLarva = getFreeLarva();
//        if (freeLarva != null) {
//            freeLarva.morph(unitType);
//        }
        for (Unit base : SelectUnits.ourBases().list()) {
            for (Unit unit : base.getLarva()) {
                base.train(unitType);
                return;
            }
        }
    }

    // --------------------------------------------------------------------
    // Auxiliary
    private Unit getFreeLarva() {
        return SelectUnits.our().ofType(UnitTypes.Zerg_Larva).first();
    }

}
