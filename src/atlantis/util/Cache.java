package atlantis.util;

import java.util.TreeMap;

public class Cache<V> {

    protected final TreeMap<String, V> data = new TreeMap<>();
    protected final TreeMap<String, Integer> cachedUntil = new TreeMap<>();

    // =========================================================

//    public Cache(T owner) {
//        this.owner = owner;
//    }

    // =========================================================

    /**
     * Get cached value or return null.
     */
    public V get(String cacheKey) {
        if (data.containsKey(cacheKey)) {
            return data.get(cacheKey);
        }

        return null;
    }

    /**
     * Get cached value or initialize it with given callback.
     */
    public V get(String cacheKey, Callback callback) {
        if (data.containsKey(cacheKey)) {
            return data.get(cacheKey);
        }
        else {
            set(cacheKey, callback);
        }

        return data.get(cacheKey);
    }

    /**
     * Get cached value or initialize it with given callback, cached for cacheForFrames.
     */
    public V get(String cacheKey, int cacheForFrames, Callback callback) {
        if (data.containsKey(cacheKey) && isCacheStillValid(cacheKey)) {
            return data.get(cacheKey);
        }
        else {
            set(cacheKey, cacheForFrames, callback);
        }

        return data.get(cacheKey);
    }

    public void set(String cacheKey, Callback callback) {
        set(cacheKey, -1, callback);
    }

    public void set(String cacheKey, int cacheForFrames, Callback callback) {
        data.put(cacheKey, (V) callback.run());
        if (cacheForFrames > -1) {
            cachedUntil.put(cacheKey, A.now() + cacheForFrames);
        } else {
            cachedUntil.remove(cacheKey);
        }
    }

    public void forget(V cacheKey) {
        data.remove(cacheKey);
    }

    // =========================================================

    protected boolean isCacheStillValid(String cacheKey) {
        return cachedUntil.containsKey(cacheKey) && cachedUntil.get(cacheKey) >= A.now();
    }
}
