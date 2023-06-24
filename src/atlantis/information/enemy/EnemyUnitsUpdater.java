package atlantis.information.enemy;

import atlantis.debug.painter.APainter;
import atlantis.game.A;
import atlantis.information.strategy.EnemyUnitDiscoveredResponse;
import atlantis.units.AUnit;
import atlantis.units.fogged.AbstractFoggedUnit;
import atlantis.units.select.Select;
import bwapi.Color;

public class EnemyUnitsUpdater extends EnemyUnits {

    public static void updateFoggedUnits() {
//        System.out.println("--- UPDATE at " + A.now());
        for (AUnit enemy : Select.enemy().list()) {
//            System.out.println("update fogged from real = " + enemy);
            updateUnitTypeAndPosition(enemy);
        }

        for (AbstractFoggedUnit foggedUnit : enemyUnitsDiscovered.values()) {
//            System.out.println("update fogged = " + foggedUnit);
            updatedFogged(foggedUnit);
        }
    }

    public static boolean updateUnitTypeAndPosition(AUnit enemy) {
        if (enemy.type().isGasBuildingOrGeyser()) {
            return true;
        }

        AbstractFoggedUnit foggedUnit = getFoggedUnit(enemy);
        if (foggedUnit != null && foggedUnit.u() != null) {
            foggedUnit.updatePosition(enemy);
            foggedUnit.updateType(enemy);
        }
        return false;
    }

    /**
     * Check if the position of fogged unit is visible and there is no unit there.
     * If so, change it, because it means we don't know where it is.
     */
    private static void updatedFogged(AbstractFoggedUnit foggedUnit) {
        AUnit unit = foggedUnit.innerAUnit();
//        System.out.println(unit + " // visible: " + (unit != null ? unit.isVisibleUnitOnMap() : "---"));

//        if (unit == null || !unit.isVisibleUnitOnMap()) {
        if (!unit.isVisibleUnitOnMap()) {
            if (foggedUnit.hasPosition()) {
                APainter.paintCircleFilled(
                    foggedUnit,
                    8,
                    foggedUnit.position().isPositionVisible() ? Color.Green : Color.Red
                );
            }

//            if (foggedUnit.hasPosition() && foggedUnit.position().isPositionVisible() && foggedUnit.u() == null) {
            if (
                foggedUnit.hasPosition()
                    && foggedUnit.position().isPositionVisible()
                    && (foggedUnit.u() == null && !foggedUnit.isCloaked() && !foggedUnit.isDetected())
            ) {
                System.out.println(">> Fogged unit no longer present at this visible position " + foggedUnit);
                foggedUnit.forceRemoveKnownPosition();
            }

//            if (foggedUnit.hasPosition() && foggedUnit.position().isPositionVisible() && foggedUnit.u() == null) {
//                System.out.println(">> Fogged unit is no longer visible, remove position " + foggedUnit);
//            }
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

        if (!enemyUnitsDiscovered.containsKey(id)) {
            AbstractFoggedUnit foggedUnit = AbstractFoggedUnit.from(enemyUnit);
//            foggedUnit.onAbstractFoggedUnitCreated(enemyUnit);

            enemyUnitsDiscovered.put(id, foggedUnit);

//            System.out.println("ADD enemyUnit = " +  enemyUnit + " / " + id);
//            if (enemyUnit.isBuilding()) {
//                A.printStackTrace("ADD enemyUnit = " + enemyUnit + " / " + enemyUnit.id());
//            }
        }
    }

    public static void removeFoggedUnit(AUnit enemyUnit) {
//        System.out.println("REMOVE a enemyUnit = " +  enemyUnit + " / " + enemyUnit.id());
        AbstractFoggedUnit foggedUnit = enemyUnitsDiscovered.get(enemyUnit.id());

        if (foggedUnit != null) {
//            System.out.println("        forceRemoveKnownPosition");
            foggedUnit.forceRemoveKnownPosition();
        }

        enemyUnitsDiscovered.remove(enemyUnit.id());
        cache.clear();
//        System.out.println("REMOVE b enemyUnit = " +  enemyUnit + " / " + enemyUnit.id());

//        if (enemyUnit.isBuilding()) {
//            A.printStackTrace("REMOVE b enemyUnit = " + enemyUnit + " / " + enemyUnit.id());
//        }
    }
}
