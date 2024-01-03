package starengine.units;

import atlantis.units.AUnit;

import java.util.ArrayList;
import java.util.List;

public class AUnitToEngineUnits {
    public static List<Unit> convert(List<AUnit> aunits) {
        List<Unit> engineUnits = new ArrayList<>();

        for (AUnit unit : aunits) {
            engineUnits.add(aUnitToEngineUnit(unit));
        }

        return engineUnits;
    }

    private static Unit aUnitToEngineUnit(AUnit unit) {
        Owner owner = unit.isOur() ? Owner.PLAYER : (unit.isEnemy() ? Owner.ENEMY : Owner.NEUTRAL);

        return new Unit(unit.x(), unit.y(), 0, 0, owner)
            .setAUnit(unit);
    }
}
