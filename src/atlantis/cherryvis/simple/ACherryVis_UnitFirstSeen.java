package atlantis.cherryvis.simple;

import atlantis.cherryvis.ACherryVis;
import atlantis.game.A;
import atlantis.game.event.AutomaticListener;
import atlantis.game.event.Event;
import atlantis.units.AUnit;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class ACherryVis_UnitFirstSeen extends AutomaticListener {
    private static final Set<Integer> seenUnitIds = new HashSet<>();
    private static final Map<Integer, List<int[]>> unitsByFrame = new TreeMap<>();

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

    /** Test seam: static maps leak between tests in one JVM, so clear it. */
    public static void clear() {
        seenUnitIds.clear();
        unitsByFrame.clear();
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
        }
        for (AUnit mineral : atlantis.units.select.Select.mineralsAndGeysers().list()) {
            record(A.now(), mineral.id(), mineral.type().ut().id, mineral.x(), mineral.y());
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
        }
    }
}
