package atlantis.debug.tweaker;

import java.util.concurrent.Callable;

public class Param {

    private String name;
    private Callable getter;
    private Callable setter;

    protected Param(String name, Callable getter, Callable setter) {
        this.name = name;
        this.getter = getter;
        this.setter = setter;
    }

    public String name() {
        return name;
    }

    public Callable getterCallable() {
        return getter;
    }

    public Callable setterCallable() {
        return setter;
    }

    public String getterValue() {
        try {
            return getter.call() + "";
        } catch (Exception e) {
            e.printStackTrace();
            return "ERROR_TRYING_TO_GET_" + name;
        }
    }
}
