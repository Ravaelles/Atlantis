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


    public R get(String cacheName, Callback callback) {
        if (data.containsKey(cacheName)) {
            return data.get(cacheName);
        }
        else {
            data.put(cacheName, (R) callback.run());
        }

        return data.get(cacheName);
    }

}
