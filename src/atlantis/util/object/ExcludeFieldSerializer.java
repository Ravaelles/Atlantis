package atlantis.util.object;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import org.objenesis.strategy.StdInstantiatorStrategy;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;

public class ExcludeFieldSerializer<T> extends Serializer<T> {

    private final String[] fieldsToExclude;  // The field name to exclude
    private final Class<T> clazz;

    public ExcludeFieldSerializer(Class<T> clazz, String fieldToExclude) {
        this.clazz = clazz;
        this.fieldsToExclude = new String[]{fieldToExclude};
    }

    public ExcludeFieldSerializer(Class<T> clazz, String[] fieldsToExclude) {
        this.clazz = clazz;
        this.fieldsToExclude = fieldsToExclude;
    }

    @Override
    public void write(Kryo kryo, Output output, T object) {
        try {
            // Serialize the object fields manually, excluding the specific field
            for (Field field : clazz.getDeclaredFields()) {
                // Skip the field to exclude
//                System.err.println("field.getName() = " + field.getName());

                // Check if string array fieldsToExclude contains field.getName()
                if (Arrays.asList(fieldsToExclude).contains(field.getName())) {
//                    System.err.println("Skipping field " + field.getName());
                    continue;
                }
//                System.err.println("DONT SKIP " + field.getName());

//                if (field.getName().equals(fieldsToExclude)) {
//                    continue;
//                }

                // Make field accessible (even if it's private)
                field.setAccessible(true);

                // Check if the field is static and skip if it's static
                if (Modifier.isStatic(field.getModifiers())) {
                    continue;
                }

                Object value = field.get(object);
                kryo.writeObject(output, value);
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public T read(Kryo kryo, Input input, Class<? extends T> type) {
        try {
            System.err.println("START: " + type.getSimpleName());
//            System.err.println("type.getDeclaredConstructor() = " + type.getDeclaredConstructor());
//            System.err.println(Arrays.stream(type.getConstructors()).findFirst());
//            System.err.println(type.getConstructors()[0]);
//            System.err.println("instance = " + instance);

            T instance = null;

//                T instance = type.getDeclaredConstructor().newInstance();
//                T instance = newInstanceOf(type);
            instance = kryo.getInstantiatorStrategy().newInstantiatorOf(type).newInstance();

            for (Field field : clazz.getDeclaredFields()) {

                if (Arrays.asList(fieldsToExclude).contains(field.getName())) {
                    System.err.println("@ SKIP = " + field.getName());
                    continue;
                }
                System.err.println("field = " + field.getName());

                // Make field accessible (even if it's private)
                field.setAccessible(true);

                // Check if the field is static and skip if it's static
                if (Modifier.isStatic(field.getModifiers())) {
                    continue;
                }

//                T instance = type.getDeclaredConstructor().newInstance();
//                T instance = newInstanceOf(type);
//                instance = kryo.getInstantiatorStrategy().newInstantiatorOf(type).newInstance();

                Object value = kryo.readObject(input, field.getType());
                field.set(instance, value);
            }

            return instance;

//            System.err.println("END - return null for " + type.getSimpleName());
//            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private T newInstanceOf(Class<? extends T> type) {
        return (new StdInstantiatorStrategy()).newInstantiatorOf(type).newInstance();
    }
}