package cherryvis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * First-seen registry for the trace: which units the viewer must match
 * against the replay, and the direct our-id to engine-id pairs that let the
 * viewer skip fuzzy matching entirely.
 *
 * <p>All methods take primitives, so the format is testable without running
 * a game. The deferred-record policy lives here (a unit at completion still
 * reports its producer's position, so its record waits a few frames until it
 * has walked out with a position of its own); looking the unit up stays the
 * caller's job via {@link #pollDue}.
 */
public class FirstSeen {
    /**
     * Frames between completion and the deferred first-seen record.
     */
    public static final int DEFER_FRAMES = 7;

    private final Set<Integer> seenUnitIds = new HashSet<>();
    private final Map<Integer, List<int[]>> unitsByFrame = new TreeMap<>();
    private final Map<Integer, Integer> engineIds = new TreeMap<>();
    private final Map<Integer, Integer> pendingFirstSeen = new TreeMap<>();

    /**
     * Records are pure data: one array per frame, each unit exactly once.
     * Frames start at 1, never 0 (the replay has no visible units on frame 0
     * yet, so frame-0 seeds match nothing and are lost forever).
     */
    public void record(int frame, int id, int type, int x, int y) {
        if (frame <= 0) frame = 1;
        if (seenUnitIds.contains(id)) return;
        seenUnitIds.add(id);
        List<int[]> records = unitsByFrame.get(frame);
        if (records == null) {
            records = new ArrayList<>();
            unitsByFrame.put(frame, records);
        }
        records.add(new int[]{id, type, x, y});
    }

    public void noteEngineId(int ourId, int engineId) {
        engineIds.put(ourId, engineId);
    }

    public void defer(int id, int frame) {
        pendingFirstSeen.put(id, frame);
    }

    /**
     * Ids whose deferred record is due at {@code now}, oldest first.
     * Returned ids are removed from the pending set; the caller records the
     * ones that are still alive and drops the rest.
     */
    public List<Integer> pollDue(int now) {
        List<Integer> due = new ArrayList<>();
        Iterator<Map.Entry<Integer, Integer>> it = pendingFirstSeen.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Integer, Integer> entry = it.next();
            if (now - entry.getValue() < DEFER_FRAMES) continue;
            it.remove();
            due.add(entry.getKey());
        }
        return due;
    }

    public String toJson() {
        StringBuilder result = new StringBuilder();
        boolean firstFrame = true;
        for (Integer frame : unitsByFrame.keySet()) {
            if (!firstFrame) result.append(",");
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
                if (i < records.size() - 1) result.append(",");
            }
            result.append("]");
        }
        return result.toString();
    }

    /**
     * {@code [[ourId, engineId], ...]} in first-note order. The viewer trusts
     * these pairs directly and skips its fuzzy match.
     */
    public String engineIdsJson() {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for (Map.Entry<Integer, Integer> entry : engineIds.entrySet()) {
            if (!first) result.append(",");
            first = false;
            result.append("[").append(entry.getKey()).append(",").append(entry.getValue()).append("]");
        }
        return result.toString();
    }

    public Map<Integer, Integer> pendingForTests() {
        return new HashMap<>(pendingFirstSeen);
    }

    public void clear() {
        seenUnitIds.clear();
        unitsByFrame.clear();
        pendingFirstSeen.clear();
        engineIds.clear();
    }
}
