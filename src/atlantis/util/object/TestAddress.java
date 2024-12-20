package atlantis.util.object;

public class TestAddress {
    private final String street;
    final int number;
    private TestObjectClass testObjectClass;

    //    private TestAddress(String street, int number) {
    public TestAddress(String street, int number) {
        this.street = street;
        this.number = number;

        testObjectClass = new TestObjectClass();
        testObjectClass.value1 = 666;
    }

    public String getStreet() {
        return street;
    }

    public int getNumber() {
        return number;
    }

    public TestObjectClass getTestObjectClass() {
        return testObjectClass;
    }
}
