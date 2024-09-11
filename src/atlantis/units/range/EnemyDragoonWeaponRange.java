package atlantis.units.range;

public class EnemyDragoonWeaponRange {
    private static int currentRange = 4;

    public static int range() {
        return currentRange;
    }

    public static void changeEnemyRangeTo6() {
        currentRange = 6;
    }
}
