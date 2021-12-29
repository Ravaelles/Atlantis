package atlantis.tests.unit;

import atlantis.Atlantis;
import atlantis.combat.eval.ACombatEvaluator;
import atlantis.combat.micro.avoid.AAvoidUnits;
import atlantis.debug.APainter;
import atlantis.enemy.EnemyInformation;
import atlantis.enemy.EnemyUnits;
import atlantis.information.AFoggedUnit;
import atlantis.tests.acceptance.AbstractTestFakingGame;
import atlantis.units.AUnitType;
import atlantis.units.select.BaseSelect;
import atlantis.units.select.Select;
import bwapi.Game;
import org.junit.After;
import org.junit.Before;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Arrays;

public class AbstractTestWithUnits extends UnitTestHelper {

//    protected int currentFrames = 1;
    protected Game game;

    // =========================================================

    @Before
    public void before() {
        if (!(this instanceof AbstractTestFakingGame)) {
            useFakeTime(0); // This needs to be 0 so every modulo division returns 0
        }
        APainter.disablePainting();
        Select.clearCache();
        BaseSelect.clearCache();
        ACombatEvaluator.clearCache();
        AFoggedUnit.clearCache();
        EnemyInformation.clearCache();
        EnemyUnits.clearCache();
        AAvoidUnits.clearCache();
    }

    @After
    public void after() {
        if (game != null) {
            game = null;
        }
        Atlantis.getInstance().setGame(null);
    }

    // =========================================================

    protected void useFakeTime(int framesNow) {
        game = Atlantis.game() == null ? newGameMock(framesNow) : Atlantis.game();

        when(game.getFrameCount()).thenReturn(framesNow);

        if (Atlantis.game() == null) {
            Atlantis.getInstance().setGame(game);
        }
    }

    protected void usingFakeOurs(Runnable runnable) {
        try (MockedStatic<BaseSelect> baseSelect = Mockito.mockStatic(BaseSelect.class)) {
            baseSelect.when(BaseSelect::ourUnits).thenReturn(mockOurUnits());

            runnable.run();
        }
    }

    protected void usingFakeEnemy(Runnable runnable) {
        try (MockedStatic<BaseSelect> baseSelect = Mockito.mockStatic(BaseSelect.class)) {
            baseSelect.when(BaseSelect::enemyUnits).thenReturn(mockEnemyUnits());

            runnable.run();
        }
    }

    protected void usingFakeNeutral(Runnable runnable) {
        try (MockedStatic<BaseSelect> baseSelect = Mockito.mockStatic(BaseSelect.class)) {
            baseSelect.when(BaseSelect::neutralUnits).thenReturn(mockNeutralUnits());

            runnable.run();
        }
    }

    protected void usingFakeOurAndFakeEnemies(FakeUnit our, FakeUnit[] enemies, Runnable runnable) {
        try (MockedStatic<BaseSelect> baseSelect = Mockito.mockStatic(BaseSelect.class)) {
            baseSelect.when(BaseSelect::ourUnits).thenReturn(Arrays.asList(our));
            baseSelect.when(BaseSelect::enemyUnits).thenReturn(Arrays.asList(enemies));

            runnable.run();
        }
    }

    protected void usingFakeOursAndFakeEnemies(FakeUnit[] ours, FakeUnit[] enemies, Runnable runnable) {
        try (MockedStatic<BaseSelect> baseSelect = Mockito.mockStatic(BaseSelect.class)) {
            baseSelect.when(BaseSelect::ourUnits).thenReturn(Arrays.asList(ours));
            baseSelect.when(BaseSelect::enemyUnits).thenReturn(Arrays.asList(enemies));

            runnable.run();
        }
    }

//    protected void usingFakeSetup(int framesNow, FakeUnit our, FakeUnit[] enemies, FoggedUnit[] fogged, Runnable runnable) {
////        MockedStatic<BaseSelect> baseSelect = Mockito.mockStatic(BaseSelect.class);
////        MockedStatic<AGame> aGame = Mockito.mockStatic(AGame.class);
////        try (baseSelect) {
//        try (MockedStatic<BaseSelect> baseSelect = Mockito.mockStatic(BaseSelect.class)) {
//            MockedStatic<AGame> aGame = Mockito.mockStatic(AGame.class);
//            aGame.when(AGame::now).thenReturn(framesNow);
//
//            baseSelect.when(EnemyUnits::combatUnitsToBetterAvoid).thenReturn(Select.from(Arrays.asList(fogged)));
//            baseSelect.when(BaseSelect::ourUnits).thenReturn(Arrays.asList(our));
//            baseSelect.when(BaseSelect::enemyUnits).thenReturn(Arrays.asList(enemies));
//
//            Atlantis.getInstance().setGame(gameMock(0));
//
//            runnable.run();
//        }
//    }

    protected FakeUnit fake(AUnitType type) {
        return new FakeUnit(type, 10, 10);
    }

    protected FakeUnit fake(AUnitType type, double x) {
        return new FakeUnit(type, x, 10);
    }

    protected FakeUnit fake(AUnitType type, int x, int y) {
        return new FakeUnit(type, x, y);
    }

    protected FakeUnit[] fakeOurs(FakeUnit... fakeUnits) {
        return fakeUnits;
    }

    protected FakeUnit[] fakeEnemies(FakeUnit... fakeUnits) {
        for (FakeUnit unit : fakeUnits) {
            unit.setEnemy();
        }
        return fakeUnits;
    }

    protected FakeFoggedUnit fogged(AUnitType type, int x) {
        return FakeFoggedUnit.fromFake(fake(type, x));
    }

    // =========================================================

    public void assertContainsAll(Object[] expected, Object[] actual) {
        boolean containsAll = (Arrays.asList(expected)).containsAll(Arrays.asList(actual));
        boolean lengthsMatch = expected.length == actual.length;

        if (!containsAll || !lengthsMatch) {
            System.err.println("\nExpected: (" + expected.length + ")");
            for (Object o : expected) {
                System.err.println(o);
            }
            System.err.println("\nActual: (" + actual.length + ")");
            for (Object o : actual ) {
                System.err.println(o);
            }
        }

        assertEquals(expected.length, actual.length);
        assertTrue(containsAll);
    }

}
