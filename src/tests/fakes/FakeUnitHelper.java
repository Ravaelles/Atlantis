package tests.fakes;

import java.util.ArrayList;
import java.util.Arrays;

public class FakeUnitHelper {
    public static ArrayList<FakeUnit> fakeUnitsToArrayList(FakeUnit[] fakeUnits) {
        return new ArrayList<>(Arrays.asList(fakeUnits));
    }

    public static FakeUnit[] merge(FakeUnit[] fakeUnits1, FakeUnit[] fakeUnits2) {
        ArrayList<FakeUnit> fakeUnitsList = new ArrayList<>(Arrays.asList(fakeUnits2));
        fakeUnitsList.addAll(Arrays.asList(fakeUnits1));
        return fakeUnitsList.toArray(new FakeUnit[]{});
    }
}
