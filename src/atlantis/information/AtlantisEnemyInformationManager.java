package atlantis.information;

import atlantis.units.AUnit;
import atlantis.units.Select;
import bwapi.Position;


/**
 * Provides various useful information about the enemy whereabouts or if even know any enemy building.
 */
public class AtlantisEnemyInformationManager {

    /**
     * Returns true if we learned the location of any still-existing enemy building.
     */
    public static boolean hasDiscoveredEnemyBuilding() {
        if (AtlantisUnitInformationManager.enemyUnitsDiscovered.isEmpty()) {
            return false;
        }

        for (UnitData enemy : AtlantisUnitInformationManager.enemyUnitsDiscovered.values()) {
            if (enemy.getType().isBuilding()) {
                return true;
            }
        }
        return false;
    }

    /**
     * If we learned about at least one still existing enemy base it returns first of them. Returns null
     * otherwise.
     */
    public static AUnit hasDiscoveredEnemyBase() {
        if (!hasDiscoveredEnemyBuilding()) {
            return null;
        }

        for (UnitData enemyUnit : AtlantisUnitInformationManager.enemyUnitsDiscovered.values()) {
            if (enemyUnit.getType().isBase()) {
                return enemyUnit.getUnit();	//TODO: check for problems with base out of sight
            }
        }

        return null;
    }

    /**
     * Gets oldest known enemy base.
     */
    public static Position getEnemyBase() {
//        System.out.println(AtlantisUnitInformationManager.enemyUnitsDiscovered.size());
        for (UnitData enemyUnitData : AtlantisUnitInformationManager.enemyUnitsDiscovered.values()) {
//            System.out.println(enemyUnit);
            if (enemyUnitData.getType().isBase() && enemyUnitData.getUnit().exists()) {
                return enemyUnitData.getPosition();
            }
        }

        return null;
    }

    /**
     *
     */
    public static UnitData getNearestEnemyBuilding() {
        AUnit mainBase = Select.mainBase();
        if (mainBase != null && !AtlantisUnitInformationManager.enemyUnitsDiscovered.isEmpty()) {
            return Select.fromData(AtlantisUnitInformationManager.enemyUnitsDiscovered.values()).buildings().nearestTo(mainBase.getPosition());
        }
        return null;
    }

}
