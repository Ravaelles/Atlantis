package atlantis.util.object;

import atlantis.units.AUnit;
import atlantis.units.select.Selection;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.util.DefaultInstantiatorStrategy;
import org.objenesis.strategy.StdInstantiatorStrategy;

import java.io.*;
import java.util.Collection;

public class ObjectToFile {
    private static Kryo setupKryo(Class<?> clazz) {
        Kryo kryo = new Kryo();

        kryo.setReferences(true);
        kryo.setRegistrationRequired(false);
        kryo.setInstantiatorStrategy(new DefaultInstantiatorStrategy(new StdInstantiatorStrategy()));

        BwapiAccessibility.makeAllBwapiClassesAndFieldsAccessible(clazz, kryo);

//        kryo.register(ArrayList.class);

//        kryo.register(Game.class, new ExcludeFieldSerializer<>(Game.class, "visibleUnits"));
//        kryo.register(Game.class, new ExcludeFieldSerializer<>(
//            Game.class,
//            new String[]{"visibleUnits", "allUnits", "clientData", "staticMinerals", "staticGeysers", "staticNeutralUnits"}
//        ));
//        kryo.register(bwem.Asserter.class);
//        kryo.register(BWEM.class, new ExcludeFieldSerializer<>(
//            BWEM.class,
////            new String[]{}
//            new String[]{"asserter"}
////            new String[]{"map"}
////            new String[]{"map", "asserter"}
//        ));
//        kryo.register(BWMap.class, new ExcludeFieldSerializer<>(
//            BWMap.class,
//            new String[]{"game", "allUnits"}
//        ));
//        kryo.register(Game.class, new ExcludeFieldSerializer<>(Game.class, "game"));
//        kryo.register(BWMap.class, new ExcludeFieldSerializer<>(Game.class, "game"));
//        kryo.register(BWMap.class, new ExcludeFieldSerializer<>(BWMap.class, "game"));

//        kryo.register(Selection.class, new Serializer<Selection>() {
//            @Override
//            public void write(Kryo kryo, Output output, Selection selection) {
//                kryo.writeObject(output, selection.dat()); // Adjust as needed
//            }
//
//            @Override
//            public Selection read(Kryo kryo, Input input, Class type) {
//                Collection<? extends AUnit> data = kryo.readObject(input, Collection.class); // Replace DataType
//                return new Selection(data); // Adjust constructor/factory
//            }
//        });

        return kryo;
    }

    // Save the entire object to a file
    public static void saveToFile(Object obj) {
        Class<?> clazz = obj.getClass();

        String fileName = fileFromClass(clazz);
//        System.err.println("SAVING file: " + fileName + " / " + obj.toString());
        Kryo kryo = setupKryo(clazz);

        try (Output output = new Output(new FileOutputStream(fileName))) {
            kryo.writeObject(output, obj);  // Serialize the object to the file
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error saving object to file: " + fileName, e);
        }
    }

    // Load the object from a file, returning the object as the specified class type
    public static Object loadFromFile(Class<?> loadAsClass) {
        return loadFromFile(loadAsClass, fileFromClass(loadAsClass));
    }

    public static Object loadFromFile(Class<?> loadAsClass, String fileName) {
        Kryo kryo = setupKryo(loadAsClass);

        try (Input input = new Input(new FileInputStream(fileName))) {
            // Deserialize the object from the file
            Object obj = kryo.readObject(input, loadAsClass);

            // Return the deserialized object
            return obj;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error loading object from file: " + fileName, e);
        }
    }

    private static String fileFromClass(Class<?> classObject) {
        return SerializedObjects.filePath(classObject.getCanonicalName() + ".ser");
    }
}