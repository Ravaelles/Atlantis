package atlantis.information.enemy;

import atlantis.information.strategy.EnemyUnitDiscoveredResponse;
import atlantis.map.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.AbstractFoggedUnit;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import atlantis.util.Cache;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

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
        AUnit aUnit = foggedUnit.innerAUnit();
//        System.out.println(aUnit + " // visible: " + (aUnit != null ? aUnit.isVisibleUnitOnMap() : "---"));
        if (aUnit == null || !aUnit.isVisibleUnitOnMap()) {
//            if (foggedUnit.hasPosition()) {
//                APainter.paintCircleFilled(
//                    foggedUnit,
//                    8,
//                    foggedUnit.position().isPositionVisible() ? Color.Green : Color.Red
//                );
//            }

            if (foggedUnit.hasPosition() && foggedUnit.position().isPositionVisible()) {
//                System.out.println(">> Fogged unit is no longer visible, remove position " + foggedUnit);
                foggedUnit.removeKnownPosition();
            }
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
        AbstractFoggedUnit foggedUnit = AbstractFoggedUnit.from(enemyUnit);

        enemyUnitsDiscovered.put(enemyUnit.id(), foggedUnit);
    }

    public static void removeFoggedUnit(AUnit enemyUnit) {
        enemyUnitsDiscovered.remove(enemyUnit.id());
        cache.clear();
    }
}
