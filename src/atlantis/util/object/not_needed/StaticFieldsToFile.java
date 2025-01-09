package atlantis.util.object.not_needed;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

public class StaticFieldsToFile {

    // Save static fields of a class to a file
    public static void saveToFile(Class<?> classObject) {
        String filename = fileFromClass(classObject);

        Kryo kryo = new Kryo();
        kryo.setRegistrationRequired(false);  // Allow non-registered classes

        try (Output output = new Output(new FileOutputStream(filename))) {
            // Map to hold static field names and values
            Map<String, Object> staticFieldValues = new HashMap<>();

            // Loop through all declared fields in the class
            for (Field field : classObject.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers())) {  // Check if it's a static field
                    field.setAccessible(true);  // Make the field accessible
                    Object value = field.get(null);  // Get the value of the static field
                    staticFieldValues.put(field.getName(), value);  // Add to the map
                }
            }

            // Write the map of static fields to the file
            kryo.writeObject(output, staticFieldValues);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error saving static fields for class: " + classObject.getName(), e);
        }
    }

    // Load static fields of a class from a file
    public static void loadFromFile(Class<?> classObject) {
        String filename = fileFromClass(classObject);

        Kryo kryo = new Kryo();
        kryo.setRegistrationRequired(false);  // Allow non-registered classes

        try (Input input = new Input(new FileInputStream(filename))) {
            // Read the map of static field values from the file
            @SuppressWarnings("unchecked")
            Map<String, Object> staticFieldValues = kryo.readObject(input, HashMap.class);

            if (staticFieldValues != null) {
                // Loop through all declared fields in the class
                for (Field field : classObject.getDeclaredFields()) {
                    if (Modifier.isStatic(field.getModifiers())) {  // Check if it's a static field
                        field.setAccessible(true);  // Make the field accessible
                        if (staticFieldValues.containsKey(field.getName())) {  // Check if field is in the map
                            field.set(null, staticFieldValues.get(field.getName()));  // Set the static field value
                        }
                    }
                }
            }
            else {
                System.out.println("No static field values found in the file.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error loading static fields for class: " + classObject.getName(), e);
        }
    }

    private static String fileFromClass(Class<?> classObject) {
        return SerializedObjects.filePath(classObject.getSimpleName() + "-static.ser");
    }
}