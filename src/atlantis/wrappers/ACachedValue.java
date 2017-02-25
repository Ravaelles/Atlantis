package atlantis.wrappers;

import atlantis.AGame;
import bwapi.Unit;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Rafal Poniatowski <ravaelles@gmail.com>
 */
public class ACachedValue<T> {

    private int cacheLifespanInFrames;

    private Map<String, T> cachedValues = new HashMap<>();
    private Map<String, Integer> cachedAtFrame = new HashMap<>();

    // =========================================================
    
    public ACachedValue() {
        this.cacheLifespanInFrames = 1;
    }

    public ACachedValue(int cacheLifespanInFrames) {
        this.cacheLifespanInFrames = cacheLifespanInFrames;
    }

    // =========================================================
    
//    public T getValueForGivenKeysOrRefresh(int objectOneId, int objectTwoId) {
//        String key = createCacheKeyFrom(objectOneId, objectTwoId);
//        T value = containsAliveCachedValue(key);
//        
//        // Previous key has been found
//        if (value != null) {
//            return value;
//        } 
//
//        // No previous value found, insert new one
//        else {
//            value = insertNewValueForKey(key);
//        }
//    }
    
    public double getDistanceBetweenUnits(Unit unit1, Unit unit2) {
        int objectOneId = unit1.getID();
        int objectTwoId = unit2.getID();
        
        String key = createCacheKeyFrom(objectOneId, objectTwoId);
        Double value = (Double) getAliveCachedValue(key);
        
        // Previous key has been found
        if (value != null) {
            return value;
        } 

        // No previous value found, insert new one
        else {
            Double dist = (double) unit1.getDistance(unit2) / 32;
            return insertDoubleValueForKey(key, dist);
        }
    }

    // =========================================================
    
    private String createCacheKeyFrom(int objectOneId, int objectTwoId) {
        int smaller;
        int bigger;
        if (objectOneId < objectTwoId) {
            smaller = objectOneId;
            bigger = objectTwoId;
        } else {
            bigger = objectOneId;
            smaller = objectTwoId;
        }

        return smaller + ";" + bigger;
    }

    /**
     * @return T cached value if we've previously stored it for the given key and only it's not older than
     * allowed maximum time for the value to live.
     * @return null otherwise
     */
    public T getAliveCachedValue(String key) {
        if (cachedValues.containsKey(key)
                && (cachedAtFrame.get(key) + cacheLifespanInFrames >= AGame.getTimeFrames())) {
            return cachedValues.get(key);
        } else {
            return null;
        }
    }

    private Double insertDoubleValueForKey(String key, Double value) {
        cachedValues.put(key, (T) value);
        cachedAtFrame.put(key, AGame.getTimeFrames());
//        System.out.println("Cache between " + key + " at " + AGame.getTimeFrames() + " is " + value);
        return value;
    }

    private T insertNewValueForKey(String key, T value) {
        cachedValues.put(key, value);
        cachedAtFrame.put(key, AGame.getTimeFrames());
        return value;
    }

}
