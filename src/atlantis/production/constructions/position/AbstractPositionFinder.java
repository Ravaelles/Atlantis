package atlantis.production.constructions.position;

import atlantis.map.position.APosition;
import atlantis.util.cache.Cache;

public abstract class AbstractPositionFinder {
    public static String _STATUS = "Init";
    //    public static boolean DEBUG = true;
    public static boolean DEBUG = false;

    protected static Cache<APosition> cache = new Cache<>();

    public static void clearCache() {
        cache.clear();
    }
}
