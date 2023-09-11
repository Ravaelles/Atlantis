package atlantis.util;

import java.util.Map;
import java.util.TreeMap;

/**
 * Auxiliary class for storing options that acts like a JSON object.
 */
public class Options {
    private Map<String, Object> options = new TreeMap<>();

    public static Options create() {
        return new Options();
    }

    public Options set(String key, Object value) {
        options.put(key, value);
        return this;
    }

    public Object get(String key) {
        if (options.containsKey(key)) {
            return options.get(key);
        }
        return null;
    }

    public boolean has(String key) {
        return options.containsKey(key);
    }

    public int getInt(String key) {
        return (int) options.get(key);
    }

    public int getIntOr(String key, int fallback) {
        if (options.containsKey(key)) {
            return (int) options.get(key);
        }
        return fallback;
    }
}
