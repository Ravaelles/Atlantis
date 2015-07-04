package atlantis.wrappers;

import java.util.HashMap;

/**
 * Allows to easily count amount of given key objects. Can be used to e.g. count number of UnitTypes. Minimum value is
 * 0.
 */
public class MappingCounter<K extends Comparable<K>> {

	private HashMap<K, Integer> mapping = new HashMap<>();

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
			mapping.put(key, Math.max(0, mapping.get(key) + deltaValue));
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

}
