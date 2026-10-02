package atlantis.architecture;

import atlantis.combat.squad.Squad;
import atlantis.architecture.ManagerFactory;
import atlantis.game.A;
import atlantis.units.AUnit;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseManager {
    protected Manager[] submanagerObjects = new Manager[0];

    protected final AUnit unit;
    protected final Squad squad;

    private Manager parent;
    protected List<String> parents = new ArrayList<>(); // Useful for debugging
    protected int parentsLastTimestamp;

    public BaseManager(AUnit unit) {
        this.unit = unit;
        this.squad = (unit != null ? unit.squad() : null);
        parentsLastTimestamp = -1;

        initChildren(managers());
    }

    protected abstract ManagerFactory[] managers();

    /**
     * Stage C: builds child managers from explicit constructor references,
     * without reflection. Order of {@code factories} is the execution order.
     * A failing constructor quits the game, same as the old reflective path.
     */
    protected final void initChildren(ManagerFactory[] factories) {
        Manager[] created = new Manager[factories.length];

        int index = 0;
        for (ManagerFactory factory : factories) {
            try {
                Manager manager = factory.create(unit);
                if (manager == null) {
                    System.err.println("MANAGER INIT null");
                    A.quit();
                }

                created[index++] = manager;
            } catch (Exception e) {
                A.printStackTrace(
                    "Could not instantiate manager / " + e.getMessage()
                        + " / " + "ERROR CLASS: " + e.getClass()
                );
                A.quit();
            }
        }

        submanagerObjects = created;
    }

    // =========================================================

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;

        return o.getClass() == this.getClass();
    }

    @Override
    public int hashCode() {
        // Must be consistent with equals(), which deliberately compares classes only
        // (see AUnit.setManagerUsed). Using unit.id() broke the equals/hashCode
        // contract and would NPE when unit is null.
        return getClass().hashCode();
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

    public boolean print(String message) {
        System.err.println(getClass().getSimpleName() + ": " + message);
        return true;
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
