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


    public R get(String cacheKey, Callback callback) {
        if (data.containsKey(cacheKey)) {
            return data.get(cacheKey);
        }
        else {
            data.put(cacheKey, (R) callback.run());
        }

        return data.get(cacheKey);
    }

    public void forget(R cacheKey) {
        data.remove(cacheKey);
    }
}
