package atlantis.units.range;

public class OurMarineRange {
    private static int currentRange = 4;

    public static int range() {
        return currentRange;
    }

    public static void onU238Researched() {
        currentRange = 5;
    }
}
