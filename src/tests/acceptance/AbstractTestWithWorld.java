package tests.acceptance;

import atlantis.information.enemy.EnemyUnits;
import atlantis.units.select.BaseSelect;
import atlantis.units.select.Select;
import org.junit.jupiter.api.AfterEach;
import tests.fakes.FakeUnit;
import tests.unit.MockEverything;
import tests.unit.UnitTest;

public abstract class AbstractTestWithWorld extends AbstractWorldCreatingTest {
    @AfterEach
    public void tearDown() {
        super.tearDown();

        cleanUp();
    }

    // =========================================================

    /**
     * Build the sample world - the 22 units of {@code mockOurUnitsArray()} and
     * the enemies of {@code mockEnemyUnitsArray()} - and run {@code eachFrame}
     * on every frame up to {@code frames}.
     *
     * <p>This and {@link #world(int, FakeUnit[], FakeUnit[], Runnable)} are the
     * only two ways to declare a world. Everything else - supply, race, neutral
     * units - is a field or an override on the test class ({@code options},
     * {@code neutralInWorld}, {@code initRace()}, {@code initEnemyRace()}).
     * The old six-overload {@code createWorld(...)} and the six
     * {@code usingFake*()} wrappers are gone: they were one operation with the
     * arguments in six different orders, which is why nobody remembered which
     * one to use. The engine behind both is {@code buildWorld(...)}.</p>
     */
    protected void world(int frames, Runnable eachFrame) {
        buildWorld(frames, eachFrame, () -> mockOurUnitsArray(), () -> mockEnemyUnitsArray(), options);
    }

    /**
     * Build an explicit world: exactly these are our units, exactly those are
     * the enemy's; {@code eachFrame} runs on every frame up to {@code frames}.
     *
     * <p>A test that cares about particular units says so here instead of
     * hoping the sample world happens to contain them. {@link #units(FakeUnit...)}
     * turns a single unit into the array this wants.</p>
     */
    protected void world(int frames, FakeUnit[] ours, FakeUnit[] enemies, Runnable eachFrame) {
        buildWorld(frames, eachFrame, () -> ours, () -> enemies, options);
    }

    /**
     * {@code units(one)} reads better than {@code new FakeUnit[]{one}} at a call
     * site - and this is a value helper, not a third way to build a world.
     */
    protected static FakeUnit[] units(FakeUnit... units) {
        return units;
    }

    // =========================================================

    protected FakeUnit nearestEnemy(FakeUnit unit) {
        return (FakeUnit) EnemyUnits.discovered().nearestTo(unit);
    }

    protected double distToNearestEnemy(FakeUnit unit) {
        return unit.distTo(nearestEnemy(unit));
    }
}
