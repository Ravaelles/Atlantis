package atlantis.util;

import java.util.TreeMap;

public class Cache<T, R> {

//    private T owner;
    private TreeMap<String, R> data = new TreeMap<>();

    // =========================================================

//    public Cache(T owner) {
//        this.owner = owner;
//    }

    // =========================================================

    /**
     * Get cached value or return null.
     */
    public R get(String cacheKey) {
        if (data.containsKey(cacheKey)) {
            return data.get(cacheKey);
        }

        return null;
    }

    /**
     * Get cached value or initialize it with given callback.
     */
    public R get(String cacheKey, Callback callback) {
        if (data.containsKey(cacheKey)) {
            return data.get(cacheKey);
        }
        else {
            set(cacheKey, callback);
        }

        return data.get(cacheKey);
    }

    public void set(String cacheKey, Callback callback) {
        data.put(cacheKey, (R) callback.run());
    }

    public void forget(R cacheKey) {
        data.remove(cacheKey);
    }
}
