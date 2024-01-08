package atlantis.util;

import atlantis.information.decisions.terran.TerranDecisions;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class ClassUtil {
    public static void changeStaticFieldsInClassTo(Class className, boolean newValue) {
//        Class<?> clazz = TerranDecisions.class;
        Field[] fields = className.getDeclaredFields();

        for (Field field : fields) {
            if (Modifier.isStatic(field.getModifiers())) {
                try {
                    field.set(null, false);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
