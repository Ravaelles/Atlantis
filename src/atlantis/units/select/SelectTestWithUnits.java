package atlantis.units.select;

import atlantis.tests.AbstractTestWithUnits;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;

public class SelectTestWithUnits extends AbstractTestWithUnits {

    @Test
    public void enemy() {
        try (MockedStatic<BaseSelect> utilities = Mockito.mockStatic(BaseSelect.class)) {
            utilities.when(BaseSelect::enemyUnits).thenReturn(mockEnemyUnits());
            assertEquals(enemyUnits.length, BaseSelect.enemyUnits().size());
        }
    }

    @Test
    public void ourRealUnits() {
        try (
                MockedStatic<BaseSelect> baseSelectMock = Mockito.mockStatic(BaseSelect.class)
        ) {
            baseSelectMock.when(BaseSelect::ourUnits).thenReturn(mockOurUnits());

            assertEquals(ourUnits.length, Select.our().size());
        }
    }

    @Test
    public void enemyRealUnits() {
        Selection selection;

        try (
                MockedStatic<BaseSelect> baseSelectMock = Mockito.mockStatic(BaseSelect.class)
        ) {
            baseSelectMock.when(BaseSelect::enemyUnits).thenReturn(mockEnemyUnits());

            assertEquals(enemyUnits.length, Select.enemyUnits().size());

            assertEquals(
                    0,
                    Select.enemyRealUnits(false, false, false).size()
            );

            assertEquals(
                    GROUND_UNITS,
                    Select.enemyRealUnits(true, false, false).size()
            );

            assertEquals(
                    GROUND_UNITS + BUILDINGS,
                    Select.enemyRealUnits(true, false, true).size()
            );

            assertEquals(
                    AIR_UNITS,
                    Select.enemyRealUnits(false, true, false).size()
            );

            assertEquals(
                    REAL_UNITS,
                    Select.enemyRealUnits(true, true, false).size()
            );

            assertEquals(
                    REAL_UNITS + BUILDINGS,
                    Select.enemyRealUnits(true, true, true).size()
            );
        }
    }

    // === Neutral ======================================================

    @Test
    public void neutralUnits() {
        try (
                MockedStatic<BaseSelect> baseSelectMock = Mockito.mockStatic(BaseSelect.class)
        ) {
            baseSelectMock.when(BaseSelect::neutralUnits).thenReturn(mockNeutralUnits());

            assertEquals(MINERAL_COUNT, Select.minerals().size());
            assertEquals(GEYSER_COUNT, Select.geysers().size());

            assertEquals(neutralUnits.length, Select.neutral().size());

            assertEquals(MINERAL_COUNT, Select.minerals().size());
            assertEquals(GEYSER_COUNT, Select.geysers().size());
        }
    }

}
