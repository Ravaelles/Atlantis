package atlantis.cherryvis.simple;

import atlantis.cherryvis.ACherryVis;
import atlantis.game.A;
import atlantis.game.event.AutomaticListener;
import atlantis.game.event.Event;
import atlantis.units.AUnit;
import cherryvis.FirstSeen;

/**
 * Bot-side first-seen hook: translates game events into primitive records on
 * the game trace. The record format and the deferral policy live in the
 * library ({@link FirstSeen}); the unit lookup stays here, where the game
 * objects are.
 */
public class ACherryVis_UnitFirstSeen extends AutomaticListener {
    private static FirstSeen registry() {
        return ACherryVis.trace().firstSeen();
    }

    /**
     * Test seam (no CherryVis frontend needed): records are pure data, so the
     * format test pins the exact JSON string without running a game.
     *
     * <p>Frames start at 1, never 0 (Stardust does the same: the replay has no
     * visible units on frame 0 yet, so frame-0 seeds match nothing and are lost
     * forever - every starting unit then reads "i???". NEXT #68.</p>
     */
    public static void record(int frame, int id, int type, int x, int y) {
        registry().record(frame, id, type, x, y);
    }

    public static String get() {
        return registry().toJson();
    }

    /**
     * Test seam: static maps leak between tests in one JVM, so clear it.
     */
    public static void clear() {
        registry().clear();
    }

    /**
     * Remembers the engine id for a traced unit. Null-safe by design: stub
     * fakes have no engine handle, and a pair is only ever written for units
     * that were actually recorded.
     */
    public static void noteEngineIdOf(AUnit unit) {
        if (unit == null || unit.u() == null) return;

        registry().noteEngineId(unit.id(), unit.u().getID());
    }

    /**
     * Explicit ids for tests (production reads them off the unit).
     */
    public static void noteEngineId(int ourId, int engineId) {
        registry().noteEngineId(ourId, engineId);
    }

    /**
     * {@code [[ourId, engineId], ...} in first-note order. The viewer trusts
     * these pairs directly and skips its fuzzy match.
     */
    public static String engineIdsJson() {
        return registry().engineIdsJson();
    }

    public static void defer(int id) {
        defer(id, A.now());
    }

    /**
     * Explicit frame for tests (production stamps the current frame).
     */
    public static void defer(int id, int frame) {
        registry().defer(id, frame);
    }

    /**
     * Records deferred units whose position is their own by now. The lookup is
     * a parameter so the rule is assertable without a game; production passes
     * the live registry. Dead or unknown units are dropped silently.
     */
    public static void processPending(java.util.function.Function<Integer, AUnit> lookup, int now) {
        for (int id : registry().pollDue(now)) {
            AUnit unit;
            try {
                unit = lookup.apply(id);
            } catch (Exception e) {
                continue;
            }
            if (unit == null || unit.isDead() || !unit.isCompleted()) continue;

            registry().record(now, unit.id(), unit.type().ut().id, unit.x(), unit.y());
            noteEngineIdOf(unit);
        }
    }

    /**
     * Records every unit already on the board (Stardust marks static neutrals
     * at initialize for the same reason): creation events only fire for units
     * born mid-game, so starting armies and mineral lines would otherwise never
     * appear in {@code units_first_seen} - on OpenBW, where the client attaches
     * late, that is every unit that matters. No Env gate inside; call sites
     * decide (production gates on {@code ACherryVis.isEnabled()}).
     */
    public static void seedCurrentUnits() {
        for (AUnit unit : atlantis.units.select.Select.ourUnitsWithUnfinishedList()) {
            // Engine type id - see onEvent (NEXT #68).
            registry().record(A.now(), unit.id(), unit.type().ut().id, unit.x(), unit.y());
            noteEngineIdOf(unit);
        }
        for (AUnit mineral : atlantis.units.select.Select.mineralsAndGeysers().list()) {
            registry().record(A.now(), mineral.id(), mineral.type().ut().id, mineral.x(), mineral.y());
            noteEngineIdOf(mineral);
        }
    }

    // =========================================================

    @Override
    public Event listensTo() {
        return Event.UNIT_DISCOVERED;
    }

    @Override
    public void onEvent(Event event, Object... data) {
        if (!ACherryVis.isEnabled()) return;

        AUnit unit = (AUnit) data[0];

        if (!unit.isNeutral()) {
            // Engine type id (bwapi), NOT AUnitType.id() (ours, 1000+): the viewer
            // matches units against the replay by BWAPI type, and anything else
            // matches nothing - every clicked unit then reads "i???" (NEXT #68).
            registry().record(A.now(), unit.id(), unit.type().ut().id, unit.x(), unit.y());
            noteEngineIdOf(unit);
        }
    }
}
