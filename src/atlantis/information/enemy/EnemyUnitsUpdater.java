package atlantis.information.enemy;

import atlantis.information.strategy.EnemyUnitDiscoveredResponse;
import atlantis.units.AUnit;
import atlantis.units.fogged.AbstractFoggedUnit;
import atlantis.units.fogged.FoggedUnit;
import atlantis.units.select.Select;

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
        if (foggedUnit != null) {
            foggedUnit.update(enemy);
        }
        return false;
    }

    /**
     * Check if the position of fogged unit is visible and there is no unit there.
     * If so, change it, because it means we don't know where it is.
     */
    private static void updatedFogged(AbstractFoggedUnit foggedUnit) {
//        AUnit aUnit = foggedUnit.innerAUnit();
//        System.out.println(aUnit + " // visible: " + (aUnit != null ? aUnit.isVisibleUnitOnMap() : "---"));
//        if (aUnit == null || !aUnit.isVisibleUnitOnMap()) {
//            if (foggedUnit.hasPosition()) {
//                APainter.paintCircleFilled(
//                    foggedUnit,
//                    8,
//                    foggedUnit.position().isPositionVisible() ? Color.Green : Color.Red
//                );
//            }

//            if (foggedUnit.hasPosition() && foggedUnit.position().isPositionVisible() && foggedUnit.u() == null) {
//                System.out.println(">> Fogged unit is no longer visible, remove position " + foggedUnit);
            foggedUnit.removeKnownPositionIfNeeded();
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
        AbstractFoggedUnit foggedUnit = AbstractFoggedUnit.from(enemyUnit);
        foggedUnit.update(enemyUnit);

        enemyUnitsDiscovered.put(enemyUnit.id(), foggedUnit);
    }

    public static void removeFoggedUnit(AUnit enemyUnit) {
        if (enemyUnit instanceof FoggedUnit) {
            FoggedUnit foggedUnit = (FoggedUnit) enemyUnit;
            foggedUnit.removeKnownPositionIfNeeded();
        }

        enemyUnitsDiscovered.remove(enemyUnit.id());
        cache.clear();
    }
}
