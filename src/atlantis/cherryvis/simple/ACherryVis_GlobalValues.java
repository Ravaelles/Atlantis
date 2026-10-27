package atlantis.cherryvis.simple;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

public class ACherryVis_GlobalValues {
    private static final TreeMap<Integer, LinkedHashMap<String, String>> samples = new TreeMap<>();

    /**
     * If it's a global value → game_values
     */
    public static String build(ACherryVisUnitLogger logger) {
        if (samples.isEmpty()) return "";

        StringBuilder out = new StringBuilder();
        boolean firstFrame = true;
        for (Map.Entry<Integer, LinkedHashMap<String, String>> frame : samples.entrySet()) {
            if (!firstFrame) out.append(",");
            firstFrame = false;
            out.append("\"").append(frame.getKey()).append("\":{");
            boolean firstKey = true;
            for (Map.Entry<String, String> row : frame.getValue().entrySet()) {
                if (!firstKey) out.append(",");
                firstKey = false;
                out.append("\"").append(row.getKey()).append("\":").append(row.getValue());
            }
            out.append("}");
        }
        return out.toString();
    }

    /**
     * Sampled graph values are numeric literals, rendered unquoted.
     */
    public static void sample(int frame, String key, int value) {
        LinkedHashMap<String, String> rows = samples.get(frame);
        if (rows == null) {
            rows = new LinkedHashMap<>();
            samples.put(frame, rows);
        }
        rows.put(key, String.valueOf(value));
    }

    /**
     * Test seam: samples are per-game state, never cross-test.
     */
    public static void clearForTests() {
        samples.clear();
    }
}
