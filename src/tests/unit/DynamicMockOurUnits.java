package tests.unit;

import atlantis.units.select.BaseSelect;
import tests.acceptance.AbstractTestWithWorld;
import tests.fakes.FakeUnit;
import tests.unit.helpers.ClearAllCaches;

import java.util.Collection;

public class DynamicMockOurUnits {
    public static void mockOur(Collection<FakeUnit> ourUnits) {
        AbstractTestWithWorld.baseSelect.when(BaseSelect::ourUnitsWithUnfinishedList).thenReturn(ourUnits);
        // Queries only: clearAll() would null the position of every unit the
        // test has just added, and the queue would then see buildings with no
        // position and never match them to their orders.
        ClearAllCaches.clearQueries();
    }
}
