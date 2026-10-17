package atlantis.core.world;

import atlantis.units.AUnit;
import atlantis.units.AUnitType;

import java.util.ArrayList;
import java.util.List;

/**
 * Per-frame unit queries over a frozen unit list (Stage F, NEXT #5).
 *
 * <p>
 * The old shape cached every select behind its own TTL in
 * {@code Select} (40+ keys, TTLs from 0 frames to 9.8 seconds), so the
 * freshness of "our completed Gateways" depended on which key a caller
 * happened to use. This answers from one frozen list instead; the only
 * invalidation is the frame moving, owned by the single holder
 * (currently {@code Select}, until the callers take instances). Pure:
 * built from an explicit list, no game reads, no statics.
 * </p>
 */
public final class UnitIndex {

    private final List<AUnit> units;

    public UnitIndex(List<AUnit> units) {
        this.units = new ArrayList<>(units);
    }

    /** Frozen units of these types, in list order. */
    public List<AUnit> ofType(AUnitType... types) {
        List<AUnit> filtered = new ArrayList<>();
        for (AUnit unit : units) {
            for (AUnitType type : types) {
                if (unit.is(type)) {
                    filtered.add(unit);
                    break;
                }
            }
        }
        return filtered;
    }

    /** Frozen completed units of these types, in list order. */
    public List<AUnit> completedOfType(AUnitType... types) {
        List<AUnit> filtered = new ArrayList<>();
        for (AUnit unit : units) {
            if (!unit.isCompleted()) continue;
            for (AUnitType type : types) {
                if (unit.is(type)) {
                    filtered.add(unit);
                    break;
                }
            }
        }
        return filtered;
    }

    /** The frozen list itself (defensive copy: callers must not move the frame). */
    public List<AUnit> all() {
        return new ArrayList<>(units);
    }
}
