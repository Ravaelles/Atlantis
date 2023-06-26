package atlantis.information.enemy;

import atlantis.game.A;
import atlantis.information.strategy.EnemyUnitDiscoveredResponse;
import atlantis.units.AUnit;
import atlantis.units.fogged.AbstractFoggedUnit;
import atlantis.units.select.Select;

public class EnemyUnitsUpdater extends EnemyUnits {

    public static void updateFoggedUnits() {
//        System.out.println("--- UPDATE at " + A.now());
        for (AUnit enemy : Select.enemy().list()) {
//            System.out.println("update fogged from real = " + enemy);
            updateTypeAndPositionOfFoggedUnitBasenOnVisibleAUnit(enemy);
        }

        for (AbstractFoggedUnit foggedUnit : enemyUnitsDiscovered.values()) {
//            System.out.println("update fogged = " + foggedUnit);
            updatedFoggedUnitEvenIfNotVisible(foggedUnit);
        }
    }

    private static boolean updateTypeAndPositionOfFoggedUnitBasenOnVisibleAUnit(AUnit enemy) {
        if (enemy.type().isGasBuildingOrGeyser()) {
            return true;
        }

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
//        System.out.println(
//            foggedUnit + " / " + foggedUnit.getUnit() + " / " + foggedUnit.getUnit().isPositionVisible()
//        );

//        AUnit unit = foggedUnit.innerAUnit();
//        System.out.println(unit + " // visible: " + (unit != null ? unit.isVisibleUnitOnMap() : "---"));

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
//                System.out.println(">> Fogged unit no longer present at this visible position " + foggedUnit);
//                foggedUnit.forceRemoveKnownPosition();
//                foggedUnit.forceRemoveKnownPosition();
                foggedUnit.foggedUnitNoLongerWhereItWasBefore();
            }

//            if (foggedUnit.hasPosition() && foggedUnit.position().isPositionVisible() && foggedUnit.u() == null) {
//                System.out.println(">> Fogged unit is no longer visible, remove position " + foggedUnit);
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

//            System.out.println("ADD enemyUnit = " +  enemyUnit + " / " + id);
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
//        System.out.println("REMOVE a enemyUnit = " +  enemyUnit + " / " + enemyUnit.id());
        AbstractFoggedUnit foggedUnit = enemyUnitsDiscovered.get(enemyUnit.id());

        if (foggedUnit != null) {
//            System.out.println("        forceRemoveKnownPosition");
            foggedUnit.foggedUnitNoLongerWhereItWasBefore();
        }

        enemyUnitsDiscovered.remove(enemyUnit.id());
        cache.clear();

//        System.out.println("REMOVE b enemyUnit = " +  enemyUnit + " / " + enemyUnit.id());

//        if (enemyUnit.isBuilding()) {
////            A.printStackTrace(
//            System.err.println(
//                "REMOVE enemyBuilding = " + enemyUnit + " / " + enemyUnit.id()
//                + " / " + enemyUnit.isVisibleUnitOnMap() + " / " + enemyUnit.isPositionVisible()
//            );
//        }
    }
}
