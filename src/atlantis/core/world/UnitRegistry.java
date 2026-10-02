package atlantis.core.world;

import atlantis.units.AUnit;
import bwapi.Unit;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Stage E (see _AI/REVIEW.md §16): owns unit identity.
 *
 * <p>Moved out of {@code AUnit} (former static {@code instances} map) so that
 * lifecycle has exactly one home. Instantiable and engine-free by itself —
 * tests build their own registries; production uses the shared instance
 * behind {@link Worlds} until the frame pipeline owns worlds directly.</p>
 */
public final class UnitRegistry {

    private final Map<Integer, AUnit> entities = new LinkedHashMap<>();

    public AUnit createFrom(Unit u) {
        return createFrom(u, true);
    }

    /**
     * Atlantis uses wrapper for BWAPI classes.
     *
     * <b>AUnit</b> class contains numerous helper methods, but if you think some methods are missing you can
     * create missing method here and you can reference original Unit class via u() method.
     * <p>
     * The idea why we don't use inner Unit class is because if you change game bridge (JBWAPI, JNIBWAPI, JBWAPI etc)
     * you need to change half of your codebase. I've done it 3 times already ;__:
     */
    public AUnit createFrom(Unit u, boolean throwErrorOnNull) {
        if (u == null) {
            if (!throwErrorOnNull) return null;
            throw new RuntimeException("AUnit constructor: unit is null");
        }

        AUnit unit;
        if (entities.containsKey(u.getID())) {
            unit = entities.get(u.getID());
            if (unit != null) {
                return unit;
            }
        }

        unit = new AUnit(u);
        entities.put(unit.id(), unit);
        return unit;
    }

    public AUnit getById(Unit u) {
        return createFrom(u);
    }

    /**
     * Direct registration, used by tests and by flows that already hold
     * a wrapper (e.g. fakes). Production engine units go through
     * {@link #createFrom(Unit)}.
     */
    public void register(AUnit unit) {
        entities.put(unit.id(), unit);
    }

    public void forgetEntirely(AUnit unit) {
        entities.remove(unit.id());
    }

    public AUnit entity(int id) {
        return entities.get(id);
    }

    public Collection<AUnit> all() {
        return Collections.unmodifiableCollection(entities.values());
    }

    public int size() {
        return entities.size();
    }

    public void clear() {
        entities.clear();
    }
}
