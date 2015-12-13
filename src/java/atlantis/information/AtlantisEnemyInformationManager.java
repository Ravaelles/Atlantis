package atlantis.information;

import atlantis.wrappers.SelectUnits;
import jnibwapi.Position;
import jnibwapi.Unit;

public class AtlantisEnemyInformationManager {

    /**
     * Returns true if we learned the location of any still-existing enemy building.
     */
    public static boolean hasDiscoveredEnemyBuilding() {
        if (AtlantisUnitInformationManager.enemyUnitsDiscovered.isEmpty()) {
            return false;
        }

        for (Unit enemy : AtlantisUnitInformationManager.enemyUnitsDiscovered) {
            if (enemy.isBuilding()) {
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

        for (Unit enemyUnit : AtlantisUnitInformationManager.enemyUnitsDiscovered) {
            if (enemyUnit.isBase()) {
                return enemyUnit;
            }
        }

        return null;
    }

    /**
     * Gets oldest known enemy base.
     */
    public static Position getEnemyBase() {
        for (Unit enemyUnit : AtlantisUnitInformationManager.enemyUnitsDiscovered) {
            if (enemyUnit.isBase()) {
                return enemyUnit;
            }
        }

        return null;
    }

    /**
     *
     */
    public static Unit getNearestEnemyBuilding() {
        Unit mainBase = SelectUnits.mainBase();
        if (mainBase != null) {
            return SelectUnits.from(AtlantisUnitInformationManager.enemyUnitsDiscovered).nearestTo(mainBase);
        }
        return null;
    }

}
