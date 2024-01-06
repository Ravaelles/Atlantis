package tests.unit;

import atlantis.units.select.BaseSelect;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;

public class BaseSelectTest extends AbstractTestWithUnits {

    @Test
    public void ourUnits() {
        try (
                MockedStatic<BaseSelect> baseSelectMock = Mockito.mockStatic(BaseSelect.class)
        ) {
            baseSelectMock.when(BaseSelect::ourUnitsWithUnfinishedList).thenReturn(mockOurUnits());

            assertEquals(ourUnits.length, BaseSelect.ourUnitsWithUnfinishedList().size());
        }
    }

    @Test
    public void enemyUnits() {
        try (
                MockedStatic<BaseSelect> baseSelectMock = Mockito.mockStatic(BaseSelect.class)
        ) {
            baseSelectMock.when(BaseSelect::enemyUnits).thenReturn(mockEnemyUnits());

            assertEquals(enemyUnits.length, BaseSelect.enemyUnits().size());
        }
    }

    @Test
    public void neutralUnits() {
        try (
                MockedStatic<BaseSelect> baseSelectMock = Mockito.mockStatic(BaseSelect.class)
        ) {
            baseSelectMock.when(BaseSelect::neutralUnits).thenReturn(mockNeutralUnits());

            assertEquals(neutralUnits.length, BaseSelect.neutralUnits().size());
        }
    }

}
