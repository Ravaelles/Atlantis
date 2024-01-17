package atlantis.units.range;

public class OurDragoonWeaponRange {
    private static int currentRange = 4;

    public static int range() {
        return currentRange;
    }

    public static void onSingularityChargeResearched() {
        currentRange = 6;
    }
}
