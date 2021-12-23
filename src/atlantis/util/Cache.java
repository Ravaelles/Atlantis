package atlantis.util;

import atlantis.units.AUnit;
import atlantis.units.select.Selection;

import java.util.TreeMap;

/**
 * V is type of objects stored e.g. Booleans or generic Object (which can be then cast in methods).
 */
public class Cache<V> {

    protected final TreeMap<String, V> data = new TreeMap<>();
    protected final TreeMap<String, Integer> cachedUntil = new TreeMap<>();

    // =========================================================

    /**
     * Get cached value or return null.
     */
    public V get(String cacheKey) {

        if (data.containsKey(cacheKey) && isCacheStillValid(cacheKey)) {
            return data.get(cacheKey);
        }

        return null;
    }

    /**
     * Get cached value or initialize it with given callback, cached for cacheForFrames.
     */
    public V get(String cacheKey, int cacheForFrames, Callback callback) {
        if (cacheKey == null) {
            return (V) callback.run();
        }

        if (!data.containsKey(cacheKey) || !isCacheStillValid(cacheKey)) {
            set(cacheKey, cacheForFrames, callback);
        }

        V result = data.get(cacheKey);
        if (result instanceof Selection) {
            return (V) ((Selection) result).clone();
        } else {
            return result;
        }
    }

    public void set(String cacheKey, int cacheForFrames, Callback callback) {
        if (cacheKey == null || cacheKey.length() <= 2) {
            throw new RuntimeException("Invalid cacheKey = /" + cacheKey + "/");
        }

        data.put(cacheKey, (V) callback.run());
        addCachedUntilEntry(cacheKey, cacheForFrames);
    }

    public void set(String cacheKey, int cacheForFrames, V value) {
        data.put(cacheKey, value);
        if (cacheForFrames != -1) {
            addCachedUntilEntry(cacheKey, cacheForFrames);
        }
    }

    public void forget(String cacheKey) {
        data.remove(cacheKey);
        cachedUntil.remove(cacheKey);
    }

    public void clear() {
        data.clear();
        cachedUntil.clear();
    }

    public void print(String message, boolean includeExpired) {
        if (message != null) {
            System.out.println("--- " + message + ":");
        }
        for (String key : data.keySet()) {
            if (includeExpired || isCacheStillValid(key)) {
                System.out.println(key + " - " + data.get(key));
            }
        }
    }

    public boolean isEmpty() {
        return data.isEmpty();
    }

    // =========================================================

    protected boolean isCacheStillValid(String cacheKey) {
        return !cachedUntil.containsKey(cacheKey) || cachedUntil.get(cacheKey) == -1 || cachedUntil.get(cacheKey) >= A.now();
    }

    protected void addCachedUntilEntry(String cacheKey, int cacheForFrames) {
        if (cacheForFrames > -1) {
            cachedUntil.put(cacheKey, A.now() + cacheForFrames);
        } else {
            cachedUntil.remove(cacheKey);
        }
    }

    public int size() {
        return data.size();
    }
}
