package atlantis.information;

import atlantis.AtlantisConfig;
import atlantis.wrappers.MappingCounter;
import atlantis.wrappers.SelectUnits;
import java.util.ArrayList;
import jnibwapi.Unit;
import jnibwapi.types.UnitType;

public class AtlantisUnitInformationManager {

    protected static ArrayList<Unit> allUnits = new ArrayList<>();

//    protected static MappingCounter<UnitType> ourUnitsFininised = new MappingCounter<>();
//    protected static MappingCounter<UnitType> ourUnitsUnfininised = new MappingCounter<>();
    protected static MappingCounter<UnitType> enemyUnitsDiscoveredCounter = new MappingCounter<>();
    protected static MappingCounter<UnitType> enemyUnitsVisibleCounter = new MappingCounter<>();

    protected static ArrayList<Unit> enemyUnitsDiscovered = new ArrayList<>();
    protected static ArrayList<Unit> enemyUnitsVisible = new ArrayList<>();

    // =========================================================
    // Special methods
    /**
     * Informs this class that new (possibly unfinished) unit exists in the game. Both our (including
     * unfinished) and enemy's.
     */
    public static void rememberUnit(Unit unit) {
        allUnits.add(unit);
    }

    /**
     * Informs this class that new (possibly unfinished) unit exists in the game. Both our (including
     * unfinished) and enemy's.
     */
    public static void forgetUnit(int unitID) {
        Unit unit = getUnitByID(unitID);
        if (unit != null) {
            allUnits.remove(unit);
        }
    }

    /**
     * Based on a stored collection, returns unit object for given unitID.
     */
    public static Unit getUnitByID(int unitID) {
        for (Unit unit : allUnits) {
            if (unit.getID() == unitID) {
                return unit;
            }
        }

        return null;
    }

    // =========================================================
    // Number of units changed
    /**
     * Saves information about our new unit being trained, so counting units works properly.
     */
//    public static void addOurUnfinishedUnit(UnitType type) {
//        ourUnitsUnfininised.incrementValueFor(type);
//    }
    /**
     * Saves information about new unit being created successfully, so counting units works properly.
     */
//    public static void addOurFinishedUnit(UnitType type) {
//        ourUnitsFininised.incrementValueFor(type);
//    }
    /**
     * Saves information about enemy unit that we see for the first time.
     */
    public static void discoveredEnemyUnit(Unit unit) {
        enemyUnitsDiscovered.add(unit);
        enemyUnitsDiscoveredCounter.incrementValueFor(unit.getType());
    }

    /**
     * Saves information about given unit being destroyed, so counting units works properly.
     */
    public static void unitDestroyed(Unit unit) {
//        if (unit.getPlayer().isSelf()) {
//            if (unit.isCompleted()) {
//                ourUnitsFininised.decrementValueFor(unit.getType());
//            } else {
//                ourUnitsUnfininised.decrementValueFor(unit.getType());
//            }
//        } else
        if (unit.getPlayer().isEnemy()) {
            enemyUnitsDiscoveredCounter.decrementValueFor(unit.getType());
            enemyUnitsVisibleCounter.decrementValueFor(unit.getType());
            enemyUnitsDiscovered.remove(unit);
            enemyUnitsVisible.remove(unit);
        }
    }

    public static void addEnemyUnitVisible(Unit unit) {
        enemyUnitsVisible.add(unit);
        enemyUnitsVisibleCounter.incrementValueFor(unit.getType());
    }

    public static void removeEnemyUnitVisible(Unit unit) {
        enemyUnitsVisible.remove(unit);
        enemyUnitsVisibleCounter.decrementValueFor(unit.getType());
    }

    // =========================================================
    // COUNT
    /**
     * Returns cached amount of our units of given type.
     */
    public static int countOurUnitsOfType(UnitType type) {

        // Bas building
        if (type.isGasBuilding()) {
            int total = 0;
            for (Unit unit : allUnits) {
                if (type.equals(unit.getType())) {
                    total++;
                }
            }
            return total;
        } else { // Anything but gas building
//            return ourUnitsUnfininised.getValueFor(type);
            return SelectUnits.ourIncludingUnfinished().ofType(type).count();
        }
    }

    /**
     * Returns number of discovered and alive enemy units of given type. Some of them (maybe even all of them)
     * may not be visible right now.
     */
    public static int countEnemyUnitsOfType(UnitType type) {
        return enemyUnitsDiscoveredCounter.getValueFor(type);
    }

    // =========================================================
    // Helper methods
    /**
     * Returns cached amount of our worker units.
     */
    public static int countOurWorkers() {
        return countOurUnitsOfType(AtlantisConfig.WORKER);
    }

    /**
     * Returns cached amount of our bases.
     */
    public static int countOurBases() {
        int total = 0;
        for (Unit unit : allUnits) {
            if (unit.isBase()) {
                total++;
            }
        }
        return total;
    }

}
