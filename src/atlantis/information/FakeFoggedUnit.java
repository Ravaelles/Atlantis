package atlantis.information;

import atlantis.tests.unit.FakeUnit;

/**
 * Used only in tests.
 */
public class FakeFoggedUnit extends AbstractFoggedUnit {

    protected FakeFoggedUnit() { }

    public static FakeFoggedUnit fromFake(FakeUnit unit) {
        FakeFoggedUnit fakeFoggedUnit = new FakeFoggedUnit();
        fakeFoggedUnit._id = unit.id();
        fakeFoggedUnit.aUnit = unit;
        fakeFoggedUnit.update(unit);

        all.put(unit.id(), fakeFoggedUnit);

        return fakeFoggedUnit;
    }

    // =========================================================

}
