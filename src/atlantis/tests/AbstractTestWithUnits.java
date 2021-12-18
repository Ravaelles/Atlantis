package atlantis.tests;

import atlantis.AGame;
import atlantis.Atlantis;
import atlantis.combat.micro.avoid.AAvoidUnits;
import atlantis.debug.APainter;
import atlantis.enemy.EnemyInformation;
import atlantis.enemy.EnemyUnits;
import atlantis.information.AFoggedUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.BaseSelect;
import atlantis.units.select.Select;
import org.junit.Before;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

public class AbstractTestWithUnits extends UnitTestHelper {

    @Before
    public void before() {
        Atlantis.getInstance().setGame(gameMock(0));
        APainter.disablePainting();

        Select.clearCache();
        BaseSelect.clearCache();
        FakeFoggedUnit.clearCache();
        EnemyInformation.clearCache();
        EnemyUnits.clearCache();
        AAvoidUnits.clearCache();
    }

//    protected void usingFakeTime(int framesNow, Runnable runnable) {
////        Atlantis.getInstance().setGame(gameMock(0));
//
//        try (MockedStatic<AGame> aGame = Mockito.mockStatic(AGame.class)) {
//            aGame.when(AGame::now).thenReturn(framesNow);
//
//            runnable.run();
//        }
//    }

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

//    protected void usingFakeSetup(int framesNow, FakeUnit our, FakeUnit[] enemies, AFoggedUnit[] fogged, Runnable runnable) {
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

    protected FakeUnit fake(AUnitType type, int x) {
        return new FakeUnit(type, x, 10);
    }

    protected FakeUnit fake(AUnitType type, int x, int y) {
        return new FakeUnit(type, x, y);
    }

    protected FakeUnit[] fakeUnits(FakeUnit... fakeUnits) {
        return fakeUnits;
    }

    protected AFoggedUnit fogged(AUnitType type, int y) {
        return FakeFoggedUnit.fromFake(fake(type, y));
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
