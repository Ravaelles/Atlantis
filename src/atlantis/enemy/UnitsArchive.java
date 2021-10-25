package atlantis.enemy;

import atlantis.information.AOurUnitsExtraInfo;
import atlantis.units.AUnit;

import java.util.ArrayList;
import java.util.HashMap;

public class UnitsArchive {

    protected static HashMap<Integer, AUnit> destroyedUnitIds = new HashMap<>();

    public static void markUnitAsDestroyed(int unitId, AUnit unit) {
        destroyedUnitIds.put(unitId, unit);

        if (unit.isEnemyUnit()) {
            AEnemyUnits.unitDestroyed(unit);
        }
        else if (unit.isOur()) {
            AOurUnitsExtraInfo.idsOfOurDestroyedUnits.add(unit.getID());
        }
    }

    public static boolean isDestroyed(int unitId) {
        return destroyedUnitIds.containsKey(unitId);
    }

}
