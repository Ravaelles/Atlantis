package atlantis.information;

import atlantis.util.UnitUtil;
import atlantis.wrappers.Select;
import bwapi.Position;
import bwapi.Unit;

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
    public static Unit hasDiscoveredEnemyBase() {
        if (!hasDiscoveredEnemyBuilding()) {
            return null;
        }

        for (UnitData enemyUnit : AtlantisUnitInformationManager.enemyUnitsDiscovered.values()) {
            if (UnitUtil.isBase(enemyUnit.getType())) {
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
            if (UnitUtil.isBase(enemyUnitData.getType()) && enemyUnitData.getUnit().exists()) {
                return enemyUnitData.getPosition();
            }
        }

        return null;
    }

    /**
     *
     */
    public static UnitData getNearestEnemyBuilding() {
        Unit mainBase = Select.mainBase();
        if (mainBase != null && !AtlantisUnitInformationManager.enemyUnitsDiscovered.isEmpty()) {
            return Select.fromData(AtlantisUnitInformationManager.enemyUnitsDiscovered.values()).buildings().nearestTo(mainBase.getPosition());
        }
        return null;
    }

}
