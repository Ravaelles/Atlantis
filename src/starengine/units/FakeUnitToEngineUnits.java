package starengine.units;

import atlantis.units.AUnit;
import tests.unit.FakeUnit;
import java.util.ArrayList;
import java.util.List;

public class FakeUnitToEngineUnits {
    public static List<Unit> convert(List<AUnit> FakeUnits) {
        List<Unit> engineUnits = new ArrayList<>();

        for (AUnit unit : FakeUnits) {
            FakeUnit fakeUnit = (FakeUnit) unit;
            engineUnits.add(fakeUnitToEngineUnit(fakeUnit));
        }

        return engineUnits;
    }

    private static Unit fakeUnitToEngineUnit(FakeUnit unit) {
        Owner owner = unit.isOur() ? Owner.PLAYER : (unit.isEnemy() ? Owner.ENEMY : Owner.NEUTRAL);

        return new Unit(unit.x(), unit.y(), 0, 0, owner)
            .setFakeUnit(unit);
    }
}
