package atlantis.information;

import atlantis.Atlantis;
import atlantis.AtlantisConfig;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.wrappers.MappingCounter;
import atlantis.units.Select;
import java.util.ArrayList;
import java.util.HashMap;


import bwapi.UnitType;	

public class AtlantisUnitInformationManager {

    protected static HashMap<Integer, UnitData> allUnits = new HashMap<>();

//    protected static MappingCounter<AUnitType> ourUnitsFininised = new MappingCounter<>();
//    protected static MappingCounter<AUnitType> ourUnitsUnfininised = new MappingCounter<>();
    protected static MappingCounter<AUnitType> enemyUnitsDiscoveredCounter = new MappingCounter<>();
    protected static MappingCounter<AUnitType> enemyUnitsVisibleCounter = new MappingCounter<>();

    protected static HashMap<Integer, UnitData> enemyUnitsDiscovered = new HashMap<>();
    protected static HashMap<Integer, AUnit> enemyUnitsVisible = new HashMap<>();

    // =========================================================
    // Special methods
    /**
     * Informs this class that new (possibly unfinished) unit exists in the game. Both our (including
     * unfinished) and enemy's.
     */
    public static void rememberUnit(AUnit unit) {
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
    	/*for (AUnit unit : allUnits) {
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
//    public static void addOurUnfinishedUnit(AUnitType type) {
//        ourUnitsUnfininised.incrementValueFor(type);
//    }
    /**
     * Saves information about new unit being created successfully, so counting units works properly.
     */
//    public static void addOurFinishedUnit(AUnitType type) {
//        ourUnitsFininised.incrementValueFor(type);
//    }
    /**
     * Saves information about enemy unit that we see for the first time.
     */
    public static void discoveredEnemyUnit(AUnit unit) {
        enemyUnitsDiscovered.put(unit.getID(), new UnitData(unit));
        enemyUnitsDiscoveredCounter.incrementValueFor(unit.getType());
    }

    /**
     * Saves information about given unit being destroyed, so counting units works properly.
     */
    public static void unitDestroyed(AUnit unit) {
//        if (unit.getPlayer().isSelf()) {
//            if (unit.isCompleted()) {
//                ourUnitsFininised.decrementValueFor(unit.getType());
//            } else {
//                ourUnitsUnfininised.decrementValueFor(unit.getType());
//            }
//        } else
//        if (Atlantis.getBwapi().self().isEnemy(unit.getPlayer()) ) {
        if (unit.isEnemy()) {
            enemyUnitsDiscoveredCounter.decrementValueFor(unit.getType());
            enemyUnitsVisibleCounter.decrementValueFor(unit.getType());
            enemyUnitsDiscovered.remove(unit.getID());
            enemyUnitsVisible.remove(unit.getID());
        }
    }

    public static void addEnemyUnitVisible(AUnit unit) {
        enemyUnitsVisible.put(unit.getID(), unit);
        enemyUnitsVisibleCounter.incrementValueFor(unit.getType());
        
        //updates discovered units
        enemyUnitsDiscovered.get(unit.getID()).update(unit);
    }

    public static void removeEnemyUnitVisible(AUnit unit) {
        enemyUnitsVisible.remove(unit.getID());
        enemyUnitsVisibleCounter.decrementValueFor(unit.getType());
    }

    // =========================================================
    // COUNT

    /**
     * Returns number of discovered and alive enemy units of given type. Some of them (maybe even all of them)
     * may not be visible right now.
     */
    public static int countEnemyUnitsOfType(AUnitType type) {
        return enemyUnitsDiscoveredCounter.getValueFor(type);
    }

}
