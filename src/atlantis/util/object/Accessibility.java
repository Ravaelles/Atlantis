package atlantis.util.object;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class Accessibility {
    /**
     * Sets all fields and methods of the given class as accessible.
     *
     * @param clazz The class whose fields and methods should be made accessible.
     */
    public static void makeAllAccessible(Class<?> clazz) {
        // Make all fields accessible
        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);
//            System.err.println("field = " + field);
//            System.err.println(field.isAccessible());
        }

        // Make all methods accessible
        for (Method method : clazz.getDeclaredMethods()) {
            method.setAccessible(true);
        }
    }
}
