package atlantis.util.object;

import atlantis.map.position.APosition;
import atlantis.map.position.Positions;
import atlantis.util.object.not_needed.StaticFieldsToFile;
import org.junit.jupiter.api.Test;
import tests.unit.UnitTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ObjectToFileTest extends UnitTest {
    @Test
    public void testItRestoresStaticPropertiesOfASimpleTestClass() {
//        // Set some values for the static fields
//        TestObjectClass.staticField1 = "Hello, Kryo!";
//        TestObjectClass.staticField2 = 42;
//
//        // Save static fields to a file
//        StaticFieldsToFile.saveToFile(TestObjectClass.class);

        // Load static fields from the file
        StaticFieldsToFile.loadFromFile(TestObjectClass.class);

        // Print the loaded static fields
        assertEquals("Hello, Kryo!", TestObjectClass.staticField1);
        assertEquals(42, TestObjectClass.staticField2);
    }

    @Test
    public void testItRestoresObjectWithoutDefaultConstructorAndWithPrivateProperties() {
//        TestAddress address = new TestAddress("Main Street", 42);
//        ObjectToFile.saveToFile(address);

        TestAddress restoredAddress = (TestAddress) ObjectToFile.loadFromFile(TestAddress.class);

        assertNotNull(restoredAddress);
        assertEquals("Main Street", restoredAddress.getStreet());
        assertEquals(42, restoredAddress.getNumber());
        assertEquals(666, restoredAddress.getTestObjectClass().getValue1());
    }

//    @Test
//    public void testItRestoresList() {
////        List<String> list = new ArrayList<>();
////        list.add("Hello");
////        list.add("World");
////        ObjectToFile.saveToFile(list);
//
////        for files with serialization data, use names that are prefixed with object and field name
//
//        Class<? extends List> clazz = ArrayList.class;
////        System.err.println("list.getClass() = " + clazz);
//        List<String> restored = (List<String>) ObjectToFile.loadFromFile(clazz);
//
//        assertNotNull(restored);
//        assertEquals(2, restored.size());
//    }

    @Test
    public void testItRestoresPositionsObject() {
        createPositionsObject(); // Uncomment to create new file

        Object o = ObjectToFile.loadFromFile(Positions.class);
        Positions<APosition> positions = (Positions<APosition>) o;

        assertNotNull(positions);
        assertEquals(2, positions.size());
        assertEquals(2, positions.first().tx());
        assertEquals(3, positions.first().ty());
    }

    private static void createPositionsObject() {
        Positions<APosition> positions = new Positions<>();
        positions.addPosition(APosition.create(2, 3));
        positions.addPosition(APosition.create(4, 5));

        ObjectToFile.saveToFile(positions);
    }

//    @Test
//    public void testItRestoresAllChokes() {
//        Object o = ObjectToFile.loadFromFile(ArrayList.class);
//        List<AChoke> chokes = (List<AChoke>) o;
//
//        assertNotNull(chokes);
//
//        System.out.println(chokes);
//        System.out.println(chokes.size());
//    }
//
//    @Test
//    public void testItRestoresUnitsSelection() {
//        Object o = ObjectToFile.loadFromFile(Selection.class);
//        Selection Selection = (Selection) o;
//
//        assertNotNull(Selection);
//
//        System.out.println(Selection);
//    }

//    @Test
//    public void testItRestoresBWEM() {
////        BWEM bwem = (BWEM) ObjectToFile.loadFromFile(BWEM.class, SerializedObjects.filePath("bwem.BWEM.ser"));
//        BWEM bwem = (BWEM) ObjectToFile.loadFromFile(BWEM.class);
//
//        System.out.println("bwem = " + bwem);
//        System.out.println(bwem.map);
//        System.out.println(bwem.asserter);
//
//        AMap.setBWEM(bwem);
//
////        AMap.initMapAnalysis();
//        System.out.println(bwem.map);
//    }

//    @Test
//    public void testRestoringRegions() {
////         Object obj = new Object();
////         ObjectToFile.saveObjectToFile(obj, "files/serialized/test.ser");
////        Object regionsObject = ObjectToFile.loadObjectFromFile(SerializedObjects.REGIONS, Regions.class);
//
//        Regions regions = (Regions) ObjectToFile.loadFromFile(Regions.class);
//
//        System.err.println("regions = " + Regions.regions().size());
////        System.err.println("regions = " + regions.);
//
////        System.out.println(regionsObject);
////
////        Regions.regions();
////
////        System.out.println("");
//    }
}
