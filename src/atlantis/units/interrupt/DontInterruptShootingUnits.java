package atlantis.units.interrupt;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Selection;
import atlantis.util.Enemy;

public class DontInterruptShootingUnits extends Manager {
    public DontInterruptShootingUnits(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
//        if (true) return false;
        if (unit.isMarine()) return true;

        if (!unit.isAttacking()) return false;
        if (!unit.isRanged()) return false;

        if (isMeleeEnemyNear()) return false;

        if (unit.isWraith()) {
            if (unit.hp() < 110 || unit.enemiesNear().buildings().canAttack(unit, 1.1).notEmpty()) return false;
        }

        return true;
    }

    private boolean isMeleeEnemyNear() {
        if (!unit.isGroundUnit()) return false;

//        Selection meleeEnemies = unit.enemiesNear().melee().canAttack(unit, 0.9);
        Selection meleeEnemies = unit.enemiesNear().melee().inRadius(
            Math.min(3.5, 1.7 + unit.woundPercent() / 200.0), unit
        );

        if (meleeEnemies.empty()) return false;

//        return true;
        return meleeEnemies.nearestTo(unit).isFacing(unit);
    }

    @Override
    protected Class<? extends Manager>[] managers() {
        return new Class[]{
            DontInterruptShootingMarines.class,
        };
    }

    @Override
    protected Manager handle() {
        if (!unit.isMarine()) {
            if (act()) return usedManager(this);
        }

        return handleSubmanagers();
    }

    private boolean act() {
//        if (true) return false;


//        int enemiesMultiplier = unit.medicNear() ? 1 : unit.meleeEnemiesNear(MELEE_ENEMIES_RANGE);
//        if (unit.hp() <= (Enemy.protoss() ? 18 : 8) * enemiesMultiplier) {
//            APainter.paintCircleFilled(unit, 10, Color.Purple);
//            System.err.println(unit.hp() + " < " + (Enemy.protoss() ? 18 : 8) * unit.meleeEnemiesNear(MELEE_ENEMIES_RANGE));
        if (unit.isWounded() && unit.hp() <= (Enemy.protoss() ? 18 : 8) * unit.meleeEnemiesNearCount()) return false;

        // Allow to use tech
        if (unit.lastActionLessThanAgo(3, Actions.USING_TECH)) {
            unit.setTooltipTactical("UsingTech");
            return true;
        }

        // Don't INTERRUPT shooting units
        if (DontInterruptStartedAttacks.shouldNotInterrupt(unit)) {
            unit.setTooltip("Shoot-" + unit.cooldownRemaining());
//            APainter.paintRectangle(unit.translateByPixels(-5, 0), 10, 3, Color.Red);
            return true;
        }

        // Allow unit to load to shuttle
//        if (allowUnitToLoadToTransport(unit)) {
//            unit.setTooltip("Load");
//            APainter.paintRectangle(unit.translateByPixels(-5, 0), 10, 3, Color.Blue);

//            return true;
//        }

        if (allowUnitToContinueRareRightClickActions(unit)) {
            unit.setTooltipTactical("RightClick");
            return true;
        }

        return false;
    }

    // =========================================================

//    private static boolean allowUnitToLoadToTransport(AUnit unit) {
//        return !unit.type().isTransport() && unit.lastActionLessThanAgo(13, UnitActions.LOAD);
//    }

    private static boolean allowUnitToContinueRareRightClickActions(AUnit unit) {
        return unit.lastActionLessThanAgo(6, Actions.RIGHT_CLICK);
    }

}
