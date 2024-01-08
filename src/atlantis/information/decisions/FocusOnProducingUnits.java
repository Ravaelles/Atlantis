package atlantis.information.decisions;

import atlantis.units.AUnitType;

import java.util.TreeSet;

public class FocusOnProducingUnits {
    private static TreeSet<AUnitType> unitsToFocusOn = new TreeSet();

    public static void addUnitTypeToFocusOn(AUnitType type) {
        unitsToFocusOn.add(type);
    }

    public static boolean isFocusedOn(AUnitType type) {
        return unitsToFocusOn.contains(type);
    }

    public static boolean haveAnyFocus() {
        return !unitsToFocusOn.isEmpty();
    }
}
