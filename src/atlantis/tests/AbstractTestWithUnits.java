package atlantis.tests;

import atlantis.Atlantis;
import atlantis.debug.APainter;
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
        Atlantis.getInstance().setGame(gameMock());
        APainter.disablePainting();

        Select.clearCache();
        BaseSelect.clearCache();
    }

    protected void usingMockedOurs(Runnable runnable) {
        try (MockedStatic<BaseSelect> baseSelectMock = Mockito.mockStatic(BaseSelect.class)) {
            baseSelectMock.when(BaseSelect::ourUnits).thenReturn(mockOurUnits());

            runnable.run();
        }
    }

    protected void usingMockedEnemy(Runnable runnable) {
        try (MockedStatic<BaseSelect> baseSelectMock = Mockito.mockStatic(BaseSelect.class)) {
            baseSelectMock.when(BaseSelect::enemyUnits).thenReturn(mockEnemyUnits());

            runnable.run();
        }
    }

    protected void usingMockedNeutral(Runnable runnable) {
        try (MockedStatic<BaseSelect> baseSelectMock = Mockito.mockStatic(BaseSelect.class)) {
            baseSelectMock.when(BaseSelect::neutralUnits).thenReturn(mockNeutralUnits());

            runnable.run();
        }
    }

    protected void usingMockedOurAndEnemies(FakeUnit our, FakeUnit[] enemies, Runnable runnable) {
        try (MockedStatic<BaseSelect> baseSelectMock = Mockito.mockStatic(BaseSelect.class)) {
            baseSelectMock.when(BaseSelect::ourUnits).thenReturn(Arrays.asList(our));
            baseSelectMock.when(BaseSelect::enemyUnits).thenReturn(Arrays.asList(enemies));

            runnable.run();
        }
    }

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
