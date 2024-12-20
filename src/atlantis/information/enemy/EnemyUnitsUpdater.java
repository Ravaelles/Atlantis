package atlantis.information.enemy;

import atlantis.game.A;
import atlantis.information.strategy.response.EnemyUnitDiscoveredResponse;
import atlantis.units.AUnit;
import atlantis.units.fogged.AbstractFoggedUnit;
import atlantis.units.select.Select;

public class EnemyUnitsUpdater extends EnemyUnits {

    public static void updateFoggedUnits() {
        for (AUnit enemy : Select.enemy().list()) {
            updateTypeAndPositionOfFoggedUnitBasenOnVisibleAUnit(enemy);
        }

        for (AbstractFoggedUnit foggedUnit : enemyUnitsDiscovered.values()) {
            updatedFoggedUnitEvenIfNotVisible(foggedUnit);
        }
    }

    private static boolean updateTypeAndPositionOfFoggedUnitBasenOnVisibleAUnit(AUnit enemy) {
        if (enemy.type().isGasBuildingOrGeyser()) return true;

        if (enemy instanceof AbstractFoggedUnit) {
            System.err.println("updateTypeAndPositionOfFoggedUnitBasenOnVisibleAUnit got AbstractFoggedUnit: " + enemy);
            A.printStackTrace();
        }

        AbstractFoggedUnit foggedUnit = getFoggedUnit(enemy);
        if (foggedUnit != null) {
            foggedUnit.updatePosition(enemy);
            foggedUnit.updateType(enemy);
        }

        return false;
    }

    /**
     * Check if the position of fogged unit is visible and there is no unit there.
     * If so, change it, because it means we don't know where it is.
     */
    private static void updatedFoggedUnitEvenIfNotVisible(AbstractFoggedUnit foggedUnit) {
        if (
            foggedUnit.hasPosition()
                && foggedUnit.position().isPositionVisible()
        ) {
            foggedUnit.foggedUnitNoLongerWhereItWasBefore();
        }
    }

    /**
     * Saves information about enemy unit that we see for the first time.
     */
    public static void weDiscoveredEnemyUnit(AUnit enemyUnit) {
        addFoggedUnit(enemyUnit);
        EnemyUnitDiscoveredResponse.updateEnemyUnitDiscovered(enemyUnit);
    }

    public static void addFoggedUnit(AUnit enemyUnit) {
        int id = enemyUnit.id();

//        System.err.println("enemyUnit = " + enemyUnit);
        if (!enemyUnitsDiscovered.containsKey(id)) {
            AbstractFoggedUnit foggedUnit = AbstractFoggedUnit.from(enemyUnit);

            enemyUnitsDiscovered.put(id, foggedUnit);
//            System.err.println("added " + foggedUnit + " / " + enemyUnitsDiscovered.size());
        }
    }

    public static void removeFoggedUnit(AUnit enemyUnit) {
        AbstractFoggedUnit foggedUnit = enemyUnitsDiscovered.get(enemyUnit.id());

        if (foggedUnit != null) {
            foggedUnit.foggedUnitNoLongerWhereItWasBefore();
        }

        enemyUnitsDiscovered.remove(enemyUnit.id());
        cache.clear();

//        if (enemyUnit.isBuilding()) {
////            A.printStackTrace(
//            System.err.println(
//                "REMOVE enemyBuilding = " + enemyUnit + " / " + enemyUnit.id()
//                + " / " + enemyUnit.isVisibleUnitOnMap() + " / " + enemyUnit.isPositionVisible()
//            );
//        }
    }
}
