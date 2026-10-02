package atlantis.core.world;

import atlantis.units.AUnit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Stage E (see _AI/REVIEW.md §16): owns unit identity and the current
 * per-frame {@link UnitSnapshot} projections.
 *
 * <p>A {@code World} is built explicitly — there is no global instance.
 * Production code will refresh it once per frame; tests construct it directly
 * from fake units, with no engine involved. Snapshots are immutable; building
 * a new {@code World} is how state advances.</p>
 */
public final class World {

    private final Map<Integer, UnitSnapshot> snapshots;

    private World(Map<Integer, UnitSnapshot> snapshots) {
        this.snapshots = Collections.unmodifiableMap(new LinkedHashMap<>(snapshots));
    }

    /**
     * Builds a world from live units. Transitional backing: reads each unit's
     * current state (Stage E will move lifecycle here and drop the static
     * {@code AUnit} registry).
     */
    public static World snapshotOf(Collection<? extends AUnit> units) {
        Map<Integer, UnitSnapshot> snapshots = new LinkedHashMap<>();
        for (AUnit unit : units) {
            UnitSnapshot snapshot = UnitSnapshots.of(unit);
            snapshots.put(snapshot.id(), snapshot);
        }
        return new World(snapshots);
    }

    /**
     * Tests (and future systems) build worlds directly, with no engine.
     */
    public static World of(UnitSnapshot... snapshots) {
        Map<Integer, UnitSnapshot> map = new LinkedHashMap<>();
        for (UnitSnapshot snapshot : snapshots) {
            map.put(snapshot.id(), snapshot);
        }
        return new World(map);
    }

    public UnitSnapshot snapshotFor(int unitId) {
        return snapshots.get(unitId);
    }

    public List<UnitSnapshot> snapshots() {
        return new ArrayList<>(snapshots.values());
    }

    public int size() {
        return snapshots.size();
    }
}
