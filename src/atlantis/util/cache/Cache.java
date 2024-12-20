package atlantis.util.cache;

import atlantis.combat.advance.focus.AFocusPoint;
import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.units.fogged.FoggedUnit;
import atlantis.units.select.Selection;
import atlantis.util.Callback;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeMap;

/**
 * T is type of objects stored e.g. Booleans or generic Object (which can be then cast in methods).
 */
public class Cache<T> {

    protected final TreeMap<String, T> data = new TreeMap<>();
    protected final TreeMap<String, Integer> cachedUntil = new TreeMap<>();

    // =========================================================

    /**
     * Get cached value or return null.
     */
    public T get(String cacheKey) {
        if (cacheKey != null && data.containsKey(cacheKey) && isCacheStillValid(cacheKey)) {
            return data.get(cacheKey);
        }

        return null;
    }

    /**
     * Get cached value or return null.
     */
    public boolean has(String cacheKey) {
        return cacheKey != null && data.containsKey(cacheKey) && isCacheStillValid(cacheKey);
    }

    /**
     * Get cached value or initialize it with given callback, cached for cacheForFrames.
     */
    public T get(String cacheKey, int cacheForFrames, Callback callback) {
//        if (cacheKey == "completedOrders")
//            System.err.println("cacheKey = " + cacheKey + " / data_size = " + data.size());

        if (cacheKey == null) {
            return (T) callback.run();
        }

        if (!data.containsKey(cacheKey) || !isCacheStillValid(cacheKey)) {
//            if (cacheKey == "completedOrders") System.err.println("SET cacheKey = " + cacheKey);
            set(cacheKey, cacheForFrames, callback);
        }

        T result = data.get(cacheKey);
        if (result instanceof Selection) {
            return (T) ((Selection) result).clone();
        }
        else {
            return result;
        }
    }

    public T getIfValid(String cacheKey, int cacheForFrames, Callback callback) {
        T value = get(cacheKey, cacheForFrames, callback);
        if (value != null) {
            if (value instanceof AFocusPoint) {
                if (((AFocusPoint) value).isValid()) {

                    return value;
                }
            }
            if (value instanceof AUnit) {
                if (((AUnit) value).isValid()) {
                    return value;
                }
            }
        }

        @SuppressWarnings("unchecked") T newValue = (T) callback.run();
        set(cacheKey, cacheForFrames, newValue);

//        if (newValue != null) {
//            if (newValue instanceof AFocusPoint) {
//                if (((AFocusPoint) value).isValid()) {
//                    return value;
//                }
//            }
//            if (newValue instanceof FoggedUnit) {
//                if (((FoggedUnit) value).isValid()) {
//                    return value;
//                }
//            }
//            if (newValue instanceof AUnit) {
//                if (((AUnit) value).isValid()) {
//                    return value;
//                }
//            }
//        }

        return newValue;

//        set(cacheKey, cacheForFrames, callback);
//        return get(cacheKey, cacheForFrames, callback);

//        return (V) callback.run();
    }

    public List<T> allValid() {
        List<T> valid = new ArrayList<>();
        for (String key : data.keySet()) {
            if (isCacheStillValid(key)) {
                valid.add(data.get(key));
            }
        }
        return valid;
    }

    public T set(String cacheKey, int cacheForFrames, Callback callback) {
        if (cacheKey == null || cacheKey.length() <= 1) {
            throw new RuntimeException("Invalid cacheKey = /" + cacheKey + "/");
        }

        T value = (T) callback.run();
        data.put(cacheKey, value);
        addCachedUntilEntry(cacheKey, cacheForFrames);

        return value;
    }

    public void set(String cacheKey, int cacheForFrames, T value) {
        if (cacheKey == null) {
            return;
        }

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

    public boolean isEmpty() {
        return data.isEmpty();
    }

    // =========================================================

    protected boolean isCacheStillValid(String cacheKey) {
        return cacheKey != null && (
            !cachedUntil.containsKey(cacheKey)
                || cachedUntil.get(cacheKey) == -1
                || cachedUntil.get(cacheKey) >= A.now()
        );
    }

    protected void addCachedUntilEntry(String cacheKey, int cacheForFrames) {
        if (cacheForFrames > -1) {
            cachedUntil.put(cacheKey, A.now() + cacheForFrames);
        }
        else {
            cachedUntil.remove(cacheKey);
        }
    }

    // =========================================================

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

    public void printKeys() {
        System.out.println("--- Cache keys ---");
        for (String key : data.keySet()) {
            System.out.println(key);
        }
    }

    // =========================================================

    public Collection<T> values() {
        return data.values();
    }

    public int size() {
        return data.size();
    }

    public TreeMap<String, T> rawCacheData() {
        return data;
    }

    public TreeMap<String, Integer> rawCachedUntil() {
        return cachedUntil;
    }
}
