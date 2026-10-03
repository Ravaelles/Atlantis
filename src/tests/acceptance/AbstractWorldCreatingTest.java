package tests.acceptance;

import atlantis.config.env.Env;
import atlantis.game.A;
import atlantis.game.GameSpeed;
import atlantis.keyboard.AKeyboard;
import atlantis.units.AUnit;
import atlantis.units.select.BaseSelect;
import atlantis.util.Options;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import starengine.StarEngine;
import starengine.StarEngineLauncher;
import starengine.events.OnStarEngineFrameEnd;
import tests.fakes.FakeUnit;
import tests.unit.AbstractTestWithUnits;
import tests.unit.UnitTest;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

public abstract class AbstractWorldCreatingTest extends AbstractTestWithUnits {
    public static MockedStatic<BaseSelect> baseSelect = null;

    protected FakeUnit[] our;
    protected FakeUnit ourFirst;
    protected FakeUnit[] enemies;
    protected FakeUnit[] neutral;
    protected StarEngine engine = null;
    protected boolean shouldQuitNow = false;

    // =========================================================

    /**
     * The one place a world is actually built and stepped. Tests never call this:
     * {@link AbstractTestWithWorld#world(int, Runnable)} and
     * {@link AbstractTestWithWorld#world(int, FakeUnit[], FakeUnit[], Runnable)}
     * are the two entry points, and both come here.
     *
     * <p>{@code generateOur} / {@code generateEnemies} are null when the caller
     * wants this class's own {@link #generateOur()} / {@link #generateEnemies()}.</p>
     */
    protected void buildWorld(
        int proceedUntilFrameReached,
        Runnable onFrame,
        Callable generateOur,
        Callable generateEnemies,
        Options options
    ) {
        this.options = options;

        // === Create fake units ==========================================

        try {
            if (generateOur != null) {
                our = (FakeUnit[]) generateOur.call();
            }
            else {
                UnitTest.ourUnits = our = generateOur();
            }
            ourFirst = our != null && our.length > 0 ? our[0] : null;

            enemies = generateEnemies != null
                ? (FakeUnit[]) generateEnemies.call() : generateEnemies();

            neutral = neutralInWorld != null ? neutralInWorld : generateNeutral();
        } catch (Exception e) {
            System.err.println("AbstractWorldCreatingTest exception");
            e.printStackTrace();
        }

        assert our != null && our[0] != null : "You have to define your units";

        // === Mock static classes ========================================

        if (AbstractTestWithWorld.baseSelect != null) {
            AbstractTestWithWorld.baseSelect.close();
            AbstractTestWithWorld.baseSelect = null;
        }
        if (AbstractTestWithWorld.baseSelect == null) {
            AbstractTestWithWorld.baseSelect = Mockito.mockStatic(BaseSelect.class);
        }

        boolean isUsingEngine = isUsingEngine();

        // Engine semantics: the game drops a unit from its player's unit list as
        // soon as it dies, so no selection Atlantis builds ever contains a corpse.
        // Several Select builders rely on that instead of re-checking isAlive()
        // (`enemyCombatUnits`, `enemies(type)`, `enemyRealUnits`), so a stub world
        // that keeps its dead units in the list produces states the game cannot:
        // a worker spending frames attacking a zergling with 0 hit points, workers
        // fleeing from bodies, corpses counted as the attack that pins them home
        // (tests/e2e scenarios, _AI/BUGS.md B-19).
        //
        // So the mocks answer with the living units of the arrays the test handed
        // us, which is also what lets a scenario kill a unit and have the world
        // move on without the test having to unregister anything.
        baseSelect.when(BaseSelect::ourUnitsWithUnfinishedList).thenAnswer(invocation -> living(our));
        baseSelect.when(BaseSelect::enemyUnits).thenAnswer(invocation -> living(enemies));
        baseSelect.when(BaseSelect::neutralUnits).thenAnswer(invocation -> living(neutral));

        baseSelect.when(BaseSelect::allUnits).thenAnswer(invocation -> {
            List<AUnit> alive = new ArrayList<>(living(our));
            alive.addAll(living(enemies));
            alive.addAll(living(neutral));
            return alive;
        });

        setUpTestLogic();

        int framesNow = 1;
        while (framesNow <= proceedUntilFrameReached && !shouldQuitNow) {
            onFrameStart(onFrame, framesNow, isUsingEngine);
            framesNow = onFrameEnd(onFrame, framesNow, isUsingEngine);
        }

//        if (isUsingEngine && engine.game().isGameEnd()) engine.closeIfNeeded();
        if (isUsingEngine) A.sleep(1 * 3000);

        closeStaticMocks();
    }

    /**
     * A static mock left registered leaks into whatever test runs next in the
     * same thread (e.g. TestWithUnits fails with "static mocking is already
     * registered"). tearDown only resets stubs, it does not unregister, so
     * the world releases its mocks explicitly here.
     */
    /**
     * The living units of {@code units}, the way the engine would list them.
     */
    private static List<AUnit> living(FakeUnit[] units) {
        List<AUnit> alive = new ArrayList<>();
        if (units == null) {
            return alive;
        }

        for (FakeUnit unit : units) {
            if (unit != null && unit.isAlive()) {
                alive.add(unit);
            }
        }

        return alive;
    }

    private void closeStaticMocks() {
        if (AbstractTestWithWorld.baseSelect != null) {
            AbstractTestWithWorld.baseSelect.close();
            AbstractTestWithWorld.baseSelect = null;
        }
    }

    // =========================================================

    private void onFrameStart(Runnable onFrame, int framesNow, boolean usingEngine) {
        A.s = framesNow / 30;
        A.now = framesNow;
    }

    private int onFrameEnd(Runnable onFrame, int framesNow, boolean usingEngine) {
        useFakeTime(framesNow);

        onFrame.run();
        if (framesNow == 1 && usingEngine) launchEngine();

        // Use StarEngine for onFrameEnd logic
        if (usingEngine) {
            OnStarEngineFrameEnd.onFrameEnd(this);
            GameSpeed.keepGamePaused();
        }

        // Simple implementation of onFrameEnd for tests, just move units
        else {
            FakeOnFrameEnd.onFrameEnd(this);
        }

        framesNow++;
        return framesNow;
    }

    // =========================================================

    protected abstract FakeUnit[] generateOur();

    protected abstract FakeUnit[] generateEnemies();

    protected FakeUnit[] generateNeutral() {
        return new FakeUnit[]{};
    }

    // === StarEngine ===========================================

    protected void useStarEngine() {
        useStarEngine(createEngine());
    }

    protected void useStarEngine(StarEngine engine) {
        this.engine = engine;
        Env.markUsingStarEngine(true);
    }

    public StarEngine createEngine() {
        StarEngine engine = new StarEngine(this);
        return engine;
    }

    private void launchEngine() {
        StarEngineLauncher.launchStarEngine(this);
        AKeyboard.listenForKeyEvents();
    }

    public boolean isUsingEngine() {
        return engine != null;
    }

    public StarEngine engine() {
        return engine;
    }

    // === END OF StarEngine ====================================

    public void setShouldQuitGameLoopNow(boolean shouldQuitNow) {
        this.shouldQuitNow = shouldQuitNow;
    }
}
