package atlantis.map.scout;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;

/**
 * Builds the per-unit manager that drives one scout: the combination of
 * {@code atlantis.map.scout} scouting policies and the combat micro the scout
 * needs to survive contact.
 *
 * <p>The interface lives here, in the Scouting context, and the implementation
 * lives in Combat ({@code atlantis.combat.squad.positioning.scout}). That split
 * is the point: the boundary test forbids {@code atlantis.map.scout..} from
 * depending on {@code atlantis.combat..}, and a scouting commander that wires
 * combat classes itself is exactly that dependency. Scouting decides <em>which
 * scout needs what</em>; Combat decides <em>how it behaves</em>.</p>
 *
 * <p>{@link ScoutState#scoutUnitManager} holds the instance, set once by
 * {@code AtlantisGameCommander} - the composition root that already builds the
 * whole commander tree.</p>
 */
public interface ScoutUnitManager {
    Manager create(AUnit scout);
}