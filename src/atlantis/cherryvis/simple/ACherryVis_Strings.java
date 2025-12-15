package atlantis.cherryvis.simple;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class ACherryVis_Strings {

    private static final int START_ID = 10000;
    private int nextId = START_ID;
    private final Map<String, Integer> stringToId = new HashMap<>();
    private final Map<Integer, String> idToString = new TreeMap<>();

    public int get(String string) {
        if (string == null) {
            return -1;
        }

        if (stringToId.containsKey(string)) {
            return stringToId.get(string);
        }

        int id = nextId++;
        stringToId.put(string, id);
        idToString.put(id, string);

        return id;
    }

    public String getJson() {
        StringBuilder result = new StringBuilder();
        for (Map.Entry<Integer, String> entry : idToString.entrySet()) {
            if (result.length() > 0) {
                result.append(",");
            }
            // Escape quotes in string if necessary? Assuming simple strings for now or
            // standard JSON escaping.
            // For safety, we should validly escape the string.
            String escaped = entry.getValue().replace("\"", "\\\"");
            result.append("\"").append(entry.getKey()).append("\":\"").append(escaped).append("\"");
        }
        return result.toString();
    }

    public boolean isEmpty() {
        return idToString.isEmpty();
    }
}
