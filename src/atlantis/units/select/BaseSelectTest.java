package atlantis.units.select;

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
            baseSelectMock.when(BaseSelect::ourUnits).thenReturn(mockOurUnits());

            assertEquals(ourUnits.length, BaseSelect.ourUnits().size());
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
