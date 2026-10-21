package atlantis.units;

/**
 * Near-counters, extracted from {@link AUnit} (NEXT #11, second slice):
 * radius-gated counts over the near selections. Pure computations over
 * the unit's selections - memoization stays on the unit
 * ({@code cacheInt}), so the per-frame cost is unchanged; only the
 * math moves, one seam per family like {@code AUnitHealth}.
 */
public final class AUnitNearCounts {

    private AUnitNearCounts() {
    }

    public static int enemiesNear(AUnit unit, double maxDist) {
        if (unit == null) return 0;
        return unit.enemiesNear().inRadius(maxDist, unit).count();
    }

    public static int meleeEnemiesNear(AUnit unit) {
        if (unit == null) return 0;
        return unit.enemiesNear().melee().inRadius(2.7, unit).size();
    }

    public static int ourBuildingsNear(AUnit unit, double radius) {
        if (unit == null) return 0;
        return unit.friendsNear().buildings().inRadius(radius, unit).size();
    }

    public static int friendsNear(AUnit unit) {
        if (unit == null) return 0;
        return unit.friendsNear().size();
    }
}
