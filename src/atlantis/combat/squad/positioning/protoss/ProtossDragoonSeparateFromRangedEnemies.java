package atlantis.combat.squad.positioning.protoss;

import atlantis.architecture.Manager;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.range.OurDragoonRange;
import atlantis.units.select.Selection;
import atlantis.util.We;

public class ProtossDragoonSeparateFromRangedEnemies extends Manager {
    private Selection enemiesNear;

    public ProtossDragoonSeparateFromRangedEnemies(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
//        if (true) return false;

        return We.protoss()
            && unit.isDragoon()
            && unit.cooldown() >= 18
//            && (unit.woundPercent() >= 20 ||
            && (enemiesNear = rangedEnemiesNear()).notEmpty();
    }

    @Override
    protected Manager handle() {
        if (enemiesNear.notEmpty()) {
            if (movedAway()) {
                return usedManager(this);
            }
        }

        return null;
    }

    private boolean movedAway() {
        HasPosition centerOfEnemies = enemiesNear.center();
        if (centerOfEnemies == null) centerOfEnemies = enemiesNear.nearestTo(unit);
        if (centerOfEnemies == null) return false;

        double moveDist = unit.distTo(centerOfEnemies) >= 1.2 ? 0.2 : 0.3;

        return unit.moveAwayFrom(centerOfEnemies, moveDist, Actions.MOVE_DANCE_AWAY, "GoonSeparate");
    }

    private Selection rangedEnemiesNear() {
        return unit.enemiesNear()
            .ranged()
            .havingPosition()
            .havingAntiGroundWeapon()
            .inRadius(OurDragoonRange.range() - 0.4, unit)
            .canAttack(unit, 0.8 + unit.woundPercent() / 80.0);
    }
}
