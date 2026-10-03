package tests.fakes;

import atlantis.units.AUnit;
import atlantis.units.UnitOrigin;

/**
 * The harness's answer to "is this unit simulated?" - it knows its own units and
 * nothing else does. Installed from {@code AbstractTestWithUnits.setUp()}, like
 * the other ports.
 */
public class FakeUnitOrigin implements UnitOrigin.Source {
    public static void installAsSource() {
        UnitOrigin.useSource(new FakeUnitOrigin());
    }

    @Override
    public boolean isSimulated(AUnit unit) {
        return unit instanceof FakeUnit;
    }
}
