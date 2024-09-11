package atlantis.units.fogged;

import atlantis.map.position.APosition;
import atlantis.units.AUnit;
import tests.fakes.FakePlayer;
import tests.fakes.FakeUnit;

/**
 * Used only in tests.
 */
public class FakeFoggedUnit extends AbstractFoggedUnit {

    protected FakeFoggedUnit() {
        super(null);
    }

    public static FakeFoggedUnit fromFake(FakeUnit unit) {
        FakeFoggedUnit fakeFoggedUnit = new FakeFoggedUnit();
        fakeFoggedUnit._id = unit.id();
        fakeFoggedUnit._lastAUnit = unit;
        fakeFoggedUnit.onAbstractFoggedUnitCreated(unit);

        all.put(unit.id(), fakeFoggedUnit);

        return fakeFoggedUnit;
    }

    // =========================================================

    public void updateType(AUnit unit) {
        if (unit instanceof FakeUnit) {
            _lastType = ((FakeUnit) unit).rawType;
        }
        else if (unit instanceof AbstractFoggedUnit) {
            _lastType = ((AbstractFoggedUnit) unit)._lastType;
        }
    }

    // =========================================================

    @Override
    public APosition position() {
//        if (aUnit.x() > 0) {
//            return aUnit.position();
//        }

        return _lastPosition;
    }

    @Override
    public int x() {
        return _lastAUnit.position().x;
    }

    @Override
    public int y() {
        return _lastAUnit.position().y;
    }

    @Override
    public FakePlayer player() {
        if (isEnemy()) {
            return FakePlayer.ENEMY;
        }
        return FakePlayer.NEUTRAL;
    }

    @Override
    public int shields() {
        return 0;
    }

}
