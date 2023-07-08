package atlantis.units.managers;

import atlantis.combat.squad.Squad;
import atlantis.game.A;
import atlantis.units.AUnit;

public abstract class Manager extends ManagerHelpers {

    protected AUnit unit;
    protected Squad squad;

    protected Class<? extends Manager>[] managers = new Class[] {};

    private Manager[] managerInstances;

    // =========================================================

    public Manager(AUnit unit) {
        this.unit = unit;
        this.squad = unit.squad();

        this.managerInstances = initializeManagerInstances();
    }

    // =========================================================

    /**
     * All sub-managers. Order matters.
     */
    protected Class<? extends Manager>[] managers() {
        return managers;
    }

    /**
     * @return True if given manager can be applied to <b>unit</b>. False otherwise. Allows to quickly exit
     * if e.g. given manager is for Protoss and we play as Terran.<br>
     *
     * Notice: there may be multiple other factors why given manager won't be used, think of this as of "authorize"
     * method, where it doesn't make sense at all to trigger this manager e.g.
     * - manager for Vulture behavior and current unit is Marine - in that case we want to return false here
     */
    public boolean applies() {
        return true;
    }

    /**
     * @return TRUE if the manager was applied, an action was taken, meaning further execution should be stopped.
     * FALSE if the manager was not applied. Further execution down the stack should be proceeded.
     */
    public Manager handle() {
        if (!applies()) {
            return null;
        }

        if (handleSubmanagers() != null) {
            return lastManager();
        }

        return null;
    }

    // =========================================================

    public Manager handleSubmanagers() {
        for (Manager manager : managerInstances){
            if (manager.handle() != null) {
                unit.setManagerUsed(manager);
                return manager;
            }
        }

        return null;
    }

    private Manager[] initializeManagerInstances() {
        managers = managers();

        managerInstances = new Manager[managers.length];

        int index = 0;
        for (Class<? extends Manager> classObject : managers){
            Manager manager = instantiateManager(classObject);

            managerInstances[index++] = manager;
        }

        return null;
    }

    private Manager instantiateManager(Class<? extends Manager> classObject) {
        try {
            return classObject.getDeclaredConstructor().newInstance(unit);
        } catch (Exception e) {
            A.printStackTrace("Could not instantiate " + classObject + " / " + e.getMessage());
            return null;
        }
    }

    // =========================================================

    /**
     * Returns the last manager used by the unit.
     */
    public Manager lastManager() {
        return unit.manager();
    }

    /**
     * Indicates this Manager was just used by the unit.
     */
    public Manager usedManager(Manager manager) {
        return usedManager(manager, null);
    }

    public Manager fallbackToUseManager(Class<? extends Manager> classObject) {
        Manager manager = instantiateManager(classObject);

        return manager.handle();
    }

    /**
     * Indicates this Manager was just used by the unit.
     */
    public Manager usedManager(Manager manager, String message) {
        if (message != null && !message.equals("")) {
            unit.setManagerUsed(manager, message);
        } else {
            unit.setManagerUsed(manager);
        }
        return manager;
    }

    /**
     * Auxiliary method indicating that the same Manager should not interrupt the actions from previous frames.
     */
    public Manager continueUsingManager() {
        return unit.manager();
    }

    // =========================================================

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }

        System.out.println(o.getClass() + " /// " + this.getClass());
        return o.getClass() == this.getClass();
    }

    @Override
    public int hashCode() {
        return (unit.id() + "," + getClass()).hashCode();
    }
}
