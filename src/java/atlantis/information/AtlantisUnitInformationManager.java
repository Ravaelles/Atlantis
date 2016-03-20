package atlantis.information;

import atlantis.AtlantisConfig;
import atlantis.AtlantisGame;
import atlantis.wrappers.MappingCounter;
import atlantis.wrappers.SelectUnits;
import java.util.ArrayList;
import jnibwapi.Unit;
import jnibwapi.types.UnitType;

public class AtlantisUnitInformationManager {

    protected static ArrayList<Unit> allUnits = new ArrayList<>();

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
            enemyUnitsDiscovered.remove(unit);
            enemyUnitsVisible.remove(unit);
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
        if (unit.getPlayer().isEnemy()) {
            if (unit.isBuilding()) {
//                AtlantisGame.sendMessage("Destroyed " + unit.getType().toString());
            }
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
    
    public static int countOurUnitsOfType(UnitType type) {
        return SelectUnits.ourIncludingUnfinished().ofType(type).count();
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
        return SelectUnits.ourWorkers().count();
    }

    /**
     * Returns cached amount of our bases.
     */
    public static int countOurBases() {
        return SelectUnits.ourBases().count();
    }

}
