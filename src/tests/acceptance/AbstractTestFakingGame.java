package tests.acceptance;

import atlantis.game.AGame;
import atlantis.information.enemy.EnemyUnits;
import atlantis.units.select.BaseSelect;
import org.junit.After;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import tests.unit.AbstractTestWithUnits;
import tests.unit.FakeUnit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.Callable;

public abstract class AbstractTestFakingGame extends AbstractTestWithUnits {

    protected FakeUnit[] our;
    protected FakeUnit ourFirst;
    protected FakeUnit[] enemies;
    protected FakeUnit[] neutral;

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
        try (MockedStatic<BaseSelect> baseSelect = Mockito.mockStatic(BaseSelect.class)) {
            baseSelect.when(BaseSelect::ourUnits).thenReturn(Arrays.asList(ours));
            baseSelect.when(BaseSelect::enemyUnits).thenReturn(Arrays.asList(enemies));
            baseSelect.when(BaseSelect::neutralUnits).thenReturn(Arrays.asList(neutral));

            mockEverything();

            runnable.run();
        }
    }

    // =========================================================

    protected void createWorld(int proceedUntilFrameReached, Runnable onFrame) {
        createWorld(proceedUntilFrameReached, onFrame, null, null);
    }

    protected void createWorld(
            int proceedUntilFrameReached, Runnable onFrame, Callable generateOur, Callable generateEnemies
    ) {
        // === Create fake units ==========================================

        try {
            our = generateOur != null ? (FakeUnit[]) generateOur.call() : generateOur();
            ourFirst = our[0];
            enemies = generateEnemies != null ? (FakeUnit[]) generateEnemies.call() : generateEnemies();
            neutral = generateNeutral();
        } catch (Exception e) {
            System.err.println("CreateWorld exception");
            e.printStackTrace();
        }

        // === Mock static classes ========================================

        try (MockedStatic<BaseSelect> baseSelect = Mockito.mockStatic(BaseSelect.class)) {
            baseSelect.when(BaseSelect::ourUnits).thenReturn(Arrays.asList(our));
            baseSelect.when(BaseSelect::enemyUnits).thenReturn(Arrays.asList(enemies));
            baseSelect.when(BaseSelect::neutralUnits).thenReturn(Arrays.asList(neutral));

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
                useFakeTime(framesNow);

                onFrame.run();

                framesNow++;

                FakeOnFrameEnd.onFrameEnd(this);
            }
        }
    }

    protected void useFakeTime(int framesNow) {
        super.useFakeTime(framesNow);

        aGame.when(AGame::now).thenReturn(framesNow);
    }

    // =========================================================

    protected abstract FakeUnit[] generateOur();

    protected abstract FakeUnit[] generateEnemies();

    protected FakeUnit[] generateNeutral() {
        return new FakeUnit[] { };
    }

    // =========================================================

    protected FakeUnit nearestEnemy(FakeUnit unit) {
        return (FakeUnit) EnemyUnits.discovered().nearestTo(unit);
    }

    protected double distToNearestEnemy(FakeUnit unit) {
        return unit.distTo(nearestEnemy(unit));
    }

}
