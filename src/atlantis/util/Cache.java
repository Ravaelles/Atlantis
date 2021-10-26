package atlantis.util;

import java.util.TreeMap;

public class Cache<V> {

    protected final TreeMap<String, V> data = new TreeMap<>();
    protected final TreeMap<String, Integer> cachedUntil = new TreeMap<>();

    // =========================================================

    /**
     * Get cached value or return null.
     */
    public V get(String cacheKey) {
//        for (String key : data.keySet()) {
//            System.out.println(key + " - " + data.get(key));
//        }

        if (data.containsKey(cacheKey) && isCacheStillValid(cacheKey)) {
            return data.get(cacheKey);
        }

        return null;
    }

    /**
     * Get cached value or initialize it with given callback, cached for cacheForFrames.
     */
    public V get(String cacheKey, int cacheForFrames, Callback callback) {
        if (data.containsKey(cacheKey) && isCacheStillValid(cacheKey)) {
            return data.get(cacheKey);
        }
        else if (callback != null) {
            set(cacheKey, cacheForFrames, callback);
        }
//        set(cacheKey, cacheForFrames, callback);

        return data.get(cacheKey);
    }

    public void set(String cacheKey, int cacheForFrames, Callback callback) {
        data.put(cacheKey, (V) callback.run());
        addCachedUntilEntry(cacheKey, cacheForFrames);
    }

    public void set(String cacheKey, int cacheForFrames, V value) {
        data.put(cacheKey, value);
        addCachedUntilEntry(cacheKey, cacheForFrames);
    }

    public void forget(String cacheKey) {
        data.remove(cacheKey);
        cachedUntil.remove(cacheKey);
    }

    public void forgetAll() {
        data.clear();
        cachedUntil.clear();
    }

    public void print(String message, boolean includeExpired) {
        System.out.println("--- " + message + ":");
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
        return !cachedUntil.containsKey(cacheKey) || cachedUntil.get(cacheKey) >= A.now();
    }

    protected void addCachedUntilEntry(String cacheKey, int cacheForFrames) {
        if (cacheForFrames > -1) {
            cachedUntil.put(cacheKey, A.now() + cacheForFrames);
        } else {
            cachedUntil.remove(cacheKey);
        }
    }
}
