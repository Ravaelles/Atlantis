package atlantis.production.constructions.position.protoss;

import atlantis.game.A;
import atlantis.information.strategy.Strategy;
import atlantis.map.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;

public class PylonTooFarFromBaseEarly {
    public static boolean isTooFar(AUnit builder, AUnitType building, APosition position) {
        if (!building.isPylon()) return false;
        if (A.supplyTotal() >= 40) return false;
        if (Strategy.get().isExpansion()) return false;

        return Select.mainOrAnyUnit().groundDist(position) > 22;
    }
}
