package atlantis.enemy;

import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.wrappers.MappingCounter;

import java.util.HashMap;

public class UnitsArchive {

    protected static HashMap<Integer, AUnit> destroyedUnitIds = new HashMap<>();
    protected static MappingCounter<AUnitType> enemyDestroyedTypes = new MappingCounter<>();
    protected static MappingCounter<AUnitType> ourDestroyedTypes = new MappingCounter<>();

    // =========================================================

    public static void markUnitAsDestroyed(int unitId, AUnit unit) {
        destroyedUnitIds.put(unitId, unit);

        if (unit.isEnemy()) {
            AEnemyUnits.removeDiscoveredUnit(unit);
            enemyUnitDestroyed(unit);
        }
        else if (unit.isOur()) {
//            OurUnitsArchive.unitDestroyed(unit);
            ourUnitDestroyed(unit);
        }
    }

    // =========================================================

    public static void paintLostUnits() {
        System.out.println("--- Lost ---");
        paint(ourDestroyedTypes);
    }

    public static void paintKilledUnits() {
        System.out.println("--- Killed ---");
        paint(enemyDestroyedTypes);
    }

    private static void paint(MappingCounter<AUnitType> types) {
        for (AUnitType type : types.map().keySet()) {
            if (type.isNotRealUnit()) {
                continue;
            }
            System.out.println(type + " - " + types.getValueFor(type));
        }
    }

    // =========================================================

    public static void ourUnitDestroyed(AUnit unit) {
        ourDestroyedTypes.incrementValueFor(unit.type());
    }

    public static void enemyUnitDestroyed(AUnit unit) {
        enemyDestroyedTypes.incrementValueFor(unit.type());
    }

    public static boolean isDestroyed(int unitId) {
        return destroyedUnitIds.containsKey(unitId);
    }

}
