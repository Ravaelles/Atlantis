package atlantis.interrupt;

import atlantis.debug.APainter;
import atlantis.units.AUnit;
import atlantis.units.actions.UnitActions;
import atlantis.util.Enemy;
import bwapi.Color;

public class DontDisturbInterrupt {

    private static final double MELEE_ENEMIES_RANGE = 2.7;

    public static boolean dontInterruptImportantActions(AUnit unit) {
//        if (true) return false;

//        System.out.println("unit.meleeEnemiesNearby(1.9) = " + unit.meleeEnemiesNearby(MELEE_ENEMIES_RANGE));
//        int enemiesMultiplier = unit.medicNearby() ? 1 : unit.meleeEnemiesNearby(MELEE_ENEMIES_RANGE);
//        if (unit.hp() <= (Enemy.protoss() ? 18 : 8) * enemiesMultiplier) {
//            APainter.paintCircleFilled(unit, 10, Color.Purple);
//            System.err.println(unit.hp() + " < " + (Enemy.protoss() ? 18 : 8) * unit.meleeEnemiesNearby(MELEE_ENEMIES_RANGE));
        if (unit.hp() <= (Enemy.protoss() ? 18 : 8) * unit.meleeEnemiesNearby(MELEE_ENEMIES_RANGE)) {
            return false;
        }

        // Allow to use tech
        if (unit.lastActionLessThanAgo(3, UnitActions.USING_TECH)) {
            unit.setTooltip("UsingTech");
            return true;
        }

        // Don't INTERRUPT shooting units
        if (DontInterruptStartedAttacks.shouldNotInterrupt(unit)) {
            unit.setTooltip("Shoot");
            APainter.paintRectangle(unit.translateByPixels(-5, 0), 10, 3, Color.Red);
            return true;
        }

        // Allow unit to load to shuttle
        if (allowUnitToLoadToTransport(unit)) {
            unit.setTooltip("Load");
            APainter.paintRectangle(unit.translateByPixels(-5, 0), 10, 3, Color.Blue);
//            System.out.println(A.now() + " TRANSP");
            return true;
        }

        if (allowUnitToContinueRareRightClickActions(unit)) {
            unit.setTooltip("RightClick");
            return true;
        }

        return false;
    }

    // =========================================================

    private static boolean allowUnitToLoadToTransport(AUnit unit) {
        return !unit.type().isTransport() && unit.lastActionLessThanAgo(13, UnitActions.LOAD);
    }

    private static boolean allowUnitToContinueRareRightClickActions(AUnit unit) {
        return unit.lastActionLessThanAgo(6, UnitActions.RIGHT_CLICK);
    }

}
