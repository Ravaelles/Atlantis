package cherryvis;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Runtime string-to-id legend for the trace.
 *
 * <p>Manager names and behavior tags ride the trace as integers; the viewer
 * resolves them through {@code custom_names}, so the table must be built at
 * runtime in first-logged order. Ids start at 10000 to never collide with
 * engine type ids. Pure java.util, no dependencies.
 */
public class StringTable {
    private static final int START_ID = 10000;

    private final Map<String, Integer> stringToId = new HashMap<>();
    private final Map<Integer, String> idToString = new TreeMap<>();
    private int nextId = START_ID;

    public int get(String string) {
        if (string == null) return -1;
        Integer existing = stringToId.get(string);
        if (existing != null) return existing;

        int id = nextId++;
        stringToId.put(string, id);
        idToString.put(id, string);
        return id;
    }

    public String getJson() {
        StringBuilder result = new StringBuilder();
        for (Map.Entry<Integer, String> entry : idToString.entrySet()) {
            if (result.length() > 0) result.append(",");
            result.append("\"").append(entry.getKey()).append("\":\"")
                .append(Json.escape(entry.getValue())).append("\"");
        }
        return result.toString();
    }

    public boolean isEmpty() {
        return idToString.isEmpty();
    }
}
