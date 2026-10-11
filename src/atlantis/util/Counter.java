package atlantis.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

/**
 * Allows to easily count amount of given key objects. Can be used to e.g. count number of UnitTypes. Minimum
 * value is 0.
 * TODO: check whether removing the need of K extend Comparable has negative side effects
 */
public class Counter<K> {
    private final HashMap<K, Integer> mapping = new HashMap<>();

    // =====================================================================

    public Counter() {
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
        }
        else {
            mapping.put(key, deltaValue);
        }
    }

    public void setValueFor(K key, int newValue) {
        mapping.put(key, newValue);
    }

    public int getValueFor(K key) {
        if (mapping.containsKey(key)) {
            return mapping.get(key);
        }
        else {
            return 0;
        }
    }

    // =========================================================
    // Export

    /**
     * A COPY of the counts, safe to read and to iterate.
     *
     * <p>
     * <b>Mutating the result does not affect this Counter.</b> Use {@link #clear()} to reset and
     * {@link #changeValueBy} to adjust - and note that a test asserting on a counter's contents
     * has to read the values through {@link #getValueFor}, not by holding on to this map.
     * </p>
     */
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

    /**
     * Forget every count.
     *
     * <p>
     * <b>Call this, never {@code map().clear()}.</b> {@code map()} returns a COPY - it builds a
     * new map and fills it - so clearing the result clears the copy and leaves this object's
     * counts exactly as they were. That is silent: the call compiles, runs, throws nothing, and
     * simply does nothing.
     * </p>
     *
     * <p>
     * It was live in {@code UnitsArchive.reset()}, which cleared six counters this way while
     * being documented as the thing that stops one game's kills and losses leaking into the
     * next. Found by a test that expected one lost Zealot and read four, with three left over
     * from earlier tests in the same JVM.
     * </p>
     */
    public void clear() {
        mapping.clear();
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

    public K keyWithHighestValue() {
        K maxKey = null;
        int maxValue = Integer.MIN_VALUE;

        for (K key : mapping.keySet()) {
            int value = mapping.get(key);
            if (value > maxValue) {
                maxValue = value;
                maxKey = key;
            }
        }

        return maxKey;
    }
}
