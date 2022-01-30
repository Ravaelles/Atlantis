package atlantis.production.orders.build;

public class BuildOrderSetting {

    private String name;
    private Object value;

    // =========================================================

    public BuildOrderSetting(String name, Object value) {
        this.name = name;
        this.value = value;
    }

    // =========================================================

    public String name() {
        return name;
    }

    public Object value() {
        return value;
    }

    public int valueInt() {
        return (int) value;
    }

    public boolean valueBoolean() {
        return (boolean) value;
    }

}
