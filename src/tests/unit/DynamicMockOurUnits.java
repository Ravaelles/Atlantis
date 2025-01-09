package tests.unit;

import atlantis.units.select.BaseSelect;
import tests.acceptance.AbstractTestWithWorld;
import tests.fakes.FakeUnit;
import tests.unit.helpers.ClearAllCaches;

import java.util.Collection;

public class DynamicMockOurUnits {
    public static void mockOur(Collection<FakeUnit> ourUnits) {
        AbstractTestWithWorld.baseSelect.when(BaseSelect::ourUnitsWithUnfinishedList).thenReturn(ourUnits);
        ClearAllCaches.clearAll();
    }
}
