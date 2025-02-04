package atlantis.architecture;

import atlantis.combat.squad.Squad;
import atlantis.game.A;
import atlantis.units.AUnit;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public abstract class BaseManager {
    protected Manager[] submanagerObjects;

    protected final AUnit unit;
    protected final Squad squad;

    private Manager parent;
    protected List<String> parents = new ArrayList<>(); // Useful for debugging
    protected int parentsLastTimestamp;

    public BaseManager(AUnit unit) {
        this.unit = unit;
        this.squad = (unit != null ? unit.squad() : null);
        parentsLastTimestamp = -1;

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
            return classObject.getDeclaredConstructor(AUnit.class).newInstance(unit);
        } catch (InvocationTargetException e) {
            System.err.println("There was an error in constructor of:\n");
            System.err.println(classObject);
            System.err.println(e.getMessage());
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
        if (o == null) return false;

        return o.getClass() == this.getClass();
    }

    @Override
    public int hashCode() {
        return (unit.id() + "," + getClass()).hashCode();
    }

    @Override
    public String toString() {
        String name = getClass().getSimpleName();
        if (name.startsWith("Terran")) name = name.replace("Terran", "");
        return A.substring(name, 0, 30);
    }

//    public Manager parents() {
//        return parents.get(parents.size() - 1);
//    }

    public String parentsStack() {
        // Convert parents to string
        StringBuilder sb = new StringBuilder();
        for (String parent : parents) {
            sb.append(parent).append(" > ");
        }
        return sb.toString();
    }

    protected boolean hasSubmanagers() {
        return submanagerObjects.length > 0;
    }

    public Manager getParent() {
        return parent;
    }

    protected void setParent(Manager parent) {
        this.parent = parent;
    }
}
