package tests.acceptance;

import atlantis.game.AGame;
import atlantis.information.enemy.EnemyUnits;
import atlantis.units.select.BaseSelect;
import org.junit.jupiter.api.AfterEach;
import org.mockito.Mockito;
import tests.fakes.FakeUnit;
import tests.unit.MockEverything;

import java.util.Arrays;
import java.util.concurrent.Callable;

public abstract class AbstractTestWithWorld extends AbstractWorldCreatingTest {
    @AfterEach
    public void tearDown() {
        super.tearDown();

        cleanUp();
    }

    // =========================================================

    protected void usingFakeOursEnemiesAndNeutral(
        FakeUnit[] ours, FakeUnit[] enemies, FakeUnit[] neutral, Runnable runnable
    ) {
        if (AbstractTestWithWorld.baseSelect != null) {
            AbstractTestWithWorld.baseSelect.close();
            AbstractTestWithWorld.baseSelect = null;
        }
        AbstractTestWithWorld.baseSelect = Mockito.mockStatic(BaseSelect.class);

        baseSelect.when(BaseSelect::ourUnitsWithUnfinishedList).thenReturn(Arrays.asList(ours));
        baseSelect.when(BaseSelect::enemyUnits).thenReturn(Arrays.asList(enemies));
        baseSelect.when(BaseSelect::neutralUnits).thenReturn(Arrays.asList(neutral));

        (new MockEverything(this)).mockEverything();

        runnable.run();
    }

    // =========================================================

    protected void createWorld(FakeUnit[] ours, FakeUnit[] enemies, Runnable onFrame) {
        createWorld(1, onFrame, () -> ours, () -> enemies, null);
    }

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

    protected void useFakeTime(int framesNow) {
        super.useFakeTime(framesNow);

        aGame.when(AGame::now).thenReturn(framesNow);
    }

    // =========================================================

    protected FakeUnit nearestEnemy(FakeUnit unit) {
        return (FakeUnit) EnemyUnits.discovered().nearestTo(unit);
    }

    protected double distToNearestEnemy(FakeUnit unit) {
        return unit.distTo(nearestEnemy(unit));
    }
}
