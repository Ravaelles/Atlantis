package tests.acceptance;

import atlantis.config.env.Env;
import atlantis.game.A;
import atlantis.game.GameSpeed;
import atlantis.keyboard.AKeyboard;
import atlantis.units.select.BaseSelect;
import atlantis.util.Options;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import starengine.StarEngine;
import starengine.StarEngineLauncher;
import starengine.events.OnStarEngineFrameEnd;
import tests.fakes.FakeUnit;
import tests.unit.AbstractTestWithUnits;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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

    public void createWorld(
        int proceedUntilFrameReached,
        Runnable onFrame,
        Callable generateOur,
        Callable generateEnemies,
        Options options
    ) {
        this.options = options;

        // === Create fake units ==========================================

        try {
            our = generateOur != null ? (FakeUnit[]) generateOur.call() : generateOur();
            ourFirst = our != null && our.length > 0 ? our[0] : null;
            enemies = generateEnemies != null ? (FakeUnit[]) generateEnemies.call() : generateEnemies();
            neutral = generateNeutral();
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
        baseSelect.when(BaseSelect::ourUnitsWithUnfinishedList).thenReturn(Arrays.asList(our));
        baseSelect.when(BaseSelect::enemyUnits).thenReturn(Arrays.asList(enemies));
        baseSelect.when(BaseSelect::neutralUnits).thenReturn(Arrays.asList(neutral));

        ArrayList<FakeUnit> allUnits = new ArrayList<>();
        Collections.addAll(allUnits, our);
        Collections.addAll(allUnits, enemies);
        Collections.addAll(allUnits, neutral);
        baseSelect.when(BaseSelect::allUnits).thenReturn(allUnits);

        setUpTestLogic();

        int framesNow = 1;
        while (framesNow <= proceedUntilFrameReached && !shouldQuitNow) {
            onFrameStart(onFrame, framesNow, isUsingEngine);
            framesNow = onFrameEnd(onFrame, framesNow, isUsingEngine);
        }

//        if (isUsingEngine && engine.game().isGameEnd()) engine.closeIfNeeded();
        if (isUsingEngine) A.sleep(1 * 3000);
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
