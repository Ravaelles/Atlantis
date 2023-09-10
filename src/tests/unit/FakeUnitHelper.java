package tests.unit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FakeUnitHelper {
    public static ArrayList<FakeUnit> fakeUnitsToArrayList(FakeUnit[] fakeUnits) {
        return new ArrayList<>(Arrays.asList(fakeUnits));
    }
}
