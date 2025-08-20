package atlantis.map.bullets;

import atlantis.util.cache.Cache;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public interface ClearsCache {
    default void clearCache() {
        Class<?> clazz = this.getClass();   // Runtime class, not the interface

        for (Field field : clazz.getDeclaredFields()) {
            try {
                // Must be static
                if (!Modifier.isStatic(field.getModifiers())) {
                    continue;
                }

                // Name must start with "cache"
                if (!field.getName().startsWith("cache")) {
                    continue;
                }

                // Must be of type Cache (or subclass)
                if (!Cache.class.isAssignableFrom(field.getType())) {
                    continue;
                }

                field.setAccessible(true);

                // Get static field value: pass null to get()
                Cache cache = (Cache) field.get(null);
                if (cache != null) {
                    cache.clear();
                }

            } catch (Exception e) {
                throw new RuntimeException("Failed to clear cache field: " + field.getName(), e);
            }
        }
    }
}
