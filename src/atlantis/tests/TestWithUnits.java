package atlantis.tests;

import atlantis.units.AUnitType;
import atlantis.units.select.BaseSelect;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import static org.junit.Assert.*;

public class TestWithUnits extends AbstractTestWithUnits {

    @Test
    public void subsequentSelectionDoesNotModifyOriginal() {
        try (
                MockedStatic<BaseSelect> baseSelectMock = Mockito.mockStatic(BaseSelect.class)
        ) {
            baseSelectMock.when(BaseSelect::neutralUnits).thenReturn(mockNeutralUnits());

            Selection selection = Select.neutral();
            int count = selection.count();

            Selection newSelection = selection.ofType(AUnitType.Resource_Vespene_Geyser);
            int newCount = newSelection.count();

            assertEquals(GEYSER_COUNT, newCount);
            assertNotEquals(count, newCount);

            // =========================================================
            // Do entire sequence once again

            selection = Select.neutral();
            count = selection.count();

            newSelection = selection.ofType(AUnitType.Resource_Vespene_Geyser);
            newCount = newSelection.count();

            assertEquals(GEYSER_COUNT, newCount);
            assertNotEquals(count, newCount);
        }
    }

//    @Test
//    public void ranged() {
//        assert true == true;
//    }
}