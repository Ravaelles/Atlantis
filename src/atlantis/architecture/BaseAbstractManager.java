package atlantis.architecture;

import atlantis.combat.squad.Squad;
import atlantis.game.A;
import atlantis.units.AUnit;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.stream.Stream;

public abstract class BaseAbstractManager {
    protected Manager[] submanagerObjects;

    protected final AUnit unit;
    protected final Squad squad;

    public BaseAbstractManager(AUnit unit) {
        this.unit = unit;
        this.squad = unit.squad();

        initializeManagerInstances();
    }

    protected abstract Class<? extends Manager>[] managers();

    protected void initializeManagerInstances() {
        Class<? extends Manager>[] managers = managers();
        submanagerObjects = new Manager[managers.length];

        int index = 0;
        for (Class<? extends Manager> classObject : managers) {
            Manager manager = instantiateManager(classObject);

            submanagerObjects[index++] = manager;
        }
    }

    public Manager instantiateManager(Class<? extends Manager> classObject) {
        try {
//            System.out.println(classObject.getDeclaredConstructor(AUnit.class));
            return classObject.getDeclaredConstructor(AUnit.class).newInstance(unit);
        } catch (InvocationTargetException e) {
            System.err.println("There was an error in constructor of:\n");
            System.err.println(classObject);
            e.printStackTrace();
            return null;
        } catch (Exception e) {
            A.printStackTrace(
                "Could not instantiate " + classObject
                    + " / " + e.getMessage()
                    + " / " + "ERROR CLASS: " + e.getClass()
            );
            A.quit();
            return null;
        }
    }

    protected static Class[] mergeManagers(Class[] raceSpecific, Class[] generic) {
        return Stream.concat(Arrays.stream(raceSpecific), Arrays.stream(generic)).toArray(Class[]::new);
    }

    // =========================================================

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }

        return o.getClass() == this.getClass();
    }

    @Override
    public int hashCode() {
        return (unit.id() + "," + getClass()).hashCode();
    }

    public String toString() {
        String name = getClass().getSimpleName();
        return name.substring(0, Math.min(20, name.length()));
    }
}
