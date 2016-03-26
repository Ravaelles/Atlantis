package atlantis.information;

import atlantis.Atlantis;
import atlantis.AtlantisConfig;
import atlantis.util.UnitUtil;
import atlantis.wrappers.MappingCounter;
import atlantis.wrappers.Select;
import java.util.ArrayList;
import java.util.HashMap;

import bwapi.Unit;
import bwapi.UnitType;	

public class AtlantisUnitInformationManager {

    protected static HashMap<Integer, UnitData> allUnits = new HashMap<>();

//    protected static MappingCounter<UnitType> ourUnitsFininised = new MappingCounter<>();
//    protected static MappingCounter<UnitType> ourUnitsUnfininised = new MappingCounter<>();
    protected static MappingCounter<UnitType> enemyUnitsDiscoveredCounter = new MappingCounter<>();
    protected static MappingCounter<UnitType> enemyUnitsVisibleCounter = new MappingCounter<>();

    protected static HashMap<Integer, UnitData> enemyUnitsDiscovered = new HashMap<>();
    protected static HashMap<Integer, Unit> enemyUnitsVisible = new HashMap<>();

    // =========================================================
    // Special methods
    /**
     * Informs this class that new (possibly unfinished) unit exists in the game. Both our (including
     * unfinished) and enemy's.
     */
    public static void rememberUnit(Unit unit) {
        allUnits.put(unit.getID(), new UnitData(unit));
    }

    /**
     * Informs this class that new (possibly unfinished) unit exists in the game. Both our (including
     * unfinished) and enemy's.
     */
    public static void forgetUnit(int unitID) {
        UnitData unitData = getUnitDataByID(unitID);
        if (unitData != null) {
            allUnits.remove(unitID);
            enemyUnitsDiscovered.remove(unitID);
            enemyUnitsVisible.remove(unitID);
        }
    }

    /**
     * Based on a stored collection, returns unit object for given unitID.
     */
    public static UnitData getUnitDataByID(int unitID) {
        return allUnits.get(unitID);
    	/*for (Unit unit : allUnits) {
            if (unit.getID() == unitID) {
                return unit;
            }
        }

        return null;*/
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
        enemyUnitsDiscovered.put(unit.getID(), new UnitData(unit));
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
        if (Atlantis.getBwapi().self().isEnemy(unit.getPlayer()) ) {
            enemyUnitsDiscoveredCounter.decrementValueFor(unit.getType());
            enemyUnitsVisibleCounter.decrementValueFor(unit.getType());
            enemyUnitsDiscovered.remove(unit.getID());
            enemyUnitsVisible.remove(unit.getID());
        }
    }

    public static void addEnemyUnitVisible(Unit unit) {
        enemyUnitsVisible.put(unit.getID(), unit);
        enemyUnitsVisibleCounter.incrementValueFor(unit.getType());
        
        //updates discovered units
        enemyUnitsDiscovered.get(unit.getID()).update(unit);
    }

    public static void removeEnemyUnitVisible(Unit unit) {
        enemyUnitsVisible.remove(unit.getID());
        enemyUnitsVisibleCounter.decrementValueFor(unit.getType());
    }

    // =========================================================
    // COUNT
    /**
     * Returns cached amount of our units of given type.
     */
    public static int countOurUnitsOfType(UnitType type) {

        // Bas building
        if (UnitUtil.isGasBuilding(type)) {
            int total = 0;
            for (UnitData unit : allUnits.values()) {
                if (type.equals(unit.getType())) {
                    total++;
                }
            }
            return total;
        } else { // Anything but gas building
//            return ourUnitsUnfininised.getValueFor(type);
            return Select.ourIncludingUnfinished().ofType(type).count();
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
        for (Unit unit : Select.our().listUnits()) {
            if (UnitUtil.isBase(unit.getType())) {
                total++;
            }
        }
        return total;
    }

}
