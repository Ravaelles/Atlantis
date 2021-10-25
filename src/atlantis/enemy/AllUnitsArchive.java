package atlantis.enemy;

import atlantis.units.AUnit;

import java.util.ArrayList;
import java.util.HashMap;

public class AllUnitsArchive {

    protected static HashMap<Integer, AUnit> destroyedUnitIds = new HashMap<>();

    public static void markUnitAsDestroyed(int unitId, AUnit unit) {
        destroyedUnitIds.put(unitId, unit);
    }

    public static boolean isDestroyed(int unitId) {
        return destroyedUnitIds.containsKey(unitId);
    }

}
