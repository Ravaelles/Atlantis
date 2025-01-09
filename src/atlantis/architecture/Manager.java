package atlantis.architecture;

import atlantis.architecture.helper.InstantiateManager;
import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.util.log.ErrorLog;

/**
 * Unit manager. Can contain submanagers (see managers() method).
 * <p>
 * If a manager return non-null value in handle(), it will prevent execution of
 * other managers in this frame.
 */
public abstract class Manager extends BaseManager {
    public Manager(AUnit unit) {
        super(unit);
    }

    // =========================================================

    /**
     * All sub-managers. Order matters.
     */
    @SuppressWarnings("unchecked")
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

    private Manager invokeFromParent(Manager parent) {
        if (!applies()) return null;

//        if (unit.isLeader() && unit.isDragoon() && A.now() >= 50) {
//            System.err.println("@ " + A.now() + " - " + this.getClass() + " - ");
//        }

        updateParentsStack(parent);

        Manager manager = handle();
        if (manager != null) {
            return manager;
        }

        return handleSubmanagers();
    }

    private void updateParentsStack(Manager parent) {
        setParent(parent);

        if (parent != null) this.parentsLastTimestamp = parent.parentsLastTimestamp;
        if (A.now() > parentsLastTimestamp) parents.clear();
        this.parentsLastTimestamp = A.now();

        if (parent != null) {
            String parentToString = parent.parentsStack() + parentToString(parent);

            this.parents.add(parentToString);
        }
//        else ErrorLog.printErrorOnce("Parent is null for " + this.getClass().getSimpleName() + "!");
    }

    // === Invoke ===========================================

    public Manager invokeFrom(Manager parent) {
        return invokeFromParent(parent);
    }

    public Manager invokeFrom(Object nonManagerObject) {
        if (nonManagerObject != null) {
            this.parents.add(nonManagerObject.getClass().getSimpleName());
        }

        return invokeFromParent(null);
    }

    public boolean invokedFrom(Manager parent) {
        return invokeFromParent(parent) != null;
    }

    public boolean invokedManager(Class<? extends Manager> managerClass) {
        Manager manager = InstantiateManager.byClass(managerClass, unit);
        if (manager == null) {
            ErrorLog.printMaxOncePerMinutePlusPrintStackTrace(
                "Failed to instantiate manager " + managerClass.getSimpleName()
            );
            return false;
        }

        if (!manager.applies()) return false;

        return manager.invokedFrom(this);
    }

    // =========================================================

    private String parentToString(Manager parent) {
//        if (parent instanceof String) return (String) parent;
//        if (parent instanceof Class) return ((Class) parent).getSimpleName();

        return parent != null
            ? parent.getClass().getSimpleName()
            : null;
    }

    public void printParentsStack() {
        System.err.println("Parents stack for " + this + ": " + parentsStack());
    }

//    public Manager invoke(Commander parent) {
//        return invokeFromParent(parent != null ? parent.getClass() : null);
//    }

    public Manager forceHandle() {
        return handle();
    }

    /**
     * @return TRUE if the manager was applied, an action was taken, meaning further execution should be stopped.
     * FALSE if the manager was not applied. Further execution down the stack should be proceeded.
     */
    protected Manager handle() {
//        if (!applies()) return null;

//        if (unit.isLeader() && unit.isDragoon() && A.now() >= 50) {
//            System.err.println("@ " + A.now() + " - " + unit.idWithHash() + " HANDLED = " + this.getClass().getSimpleName());
//        }

        return handleSubmanagers();
    }

    // =========================================================

    protected Manager handleSubmanagers() {
        for (Manager submanager : submanagerObjects) {
            if (submanager.invokeFromParent(this) != null) {
                return submanager;
            }
        }

        return null;
    }

    public Manager anySubmanagerApplies() {

        // Has submanagers
        if (submanagerObjects.length > 0) {
            for (Manager submanager : submanagerObjects) {
                if (submanager.applies()) {
                    if (submanager.hasSubmanagers()) {
                        return submanager.anySubmanagerApplies();
                    }
                    //                System.err.println("Dragoon B = " + submanager);
                    return submanager;
                }
            }

            return null;
        }

        // No submanagers
        else {
            return applies() ? this : null;
        }
    }

    // =========================================================

    /**
     * Indicates this Manager was just used by the unit.
     */
    public Manager usedManager(Manager manager) {
        return usedManager(manager, null);
    }

    public Manager usedManager(Class managerClass) {
        Manager manager = InstantiateManager.byClass(managerClass, unit);

        return usedManager(manager, null);
    }

    /**
     * Indicates this Manager was just used by the unit.
     */
    public Manager usedManager(Manager manager, String message) {
        if (manager == null) return null;

        if (unit.isLeader() && unit.isDragoon() && A.now() >= 1) {
//            System.err.println("@ " + A.now() + " - " + unit.idWithHash() + " USED = " + this.getClass().getSimpleName());
//            System.err.println("@ " + A.now() + " - " + unit.idWithHash() + " USED = " + this.getClass().getSimpleName() + " / " + parentsStack());
        }

        if (message != null && !message.isEmpty()) {
            unit.setManagerUsed(manager, message);
        }
        else {
            unit.setManagerUsed(manager);
        }

        return manager;
    }

    public AUnit unit() {
        return unit;
    }
}
