package tests.acceptance;

import atlantis.Atlantis;
import atlantis.config.env.Env;
import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.game.GameSpeed;
import atlantis.information.enemy.EnemyUnits;
import atlantis.keyboard.AKeyboard;
import atlantis.units.select.BaseSelect;
import atlantis.util.Options;
import org.junit.After;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import starengine.StarEngine;
import starengine.StarEngineLauncher;
import starengine.events.OnStarEngineFrameEnd;
import tests.unit.AbstractTestWithUnits;
import tests.fakes.FakeUnit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.Callable;

public abstract class AbstractTestFakingGame extends AbstractTestWithUnits {
    public static MockedStatic<BaseSelect> baseSelect;

    protected FakeUnit[] our;
    protected FakeUnit ourFirst;
    protected FakeUnit[] enemies;
    protected FakeUnit[] neutral;
    protected StarEngine engine = null;

    // =========================================================

    @After
    public void after() {
        super.after();

        cleanUp();
    }

    // =========================================================

    protected void usingFakeOursEnemiesAndNeutral(
        FakeUnit[] ours, FakeUnit[] enemies, FakeUnit[] neutral, Runnable runnable
    ) {
        try (MockedStatic<BaseSelect> baseSelect = AbstractTestFakingGame.baseSelect = Mockito.mockStatic(BaseSelect.class)) {
            baseSelect.when(BaseSelect::ourUnitsWithUnfinishedList).thenReturn(Arrays.asList(ours));
            baseSelect.when(BaseSelect::enemyUnits).thenReturn(Arrays.asList(enemies));
            baseSelect.when(BaseSelect::neutralUnits).thenReturn(Arrays.asList(neutral));

            mockEverything();

            runnable.run();
        }
    }

    // =========================================================

    protected void createWorld(int proceedUntilFrameReached, Runnable onFrame) {
        createWorld(proceedUntilFrameReached, onFrame, null, null, null);
    }

    protected void createWorld(
        int proceedUntilFrameReached,
        Runnable onFrame,
        Callable generateOur,
        Callable generateEnemies
    ) {
        createWorld(proceedUntilFrameReached, onFrame, generateOur, generateEnemies, null);
    }

    protected void createWorld(
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
            System.err.println("CreateWorld exception");
            e.printStackTrace();
        }

        assert our != null && our[0] != null : "You have to define your units";

        // === Mock static classes ========================================

        boolean usingEngine = isUsingEngine();
        try (MockedStatic<BaseSelect> baseSelect = AbstractTestFakingGame.baseSelect = Mockito.mockStatic(BaseSelect.class)) {
//            if (!usingEngine) {
            baseSelect.when(BaseSelect::ourUnitsWithUnfinishedList).thenReturn(Arrays.asList(our));
            baseSelect.when(BaseSelect::enemyUnits).thenReturn(Arrays.asList(enemies));
            baseSelect.when(BaseSelect::neutralUnits).thenReturn(Arrays.asList(neutral));
//            } else {
//                baseSelect.when(BaseSelect::ourUnits).thenReturn(Arrays.asList(our));
//                baseSelect.when(BaseSelect::enemyUnits).thenReturn(Arrays.asList(enemies));
//                baseSelect.when(BaseSelect::neutralUnits).thenReturn(Arrays.asList(neutral));
//            }

            ArrayList<FakeUnit> allUnits = new ArrayList<>();
            Collections.addAll(allUnits, our);
            Collections.addAll(allUnits, enemies);
            Collections.addAll(allUnits, neutral);
            baseSelect.when(BaseSelect::allUnits).thenReturn(allUnits);

            mockAtlantisConfig();
            mockGameObject();
            mockAGameObject();
            mockOtherStaticClasses();

            beforeTestLogic();

            int framesNow = 1;
            while (framesNow <= proceedUntilFrameReached) {
                onFrameStart(onFrame, framesNow, usingEngine);
                framesNow = onFrameEnd(onFrame, framesNow, usingEngine);
            }
        }

        if (usingEngine) A.sleep(1000 * 30);
    }

    private void onFrameStart(Runnable onFrame, int framesNow, boolean usingEngine) {
        A.s = framesNow / 30;
        A.fr = framesNow;
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

    protected void useFakeTime(int framesNow) {
        super.useFakeTime(framesNow);

        aGame.when(AGame::now).thenReturn(framesNow);
    }

    // =========================================================

    private void launchEngine() {
        StarEngineLauncher.launchStarEngine();
        AKeyboard.listenForKeyEvents();
    }

    // =========================================================

    protected abstract FakeUnit[] generateOur();

    protected abstract FakeUnit[] generateEnemies();

    protected FakeUnit[] generateNeutral() {
        return new FakeUnit[]{};
    }

    // =========================================================

    protected FakeUnit nearestEnemy(FakeUnit unit) {
        return (FakeUnit) EnemyUnits.discovered().nearestTo(unit);
    }

    protected double distToNearestEnemy(FakeUnit unit) {
        return unit.distTo(nearestEnemy(unit));
    }

    protected void useEngine(StarEngine engine) {
        this.engine = engine;
        Env.markUsingStarEngine(true);
    }

    public boolean isUsingEngine() {
        return engine != null;
    }

    public StarEngine engine() {
        return engine;
    }
}
