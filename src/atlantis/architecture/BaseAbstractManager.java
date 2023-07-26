package atlantis.architecture;

import atlantis.game.A;
import atlantis.units.AUnit;

import java.util.Arrays;
import java.util.stream.Stream;

public abstract class BaseAbstractManager {
    protected AUnit unit;
    protected Manager[] managerInstances;

    protected abstract Class<? extends Manager>[] managers();

    protected Manager[] initializeManagerInstances() {
        Class<? extends Manager>[] managers = managers();

        managerInstances = new Manager[managers.length];

        int index = 0;
        for (Class<? extends Manager> classObject : managers) {
            Manager manager = instantiateManager(classObject);

            managerInstances[index++] = manager;
        }

        return null;
    }

    protected Manager instantiateManager(Class<? extends Manager> classObject) {
        try {
            return classObject.getDeclaredConstructor().newInstance(unit);
        } catch (Exception e) {
            A.printStackTrace("Could not instantiate " + classObject + " / " + e.getMessage());
            return null;
        }
    }

    protected static Class<? extends Manager>[] mergeManagers(Class[] raceSpecific, Class[] generic) {
        return (Class<? extends Manager>[]) Stream.concat(
            Arrays.stream(raceSpecific), Arrays.stream(generic)
        ).toArray();
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
}
