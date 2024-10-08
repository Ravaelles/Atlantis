package tests.unit;

import atlantis.units.select.BaseSelect;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import tests.acceptance.AbstractTestFakingGame;
import tests.fakes.FakeUnit;

import java.util.Collection;

public class DynamicMockOurUnits {
    public static void mockOur(Collection<FakeUnit> ourUnits) {
        AbstractTestFakingGame.baseSelect.when(BaseSelect::ourUnitsWithUnfinishedList).thenReturn(ourUnits);
        BaseSelect.clearCache();
        Select.clearCache();
        Count.clearCache();
    }
}
