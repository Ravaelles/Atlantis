package atlantis.units.managers;

import atlantis.units.AUnit;

public abstract class Manager {

//    protected AUnit unit;
//
//    // =========================================================
//
//    public Manager(AUnit unit) {
//        this.unit = unit;
//    }

    // =========================================================

    public static Manager skipped() {
        return NullManager.getInstance();
    }

    public static Manager usedManager(Class managerClass) {
        return manager;
    }

}
