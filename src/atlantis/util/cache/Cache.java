package atlantis.util.cache;

import atlantis.config.env.Env;
import atlantis.util.Callback;
import atlantis.util.GameClock;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeMap;

/**
 * T is type of objects stored e.g. Booleans or generic Object (which can be then cast in methods).
 */
public class Cache<T> {
    private static ArrayList<Cache<?>> allInstances = new ArrayList<>();

    protected final TreeMap<String, T> data = new TreeMap<>();
    protected final TreeMap<String, Integer> cachedUntil = new TreeMap<>();

    // =========================================================

    public Cache() {
        if (Env.isTesting()) allInstances.add(this);
    }

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
     *
     * <p>A cached value that implements {@link Copyable} is handed out as a copy, so
     * a caller that filters or sorts what it got does not rewrite the cached entry for
     * the next caller. Everything else is returned as stored.</p>
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
        if (result instanceof Copyable) {
            return (T) ((Copyable<T>) result).copy();
        }
        else {
            return result;
        }
    }

    /**
     * Like {@link #get(String, int, Callback)}, but a cached {@code AUnit} or
     * {@code AFocusPoint} that already died (became invalid) is treated as a
     * miss: it is dropped and recomputed via the callback. Other value types
     * pass through untouched.
     */
    public T getIfValid(String cacheKey, int cacheForFrames, Callback callback) {
        T value = get(cacheKey, cacheForFrames, callback);
        if (value != null && isKnownInvalid(value)) {
            forget(cacheKey);
            value = get(cacheKey, cacheForFrames, callback);
        }

        return value;
    }

    private static boolean isKnownInvalid(Object value) {
        return value instanceof ValidityCheck && !((ValidityCheck) value).isValid();
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
                || cachedUntil.get(cacheKey) >= GameClock.frames()
        );
    }

    protected void addCachedUntilEntry(String cacheKey, int cacheForFrames) {
        if (cacheForFrames > -1) {
            cachedUntil.put(cacheKey, GameClock.frames() + cacheForFrames);
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

    public static void nukeAllCaches() {
//        System.out.println("Nuking all caches...");
        for (Cache<?> cache : allInstances) {
            cache.clear();
        }
        // The registry is deliberately NOT emptied. Cache instances are static
        // fields and register themselves once, in their constructor - so dropping
        // them here meant that every nuke after the first one in a JVM cleared
        // nothing at all, and stale selections, enemy lists and safety margins
        // survived from one test into the next. That is what made tests that pass
        // alone fail inside a package run.
    }
}
