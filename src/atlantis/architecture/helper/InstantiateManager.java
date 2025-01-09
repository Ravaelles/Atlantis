package atlantis.architecture.helper;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;

public class InstantiateManager {
    public static Manager byClass(Class managerClass, AUnit unit) {
        try {
            Class<? extends Manager> managerCastClass = (Class<? extends Manager>) managerClass;

            return managerCastClass.getConstructor(AUnit.class).newInstance(unit);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
