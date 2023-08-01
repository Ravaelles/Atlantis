package atlantis.architecture;

import atlantis.units.AUnit;

/**
 * Unit manager. Can contain submanagers (see managers() method).
 * <p>
 * If a manager return non-null value in handle(), it will prevent execution of
 * other managers in this frame.
 */
public abstract class Manager extends BaseAbstractManager {
    public Manager(AUnit unit) {
        super(unit);
    }

    // =========================================================

    /**
     * All sub-managers. Order matters.
     */
    protected Class<? extends Manager>[] managers() {
        return new Class[]{};
    }

    /**
     * @return True if given manager can be applied to <b>unit</b>. False otherwise. Allows to quickly exit
     * if e.g. given manager is for Protoss and we play as Terran.<br>
     * <p>
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

        Manager submanager = handleSubmanagers();

        return submanager;
    }

    // =========================================================

    public Manager handleSubmanagers() {
        for (Manager submanager : submanagerObjects) {
            if (submanager.applies() && submanager.handle() != null) {
                return submanager;
            }
        }

        return null;
    }

    // =========================================================

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
        if (message != null && !message.isEmpty()) {
            unit.setManagerUsed(manager, message);
        }
        else {
            unit.setManagerUsed(manager);
        }
        return manager;
    }

    /**
     * Auxiliary method indicating that the same Manager should not interrupt the actions from previous frames.
     */
    public Manager continueUsingLastManager() {
        return unit.manager();
    }
}
