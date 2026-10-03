package tests.fakes;

import atlantis.map.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.fogged.AbstractFoggedUnit;
import atlantis.units.fogged.FoggedUnit;

/**
 * The harness's "unit that went behind the fog": it has no engine object to read
 * a position from, so it answers from the fake it was built on.
 *
 * <p>This class used to live in {@code src/atlantis/units/fogged}, which made the
 * production jar ship a test double and forced
 * {@code AbstractFoggedUnit.from()} to branch on {@code instanceof FakeUnit}.
 * Both are gone: the wrapping is a port
 * ({@link AbstractFoggedUnit.FoggedUnitFactory}) and this file is a test again.</p>
 */
public class FakeFoggedUnit extends AbstractFoggedUnit {

    /**
     * Makes this class the answer to {@code AbstractFoggedUnit.from(...)} here.
     * A unit that does have an engine object still gets the game's own fogged
     * unit - the branching that used to sit in production now sits here, with the
     * fakes.
     */
    public static void installAsFactory() {
        AbstractFoggedUnit.useFactory(unit ->
            unit instanceof FakeUnit
                ? fromFake((FakeUnit) unit)
                : FoggedUnit.from(unit));
    }

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
            // type() rather than the protected field: this class no longer lives
            // in the same package as AbstractFoggedUnit, so it has no access to
            // a protected member through a base-typed reference. type() is what
            // callers read anyway.
            _lastType = ((AbstractFoggedUnit) unit).type();
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
