package atlantis.units.select;

import atlantis.Atlantis;
import atlantis.tests.UnitTestHelper;
import org.junit.Before;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

public class AbstractTestWithUnits extends UnitTestHelper {

    @Before
    public void before() {
        Atlantis.getInstance().setGame(gameMock());
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

}
