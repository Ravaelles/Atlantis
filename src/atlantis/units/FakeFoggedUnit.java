package atlantis.units;

import atlantis.map.position.APosition;
import tests.fakes.FakePlayer;
import tests.unit.FakeUnit;

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
        fakeFoggedUnit.aUnit = unit;
        fakeFoggedUnit.update(unit);

        all.put(unit.id(), fakeFoggedUnit);

        return fakeFoggedUnit;
    }

    // =========================================================

    protected void updateType(AUnit unit) {
        _lastType = ((FakeUnit) unit).rawType;
    }

    // =========================================================

    @Override
    public APosition position() {
        return aUnit.position();
    }

    @Override
    public int x() {
        return aUnit.position().x;
    }

    @Override
    public int y() {
        return aUnit.position().y;
    }

    @Override
    public FakePlayer player() {
        if (isEnemy()) {
            return FakePlayer.ENEMY;
        }
        return FakePlayer.NEUTRAL;
    }

    @Override
    public boolean isCloaked() {
        return aUnit.isCloaked();
    }

    @Override
    public int shields() {
        return 0;
    }

}
