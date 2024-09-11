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

//            foggedUnit + " / " + foggedUnit.getUnit() + " / " + foggedUnit.getUnit().isPositionVisible()
//        );

//        AUnit unit = foggedUnit.innerAUnit();


//        if (unit == null || !unit.isVisibleUnitOnMap()) {
//        System.err.println(unit + " / isVisibleUnitOnMap:" + unit.isVisibleUnitOnMap());
//        if (!unit.isVisibleUnitOnMap()) {
//            if (foggedUnit.hasPosition()) {
//                APainter.paintCircleFilled(
//                    foggedUnit,
//                    4,
//                    foggedUnit.position().isPositionVisible() ? Color.Green : Color.Red
//                );
//            }

//            if (foggedUnit.hasPosition() && foggedUnit.position().isPositionVisible() && foggedUnit.u() == null) {
        if (
//                foggedUnit.u() == null
            foggedUnit.hasPosition()
                && foggedUnit.position().isPositionVisible()
//                    && (foggedUnit.u() == null && !foggedUnit.isCloaked() && !foggedUnit.isDetected())
//                    && (!foggedUnit.isCloaked() && !foggedUnit.isDetected())
        ) {

//                foggedUnit.forceRemoveKnownPosition();
//                foggedUnit.forceRemoveKnownPosition();
            foggedUnit.foggedUnitNoLongerWhereItWasBefore();
        }

//            if (foggedUnit.hasPosition() && foggedUnit.position().isPositionVisible() && foggedUnit.u() == null) {

//            }
//        }
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

        if (!enemyUnitsDiscovered.containsKey(id)) {
            AbstractFoggedUnit foggedUnit = AbstractFoggedUnit.from(enemyUnit);
//            foggedUnit.onAbstractFoggedUnitCreated(enemyUnit);

            enemyUnitsDiscovered.put(id, foggedUnit);


//            if (enemyUnit.isBuilding()) {
//                A.printStackTrace(
//                System.err.println(
//                    "ADD enemyBuilding = " + enemyUnit + " / " + enemyUnit.id()
//                    + " / " + enemyUnit.isVisibleUnitOnMap() + " / " + enemyUnit.isPositionVisible()
//                );
//            }
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
