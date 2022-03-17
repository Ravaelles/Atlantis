package atlantis.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

/**
 * Allows to easily count amount of given key objects. Can be used to e.g. count number of UnitTypes. Minimum
 * value is 0.
 * TODO: check whether removing the need of K extend Comparable has negative side effects
 */
public class MappingCounter<K> {

    private final HashMap<K, Integer> mapping = new HashMap<>();

    // =====================================================================
    public MappingCounter() {
    }

    // =====================================================================
    // Value mapping methods
    public void incrementValueFor(K key) {
        changeValueBy(key, 1);
    }

    public void decrementValueFor(K key) {
        changeValueBy(key, -1);
    }

    public void changeValueBy(K key, int deltaValue) {
        if (mapping.containsKey(key)) {
            mapping.put(key, mapping.get(key) + deltaValue);
        } else {
            mapping.put(key, deltaValue);
        }
    }

    public void setValueFor(K key, int newValue) {
        mapping.put(key, newValue);
    }

    public int getValueFor(K key) {
        if (mapping.containsKey(key)) {
            return mapping.get(key);
        } else {
            return 0;
        }
    }

    // =========================================================
    // Export

    public HashMap<K, Integer> map() {
        HashMap<K, Integer> map = new HashMap<>();
        for (K key : mapping.keySet()) {
            map.put(key, mapping.get(key));
        }
        return map;
    }

    public Set<K> keys() {
        return mapping.keySet();
    }

    public void print(String message) {
        if (message != null) {
            System.out.println("--- " + message + " ---");
        }
        for (K key : mapping.keySet()) {
            System.out.println(key + ": " + mapping.get(key));
        }
    }

    public boolean isEmpty() {
        return mapping.isEmpty();
    }

    public int size() {
        return mapping.size();
    }

    public K first() {
        return (new ArrayList<K>(mapping.keySet())).get(0);
    }
}
