package atlantis.cherryvis.simple;

import atlantis.cherryvis.ACherryVis;
import atlantis.game.A;
import atlantis.game.event.AutomaticListener;
import atlantis.game.event.Event;
import atlantis.units.AUnit;

import java.util.*;

public class ACherryVis_UnitFirstSeen extends AutomaticListener {
    private static final Set<Integer> seenUnitIds = new HashSet<>();
    private static final Map<Integer, List<int[]>> unitsByFrame = new TreeMap<>();

    /**
     * Direct our-id to engine-id pairs (NEXT #76): the viewer fuzzy-matches
     * first-seen units against the replay by (frame, type, x, y) with no
     * retry, so a missed seed frame strands dozens of units forever. When
     * {@code unit_ids} is present the viewer skips fuzzy matching entirely
     * ({@code cvis_dbg_matching.js}: pairs are trusted directly). Filled next
     * to every first-seen record from the unit's own engine handle.
     */
    private static final Map<Integer, Integer> engineIds = new TreeMap<>();
    /**
     * Units whose own position is not known yet (NEXT #68 follow-up): a mobile
     * unit at completion still reports its PRODUCER's position - every late
     * Probe recorded at the Nexus, and the viewer (nearest same-type within
     * 25px, no retry) matches nothing in the crowd. The id is parked here with
     * its completion frame; {@link #processPending} records it a few frames
     * later, when the unit has walked out with a position of its own. Same
     * frame in the trace and in the replay means distance zero, so the delay
     * cannot hurt matching - only a wrong position can.
     */
    private static final Map<Integer, Integer> pendingFirstSeen = new TreeMap<>();
    /**
     * Frames between completion and the deferred first-seen record.
     */
    private static final int DEFER_FRAMES = 7;

    /**
     * "10203": [
     * {
     * "id": 246,
     * "type": 37,
     * "x": 3744,
     * "y": 3795
     * }
     * ],
     * "10278": [
     * {
     * "id": 249,
     * "type": 35,
     * "x": 3525,
     * "y": 2864
     * }
     * ],
     *
     * <p>One array per frame (several units can be discovered on the same
     * frame), each unit exactly once. This is the shape the CherryVis viewer
     * parses - a single object instead of an array, or a lookup by counter
     * instead of by frame, silently drops units or crashes the trace write.</p>
     */
    public static String get() {
        StringBuilder result = new StringBuilder();

        boolean firstFrame = true;
        for (Integer frame : unitsByFrame.keySet()) {
            if (!firstFrame) {
                result.append(",");
            }
            firstFrame = false;

            result.append("\"").append(frame).append("\":[");

            List<int[]> records = unitsByFrame.get(frame);
            for (int i = 0; i < records.size(); i++) {
                int[] record = records.get(i);
                result.append("{\"id\":").append(record[0])
                    .append(",\"type\":").append(record[1])
                    .append(",\"x\":").append(record[2])
                    .append(",\"y\":").append(record[3])
                    .append("}");
                if (i < records.size() - 1) {
                    result.append(",");
                }
            }
            result.append("]");
        }

        return result.toString();
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
        if (frame <= 0) frame = 1;
        if (!seenUnitIds.contains(id)) {
            seenUnitIds.add(id);
            if (!unitsByFrame.containsKey(frame)) {
                unitsByFrame.put(frame, new ArrayList<>());
            }
            unitsByFrame.get(frame).add(new int[]{id, type, x, y});
        }
    }

    /**
     * Test seam: static maps leak between tests in one JVM, so clear it.
     */
    public static void clear() {
        seenUnitIds.clear();
        unitsByFrame.clear();
        pendingFirstSeen.clear();
        engineIds.clear();
    }

    /**
     * Remembers the engine id for a traced unit. Null-safe by design: stub
     * fakes have no engine handle, and a pair is only ever written for units
     * that were actually recorded.
     */
    public static void noteEngineIdOf(AUnit unit) {
        if (unit == null || unit.u() == null) return;

        noteEngineId(unit.id(), unit.u().getID());
    }

    /**
     * Explicit ids for tests (production reads them off the unit).
     */
    public static void noteEngineId(int ourId, int engineId) {
        engineIds.put(ourId, engineId);
    }

    /**
     * {@code [[ourId, engineId], ...} in first-note order. The viewer trusts
     * these pairs directly and skips its fuzzy match.
     */
    public static String engineIdsJson() {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for (Map.Entry<Integer, Integer> entry : engineIds.entrySet()) {
            if (!first) result.append(",");
            first = false;
            result.append("[").append(entry.getKey()).append(",").append(entry.getValue()).append("]");
        }
        return result.toString();
    }

    public static void defer(int id) {
        defer(id, A.now());
    }

    /**
     * Explicit frame for tests (production stamps the current frame).
     */
    public static void defer(int id, int frame) {
        pendingFirstSeen.put(id, frame);
    }

    /**
     * Records deferred units whose position is their own by now. The lookup is
     * a parameter so the rule is assertable without a game; production passes
     * the live registry. Dead or unknown units are dropped silently.
     */
    public static void processPending(java.util.function.Function<Integer, AUnit> lookup, int now) {
        if (pendingFirstSeen.isEmpty()) return;

        java.util.Iterator<Map.Entry<Integer, Integer>> it = pendingFirstSeen.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Integer, Integer> entry = it.next();
            if (now - entry.getValue() < DEFER_FRAMES) continue;

            it.remove();
            AUnit unit;
            try {
                unit = lookup.apply(entry.getKey());
            } catch (Exception e) {
                continue;
            }
            if (unit == null || unit.isDead() || !unit.isCompleted()) continue;

            record(now, unit.id(), unit.type().ut().id, unit.x(), unit.y());
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
            record(A.now(), unit.id(), unit.type().ut().id, unit.x(), unit.y());
            noteEngineIdOf(unit);
        }
        for (AUnit mineral : atlantis.units.select.Select.mineralsAndGeysers().list()) {
            record(A.now(), mineral.id(), mineral.type().ut().id, mineral.x(), mineral.y());
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
            record(A.now(), unit.id(), unit.type().ut().id, unit.x(), unit.y());
            noteEngineIdOf(unit);
        }
    }
}
