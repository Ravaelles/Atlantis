package atlantis.units;

/**
 * Health predicates, extracted from {@link AUnit} (NEXT #11, first
 * slice): the wound/hp-percent family is per-condition logic over
 * {@code hp/maxHp/shields/maxShields} with no engine calls, so it moves
 * verbatim. {@code AUnit} keeps one-line delegates - zero call-site
 * changes, the same recipe as the {@code Selection} slices. State
 * accessors stay on the unit; only the conditions move.
 */
public final class AUnitHealth {

    private AUnitHealth() {
    }

    public static boolean isFullyHealthy(AUnit unit) {
        return unit.hp() >= unit.maxHp();
    }

    public static int hpPercent(AUnit unit) {
        return 100 * unit.hp() / unit.maxHp();
    }

    public static boolean hpPercent(AUnit unit, int minPercent) {
        return hpPercent(unit) >= minPercent;
    }

    public static int woundHp(AUnit unit) {
        return unit.maxHp() - unit.hp();
    }

    public static double woundPercent(AUnit unit) {
        return 100 - 100.0 * unit.hp() / unit.maxHp();
    }

    public static boolean woundPercentMin(AUnit unit, int minWoundPercent) {
        return woundPercent(unit) >= minWoundPercent;
    }

    public static boolean woundPercentMax(AUnit unit, int maxWoundPercent) {
        return woundPercent(unit) <= maxWoundPercent;
    }

    public static boolean isWounded(AUnit unit) {
        return unit.hp() < unit.maxHp() || unit.shields() < unit.maxShields();
    }
}
