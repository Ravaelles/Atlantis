package atlantis.units.fogged;

import atlantis.map.position.APosition;
import atlantis.units.AUnit;
import tests.fakes.FakePlayer;
import tests.fakes.FakeUnit;

/**
 * Used only in tests.
 */
public class FakeFoggedUnit extends AbstractFoggedUnit {

//    protected FakeFoggedUnit() {
//        super(null);
//    }

    private FakeFoggedUnit(FakeUnit unit) {
        super(unit);
    }

    public static FakeFoggedUnit fromFake(FakeUnit unit) {
        FakeFoggedUnit fakeFoggedUnit = new FakeFoggedUnit(unit);
        fakeFoggedUnit._id = unit.id();
        fakeFoggedUnit._lastAUnit = unit;
        fakeFoggedUnit._lastType = unit.type();

        fakeFoggedUnit.lateInitByAbstractFoggedUnit();

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

    /**
     * Must answer exactly what {@link FoggedUnit#hp()} answers.
     *
     * <p>{@link AbstractFoggedUnit#hp()} returns -69 to say "hit points behind
     * the fog are unknown, do not treat me as a living target". The real game
     * class overrides that with {@code maxHp()} so that sums like
     * {@code EnemyArmyStrength} still count the unit. When this double did not,
     * every fogged unit subtracted 69 from the enemy army score, the score hit
     * its floor of 1 and {@code ArmyStrength.ourArmyRelativeStrength()}
     * answered the 999 clamp - a strength reading no game could produce, pinned
     * as if it were real by EnemyUnitsTest. A test double that disagrees with the
     * class it stands in for is a bug in the double.</p>
     */
    @Override
    public int hp() {
        int hp = super.hp();
        if (hp > 0) {
            return hp;
        }

        return super.maxHp();
    }

    @Override
    public int shields() {
        return 0;
    }

}
